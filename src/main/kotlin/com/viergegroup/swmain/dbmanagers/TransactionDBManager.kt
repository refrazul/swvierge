package com.viergegroup.swmain.dbmanagers

import com.google.gson.GsonBuilder
import com.shared.commons.DynamicISOFields
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.dtos.transactions.TransactionFilterDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.dtosgsonadapters.TransactionDtoGSONAdapter
import com.viergegroup.swmain.entities.catalogs.* 
import com.viergegroup.swmain.managers.HostMasterManager
import com.viergegroup.swmain.webwatcher.Watcher
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import com.shared.commons.GsonConfig
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
 import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
  import com.shared.commons.AppSettings

internal object TransactionDBManager{ 

    private lateinit var transactionDataService: TransactionDataService
    //,reversesDBManager:ReversesDBManager, clientActivityDBManager:ClientActivityDBManager
    fun init( transactionDataService: TransactionDataService){
        this.transactionDataService=transactionDataService
    }
  
    internal fun getTxnsToBeReversed(hostType: HostType): List<Long> {
        WatcherGlobalLog.reverseDebug("getTxnsToBeReversed STARTED for hostType=$hostType")
    
        return try {
            return  transactionDataService.getTxnsToBeReversed(hostType)
                
        } catch (e: Exception) {
            WatcherGlobalLog.reverseDebug("getTxnsToBeReversed ERROR for hostType=$hostType - Exception: ${e.message}")
            emptyList()
        }
    } 
     
    internal fun updateTransactionStatus(pTransactionId: Long, transactionStatus: TransactionStatus) {
        WatcherGlobalLog.reverseDebug("updateTransactionStatus STARTED for transactionId=$pTransactionId, newStatus=$transactionStatus")
    
        try {
            transactionDataService.updateTransactionStatus(pTransactionId, transactionStatus)
            WatcherGlobalLog.reverseDebug("updateTransactionStatus SUCCESS: Updated transactionId=$pTransactionId to status=$transactionStatus")
        } catch (e: Exception) {
            WatcherGlobalLog.reverseDebug("updateTransactionStatus ERROR for transactionId=$pTransactionId - Exception: ${e.message}")
        }
    }
 
  
    internal fun updateIsoRequestByUuid( watcher:Watcher, uuid: ByteArray,rrn: String,
        terminalSerialNumber: String,
         metaData: ByteArray,
        isoAsciiRequest: ByteArray,
      hostType: HostType){ 
          val hostConfig = AppSettings.getHostConfig(hostType)
        val timeZone = hostConfig.zoneTime
        
            watcher.debug("rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}] isoAsciiRequest [${isoAsciiRequest}]  timeZone [${timeZone}]")
            
        try { 
            val returnlong=transactionDataService.updateIsoRequestByUuid(uuid,metaData, isoAsciiRequest,timeZone)
            watcher.debug(" returnlong [${returnlong}] rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}] isoAsciiRequest [${isoAsciiRequest}]  timeZone [${timeZone}]")
        } catch (ex: Exception) {
              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
             watcher.debug(  " updateIsoRequestByUuid EXCEPTION ${err}")
           
             WatcherGlobalLog.reverseDebug(rrn, " rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  updateIsoRequestByUuid EXCEPTION ${err}")
             WatcherGlobalLog.dbError( " rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  updateIsoRequestByUuid EXCEPTION ${err}")

             AlarmEmailService.sendAlarmEmail(
            AlarmCause.DB_ERROR,
            " rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  updateIsoRequestByUuid EXCEPTION ${err}"
          )
        }
    }





