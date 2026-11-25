package com.viergegroup.swmain.managers

import com.shared.commons.AppSettings
import com.shared.commons.DynamicISOFields
import com.shared.commons.ErrorCode
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.isoprocessor.models.iso.Iso8583TransactionMetaData
import com.viergegroup.swmain.dtos.comm.DisconnectHostResponseDto
import com.viergegroup.swmain.dtos.comm.HostStatusResponseDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.KeyDirection
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.BeanUtils
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause


object HostMasterManagerMulti {

    // ===== POOL plano: (HostType, LN) -> HostCommManager =====
    private val hosts = mutableMapOf<Pair<HostType, Int>, HostCommManager>()
    // cursor RR por host
    private val rrCursor = mutableMapOf<HostType, AtomicInteger>()

    // ===== INIT (sin cambiar firma) =====
    fun init() {
        WatcherGlobalLog.debug("HostMasterManagerMulti IS STARTING.")

        for (hostType in HostType.values()) {
            if (hostType == HostType.Undefined) continue

            val cfg = AppSettings.getHostConfig(hostType)
            val count = cfg.numberOfLogicalNetworks.coerceAtLeast(1)

            WatcherGlobalLog.info("INITIALIZATING: $hostType with $count logical networks")
            rrCursor.putIfAbsent(hostType, AtomicInteger(0))
            if(cfg.mustSendTxnToHost)
                AlarmEmailService.sendAlarmEmail(AlarmCause.HOST_COMM_INITIALIZED,"INITIALIZATING: $hostType with $count logical networks")
             
            for (lnId in 0 until count) { 
                val watcher = WatcherContext.initHost(hostType)
                val hcm = HostCommManager()
                hcm.init(hostType, lnId, watcher)

                // solo LN 0 corre el worker de reversos
                hcm.enableReversesWorker(lnId == 0)

                hosts[Pair(hostType, lnId)] = hcm
            }
        }

        // Intentar conectar todos
        for ((key, hcm) in hosts) {
            try {
                hcm.tryToConnect()
            } catch (ex: Exception) {
                WatcherGlobalLog.error("INIT connect EXCEPTION [$key]: ${ex.message}")
            }
        }
    }
 
    // Convierte un TransactionResponse "grande" en Iso8583TransactionMetaData "ligero"
    private fun toTxnMetaData(full: TransactionResponse): Iso8583TransactionMetaData {
        val out = Iso8583TransactionMetaData()
        out.errorCode = full.errorCode
        out.errorMessage = full.errorMessage

        // Evitar cargar objetos enormes: crea TransactionData vacíos
        out.iso8583MetaDataRequest.transactionData = TransactionData()
        out.iso8583MetaDataResponse.transactionData = TransactionData()

        // Copia superficial de propiedades del request/response hacia los TransactionData de salida
        full.transactionRequest?.let {
            org.springframework.beans.BeanUtils.copyProperties(it, out.iso8583MetaDataRequest.transactionData!!)
        }
        full.transactionResponse?.let {
            org.springframework.beans.BeanUtils.copyProperties(it, out.iso8583MetaDataResponse.transactionData!!)
        }
        return out
    }



    // ======= VALIDACIÓN (privada, sin cambiar llamadas) =======
    private fun validateHost(hostType: HostType): Iso8583MetaData {
        val response = Iso8583MetaData()
        if (hostType == HostType.Undefined) {
            response.errorCode = ErrorCode.HostIsUnknown.errorCodeInt
            response.errorMessage = "$hostType ${ErrorCode.HostIsUnknown.errorCodeMessage}"
            return response
        }
        if (!AppSettings.getHostConfig(hostType).hostIsActive()) {
            response.errorCode = ErrorCode.HostIsInactive.errorCodeInt
            response.errorMessage = "$hostType ${ErrorCode.HostIsInactive.errorCodeMessage}"
            return response
        }
        if (hosts.none { it.key.first == hostType }) {
            response.errorCode = ErrorCode.HostIsUnknown.errorCodeInt
            response.errorMessage = "$hostType NO POOL -> ${ErrorCode.HostIsUnknown.errorCodeMessage}"
        }
        WatcherContext.finishOperation()
        return response
    }

    // ======= Balanceo =======
    private fun pickManager(hostType: HostType): HostCommManager {
        val cfg = AppSettings.getHostConfig(hostType)
        val pool = hosts.filterKeys { it.first == hostType }.values.toList()
        if (pool.isEmpty()) error("No hay managers para $hostType")

        val healthy = pool.filter { it.isHealthy() }
        if (healthy.isEmpty()) {
            val idx = rrCursor[hostType]!!.getAndIncrement().getSafeModulo(pool.size)
            return pool[idx]
        }

        return when (cfg.balancePolicy.uppercase()) {
            "LEAST_IN_FLIGHT" -> healthy.minByOrNull { it.getInFlight() } ?: healthy.first()
            else -> {
                val idx = rrCursor[hostType]!!.getAndIncrement().getSafeModulo(healthy.size)
                healthy[idx]
            }
        }
    }

