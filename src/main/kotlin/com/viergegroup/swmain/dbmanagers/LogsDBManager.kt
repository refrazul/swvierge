package com.viergegroup.swmain.dbmanagers

import com.shared.commons.ErrorCode 
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.decodeHex
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbservices.ClientActivityDBService
import com.viergegroup.swmain.dbservices.LogsMetaDataDBService 
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto

internal object   LogsDBManager {
    private lateinit var logsMetaDataDBService: LogsMetaDataDBService
    fun init( logsMetaDataDBService: LogsMetaDataDBService){
        this.logsMetaDataDBService=logsMetaDataDBService
    } 

    fun checkLogExists(uuid: String): Long {
        return logsMetaDataDBService.checkLogExists(uuid.decodeHex())
    }
    fun upsertLogMetaData(requestEntity: RequestEntity,watcher:Watcher?): Long? {
        val data= LogsMetaData()
        StringUtils.copyNonEmptyProperties(requestEntity,data)
        data.uuid=StringUtils.hexStringToByteArray(requestEntity.uuid)
        data.operationId=requestEntity.operationIdRequest
        data.moduleId=requestEntity.operationIdRequest.moduleId
        data.serverName=requestEntity.serverNameRequest

        return logsMetaDataDBService.upsertLogMetaData(data,watcher)
    }

    fun upsertLogMetaData(responseEntity: ResponseEntity,watcher:Watcher?): Long? {
        val data= LogsMetaData()
        StringUtils.copyNonEmptyProperties(responseEntity,data)
        data.uuid=StringUtils.hexStringToByteArray(responseEntity.uuid)
        data.errorCode=ErrorCode.fromId(responseEntity.errorCode)
        data.errorCodeNumber=responseEntity.errorCode
        data.operationId=responseEntity.operationIdRequest
        data.moduleId=responseEntity.operationIdRequest.moduleId
        return logsMetaDataDBService.upsertLogMetaData(data,watcher) 
    }
    
    fun updateLogErrorData(responseEntity: ResponseEntity,merchantDto:MerchantDataDto): Long? {
        return logsMetaDataDBService.updateLogErrorData(responseEntity,merchantDto)
    }


}