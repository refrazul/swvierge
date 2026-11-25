package com.viergegroup.swmain.managers

import com.shared.commons.*
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.isoprocessor.models.iso.Iso8583TransactionMetaData

import com.viergegroup.swmain.dtos.comm.DisconnectHostResponseDto
import com.viergegroup.swmain.dtos.comm.HostStatusResponseDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.catalogs.KeyDirection


import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.BeanUtils
 
object HostMasterManager { 
   // private  lateinit var transactionsDBServiceOLD: TransactionsDBServiceOLD
    private val hosts= mutableMapOf<HostType, HostCommManager>()
    fun init(){
    //    HostMasterManager.transactionsDBServiceOLD =transactionsDBServiceOLD
        WatcherGlobalLog.debug("HostMasterManager IS STARTING....")
        for(hostType in HostType.values()){
            if(hostType!=HostType.Undefined&&!hosts.containsKey(hostType)){
                WatcherGlobalLog.info("INITIALIZATING: $hostType")
                val watcher= WatcherContext.initHost(hostType)
                val hostCommManager= HostCommManager()
                hostCommManager.init(hostType, watcher)
                hosts[hostType] = hostCommManager
            }

        }
        for(hostType in HostType.values()){
            if(hostType!=HostType.Undefined){
                WatcherGlobalLog.debug("CONNECTING: $hostType")
                try { 
                    hosts[hostType]?.tryToConnect()
                }catch (ex:Exception){
                   WatcherGlobalLog.error("EXCEPTION ON INIT HOST : [${ex.message}]")
                }
            }

        }
    }
    fun validateHost(hostType: HostType):Iso8583MetaData{
       // val watcher=WatcherContext.validateHost(hostType)
        val response=Iso8583MetaData()
        if(hostType==HostType.Undefined){
            response.errorCode= ErrorCode.HostIsUnknown.errorCodeInt
            response.errorMessage="$hostType "+ ErrorCode.HostIsUnknown.errorCodeMessage
            return response

        }
        if(!AppSettings.getHostConfig(hostType).hostIsActive()){

            response.errorCode= ErrorCode.HostIsInactive.errorCodeInt
            response.errorMessage="$hostType "+ ErrorCode.HostIsInactive.errorCodeMessage
            return response

        }  
        if(!hosts.containsKey(hostType)){
            response.errorCode= ErrorCode.HostIsUnknown.errorCodeInt
            response.errorMessage="$hostType NO IN MAP-> "+ ErrorCode.HostIsUnknown.errorCodeMessage
        }
        WatcherContext.finishOperation()
        return response

    }
    fun sendMessage(iso8583MetaData: Iso8583MetaData, watcher: Watcher): Iso8583MetaData {
        val response= validateHost(iso8583MetaData.getHostType())
        if(response.errorCode!=0) return response
  
        return hosts[iso8583MetaData.getHostType()]!!.sendMessage(iso8583MetaData,watcher)


    }
    fun connectToHost(hostType: HostType, watcher: Watcher): Iso8583TransactionMetaData {
        val validationResponse= validateHost(hostType)
        val response=Iso8583TransactionMetaData()
        if(validationResponse.errorCode!=0){
            response.errorCode=validationResponse.errorCode
            response.errorMessage=validationResponse.errorMessage
            return response
        }
        return hosts[hostType]!!.connectToHost(watcher)
    }



    fun hostStatus(hostType: HostType, watcher: Watcher): HostStatusResponseDto {
        val validationResponse= validateHost(hostType)
        val response=HostStatusResponseDto()
        watcher.info("hostStatus ")
        if(validationResponse.errorCode!=0){
            response.errorCode=validationResponse.errorCode
            response.errorMessage=validationResponse.errorMessage
            return response
        }

        return hosts[hostType]!!.hostStatus()
    }


    fun disconnectHost(hostType: HostType, watcher: Watcher): DisconnectHostResponseDto {
        val validationResponse= validateHost(hostType)
        val response= DisconnectHostResponseDto()
        if(validationResponse.errorCode!=0){
            response.errorCode=validationResponse.errorCode
            response.errorMessage=validationResponse.errorMessage
            return response

        }

        return hosts[hostType]!!.disconnectHost(watcher)
    }

    fun removeRequestMessage(hostType: HostType, isoMetaData: Iso8583MetaData, isoResponse:Iso8583MetaData?, watcher: Watcher){

        return hosts[hostType]!!.removeRequestMessage(isoMetaData,isoResponse,watcher)

    }

    fun checkReceivedMessage(hostType: HostType, systemTraceNumber: String, watcher: Watcher): Iso8583MetaData?{
        val response= validateHost(hostType)
        if(response.errorCode!=0) return response

        return hosts[hostType]!!.checkReceivedMessage(systemTraceNumber,watcher)
    }