    private fun Int.getSafeModulo(div: Int): Int = abs(this % div)

    // ======= STATUS (firma intacta) =======
    fun hostStatus(hostType: HostType, watcher: Watcher): HostStatusResponseDto {
        val validation = validateHost(hostType)
        val resp = HostStatusResponseDto()
        watcher.info("hostStatus")
        if (validation.errorCode != 0) {
            resp.errorCode = validation.errorCode
            resp.errorMessage = validation.errorMessage
            return resp
        }

        val pool = hosts.filterKeys { it.first == hostType }.values.toList()
        if (pool.isEmpty()) {
            resp.errorCode = ErrorCode.HostIsUnknown.errorCodeInt
            resp.errorMessage = "$hostType NO POOL"
            return resp
        }

        val base = pool.first().hostStatus()
        val connected = pool.count { it.isConnected() }
        val inFlight = pool.sumOf { it.getInFlight() }
        base.errorMessage = (base.errorMessage ?: "") + " | LNs=${pool.size} connected=$connected inFlight=$inFlight"
        return base
    }

    // ======= CONNECT (firma EXACTA que pediste) =======
    fun connectToHost(hostType: HostType, watcher: Watcher): Iso8583TransactionMetaData {
        val validationResponse = validateHost(hostType)
        val response = Iso8583TransactionMetaData()
        if (validationResponse.errorCode != 0) {
            response.errorCode = validationResponse.errorCode
            response.errorMessage = validationResponse.errorMessage
            return response
        } 

        // Conecta TODOS los LNs de ese host; devolvemos el resultado del LN 0
        var ln0Resp: Iso8583TransactionMetaData? = null
        hosts
            .filterKeys { it.first == hostType }
            .toList()
            .sortedBy { it.first.second } // ordenar por LN
            .forEach { (key, hcm) ->
                val r = hcm.connectToHost(watcher)
                if (key.second == 0) ln0Resp = r
            }

        return ln0Resp ?: response
    }

    // ======= DISCONNECT (firma intacta) =======
    fun disconnectHost(hostType: HostType, watcher: Watcher): DisconnectHostResponseDto {
        val validation = validateHost(hostType)
        val resp = DisconnectHostResponseDto()
        if (validation.errorCode != 0) {
            resp.errorCode = validation.errorCode
            resp.errorMessage = validation.errorMessage
            return resp
        }
        var last = DisconnectHostResponseDto()
        hosts.filterKeys { it.first == hostType }.values.forEach { hcm ->
            last = hcm.disconnectHost(watcher)
        }
        return last
    }

    // ======= TRACKING (firmas intactas) =======
    fun removeRequestMessage(hostType: HostType, isoMetaData: Iso8583MetaData, isoResponse: Iso8583MetaData?, watcher: Watcher) {
        hosts.filterKeys { it.first == hostType }.values.forEach { hcm ->
            try {
                hcm.removeRequestMessage(isoMetaData, isoResponse, watcher)
                return
            } catch (_: Exception) { /* ignorar si no estaba en ese LN */ }
        }
    }

    fun checkReceivedMessage(hostType: HostType, systemTraceNumber: String, watcher: Watcher): Iso8583MetaData? {
        val validation = validateHost(hostType)
        if (validation.errorCode != 0) return validation
        hosts.filterKeys { it.first == hostType }.values.forEach { hcm ->
            val resp = hcm.checkReceivedMessage(systemTraceNumber, watcher)
            if (resp != null) return resp
        }
        return null
    }

    // ======= SEND (firmas intactas) =======
    fun sendMessage(iso8583MetaData: Iso8583MetaData, watcher: Watcher): Iso8583MetaData {
        val hostType = iso8583MetaData.getHostType()
        val validation = validateHost(hostType)
        if (validation.errorCode != 0) return validation
        val hcm = pickManager(hostType)
        return hcm.sendMessage(iso8583MetaData, watcher)
    }

    fun sendIsoAndWaitForResponse(hostType: HostType, isoAscii: String, watcher: Watcher): Iso8583TransactionMetaData {
        val validation = validateHost(hostType)
        if (validation.errorCode != 0) {
            val out = Iso8583TransactionMetaData()
            out.errorCode = validation.errorCode
            out.errorMessage = validation.errorMessage
            return out
        }
        val hcm = pickManager(hostType)
        val full = hcm.sendIsoAndWaitForResponse(isoAscii)
        return toTxnMetaData(full)
    }
 
    fun sendIsoAndWaitForResponse(isoRequest: Iso8583MetaData, watcher: Watcher): TransactionResponse {
        return sendIsoAndWaitForResponse(isoRequest.getHostType(), isoRequest, watcher)
    }

