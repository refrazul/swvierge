#!/usr/bin/env bash
# ==============================================================================
# Despliegue remoto idempotente en EC2 para APPSW.war (vía AWS SSM)
# - Soporta ejecución directa con `java -jar` (default) o `systemd` (USE_SYSTEMD=true)
# - Nunca usa `killall java` ni `pkill java`: identifica solo el proceso de este artefacto
# - Valida SHA-256, respalda la versión previa y ejecuta rollback automático si falla
# ==============================================================================
set -euo pipefail

S3_URI="${S3_URI:?Define S3_URI (ej. s3://bucket/backendvg/APPSW-sha.war)}"
S3_SHA_URI="${S3_SHA_URI:?Define S3_SHA_URI}"
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
RELEASE_VERSION="${RELEASE_VERSION:-unknown}"

APP_FILE="${APP_INSTALL_DIR}/${ARTIFACT_NAME}"
BACKUP_DIR="${APP_INSTALL_DIR}/backups"
STAGING_DIR="${APP_INSTALL_DIR}/staging"
LOG_DIR="${APP_INSTALL_DIR}/logs"
PID_FILE="${APP_INSTALL_DIR}/${ARTIFACT_NAME}.pid"
LAST_GOOD_LINK="${BACKUP_DIR}/last-good-${ARTIFACT_NAME}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
STAGING_FILE="${STAGING_DIR}/${ARTIFACT_NAME}.${TIMESTAMP}"
STAGING_SHA="${STAGING_FILE}.sha256"
BACKUP_FILE="${BACKUP_DIR}/${ARTIFACT_NAME}.${TIMESTAMP}.bak"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [DEPLOY] $*"
}

find_app_pid() {
  if [ -f "${PID_FILE}" ]; then
    local pid
    pid="$(cat "${PID_FILE}" 2>/dev/null || true)"
    if [ -n "${pid}" ] && kill -0 "${pid}" 2>/dev/null; then
      if ps -p "${pid}" -o args= 2>/dev/null | grep -Fq "${APP_FILE}"; then
        echo "${pid}"
        return 0
      fi
    fi
  fi
  # Búsqueda exacta únicamente por la ruta completa del artefacto (nunca killall/pkill java)
  pgrep -f "java.*-jar[[:space:]]+${APP_FILE}" | head -n 1 || true
}

stop_app() {
  if [ "${USE_SYSTEMD}" = "true" ]; then
    log "Deteniendo servicio systemd: ${SYSTEMD_SERVICE}"
    sudo systemctl stop "${SYSTEMD_SERVICE}" || true
    return 0
  fi

  local pid
  pid="$(find_app_pid)"
  if [ -z "${pid}" ]; then
    log "No hay proceso previo activo para ${APP_FILE}"
    return 0
  fi

  log "Deteniendo proceso PID=${pid} (${APP_FILE}) con SIGTERM..."
  kill -TERM "${pid}" 2>/dev/null || true

  local waited=0
  while kill -0 "${pid}" 2>/dev/null && [ "${waited}" -lt 30 ]; do
    sleep 1
    waited=$((waited + 1))
  done

  if kill -0 "${pid}" 2>/dev/null; then
    log "Proceso PID=${pid} no terminó tras 30s; enviando SIGKILL únicamente a PID=${pid}"
    kill -KILL "${pid}" 2>/dev/null || true
    sleep 2
  fi
  rm -f "${PID_FILE}"
}

