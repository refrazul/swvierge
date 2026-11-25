package com.viergegroup.swmain.dbservices

import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dtos.logs.LogsFilterDto
import com.viergegroup.swmain.dbrepositories.LogDataRepository
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.AppSettings
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto
import org.springframework.scheduling.annotation.Async
import kotlinx.coroutines.*
import com.viergegroup.swmain.managers.MetricsManager
import com.viergegroup.swmain.managers.DBCipherManager
import org.springframework.beans.BeanUtils


@Service
class LogsMetaDataDBService {

    @Autowired
    lateinit var logRepository: LogDataRepository

    @Transactional(readOnly = true)
    fun checkLogExists(uuid: ByteArray): Long { 
        return logRepository.logExists(uuid)
    }
    @Transactional(readOnly = true) 
    fun getLogs(dto: LogsFilterDto): List<LogsMetaData> {
        WatcherGlobalLog.info("getLogs  dto.transactionType.name [${   dto.transactionType.name}]  dto.startDate ${dto.startDate} dto.endDate ${dto.endDate}")
        return logRepository.getLogs(
            dto.errorCode,
            dto.serverName,
            dto.hostType.name,
            dto.terminalSerialNumberFilter,
            dto.rrnFilter,
            dto.moduleId.name, 
            dto.operationId.name,
            dto.transactionType.name,
            dto.startDate,
            dto.endDate,
            dto.startRow,
            dto.totalRows
        )
    }

    @Transactional(readOnly = true)
    fun getLogsPages(dto: LogsFilterDto): Int {
        WatcherGlobalLog.info("getLogsPages    dto.transactionType.name [${   dto.transactionType.name}] dto.startDate ${dto.startDate} dto.endDate ${dto.endDate}")
        return logRepository.getLogsPages(
            dto.errorCode,
            dto.serverName,
            dto.hostType.name,
            dto.terminalSerialNumberFilter,
            dto.rrnFilter,
            dto.moduleId.name,
            dto.operationId.name,
            dto.transactionType.name,
            dto.startDate,
            dto.endDate,
            dto.totalRows
        )
    }

    @Transactional
    fun updateLogErrorData(data: ResponseEntity, merchantDto: MerchantDataDto): Long? {
         CoroutineScope(Dispatchers.IO).async {
            try{

                val sql = """
                SELECT update_log_error_data(
                    '${StringUtils.hexStringToByteArray(data.uuid).joinToString("") { String.format("\\x%02x", it) }}',
                    '${data.errorCodeEnum.name}',
                    ${data.errorCode},
                    '${data.errorMessage}',
                    '${data.operationIdRequest.name}',
                    ${data.operationDuration},
                    '${merchantDto.rrn}',
                    '${merchantDto.responseCode}',
                    '${merchantDto.cardBin}',
                    '${merchantDto.cardNumber}',
                    '${merchantDto.cardType.name}',
                    '${merchantDto.affiliation}',
                    '${merchantDto.bankName}',
                    '${merchantDto.merchantName}',
                    '${merchantDto.merchantTypeCode}',
                    '${merchantDto.merchantAddress}'
                );
            """
            WatcherGlobalLog.debug("updateLogErrorData SQL:\n$sql")


                logRepository.updateLogErrorData(
                    StringUtils.hexStringToByteArray(data.uuid),
                    data.errorCodeEnum.name,
                    data.errorCode,
                    data.errorMessage,
                    data.operationIdRequest.name,
                    data.operationDuration,
                    merchantDto.rrn,
                    merchantDto.responseCode,
                    merchantDto.cardBin, 
                    merchantDto.cardNumber,
                    merchantDto.cardType.name,
                    merchantDto.affiliation,
                    merchantDto.bankName,
                    merchantDto.merchantName,
                    merchantDto.merchantTypeCode,
                    merchantDto.merchantAddress
                )
            }catch(ex:Exception){
                WatcherGlobalLog.error("updateLogErrorData EXCEPTION: ${ex.message}")

            }
          
        }
        return 0
    }
    
    @Transactional
    fun upsertLogMetaData(orgData: LogsMetaData, watcher: Watcher?): Long? {
        WatcherGlobalLog.debug("LogsMetaDataDBService_upsertLogMetaData=>")
        var data=LogsMetaData()
        BeanUtils.copyProperties(orgData,data)
 
        WatcherGlobalLog.obj(data)
        val hostConfig = AppSettings.getHostConfig(data.hostType)
        val timezone = hostConfig.zoneTime
        if (watcher != null) watcher.debug(" timezone=>${timezone}")
    
        if (watcher != null) watcher.debug(" upsertLogMetaData data.getServerZoneTime=>${data.creationDateTime}")
        data.creationDateTime = StringUtils.getServerZoneTime(data.hostType)
        if (watcher != null) watcher.debug(" upsertLogMetaData data.getServerZoneTime=>${data.creationDateTime}")
        data= MetricsManager.setMetricsLogs(data) 

         CoroutineScope(Dispatchers.IO).async {
            logRepository.upsertLogMetaData(
                timezone,
                data.bank.toString(), 
                data.clientEmail,
                data.clientGeolocation,
                data.clientIP,
                data.clientUserName,
                data.creationDateTime,
                data.deviceModel,
                data.errorCode.name,
                data.errorCodeNumber,
                data.errorMessage,
                data.hostType.name,
                data.lastModificationDateTime,
                data.merchantType.name,
                data.moduleId.name,
                data.operationId.name,
                data.pointOfServiceEntryMode.name,
                data.responseCode,
                data.rrn,
                data.serverName,
                data.terminalSerialNumber,
                data.transactionAmount,
                data.transactionType.toString(),

                data. ramTotal,  
                data. ramUsed,
                data.ramUsagePercentage,
                data. storageTotal,
                data.storageUsed, 
                data. storageUsagePercentage,
                data.networkUsagePercentage,
                data.cpuUsagePercentage,
        
                data.uuid
            )
        }
    
        return 0
    }
}