    fun sendIsoAndWaitForResponse(hostType: HostType, isoRequest: Iso8583MetaData, watcher: Watcher): TransactionResponse {
        val hostSettings = AppSettings.getHostConfig(hostType)
        var response = TransactionResponse(isoRequest, null)
        watcher.info("sendIsoAndWaitForResponse hostType $hostType hostSettings.hostResponseHardcode  ${hostSettings.hostResponseHardcode}")

        val sendKeyLoadHardcode =
            (isoRequest.transactionData!!.transactionType == TransactionType.KeyLoad ||
             isoRequest.transactionData!!.transactionType == TransactionType.KeyLoadPin) &&
            hostSettings.enableKeyLoadHarcode

        val affiliation = isoRequest.transactionData!!.affiliation
        val merchantName = isoRequest.transactionData!!.merchantName

        watcher.info("enableKeyLoadHarcode [${hostSettings.enableKeyLoadHarcode}] transactionType [${isoRequest.transactionData!!.transactionType}]")
        watcher.info("sendIsoAndWaitForResponse_affiliation [$affiliation] merchantName [$merchantName]")

        if (!hostSettings.hostResponseHardcode &&
            !sendKeyLoadHardcode &&
            !affiliation.equals("0000000") &&
            !affiliation.equals("00000000")) {

            val validationResponse = validateHost(hostType)
            if (validationResponse.errorCode != 0) {
                response.errorCode = validationResponse.errorCode
                response.errorMessage = validationResponse.errorMessage
                return response
            }
            watcher.info("keepConnectionAlive  ${hostSettings.keepConnectionAlive}")

            if (hostSettings.keepConnectionAlive) {
                val hcm = pickManager(hostType)
                response = hcm.sendIsoAndWaitForResponse(isoRequest)
                watcher.info("ISO RESPONSE IS_NULL: [${response.isoResponse != null}]")
                if (response.isoResponse != null) {
                    try { watcher.info("ISO_RESPONSE_ASCII ${response.isoResponse!!.isoAscii} ") }
                    catch (ex: Exception) { watcher.error("EXCEPTION ${ex.message} ") }
                    watcher.debug("BEFORE watcher.saveIsoRaw ")
                    watcher.saveIsoRaw(response.isoResponse!!)
                    watcher.debug("AFTER watcher.saveIsoRaw ")
                }
            } else {
                val hcm = pickManager(hostType)
                response = hcm.sendIsoAndCloseConnection(isoRequest, hostType, watcher)
            }
            return response
        }

        // ----- HARD-CODE (mínimo, respeta tu lógica) -----
        val isoResponse = Iso8583MetaData()
        val transDataResponse = TransactionData()
        val errorMessage = isoRequest.errorMessage
        val errorCode = isoRequest.errorCode

        isoResponse.transactionData = transDataResponse
        if (errorCode != 0) {
            response.errorMessage = errorMessage
            response.errorCode = errorCode
            isoResponse.setResponseCode(ErrorCode.fromId(errorCode))
            watcher.error("ERROR sendIsoAndWaitForResponse $errorMessage")
        } else {
            isoResponse.setResponseCode(ErrorCode.Approved)
        }
        BeanUtils.copyProperties(isoRequest.transactionData!!, transDataResponse)
        val authResId = Iso8583MetaData.generateAuthId(hostType)
        isoResponse.setAuthorizationId(authResId)
        isoResponse.errorCode = errorCode
        isoResponse.errorMessage = errorMessage
        response = TransactionResponse(isoRequest, isoResponse)
        return response
    }

    // ======= UTILIDADES (firmas intactas) =======
    fun sendEchoAndWaitForResponse(hostType: HostType, watcher: Watcher): TransactionResponse {
        val requestDto = DynamicISOFields.getEchoRequestMetaData(hostType)
        requestDto.transactionData!!.hostType = hostType
        return sendIsoAndWaitForResponse(hostType, requestDto, watcher)
    }

    fun sendFunKeysAndWaitForResponse(
        transactionType: TransactionType,
        keyDirection: KeyDirection,
        hostType: HostType,
        watcher: Watcher
    ): TransactionResponse {
        val requestDto = DynamicISOFields.getFunKeysMetaData(transactionType, keyDirection, hostType)
        requestDto.transactionData!!.hostType = hostType
        requestDto.transactionData!!.transactionType = transactionType
        requestDto.transactionData!!.keyDirection = keyDirection
        return sendIsoAndWaitForResponse(hostType, requestDto, watcher)
    }

    fun turnOnCheckReverses(hostType: HostType) {
        // enciende el check de reversos en LN 0
        hosts[Pair(hostType, 0)]?.setCheckReverses(true)
    }
}