    internal fun updateIsoResponseByUuid( watcher:Watcher, uuid: ByteArray,rrn: String,
        terminalSerialNumber: String,
        isoAsciiResponse: ByteArray, 
      hostType: HostType){
          val hostConfig = AppSettings.getHostConfig(hostType)
        val timeZone = hostConfig.zoneTime
        
            watcher.debug("rrn [${rrn}]  terminalSerialNumber [${terminalSerialNumber}] isoAsciiResponse [${isoAsciiResponse}]  timeZone [${timeZone}]")
            
        try { 
            val returnlong=transactionDataService.updateIsoResponseByUuid(uuid, isoAsciiResponse,timeZone)
            watcher.debug(" returnlong [${returnlong}] isoAsciiResponse [${isoAsciiResponse}]  timeZone [${timeZone}]")
        } catch (ex: Exception) {
              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
              watcher.debug(  " updateIsoResponseByUuid EXCEPTION ${err}")
             WatcherGlobalLog.reverseDebug(rrn,  " rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  isoAsciiResponse [${StringUtils.asciiFromByteArray(isoAsciiResponse)}]   updateIsoResponseByUuid EXCEPTION ${err}")
             WatcherGlobalLog.dbError(" rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  isoAsciiResponse [${StringUtils.asciiFromByteArray(isoAsciiResponse)}]   updateIsoResponseByUuid EXCEPTION ${err}")

             AlarmEmailService.sendAlarmEmail(
                AlarmCause.DB_ERROR, 
                " rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  isoAsciiResponse [${StringUtils.asciiFromByteArray(isoAsciiResponse)}]   updateIsoResponseByUuid EXCEPTION ${err}"
            )
        }
    }


    //LACPI2
    internal fun getTransactionByTypeAndRrn(rrn: String, transactionType: TransactionType,watcher: Watcher):TransactionDBData?{
        watcher.debug("_LACPI2 getTransactionByTypeAndRrn rrn => dto.transactionId [${rrn}],transactionType[${transactionType}] ")
        return  transactionDataService.getTransactionByTypeAndRrn(rrn,transactionType.name,watcher)
    }
 



    internal fun saveTransactionInitial(watcher:Watcher,transDto: TransactionDto): TransactionDBData? {
        watcher.debug("TransactionDBManager_saveTransaction_initial ")
        val transDbData = TransactionDBData()

        watcher.debug("checKAndSaveInReverses errorCode: [${transDto.errorCode}] ,errorCode: [${transDto.errorCode}] , transactionStatus ${transDto.transactionStatus} transType: ${transDto.transactionType}")
        val errorCodeDtoEnum=ErrorCode.fromId(transDto.errorCode)
        val errorCodeResponseEnum=ErrorCode.fromId(transDto.errorCode)
        watcher.debug("errorCodeDtoEnum [${errorCodeDtoEnum}]")
        watcher.debug("errorCodeResponseEnum [${errorCodeResponseEnum}]")
        watcher.debug("transDto.transactionStatus [${transDto.transactionStatus}]")
        
        var time= DynamicISOFields.formatDate(transDto.hostType,"MMddHHmmssSS",transDto.transactionDateTime)
        if(time.length>=11){
            time=time.substring(0,time.length-1)
        }
        transDto.originalDataElements= (transDto.transactionType.messageTypeRequest+ transDto.rrn +  time + transDto.captureDate).padEnd(42, '0')
        watcher.debug("originalDataElements  ${ transDto.originalDataElements} ")
        val jsonString = GsonConfig.gsonRaw.toJson(transDto)
        BeanUtils.copyProperties(transDto, transDbData)
        transDbData.injectMetaDataStr(jsonString) 
         val maskPCI=  AppSettings.getInstance().maskPCI
        if(!maskPCI){
            watcher.debug("json_ORIGINAL  ${jsonString} ") 
        }
        watcher.debug("  transDbData_transactionStatus [${   transDbData.transactionStatus}]")
  
        transDbData. transactionMessageType=transDto.transactionType.transactionMessageType
        watcher.debug("setRequestISOASCIIStr  ")
        
        transDbData.injectUUID(transDto.uuid)
        transDbData.operationId=transDto.operationIdRequest
        watcher.debug(" transDto.operationIdRequest  ${transDto.operationIdRequest}  ")
        watcher.debug(" transDbData.operationId  ${ transDbData.operationId}  ")
        transDbData.errorCodeNumber=-1
        transDbData.errorMessage="Only Received Request"
        transDbData.errorCode=ErrorCode.fromId(  transDbData.errorCodeNumber)
        transDbData.transactionType=transDto.transactionType
        watcher.debug("saveTransaction ${transDto.transactionType}  ")
  
        watcher.debug(" transDbData.errorCode [${ transDbData.errorCode}] transDbData.errorCodeNumber  [${ transDbData.errorCodeNumber}]   transDbData.errorMessage [${ transDbData.errorMessage}]")
       //  val maskPCI=  AppSettings.getInstance().maskPCI
        if(!maskPCI)
          watcher.debug(" saveTransactionInitial_transDbData  ${GsonConfig.gsonRaw.toJson(transDbData)}  ")
        watcher.debug(" saveTransactionInitial_transactionType  ${transDbData.transactionType}  ")
        watcher.debug(" saveTransactionInitial_dto_transactionType  ${transDto.transactionType}  ")
  
     //   checKAndSaveInReverses(watcher,transDto,transResponse)
        return  saveTransaction(watcher,transDbData,true)
    }
   