start_app() {
  if [ "${USE_SYSTEMD}" = "true" ]; then
    log "Iniciando servicio systemd: ${SYSTEMD_SERVICE}"
    sudo systemctl start "${SYSTEMD_SERVICE}"
    return 0
  fi

  if [ "${APP_USER}" = "root" ]; then
    log "ERROR: APP_USER no puede ser root; debe ejecutarse como usuario sin privilegios (ej. ec2-user)"
    exit 1
  fi

  mkdir -p "${LOG_DIR}"
  chown -R "${APP_USER}:${APP_USER}" "${APP_INSTALL_DIR}"

  local port_opt=""
  if [ -n "${SERVER_PORT}" ]; then
    port_opt="-Dserver.port=${SERVER_PORT}"
  fi

  log "Iniciando ${APP_FILE} con perfil=${SPRING_PROFILE} ${port_opt} como usuario=${APP_USER} (sin root)"
  if [ "$(id -un)" = "${APP_USER}" ]; then
    cd "${APP_INSTALL_DIR}"
    nohup "${JAVA_BIN}" ${JAVA_OPTS} -Dspring.profiles.active="${SPRING_PROFILE}" ${port_opt} -jar "${APP_FILE}" \
      >> "${LOG_DIR}/appsw-stdout.log" 2>> "${LOG_DIR}/appsw-stderr.log" </dev/null &
    echo $! > "${PID_FILE}"
  else
    runuser -u "${APP_USER}" -- bash -lc \
      "cd '${APP_INSTALL_DIR}' && nohup '${JAVA_BIN}' ${JAVA_OPTS} -Dspring.profiles.active='${SPRING_PROFILE}' ${port_opt} -jar '${APP_FILE}' >> '${LOG_DIR}/appsw-stdout.log' 2>> '${LOG_DIR}/appsw-stderr.log' </dev/null & echo \$! > '${PID_FILE}'"
  fi

  local started_pid
  started_pid="$(cat "${PID_FILE}" 2>/dev/null || echo '')"
  local started_user
  started_user="$(ps -o user= -p "${started_pid}" 2>/dev/null | xargs || echo 'N/A')"
  log "Proceso iniciado con PID=${started_pid:-N/A} user=${started_user}"
}

check_health() {
  local elapsed=0
  log "Validando estabilidad y salud (timeout=${HEALTHCHECK_TIMEOUT_SEC}s, url=${HEALTHCHECK_URL})"
  sleep 5
  elapsed=5

  while [ "${elapsed}" -le "${HEALTHCHECK_TIMEOUT_SEC}" ]; do
    if [ "${USE_SYSTEMD}" = "true" ]; then
      if ! sudo systemctl is-active --quiet "${SYSTEMD_SERVICE}"; then
        log "ERROR: ${SYSTEMD_SERVICE} dejó de estar activo"
        return 1
      fi
    else
      local pid
      pid="$(find_app_pid)"
      if [ -z "${pid}" ]; then
        log "ERROR: El proceso Java de ${APP_FILE} murió durante el arranque"
        return 1
      fi
    fi

    # Acepta cualquier respuesta HTTP < 500 (200, 302, 401, 403, 404 indican Tomcat/Spring arriba)
    local http_code
    http_code="$(curl -sk -o /dev/null -w '%{http_code}' --max-time 3 "${HEALTHCHECK_URL}" || echo "000")"
    if [ "${http_code}" != "000" ] && [ "${http_code}" -lt 500 ]; then
      log "Health check OK (HTTP ${http_code}) tras ${elapsed}s"
      return 0
    fi

    sleep 3
    elapsed=$((elapsed + 3))
  done

  log "ERROR: Timeout esperando respuesta HTTP válida en ${HEALTHCHECK_URL}"
  return 1
}

run_rollback() {
  log "INICIANDO ROLLBACK AUTOMÁTICO..."
  local rollback_script
  rollback_script="$(dirname "$0")/rollback.sh"
  if [ -x "${rollback_script}" ]; then
    APP_INSTALL_DIR="${APP_INSTALL_DIR}" \
    ARTIFACT_NAME="${ARTIFACT_NAME}" \
    APP_USER="${APP_USER}" \
    JAVA_BIN="${JAVA_BIN}" \
    JAVA_OPTS="${JAVA_OPTS}" \
    SPRING_PROFILE="${SPRING_PROFILE}" \
    USE_SYSTEMD="${USE_SYSTEMD}" \
    SYSTEMD_SERVICE="${SYSTEMD_SERVICE}" \
    HEALTHCHECK_URL="${HEALTHCHECK_URL}" \
    HEALTHCHECK_TIMEOUT_SEC="${HEALTHCHECK_TIMEOUT_SEC}" \
    "${rollback_script}"
  elif [ -f "${BACKUP_FILE}" ]; then
    stop_app
    cp -f "${BACKUP_FILE}" "${APP_FILE}"
    start_app
    check_health
  else
    log "ERROR: No existe respaldo previo para rollback"
    return 1
  fi
}