    fun sendIsoAndWaitForResponse(hostType: HostType, isoAscii: String, watcher: Watcher): Iso8583TransactionMetaData {
        val validationResponse= validateHost(hostType)
        val response= Iso8583TransactionMetaData()
        if(validationResponse.errorCode!=0){
            response.errorCode=validationResponse.errorCode
            response.errorMessage=validationResponse.errorMessage

            return response
        }
        val fullResponse= hosts[hostType]!!.sendIsoAndWaitForResponse(isoAscii)
        response.errorCode=fullResponse.errorCode
        response.errorMessage=fullResponse.errorMessage

        response.iso8583MetaDataRequest.transactionData= TransactionData()
        response.iso8583MetaDataResponse.transactionData=TransactionData()

        fullResponse.transactionRequest?.let { BeanUtils.copyProperties(it, response.iso8583MetaDataRequest.transactionData!!) }
        fullResponse.transactionResponse?.let { BeanUtils.copyProperties(it, response.iso8583MetaDataResponse.transactionData!!) }


        return response
    }
    fun sendIsoAndWaitForResponse(isoRequest: Iso8583MetaData, watcher: Watcher): TransactionResponse {
        return sendIsoAndWaitForResponse(isoRequest.getHostType(),isoRequest,watcher)
    } 

     