    internal fun saveTransaction(watcher:Watcher,transactionResponse: TransactionResponse): TransactionDBData? {
        watcher.debug("TransactionDBManager_saveTransaction ")
        var transDto= TransactionDto()

        if(  transactionResponse.transactionRequest!=null){
            watcher.debug("transactionResponse.transactionRequest!!.uuid ${transactionResponse.transactionRequest!!.uuid}  ")
           if(transactionResponse.errorCode!=-1) {
               transactionResponse.transactionRequest!!.errorCode = transactionResponse.errorCode
               transactionResponse.transactionRequest!!.errorMessage = transactionResponse.errorMessage

               
                var errorCodeRS=ErrorCode.fromResponseCode(transactionResponse.getResponseCode())
                if(transactionResponse.getResponseCode()=="")
                    {                  
                        
                        watcher.debug(" transactionResponse.getResponseCode()_ISEMPTY_LACPI ${transactionResponse.errorCode}  ") //#LACPI

                        errorCodeRS=ErrorCode.fromId(transactionResponse.errorCode)
                       watcher.debug(" transactionResponse.getResponseCode()_ISEMPTY_LACPI errorCodeRS.responseCode ${errorCodeRS.responseCode}  ") //#LACPI

                  }
               transactionResponse.transactionRequest!!.responseCode=errorCodeRS.responseCode
                  watcher.debug(" transactionResponse.getResponseCode()_LACPI ${ transactionResponse.getResponseCode()}  ") //#LACPI
                watcher.debug("transactionResponse.transactionRequest!!.responseCode_LACPI ${ transactionResponse.transactionRequest!!.responseCode}  ") //#LACPI

                
                transDto.errorCode = transactionResponse.errorCode
             transDto.errorMessage = transactionResponse.errorMessage


           }else{ 

            transactionResponse.transactionRequest!!.errorCode = transactionResponse.errorCode
               transactionResponse.transactionRequest!!.errorMessage = transactionResponse.errorMessage


                val errorCodeRS=ErrorCode.fromResponseCode(transactionResponse.getResponseCode())
               transactionResponse.transactionRequest!!.errorCode=errorCodeRS.errorCodeInt 
               transactionResponse.transactionRequest!!.errorMessage=errorCodeRS.errorCodeMessage 
                 transDto.errorCode =errorCodeRS.errorCodeInt 
                 transDto.errorMessage= errorCodeRS.errorCodeMessage 
           }    
            
            watcher.debug(" transactionResponse.transactionRequest!!.errorCode ${ transactionResponse.transactionRequest!!.errorCode}  ")
            watcher.debug(" transactionResponse.transactionRequest!!.errorMessage ${ transactionResponse.transactionRequest!!.errorMessage}  ")

            StringUtils.copyNonEmptyProperties(transactionResponse.transactionRequest!!,transDto)
            watcher.debug("COPY_TRANS_REQUEST AND REMOVE SENSITIVEDATA ")

           



        }else{
            watcher.debug("transactionResponse.transactionRequest IS_NULL ")

        } 


        watcher.debug("checKAndSaveInReverses errorCode: [${transDto.errorCode}] ,errorCode: [${transDto.errorCode}] , transactionStatus ${transDto.transactionStatus} transType: ${transDto.transactionType}")
        val errorCodeDtoEnum=ErrorCode.fromId(transDto.errorCode)
        val errorCodeResponseEnum=ErrorCode.fromId(transDto.errorCode)
        watcher.debug("errorCodeDtoEnum [${errorCodeDtoEnum}]")
        watcher.debug("errorCodeResponseEnum [${errorCodeResponseEnum}]")
        watcher.debug("transDto.transactionStatus [${transDto.transactionStatus}]")
        if(errorCodeDtoEnum!=ErrorCode.Approved){
      
            transDto.transactionStatus=TransactionStatus.Rejected
        }       
        if(errorCodeDtoEnum==ErrorCode.Approved){

            transDto.transactionStatus=TransactionStatus.Approved
    
        }
        if(errorCodeDtoEnum.mustSendAReverse||
        errorCodeResponseEnum.mustSendAReverse){ 
     
            transDto.transactionStatus=TransactionStatus.MustBeReversed
        } 
         watcher.debug("errorCodeDtoEnum_mustSendAReverse [${errorCodeDtoEnum.mustSendAReverse}]")
         watcher.debug("  errorCodeResponseEnum_mustSendAReverse [${  errorCodeResponseEnum.mustSendAReverse}]")
         watcher.debug("   transDto_transactionStatus [${   transDto.transactionStatus}]")
        watcher.debug("checKAndSaveInReverses transactionStatus ${transDto.transactionStatus} transType: ${transDto.transactionType}")
        watcher.debug("MustBeReversed ${transDto.transactionStatus == TransactionStatus.MustBeReversed}")
            
 
        val transDbData = TransactionDBData()
        var transResponse= TransactionDto()
        if(transactionResponse.transactionResponse!=null){
            BeanUtils.copyProperties(transactionResponse.transactionResponse!!,transResponse)
            watcher.debug("COPY_TRANS_RESPONSE AND REMOVE SENSITIVEDATA ")
            watcher.debug("COPY_TRANS_RESPONSE AND REMOVE SENSITIVEDATA ")

        }
       /* al gson = GsonBuilder().registerTypeAdapter(
            TransactionDto::class.java,
            TransactionDtoGSONAdapter()
        ).setPrettyPrinting().create()*/ 


        var time= DynamicISOFields.formatDate(transDto.hostType,"MMddHHmmssSS",transDto.transactionDateTime)
        if(time.length>=11){
            time=time.substring(0,time.length-1)
        }
        transDto.originalDataElements= (transDto.transactionType.messageTypeRequest+ transDto.rrn +  time + transDto.captureDate).padEnd(42, '0')
        watcher.debug("originalDataElements  ${ transDto.originalDataElements} ")
        val jsonString = GsonConfig.gsonRaw.toJson(transDto)
        BeanUtils.copyProperties(transDto, transDbData)
        transDbData.injectMetaDataStr(jsonString) 
  
       // watcher.debug("json_ORIGINAL  ${jsonString} ")
        watcher.debug("  transDbData_transactionStatus [${   transDbData.transactionStatus}]")
 

        transDbData. transactionMessageType=transDto.transactionType.transactionMessageType
        watcher.debug("setRequestISOASCIIStr  ")
        transDbData.injectRequestISOASCIIStr(transactionResponse.getIsoRequestASCII())
        watcher.debug("setResponseISOASCIIStr  ")
        transDbData.injectResponseISOASCIIStr(transactionResponse.getIsoResponseASCII())
        watcher.debug("transDto.uuid ${transDto.uuid}  ")
        transDbData.injectUUID(transDto.uuid)
        transDbData.operationId=transDto.operationIdRequest
        watcher.debug(" transDto.operationIdRequest  ${transDto.operationIdRequest}  ")
        watcher.debug(" transDbData.operationId  ${ transDbData.operationId}  ")
        transDbData.errorCodeNumber=transDto.errorCode
        transDbData.errorMessage=transDto.errorMessage
        transDbData.errorCode=ErrorCode.fromId(  transDbData.errorCodeNumber)
        transDbData.transactionType=transDto.transactionType
        watcher.debug("saveTransaction ${transDto.transactionType}  ")

        watcher.debug(" transDbData.errorCode [${ transDbData.errorCode}] transDbData.errorCodeNumber  [${ transDbData.errorCodeNumber}]   transDbData.errorMessage [${ transDbData.errorMessage}]")
      
     //   checKAndSaveInReverses(watcher,transDto,transResponse)
        return  saveTransaction(watcher,transDbData,false)
    } 
 