# 1. Validar directorios
log "Iniciando despliegue versión=${RELEASE_VERSION} en ${APP_INSTALL_DIR}"
mkdir -p "${APP_INSTALL_DIR}" "${BACKUP_DIR}" "${STAGING_DIR}" "${LOG_DIR}"

# 2. Descargar artefacto y checksum desde S3
log "Descargando artefacto desde ${S3_URI}"
aws s3 cp "${S3_URI}" "${STAGING_FILE}" --only-show-errors
aws s3 cp "${S3_SHA_URI}" "${STAGING_SHA}" --only-show-errors

# 3. Verificar integridad SHA-256
EXPECTED_SHA="$(awk '{print $1}' "${STAGING_SHA}")"
ACTUAL_SHA="$(sha256sum "${STAGING_FILE}" | awk '{print $1}')"
if [ "${EXPECTED_SHA}" != "${ACTUAL_SHA}" ]; then
  log "ERROR: Integridad SHA-256 inválida (esperado=${EXPECTED_SHA}, actual=${ACTUAL_SHA})"
  rm -f "${STAGING_FILE}" "${STAGING_SHA}"
  exit 1
fi
log "Integridad SHA-256 verificada: ${ACTUAL_SHA}"

# 4. Respaldar artefacto actual (sin borrar el último funcional)
if [ -f "${APP_FILE}" ]; then
  cp -p "${APP_FILE}" "${BACKUP_FILE}"
  ln -sfn "${BACKUP_FILE}" "${LAST_GOOD_LINK}"
  log "Respaldo creado en ${BACKUP_FILE}"
else
  log "Instalación inicial: no había artefacto previo en ${APP_FILE}"
fi

# 5. Detener, instalar nuevo artefacto y arrancar
stop_app
mv -f "${STAGING_FILE}" "${APP_FILE}"
rm -f "${STAGING_SHA}"
if id "${APP_USER}" >/dev/null 2>&1; then
  chown "${APP_USER}:${APP_USER}" "${APP_FILE}" || true
fi
echo "${RELEASE_VERSION} ${ACTUAL_SHA} ${TIMESTAMP}" > "${APP_INSTALL_DIR}/CURRENT_RELEASE"

start_app

# 6. Validar arranque; si falla -> mostrar últimas líneas de log, rollback automático y salir con error
if ! check_health; then
  log "--- ÚLTIMAS LÍNEAS appsw-stderr.log ---"
  tail -n 30 "${LOG_DIR}/appsw-stderr.log" 2>/dev/null || true
  log "--- ÚLTIMAS LÍNEAS appsw-stdout.log ---"
  tail -n 30 "${LOG_DIR}/appsw-stdout.log" 2>/dev/null || true
  log "FALLO EN VALIDACIÓN DE LA NUEVA VERSIÓN. Ejecutando rollback..."
  if run_rollback; then
    log "ROLLBACK_STATUS=SUCCESS (versión anterior restaurada y saludable)"
  else
    log "ROLLBACK_STATUS=FAILED (requiere intervención manual)"
  fi
  exit 1
fi

# Conservar últimos 5 respaldos (nunca borrar el último funcional)
ls -1t "${BACKUP_DIR}/${ARTIFACT_NAME}".*.bak 2>/dev/null | tail -n +6 | xargs -r rm -f || true
log "DESPLIEGUE EXITOSO versión=${RELEASE_VERSION} sha256=${ACTUAL_SHA}"
