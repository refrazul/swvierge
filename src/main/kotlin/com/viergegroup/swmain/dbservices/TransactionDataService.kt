package com.viergegroup.swmain.dbservices
import com.shared.commons.GsonConfig

import com.shared.commons.AppSettings
import com.shared.commons.DynamicISOFields
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dtos.transactions.TransactionFilterDto
import com.viergegroup.swmain.dbrepositories.TransactionDataRepository
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.managers.DBCipherManager
import com.viergegroup.swmain.webwatcher.Watcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.managers.MetricsManager
import kotlinx.coroutines.*
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause

@Service
class TransactionDataService { 
    @Autowired
    lateinit var transactionDataRepository: TransactionDataRepository
    //DBCipherManager.decryptTransactionData(data,watcher)
    @Transactional
    fun updateTransactionStatus(pTransactionId: Long, transactionStatus: TransactionStatus){
        WatcherGlobalLog.reverseDebug(" getTransactionData => ${transactionStatus.toString() }")
        WatcherGlobalLog.reverseDebug(" getTransactionData => ${transactionStatus.name  }")

        transactionDataRepository.updateTransactionStatus(
            pTransactionId,transactionStatus.name 
        ) 
    }      

     @Transactional
    fun updateIsoRequestByUuid(   uuid: ByteArray,
         metaData: ByteArray,
        isoAsciiRequest: ByteArray,
      timeZone: String){ 
        transactionDataRepository.updateIsoRequestByUuid(
            uuid,metaData,isoAsciiRequest,timeZone
        ) 
    }     
   @Transactional
    fun updateIsoResponseByUuid(  uuid: ByteArray,
        isoAsciiResponse: ByteArray,
      timeZone: String){ 
        transactionDataRepository.updateIsoResponseByUuid(
            uuid,isoAsciiResponse,timeZone
        ) 
    }      


    @Transactional(readOnly = true)
    fun getTransactionData(dto: TransactionFilterDto,watcher: Watcher):TransactionDBData? {
        watcher.debug(" getTransactionData => dto.transactionId ${dto.transactionId},hostType[${ dto.hostType.name}] ,transactionMessageType[${  dto.transactionMessageType.name}],transactionStatus[${  dto.transactionStatus}],terminalSerialNumberFilter[${   dto.terminalSerialNumberFilter}],rrn[${   dto.rrn}],authorizationIdResponse[${   dto.authorizationIdResponse}]")
        watcher.debugObj(dto) 
         if(dto.transactionType==TransactionType.Reverse)
       WatcherGlobalLog.reverseDebugRrn(dto.rrn," getTransactionData => dto.transactionId ${dto.transactionId},hostType[${ dto.hostType.name}] ,transactionMessageType[${  dto.transactionMessageType.name}],transactionStatus[${  dto.transactionStatus}],terminalSerialNumberFilter[${   dto.terminalSerialNumberFilter}],rrn[${   dto.rrn}],authorizationIdResponse[${   dto.authorizationIdResponse}]")

        val orgData=transactionDataRepository.getTransactionData(
            dto.transactionId,
            dto.hostType.name, 
            dto.transactionMessageType.name,
            dto.transactionStatus.name,  
            dto.terminalSerialNumberFilter,
            dto.rrn,  
            dto.authorizationIdResponse
        ).firstOrNull()
        if(orgData==null){
            watcher.debug(" getTransactionData IS NULL ")
            return null
        }else{ 
            watcher.debug(" getTransactionData IS NOT NULL ")
            //watcher.debugObj(orgData)
        } 
         
        return DBCipherManager.decryptTransactionData(orgData,watcher)
    }
    @Transactional(readOnly = true)
    fun getTransactionsList(dto: TransactionFilterDto): List<TransactionDBData> {
        return transactionDataRepository.getTransactionsList(
            dto.serverName,
            dto.operationId.name,
 
            dto.hostType.name,
            dto.transactionType.name,
            dto.terminalSerialNumberFilter,
            dto.rrnFilter,
            dto.startDate,
            dto.endDate,
            dto.startRow,
            dto.totalRows
        )
    }


    @Transactional(readOnly = true)
    fun getTxnsToBeReversed( hostType: HostType):List<Long> {
        return transactionDataRepository.getTxnsToBeReversed(
            hostType.name
        )
    }

   