    internal fun findTxn(rrn:String, terminalSerialNumber:String,hostType: HostType,watcher: Watcher):TransactionDBData?{
        watcher.debug("findTxn rrn $rrn terminalSerialNumber $terminalSerialNumber  hostType $hostType ")
        val  transactionFilterDto= TransactionFilterDto() 
        transactionFilterDto.rrn=rrn  
        transactionFilterDto.terminalSerialNumberFilter=terminalSerialNumber
        transactionFilterDto.hostType=hostType
        watcher.debug("findTxn =>transactionFilterDto.transactionStatus [${transactionFilterDto.transactionStatus}]   transactionFilterDto.transactionMessageType [${  transactionFilterDto.transactionMessageType}]")
       
       
        return  transactionDataService.getTransactionData(transactionFilterDto,watcher)
    } 
    


    internal fun findTxn(transactionId:Long,watcher: Watcher):TransactionDBData?{
        watcher.debug("findTxn transactionId $transactionId t")
        val  transactionFilterDto= TransactionFilterDto()
        transactionFilterDto.transactionId=transactionId
        return  transactionDataService.getTransactionData(transactionFilterDto,watcher)
    } 
 
   

    internal fun findOriginalTxn(rrn:String, terminalSerialNumber:String,authorizationIdResponse:String,hostType: HostType,watcher: Watcher):TransactionDBData?{
        watcher.debug("findOriginalTxn => authorizationIdResponse [$authorizationIdResponse] rrn [$rrn] terminalSerialNumber [$terminalSerialNumber]  hostType [$hostType] ")
        val  transactionFilterDto= TransactionFilterDto()
        transactionFilterDto.rrn=rrn
        transactionFilterDto.terminalSerialNumberFilter=terminalSerialNumber
        transactionFilterDto.authorizationIdResponse=authorizationIdResponse
        transactionFilterDto.hostType=hostType
        transactionFilterDto.transactionStatus=TransactionStatus.Approved
        transactionFilterDto.transactionMessageType=TransactionMessageType.Financial
        watcher.debug("findOriginalTxn =>transactionFilterDto.transactionStatus [${transactionFilterDto.transactionStatus}]   transactionFilterDto.transactionMessageType [${  transactionFilterDto.transactionMessageType}]")
 
        var txn=  transactionDataService.getTransactionData(transactionFilterDto,watcher)
        if(txn!=null){ 
            return txn
        }
        transactionFilterDto.rrn=rrn.trim()
        transactionFilterDto.authorizationIdResponse=authorizationIdResponse.trim()
        if(  transactionFilterDto.authorizationIdResponse ==""&& transactionFilterDto.rrn==""){
            watcher.debug("findOriginalTxn =>RETURN_NULL ")
            return null
        }
         txn=  transactionDataService.getTransactionData(transactionFilterDto,watcher)
        if(txn!=null){
            watcher.debug("findOriginalTxn =>IS_NOT_NULL RRN [${txn.rrn}] SN [${txn.terminalSerialNumber}]  AUTH [${txn.authorizationIdResponse}]")


        }
        return txn
    }

 