    fun sendIsoAndWaitForResponse(hostType: HostType, isoRequest: Iso8583MetaData, watcher: Watcher): TransactionResponse {
        val hostSettings=AppSettings.getHostConfig(hostType)
        var response= TransactionResponse(isoRequest,null) 
       watcher.info("sendIsoAndWaitForResponse hostType $hostType hostSettings.hostResponseHardcode  ${hostSettings.hostResponseHardcode}" )
        val sendKeyLoadHardcode=(isoRequest.transactionData!!.transactionType== TransactionType.KeyLoad
        ||isoRequest.transactionData!!.transactionType== TransactionType.KeyLoadPin)&&hostSettings.enableKeyLoadHarcode
        val affiliation=isoRequest.transactionData!!.affiliation
        val merchantName=isoRequest.transactionData!!.merchantName

       watcher.info("enableKeyLoadHarcode [${hostSettings.enableKeyLoadHarcode}] transactionType [${isoRequest.transactionData!!.transactionType}]")
       watcher.info("sendIsoAndWaitForResponse_affiliation [$affiliation] merchantName [$merchantName]" )
       watcher.info("sendKeyLoadHardcode [$sendKeyLoadHardcode]" )
       
      /*   if(!hostSettings.isDev&&affiliation.equals("0000000")&&(merchantName.contains("NETO")||merchantName.contains("ELEKTRA"))){ 
             response.errorCode = ErrorCode.InvalidTransaction.errorCodeInt
             response.errorMessage = ErrorCode.InvalidTransaction.errorCodeMessage+" FOR AFF [${affiliation}] MERN ["+merchantName+"]"
             return response
        } 
*/
 
   
       //AC01FD7
 
        if(!hostSettings.hostResponseHardcode&&!sendKeyLoadHardcode&&!affiliation.equals("0000000")&&!affiliation.equals("00000000") ) {
            val validationResponse= validateHost(hostType)
            if (validationResponse.errorCode != 0) { 
                response.errorCode = validationResponse.errorCode
                response.errorMessage = validationResponse.errorMessage

                return response
            } 
           watcher.info("keepConnectionAlive  ${hostSettings.keepConnectionAlive}" )
            if(hostSettings.keepConnectionAlive){
                response = hosts[hostType]!!.sendIsoAndWaitForResponse(isoRequest)
                watcher.info("ISO RESPONSE IS_NULL: [${response.isoResponse!=null}]" )
                if(response.isoResponse!=null){
                    try{
                        watcher.info("ISO_RESPONSE_ASCII ${response.isoResponse!!.isoAscii} ")
                    }catch(ex:Exception){
                        watcher.error("EXCEPTION ${ex.message} ") 
                    }
                }
              
                if(response.isoResponse!=null){
                    watcher.debug("BEFORE   watcher.saveIsoRaw ")
                    watcher.saveIsoRaw(response.isoResponse!!)
                    watcher.debug("ISO_RESPONSE :" + response.isoResponse!!.getDetails())
                    watcher.debug("AFTER   watcher.saveIsoRaw ")
                }

            }
            else{
                val hostCommManager= HostCommManager()
               watcher.info("CREATE -> HostCommManager()->sendIsoAndCloseConnection" )
                hosts[hostType]=hostCommManager
                response = hosts[hostType]!!.sendIsoAndCloseConnection(isoRequest,hostType, watcher)
            }
 
        }else{
            watcher.info("send_hardcode_iso" ) 
      
            val isoResponse=Iso8583MetaData()
            val transDataResponse=TransactionData()
            val errorMessage=isoRequest.errorMessage
            val errorCode=isoRequest.errorCode

            isoResponse.transactionData=transDataResponse

            if (errorCode != 0) { 
                response.errorMessage=errorMessage
                response.errorCode=errorCode
                isoResponse.setResponseCode(ErrorCode.fromId(errorCode))
               watcher.error("ERROR  sendIsoAndWaitForResponse ${errorMessage}")
            }else{
                isoResponse.setResponseCode(ErrorCode.Approved)
            }
   
            BeanUtils.copyProperties(isoRequest.transactionData!!,transDataResponse)
            val authResId=Iso8583MetaData.generateAuthId(hostType)
            isoResponse.setAuthorizationId(authResId)
            isoResponse.errorCode=errorCode
            isoResponse.errorMessage=errorMessage
           watcher.info("BEFOR HARDCODE PEK [${isoRequest.transactionData!!.transactionType}] ")
            if(isoRequest.transactionData!!.transactionType==TransactionType.KeyLoad||isoRequest.transactionData!!.transactionType==TransactionType.KeyLoadPin){
                transDataResponse.pekEX="693D0BA7D6039B07CC8AFE5A0C84DB7D" 
                transDataResponse.ksnEX="00000107130986400000"
                transDataResponse.checkValueEX="000000"
                transDataResponse.newKeyRequestStatusEX="00"
                transDataResponse.crc32EX="000000"
                watcher.info("HARDCODE_PEK : [${transDataResponse.pekEX}]  transDataResponse.ksnEX: [${ transDataResponse.ksnEX}] ")
            } 
 
            isoResponse.transactionData=transDataResponse
            response= TransactionResponse(isoRequest,isoResponse)
            if (errorCode != 0) {
                response.errorMessage=errorMessage
                response.errorCode=errorCode
               watcher.error("ERROR  sendIsoAndWaitForResponse ${response.errorMessage}")

            }

            val errorMessageHardcode=hostSettings.errorMessageHardcode
            val responseCodeHardcode=hostSettings.responseCodeHardcode
            if(errorMessageHardcode!=""&&responseCodeHardcode!="00"){
               watcher.info("ERROR CODE HARDCODE: $responseCodeHardcode , [$errorMessage] ")
                val errorCodeEnum=ErrorCode.fromResponseCode(responseCodeHardcode)
                response.errorCode=errorCodeEnum.errorCodeInt
                response.errorMessage=errorCodeEnum.errorCodeMessage+" "+errorMessageHardcode
                isoResponse.setResponseCode(errorCodeEnum)
                isoResponse.errorMessage= response.errorMessage
            }  
            WatcherGlobalLog.info("authResId: ${authResId}")
            isoRequest.setAuthorizationId(authResId)
            response= TransactionResponse(isoRequest,isoResponse)
            if(response.transactionResponse==null){
               watcher.info("transactionResponse IS_NULL ")
                response.transactionResponse= TransactionDto() 
                StringUtils.copyNonEmptyProperties(transDataResponse,response.transactionResponse!!)
            }
            val transactionType=response.transactionResponse!!.transactionType
            watcher.info("isoResponse.transactionData.transactionType [${transactionType}] ")

           val errorCodeSimulated= hostSettings.getErrorCodeForTransaction( transactionType)
           watcher.info("errorCodeSimulated [${errorCodeSimulated}] ")
           watcher.info(" response.transactionResponse!!.responseCode [${ response.transactionResponse!!.responseCode}] ")

           response.transactionResponse!!.responseCode=errorCodeSimulated.responseCode
           response.errorCode=  errorCodeSimulated.errorCodeInt
           watcher.info("    response.errorCode [${    response.errorCode}] ")

           response.errorMessage=errorCodeSimulated.errorCodeMessage+" "+errorMessageHardcode
           watcher.info("    response.errorMessage [${    response.errorMessage}] ")

           isoResponse.setResponseCode(errorCodeSimulated)

        }

        return response
    }




    fun sendEchoAndWaitForResponse(hostType: HostType, watcher: Watcher): TransactionResponse {
        val requestDto= DynamicISOFields.getEchoRequestMetaData(hostType)
        requestDto.transactionData!!.hostType=hostType
        return sendIsoAndWaitForResponse(hostType,requestDto,watcher)
    }
 
    fun sendFunKeysAndWaitForResponse(transactionType:TransactionType,keyDirection:KeyDirection,hostType: HostType, watcher: Watcher): TransactionResponse {
        val requestDto= DynamicISOFields.getFunKeysMetaData(transactionType,keyDirection,hostType)
        requestDto.transactionData!!.hostType=hostType
         requestDto.transactionData!!.transactionType=transactionType
         requestDto.transactionData!!.keyDirection=keyDirection
  
        return sendIsoAndWaitForResponse(hostType,requestDto,watcher)
    }
 
 


    fun turnOnCheckReverses(hostType:HostType){
        hosts[hostType]!!.setCheckReverses(true)

    }






}