    @Transactional(readOnly = true)
    fun getTotalTransactionPages(dto: TransactionFilterDto): Int {
        return transactionDataRepository.getTotalTransactionPages(

            dto.serverName,
            dto.operationId.name,

            dto.hostType.name,

            dto.transactionType.name,

            dto.terminalSerialNumberFilter,
            dto.rrnFilter,
            dto.startDate,

            dto.endDate,
            dto.totalRows
        )
    } 


    
     //LACPI2
     @Transactional(readOnly = true)
    fun getTransactionByTypeAndRrn(rrn: String, transactionType: String,watcher: Watcher):TransactionDBData? {
        watcher.debug(" _LACPI2 rrn => dto.transactionId [${rrn}],transactionType[${transactionType}] ")
        val orgData=transactionDataRepository.getTransactionByTypeAndRrn(
            rrn,
            transactionType
        ).firstOrNull()
        if(orgData==null){
            watcher.debug(" _LACPI2 getTransactionByTypeAndRrn IS NULL ")
            return null
        }else{ 
            watcher.debug(" _LACPI2 getTransactionByTypeAndRrn IS NOT NULL ")
            //watcher.debugObj(orgData)
        }  
        return DBCipherManager.decryptTransactionData(orgData,watcher)
    }

    
    @Transactional
    fun insertTransactionData(orgData: TransactionDBData,isInitial:Boolean,watcher: Watcher): Long? {

        var data=DBCipherManager.encryptTransactionData(orgData,watcher)

     
        val hostConfig = AppSettings.getHostConfig(data.hostType)
        val timezone = hostConfig.zoneTime
        
        watcher.debug(" timezone=>${ timezone}")
 
   
        watcher.debug("insertTransactionData_data=>")
        data.errorCode=ErrorCode.fromId( data.errorCodeNumber)

        watcher.debugObj(data)

        watcher.debug(" data.getServerZoneTime=>${ data.creationDateTime}")

        data.creationDateTime=StringUtils.getServerZoneTime(data.hostType);
        watcher.debug(" data.getServerZoneTime=>${ data.creationDateTime}")
        data.lastModificationDateTime=StringUtils.getServerZoneTime(data.hostType);

        try{
            watcher.debug("  data.transactionCurrencyCode=>${  data.transactionCurrencyCode}") 
            watcher.debug("  data.transactionCurrencyCodeNumber=>${  data.transactionCurrencyCodeNumber}")
             data.transactionCurrencyCodeNumber= data.transactionCurrencyCode.toInt()
        }catch(ex:Exception){
            watcher.error("ex=${  ex.message}")
          
        } 
        // Construir la consulta SQL simulada para el log
        
           //  p_device_type => '${data.deviceType.name}',
              //    p_client_id => '${data.clientId}',
        val sqlLog = """ 
        SELECT insert_transaction_data(
            p_timezone => '${timezone}',
            p_creation_date_time => '${data.creationDateTime}',
            p_last_modification_date_time => '${data.lastModificationDateTime}',
            
            p_error_code => '${data.errorCode.name}',
            p_error_code_number => ${data.errorCodeNumber},
            p_error_message => '${data.errorMessage ?: " "}',
    
            p_card_number => '${data.cardNumber ?: ""}',
            p_affiliation => '${data.affiliation ?: ""}',
            p_bank_name => '${data.bankName ?: ""}',
            p_merchant_name => '${data.merchantName ?: ""}',
            p_merchant_type_code => '${data.merchantTypeCode ?: ""}',
            p_merchant_address => '${data.merchantAddress ?: ""}',
            p_operation_id => '${data.operationId.name}',

             p_device_type => '${data.deviceType.name}',
                p_client_id => '${data.clientId}',
              p_fiid=> '${data.fiid}',
              p_transaction_id_mpp=> '${data.transactionIdMpp}', 

            p_authorization_id_response => '${data.authorizationIdResponse ?: ""}',
            p_bank => '${data.bank.name}',
            p_card_type => '${data.cardType.name}',
            p_client_email => '${data.clientEmail ?: ""}',
            p_client_ip => '${data.clientIP ?: ""}',
            p_client_location => '${data.clientLocation ?: ""}',
            p_client_user_name => '${data.clientUserName ?: ""}', 
            p_db_kcv => '${data.dbKcv ?: ""}', 
            p_device_model => '${data.deviceModel ?: ""}',
            p_emv_data => '${StringUtils.byteArrayToHex(data.emvData)}',
            p_host_type => '${data.hostType.name}',
            p_iso_ascii_request => '${StringUtils.byteArrayToHex(data.isoASCIIRequest)}',
            p_iso_ascii_response => '${StringUtils.byteArrayToHex(data.isoASCIIResponse)}',
            p_key_serial_number => '${data.keySerialNumber ?: ""}',
            p_merchant_type => '${data.merchantType.name}',
            p_meta_data => '${StringUtils.byteArrayToHex(data.metaData)}',
            p_original_transaction_id => ${data.originalTransactionId ?: -1},
            p_payment_plan_type => '${data.paymentPlanType.name}',
            p_point_of_service_entry_mode => '${data.pointOfServiceEntryMode.name}',
            p_response_code => '${data.responseCode ?: ""}',
            p_rrn => '${data.rrn ?: ""}',
            p_system_trace_audit_number=> '${data.systemTraceAuditNumber ?: ""}',

            p_server_name => '${data.serverName ?: ""}',
            p_terminal_serial_number => '${data.terminalSerialNumber ?: ""}',
            p_transaction_amount => ${data.transactionAmount},
            p_transaction_direction => '${data.transactionDirection.name}',
            p_transaction_message_type => '${data.transactionMessageType.name}',
            p_transaction_status => '${data.transactionStatus.name}',
            p_transaction_type => '${data.transactionType.name}',
            p_transaction_currency_code_number => ${data.transactionCurrencyCodeNumber},
  
                  
           ram_total => ${data.ramTotal},
        ram_used => ${data.ramUsed},
        ram_usage_percentage => ${data.ramUsagePercentage},

        storage_total => ${data.storageTotal},
        storage_used => ${data.storageUsed},
        storage_usage_percentage => ${data.storageUsagePercentage},
        network_usage_percentage => ${data.networkUsagePercentage},
        cpu_usage_percentage => ${data.cpuUsagePercentage}, 

            p_uuid => '${StringUtils.byteArrayToHex(data.uuid)}'
        );
    """.trimIndent() 
    

        if(orgData.transactionType==TransactionType.Reverse){
            WatcherGlobalLog.reverseDebug("SQL_LOG:\n$sqlLog")

        }
        watcher.debug("SQL_LOG:\n$sqlLog")
 
        var exceptionDetails=""
        data= MetricsManager.setMetrics(data)  
      for (attempt in 1..7) {

            try { 
            val txnId= transactionDataRepository.insertTransactionData(
                isInitial,
                    timezone,
                    data.creationDateTime,
                    data.lastModificationDateTime,
                    
                    data.errorCode.name,
                    data.errorCodeNumber,
                    data.errorMessage,  
        
                    data.errorMessage,  
        
                    
                    data.cardNumber,  
                    data.affiliation,  
                    data.bankName,  
                    data. merchantName,  
                    data.merchantTypeCode,  
                    data. merchantAddress,  
        
        
         
                    data.operationId.name,
                  data.deviceType.name,
                   data.clientId, 
                    data.fiid,   
                    data.transactionIdMpp,  

                    data.authorizationIdResponse,
                    data.bank.name,
                    data.cardType.name,
                    data.clientEmail,
                    data.clientIP,
                    data.clientLocation,
                    data.clientUserName,
                    data.dbKcv,
                    data.deviceModel,
                    data.emvData,
                    data.hostType.name,
                    data.isoASCIIRequest,
                    data.isoASCIIResponse,
                    data.keySerialNumber,
                    data.merchantType.name,
                    data.metaData,
                    data.originalTransactionId,
                    data.paymentPlanType.name,
                    data.pointOfServiceEntryMode.name,
                    data.responseCode,
                    data.rrn,
                    data.systemTraceAuditNumber,

                    data.serverName,
                    data.terminalSerialNumber,
                    data.transactionAmount,
                    data.transactionDirection.name,
                    data.transactionMessageType.name,
                    data.transactionStatus.name,
                    data.transactionType.name, 
                    data.transactionCurrencyCodeNumber,
        
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
                return txnId
            } catch (e: Exception)   {  
                exceptionDetails+=e.message+"\n"
                WatcherGlobalLog.error("insertTransactionData - Error: ${e.message}")
            } 

        }


        WatcherGlobalLog.dbError(  "DETALLES PARAMETROS : sqlLog [${sqlLog}] \n  ${GsonConfig.gsonRaw.toJson(data)} | \n\n EXCEPCION DETALLES: ${exceptionDetails} ")

        AlarmEmailService.sendAlarmEmail(
            AlarmCause.DB_ERROR,
            "DETALLES PARAMETROS : sqlLog [${sqlLog}] \n  ${GsonConfig.gsonRaw.toJson(data)} | \n\n EXCEPCION DETALLES: ${exceptionDetails} "
        )
        return 0L
    } 


    fun generateTxns(numberTxns:Int){
        for( i in 0..numberTxns){
            val data=TransactionDBData()
            data.rrn= DynamicISOFields.generateRandomRRN()
            data.responseCode="00"
            data.cardType= CardType.getRandomCardType()
            if(data.cardType== CardType.Undefined||data.cardType== CardType.DISCOVER){
                data.cardType= CardType.VISA
            }

            data.transactionMessageType= TransactionMessageType.Financial
            data.hostType= HostType.Banorte
            data.pointOfServiceEntryMode= PointOfServiceEntryMode.Contactless
            data.transactionStatus= TransactionStatus.Approved
            data.authorizationIdResponse= DynamicISOFields.getAuthorizationIdentificationResponse(data.hostType)
            data.transactionAmount= DynamicISOFields.generateRandomTransactionAmount()
            data.terminalSerialNumber= DynamicISOFields.generateRandomTerminalSerialNumber()
            data.serverName= AppSettings.getInstance().serverName

            data.keySerialNumber= DynamicISOFields.generateRandomKeySerialNumber()
            data.metaData= StringUtils.generateRandomByteArray(1500)
            data.isoASCIIRequest= StringUtils.generateRandomByteArray(200)
            data.isoASCIIResponse= StringUtils.generateRandomByteArray(200)

            data.emvData= StringUtils.generateRandomByteArray(400)
            data.creationDateTime= DynamicISOFields.generateRandomDateTime(0,12)
            data.lastModificationDateTime= data.creationDateTime
            transactionDataRepository.save(data)

        }
    }


}
