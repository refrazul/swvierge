#!/usr/bin/env bash
# ==============================================================================
# Rollback seguro e idempotente para APPSW.war en EC2
# Restaura el último respaldo funcional (backups/last-good-APPSW.war)
# ==============================================================================
set -euo pipefail

APP_INSTALL_DIR="${APP_INSTALL_DIR:-/home/ec2-user/app}"
ARTIFACT_NAME="${ARTIFACT_NAME:-APPSW.war}"
APP_USER="${APP_USER:-ec2-user}"
JAVA_BIN="${JAVA_BIN:-java}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx2048m}"
SPRING_PROFILE="${SPRING_PROFILE:-sw-prod-tr}"
SERVER_PORT="${SERVER_PORT:-}"
USE_SYSTEMD="${USE_SYSTEMD:-false}"
SYSTEMD_SERVICE="${SYSTEMD_SERVICE:-iniswtrp.service}"
HEALTHCHECK_URL="${HEALTHCHECK_URL:-http://127.0.0.1:80/}"
HEALTHCHECK_TIMEOUT_SEC="${HEALTHCHECK_TIMEOUT_SEC:-60}"

APP_FILE="${APP_INSTALL_DIR}/${ARTIFACT_NAME}"
BACKUP_DIR="${APP_INSTALL_DIR}/backups"
LOG_DIR="${APP_INSTALL_DIR}/logs"
PID_FILE="${APP_INSTALL_DIR}/${ARTIFACT_NAME}.pid"
LAST_GOOD_LINK="${BACKUP_DIR}/last-good-${ARTIFACT_NAME}"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [ROLLBACK] $*"
}

if [ ! -f "${LAST_GOOD_LINK}" ]; then
  LATEST_BAK="$(ls -1t "${BACKUP_DIR}/${ARTIFACT_NAME}".*.bak 2>/dev/null | head -n 1 || true)"
  if [ -z "${LATEST_BAK}" ]; then
    log "ERROR: No se encontró respaldo funcional en ${BACKUP_DIR}"
    exit 1
  fi
  TARGET_BACKUP="${LATEST_BAK}"
else
  TARGET_BACKUP="$(readlink -f "${LAST_GOOD_LINK}")"
fi

log "Restaurando respaldo funcional: ${TARGET_BACKUP}"

if [ "${USE_SYSTEMD}" = "true" ]; then
  sudo systemctl stop "${SYSTEMD_SERVICE}" || true
else
  PID="$(pgrep -f "java.*-jar[[:space:]]+${APP_FILE}" | head -n 1 || true)"
  if [ -n "${PID}" ]; then
    kill -TERM "${PID}" 2>/dev/null || true
    for _ in $(seq 1 30); do
      kill -0 "${PID}" 2>/dev/null || break
      sleep 1
    done
    kill -0 "${PID}" 2>/dev/null && kill -KILL "${PID}" 2>/dev/null || true
  fi
  rm -f "${PID_FILE}"
fi

cp -pf "${TARGET_BACKUP}" "${APP_FILE}"
if id "${APP_USER}" >/dev/null 2>&1; then
  chown "${APP_USER}:${APP_USER}" "${APP_FILE}" || true
fi

if [ "${USE_SYSTEMD}" = "true" ]; then
  sudo systemctl start "${SYSTEMD_SERVICE}"
else
  mkdir -p "${LOG_DIR}"
  chown -R "${APP_USER}:${APP_USER}" "${APP_INSTALL_DIR}"
  if [ ! -x "${JAVA_BIN}" ]; then
    FOUND_JAVA="$(runuser -u "${APP_USER}" -- bash -lc 'command -v java 2>/dev/null || true' 2>/dev/null || true)"
    if [ -n "${FOUND_JAVA}" ] && [ -x "${FOUND_JAVA}" ]; then
      JAVA_BIN="${FOUND_JAVA}"
    else
      for candidate in /usr/bin/java /usr/lib/jvm/*/bin/java /opt/*/bin/java; do
        if [ -x "${candidate}" ]; then
          JAVA_BIN="${candidate}"
          break
        fi
      done
    fi
  fi
  PORT_OPT=""
  if [ -n "${SERVER_PORT}" ]; then
    PORT_OPT="-Dserver.port=${SERVER_PORT}"
  fi
  if [ "$(id -un)" = "${APP_USER}" ]; then
    cd "${APP_INSTALL_DIR}"
    nohup "${JAVA_BIN}" ${JAVA_OPTS} -Dspring.profiles.active="${SPRING_PROFILE}" ${PORT_OPT} -jar "${APP_FILE}" \
      >> "${LOG_DIR}/appsw-stdout.log" 2>> "${LOG_DIR}/appsw-stderr.log" </dev/null &
    echo $! > "${PID_FILE}"
  else
    runuser -u "${APP_USER}" -- bash -lc \
      "cd '${APP_INSTALL_DIR}' && nohup '${JAVA_BIN}' ${JAVA_OPTS} -Dspring.profiles.active='${SPRING_PROFILE}' ${PORT_OPT} -jar '${APP_FILE}' >> '${LOG_DIR}/appsw-stdout.log' 2>> '${LOG_DIR}/appsw-stderr.log' </dev/null & echo \$! > '${PID_FILE}'"
  fi
fi

sleep 5
ELAPSED=5
while [ "${ELAPSED}" -le "${HEALTHCHECK_TIMEOUT_SEC}" ]; do
  HTTP_CODE="$(curl -sk -o /dev/null -w '%{http_code}' --max-time 3 "${HEALTHCHECK_URL}" 2>/dev/null || true)"
  if [[ "${HTTP_CODE}" =~ ^[1-4][0-9]{2}$ ]]; then
    log "Rollback completado y validado (HTTP ${HTTP_CODE})"
    exit 0
  fi
  sleep 3
  ELAPSED=$((ELAPSED + 3))
done

log "ERROR: La versión restaurada no respondió en ${HEALTHCHECK_URL}"
exit 1