    private  fun checKAndSaveInReverses(watcher:Watcher, transDto: TransactionDto, transResponseDto: TransactionDto){
        watcher.debug("checKAndSaveInReverses errorCode: [${transResponseDto.errorCode}] ,errorCode: [${transDto.errorCode}] , transactionStatus ${transDto.transactionStatus} transType: ${transDto.transactionType}")
        val errorCodeDtoEnum=ErrorCode.fromId(transDto.errorCode)
        val errorCodeResponseEnum=ErrorCode.fromId(transResponseDto.errorCode)
        watcher.debug("errorCodeDtoEnum [${errorCodeDtoEnum}]")
        watcher.debug("errorCodeResponseEnum [${errorCodeResponseEnum}]")
        val transAdvice=transDto.transactionType.transactionMessageType== TransactionMessageType.Advice
        watcher.debug("transDto.transactionStatus [${transDto.transactionStatus}] transDto.transactionType.transactionMessageType [${transDto.transactionType.transactionMessageType}]")
        watcher.debug("transAdvice [${transAdvice}]")
    
        if((errorCodeDtoEnum.mustSendAReverse||
        errorCodeResponseEnum.mustSendAReverse)&&!transAdvice){
          
            transDto.transactionStatus=TransactionStatus.MustBeReversed
        }  
         watcher.debug("errorCodeDtoEnum_mustSendAReverse [${errorCodeDtoEnum.mustSendAReverse}]")
         watcher.debug("  errorCodeResponseEnum_mustSendAReverse [${  errorCodeResponseEnum.mustSendAReverse}]")
         watcher.debug("   transDto_transactionStatus [${   transDto.transactionStatus}]")
        watcher.debug("checKAndSaveInReverses transactionStatus ${transDto.transactionStatus} transType: ${transDto.transactionType}")
        watcher.debug("MustBeReversed ${transDto.transactionStatus == TransactionStatus.MustBeReversed}")
      
        
    

    }

  
    internal  fun saveTransaction(watcher:Watcher,transactionData: TransactionDBData,isInitial:Boolean): TransactionDBData? {
        val canSave= availableToSave(transactionData)
        watcher.debug("transactionData.transactionType: ${transactionData.transactionType}")
        watcher.debug("saveTransaction_CAN_SAVE TXN: ${canSave}")

        if (transactionData.hostType.authorizer != Authorizer.VIERGE) {
            var emvData=transactionData.readEmvDataStr() 
            emvData = 
                StringUtils.removeEmvTagAndValue(emvData, "57")
            emvData=
                StringUtils.removeEmvTagAndValue(emvData, "5A")
            emvData = 
                StringUtils.removeEmvTagAndValue(emvData, "5F24") 
            transactionData.injectEmvDataStr(emvData)
        }


        transactionData.cardNumber=StringUtils.maskCardNumber(transactionData.cardNumber) 
        watcher.debug("saveTransaction_maskCardNumber [${transactionData.cardNumber}] ")

        //  logger.info("SAVE_DB_DATA - transDbData: ${GsonBuilder().setPrettyPrinting().create().toJson(transactionData)} ")
        watcher.debug("SAVE_DB_DATA - transDbData ")

        if(!canSave) {
            try {
                watcher.debug("SAVE_DATA")
                val errorDBData = LogsMetaData()
                BeanUtils.copyProperties(transactionData,errorDBData)
                //   insertErrorData(errorDBData)
            }catch (ex:Exception){
                watcher.error("SAVE_ERROR_DATA EX: ${ex.message}")
            }
            return null
        }


        var  transactionFilterDto= TransactionFilterDto()
        transactionFilterDto.transactionId=transactionData.transactionId
        transactionFilterDto.terminalSerialNumberFilter=transactionData.terminalSerialNumber
        transactionFilterDto.hostType=transactionData.hostType 
        val transactionDataDb= transactionDataService.getTransactionData(transactionFilterDto,watcher)
        watcher.debug(" saveTransaction_transactionData_transactionMessageType [${transactionData.transactionMessageType}]")
        watcher.debug(" saveTransaction_getTransactionData ORGDATA authorizationIdResponse ${ transactionFilterDto.authorizationIdResponse}")
        watcher.debug(" saveTransaction_getTransactionData ORGDATA rrn ${ transactionFilterDto.rrn}")
        val isReverse=transactionData.transactionMessageType== TransactionMessageType.Reverse
        watcher.debug(" saveTransaction_isReverse  ${ isReverse}")
        if((transactionData.transactionMessageType== TransactionMessageType.Advice
        ||transactionData.transactionMessageType== TransactionMessageType.Reverse)&&transactionFilterDto.authorizationIdResponse!=""){
        
            transactionFilterDto= TransactionFilterDto()
            transactionFilterDto.authorizationIdResponse=transactionData.authorizationIdResponse
            transactionFilterDto.terminalSerialNumberFilter=transactionData.terminalSerialNumber
            transactionFilterDto.rrn=transactionData.originalRrn
            if(isReverse ){
                transactionFilterDto.authorizationIdResponse="";
            }else{ 
                transactionFilterDto.rrn="" 
            }
            transactionFilterDto.hostType=transactionData.hostType
            watcher.debug("saveTransaction_transactionData_originalRrn ${ transactionData.originalRrn}")
          
            watcher.debug("saveTransaction_transactionFilterDto_authorizationIdResponse ${ transactionFilterDto.authorizationIdResponse}")
            watcher.debug("saveTransaction_transactionFilterDto_terminalSerialNumberFilter ${ transactionFilterDto.terminalSerialNumberFilter}")
            watcher.debug("saveTransaction_transactionFilterDto_rrn ${ transactionFilterDto.rrn}")
            watcher.debug("saveTransaction_transactionFilterDto_hostType ${ transactionFilterDto.hostType}")

            val originalData= transactionDataService.getTransactionData(transactionFilterDto,watcher)

            if(originalData!=null&&transactionData.errorCode!=ErrorCode.Approved){  
                if(transactionData.transactionType==TransactionType.Refund) {
                    originalData.transactionStatus=TransactionStatus.Refunded
                }
                if(transactionData.transactionType==TransactionType.Cancellation) {
                    originalData.transactionStatus=TransactionStatus.Cancelled
                }
                if(transactionData.transactionType==TransactionType.Reverse) {
                    originalData.transactionStatus=TransactionStatus.Reversed
                }
            
                 
                transactionData.originalTransactionId=originalData.transactionId
                watcher.debug("saveTransaction_BEFORE 1 insertTransactionData , transactionData.originalTransactionId ${transactionData.originalTransactionId}")
                
               val idInserted=  transactionDataService.insertTransactionData(originalData,isInitial,watcher)
                watcher.debug("saveTransaction_AFTER 1 insertTransactionData idInserted ${idInserted}")
                return transactionDataDb 
            } 
        }
        watcher.debug("saveTransaction_transactionDataDb  authid: ${transactionData.authorizationIdResponse}")
        watcher.debug("saveTransaction_BEFORE 2 insertTransactionData")
          /*val errorCodeEnum = ErrorCode.fromId(transactionData.errorCode.errorCodeInt)

             if(transactionData.responseCode=="")
                transactionData.responseCode=errorCodeEnum.responseCode*/


       val idInserted= transactionDataService.insertTransactionData(transactionData,isInitial,watcher)
        watcher.debug("saveTransaction_AFTER 2 insertTransactionData idInserted $idInserted")
        return transactionDataDb 
    }

    
    
    internal fun insertTransactionData(watcher: Watcher, transactionData: TransactionDBData): TransactionDBData? {
        watcher.debug("insertTransactionData STARTED - Attempting to insert transaction")
        WatcherGlobalLog.reverseDebug("insertTransactionData STARTED - Attempting to insert transaction")
         
        try {   
            // Log de los datos más importantes de la transacción antes de insertarla
            watcher.debug("Transaction Details: uuid=[${StringUtils.asciiFromByteArray(transactionData.uuid)}] rrn=${transactionData.rrn}, amount=${transactionData.transactionAmount}, merchant=${transactionData.merchantName}, hostType=${transactionData.hostType}, transactionType=${transactionData.transactionType}, status=${transactionData.transactionStatus}")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"Transaction Details: uuid=[${StringUtils.asciiFromByteArray(transactionData.uuid)}] rrn=${transactionData.rrn}, amount=${transactionData.transactionAmount}, merchant=${transactionData.merchantName}, hostType=${transactionData.hostType}, transactionType=${transactionData.transactionType}, status=${transactionData.transactionStatus}")
         //   transactionData.uuid=StringUtils.generateUUIDAsByteArray()
            val idInserted = transactionDataService.insertTransactionData(transactionData,true, watcher) 
            watcher.debug("insertTransactionData SUCCESS - Inserted transaction with ID: $idInserted")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData SUCCESS - Inserted transaction with ID: $idInserted")
      
            return transactionData 
        } catch (e: Exception) {
            watcher.error("insertTransactionData ERROR - Exception: ${e.message}")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData ERROR - Exception: ${e.message}")
            return null
        } finally { 
            watcher.debug("insertTransactionData COMPLETED - Finalizing insertion process")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData COMPLETED - Finalizing insertion process")
        } 
    } 

      internal fun insertTransactionDataEnd(watcher: Watcher, transactionData: TransactionDBData): TransactionDBData? {
        watcher.debug("insertTransactionData STARTED - Attempting to insert transaction")
        WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData STARTED - Attempting to insert transaction")
          
        try {  
            // Log de los datos más importantes de la transacción antes de insertarla
            watcher.debug("Transaction Details:uuid=[${StringUtils.asciiFromByteArray(transactionData.uuid)}] rrn=${transactionData.rrn}, amount=${transactionData.transactionAmount}, merchant=${transactionData.merchantName}, hostType=${transactionData.hostType}, transactionType=${transactionData.transactionType}, status=${transactionData.transactionStatus}")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"Transaction Details: uuid=[${StringUtils.asciiFromByteArray(transactionData.uuid)}] rrn=${transactionData.rrn}, amount=${transactionData.transactionAmount}, merchant=${transactionData.merchantName}, hostType=${transactionData.hostType}, transactionType=${transactionData.transactionType}, status=${transactionData.transactionStatus}")
         //   transactionData.uuid=StringUtils.generateUUIDAsByteArray()
            val idInserted = transactionDataService.insertTransactionData(transactionData,false, watcher) 
            watcher.debug("insertTransactionData SUCCESS - Inserted transaction with ID: $idInserted")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData SUCCESS - Inserted transaction with ID: $idInserted")
       
            return transactionData  
        } catch (e: Exception) {
            watcher.error("insertTransactionData ERROR - Exception: ${e.message}")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData ERROR - Exception: ${e.message}")
            return null
        } finally { 
            watcher.debug("insertTransactionData COMPLETED - Finalizing insertion process")
            WatcherGlobalLog.reverseDebugRrn(transactionData.rrn,"insertTransactionData COMPLETED - Finalizing insertion process")
        }
    }
    

   private fun availableToSave(transactionData: TransactionDBData):Boolean{

       return transactionData.transactionType!= TransactionType.KeyLoad&&transactionData.transactionType!= TransactionType.KeyLoadConfirm
       &&transactionData.transactionType!= TransactionType.Undefined
       //||transactionData.transactionMessageType== TransactionMessageType.Reverse)
               //RDM_REVERSE &&transactionData.transactionType!= TransactionType.Reverse   &&transactionData.transactionStatus!= TransactionStatus.MustBeReversed
    }
 
  
}