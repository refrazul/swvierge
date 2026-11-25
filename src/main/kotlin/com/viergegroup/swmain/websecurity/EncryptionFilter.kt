package com.viergegroup.swmain.websecurity

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.GsonBuilder
import com.shared.commons.*
import com.viergegroup.swmain.dtos.gateway.GatewayResponse
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionDto
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionResponse
import com.viergegroup.swmain.dtos.iso.DescribeISOAsciiResponse
import com.viergegroup.swmain.dtos.logs.FileLogResponseDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import com.viergegroup.swmain.dtos.vault.BankRsaKeyResponse
import com.viergegroup.swmain.dtosgsonadapters.*
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.OperationId
import com.viergegroup.swmain.managers.PandoraneoManager

import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.websecurity.bodyencryptor.BodyEncryptResponse
import com.viergegroup.swmain.websecurity.bodyencryptor.CryptoServerVGUtil
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.bouncycastle.ocsp.Req
import org.springframework.stereotype.Component
import java.io.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper
import kotlin.reflect.KClass
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto

import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse

import com.viergegroup.swmain.dtos.comm.*
import com.viergegroup.swmain.managers.CommManager

import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadRSAData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ResponseReadClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadClientCredData
import com.shared.commons.GsonConfig


@Component
internal class EncryptionFilter(private val objectMapper: ObjectMapper) : Filter {


    companion object{

    }


    override fun init(filterConfig: FilterConfig) {
        // No initialization necessary
    }
    fun clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor()
            } else {
                // Usar secuencias de escape ANSI para limpiar la consola en sistemas Unix
                print("\u001b[H\u001b[2J")
                System.out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


  
    fun rebootSystem(): ConnectToHostResponseDto{
        val  requestDto: CommHostRequestDto=CommHostRequestDto()
        requestDto.hostType=HostType.ProsaDUKPT
        requestDto.rrn="rebot_system"
        requestDto.terminalSerialNumber="777777777TESTRDMYLX7"

        val response=ConnectToHostResponseDto()
        response.errorMessage= RebootService().canRebootSystem()
        return  response
    }

   
    fun restartHost(): ConnectToHostResponseDto {
       val  requestDto: CommHostRequestDto=CommHostRequestDto()
       requestDto.hostType=HostType.ProsaDUKPT
       requestDto.rrn="rebot_system"
       requestDto.terminalSerialNumber="777777777TESTRDMYLX7"



        CommManager().disconnectHost(requestDto)
        val response=ConnectToHostResponseDto()
        val response2=  CommManager().connectToHost(requestDto)
        StringUtils.copyNonEmptyProperties(response2,response)
        response.isConnected=response2.errorCode==0
        response.hostIp=AppSettings.getHostConfig(requestDto.hostType).hostIp
        response.hostPort=AppSettings.getHostConfig(requestDto.hostType).hostPort
        response.timeOut=AppSettings.getHostConfig(requestDto.hostType).hostConnectionTimeOut
        return response 
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        try {
            doFilterInternal(request, response, chain)
        } finally {
            // Garantiza cerrar el Watcher del request en CUALQUIER salida (incl. excepción).
            // finishOperation() es idempotente y NO toca el log global (a diferencia de closeWatcher()).
            WatcherContext.finishOperation()
        }
    }


        private fun doFilterInternal(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val requestUri = httpRequest.requestURI
        val startTime = System.currentTimeMillis()
        val cryptoServerVGUtil= CryptoServerVGUtil()
        val operationIdRequest= OperationId.fromApiPath(httpRequest.requestURI)
        val operationId =operationIdRequest 
        val requestClass = operationId.requestDtoClass
        val responseClass = operationId.responseDtoClass
        val merchantDataDto=MerchantDataDto()
       // println("doFilter_operationIdRequest ${operationIdRequest} httpRequest.requestURI ${httpRequest.requestURI}")
  
        //val watcher:Watcher
        val wrappedResponse = CustomHttpServletResponseWrapper(httpResponse)
        val responseEntity = ResponseEntity()
        var watcher: Watcher? = null

 
        
        if(httpRequest.requestURI.contains("/sw/restart")){
            httpResponse.status = HttpServletResponse.SC_OK
           val response= restartHost()
            httpResponse.writer.write("RESTARTED "+GsonConfig.gsonLogs.toJson(response))
            return  
        }       

        if(httpRequest.requestURI.contains("/sw/reboot")){
            httpResponse.status = HttpServletResponse.SC_OK
            val response= rebootSystem()
            httpResponse.writer.write("REBOOT "+GsonConfig.gsonLogs.toJson(response))
            return
        }        
 
        if(httpRequest.requestURI.contains("/users/check")||httpRequest.requestURI.contains("/myhsm/test")){
            httpResponse.status = HttpServletResponse.SC_OK
            httpResponse.writer.write("I am Good :) V[${AppSettings.getInstance().swVGVersion}] PN[${AppSettings.getInstance().pandoraLocal}]")
            return
        }       

        if(AppSettings.getInstance().clearConsolePerOperation){
            clearConsole()
        }
        WatcherGlobalLog.debug("Entering EncryptionFilter  ${httpRequest.requestURI}")
        WatcherGlobalLog.debug("Expected request class: ${requestClass.qualifiedName}")
        WatcherGlobalLog.debug("Expected response class: ${responseClass.qualifiedName}")
        // Leer el cuerpo de la solicitud una vez y almacenarlo en una variable
        var rawRequestInitial = readRequestBody(httpRequest)
        // Verificar si la solicitud está encriptada
        val bodyEncryptResponse = checkIfEncrypted(rawRequestInitial, operationIdRequest, cryptoServerVGUtil)
        if(operationIdRequest==OperationId.Undefined){ 
            WatcherGlobalLog.error("BAD REQUEST[RESPONSE]->[${requestUri}]=>UNDEFINED IN OPERATIONID")
            sendErrorResponseHttp("BAD REQUEST[RESPONSE]->[${requestUri}]=>UNDEFINED IN OPERATIONID",httpResponse,wrappedResponse)
            return
        } 
        if(bodyEncryptResponse.responseEntity.errorCode!=0){
            sendErrorResponse(merchantDataDto,httpRequest,httpResponse, bodyEncryptResponse.responseEntity,bodyEncryptResponse,cryptoServerVGUtil,null)
            return
        }
        var rawRequest=bodyEncryptResponse.rawRequest
        val isEncrypted=bodyEncryptResponse.isEncrypted
        WatcherGlobalLog.debug("WATCH_rawRequest ${StringUtils.maskJson(rawRequest)}")
 
        var  requestEntity= RequestEntity()
        requestEntity=fillRequestEntity(requestEntity,httpRequest)
        WatcherGlobalLog.debug("operationIdRequest $operationIdRequest isEncrypted $isEncrypted \n contentType: ${httpRequest.contentType}")


        try {
          //  WatcherGlobalLog.debug("BEFORE GsonConfig.gsonLogs.fromJson(rawRequest, requestClass.java) ")

            var requestData = GsonConfig.gson.fromJson(rawRequest, requestClass.java)
        //    WatcherGlobalLog.debug("requestEntity  ${GsonConfig.gson.toJson(requestEntity)}")
            WatcherGlobalLog.debug("requestData INITIAL ${GsonConfig.gsonLogs.toJson(requestData)}")

       
    

            

            if(requestData is RequestEntity){
                WatcherGlobalLog.debug("requestData is RequestEntity")
                requestEntity=fillRequestEntity(requestData,httpRequest)

                watcher=WatcherContext.initOperation(requestEntity)

                requestData.uuid = requestEntity.uuid
                WatcherGlobalLog.debug("BEFORE  existsRequest")
                requestData.clientIP = requestEntity.clientIP
                requestData.serverNameRequest = requestEntity.serverNameRequest
                requestData.moduleIdRequest = requestEntity.moduleIdRequest
                requestData.operationIdRequest = requestEntity.operationIdRequest
                requestData.terminalSerialNumber = requestEntity.terminalSerialNumber
                if (requestData is TransactionTerminalDto){
                    WatcherGlobalLog.debug("requestData is TransactionTerminalDto")
                    StringUtils.copyNonEmptyProperties(requestData,merchantDataDto)
                    WatcherGlobalLog.debug("merchantDataDto ${GsonConfig.gsonLogs.toJson(merchantDataDto)}")

                }
                if (requestData.terminalSerialNumber.isEmpty()||requestData.terminalSerialNumber.length<20) {
                    responseEntity.uuid = requestEntity.uuid
                    responseEntity.errorCode = ErrorCode.SerialIsEmpty.errorCodeInt
                    responseEntity.errorMessage = ErrorCode.SerialIsEmpty.errorCodeMessage
                    sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
                    return
                }

            }else if (requestData is GatewayTransactionDto){
                WatcherGlobalLog.debug("BEFORE  getTransactionTerminalDto")

                requestData.getTransactionTerminalDto()
                
                if(requestData.instanceRequest.metaData==null){
                    watcher=  WatcherContext.initOperation(requestEntity)
                    requestEntity=fillRequestEntity(requestEntity,httpRequest) 
                    responseEntity.uuid = requestEntity.uuid
                    responseEntity.errorCode = ErrorCode.SecurityError.errorCodeInt
                    responseEntity.errorMessage = ErrorCode.SecurityError.errorCodeMessage+" METADATA NULL"
                    sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
                    return
                }
                requestEntity=requestData.instanceRequest.metaData!!
                requestEntity=fillRequestEntity(requestEntity,httpRequest)
                WatcherGlobalLog.debug("GatewayTransactionDto_requestEntity")
                WatcherGlobalLog.obj(requestEntity)

                watcher= WatcherContext.initOperation(requestEntity)
                WatcherGlobalLog.debug("requestData is GatewayTransactionDto")
                WatcherGlobalLog.debug("requestData is RequestEntity")
                requestData.instanceRequest.metaData!!.uuid = requestEntity.uuid
                WatcherGlobalLog.debug(" requestEntity.uuid ${requestEntity.uuid}")
                WatcherGlobalLog.debug(" requestData.instanceRequest.metaData!!.uuid ${ requestData.instanceRequest.metaData!!.uuid}")
                requestData.instanceRequest.metaData!!.serverName = requestEntity.serverNameRequest
                requestData.instanceRequest.metaData!!.serverNameRequest = requestEntity.serverNameRequest
                WatcherGlobalLog.debug("BEFORE  existsRequest")
                requestData.instanceRequest.metaData!!.clientIP = requestEntity.clientIP
                requestData.instanceRequest.metaData!!.serverNameRequest = requestEntity.serverNameRequest
                requestData.instanceRequest.metaData!!.moduleIdRequest = requestEntity.moduleIdRequest
                requestData.instanceRequest.metaData!!.operationIdRequest = requestEntity.operationIdRequest
                requestData.instanceRequest.metaData!!.terminalSerialNumber = requestEntity.terminalSerialNumber
                StringUtils.copyNonEmptyProperties(requestData.instanceRequest.metaData!!,merchantDataDto)
                WatcherGlobalLog.debug("merchantDataDto ${GsonConfig.gsonLogs.toJson(merchantDataDto)}")

                if (  requestData.instanceRequest.metaData!!.terminalSerialNumber.isEmpty()||  requestData.instanceRequest.metaData!!.terminalSerialNumber.length<20) {
                    responseEntity.uuid = requestEntity.uuid
                    responseEntity.errorCode = ErrorCode.SerialIsEmpty.errorCodeInt
                    responseEntity.errorMessage = ErrorCode.SerialIsEmpty.errorCodeMessage
                    sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
                    return
                }

            }

            else{
                requestEntity=fillRequestEntity(requestEntity,httpRequest)
                responseEntity.uuid = requestEntity.uuid
                responseEntity.errorCode = ErrorCode.SecurityError.errorCodeInt
                responseEntity.errorMessage = ErrorCode.SecurityError.errorCodeMessage +" INTERNAL ERROR CLASS : [${requestClass.qualifiedName}]=> IS NOT REQUEST ENTITY"
                sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
                return
            }
            WatcherGlobalLog.debug("BEFORE watcher.webRequest(requestData")

            watcher.webRequest(requestData)
            WatcherGlobalLog.debug("AFTER watcher.webRequest(requestData")
            WatcherGlobalLog.debug("BEFORE   rawRequest=GsonConfig.gsonLogs.toJson(requestData)")

            rawRequest=GsonConfig.gsonRaw.toJson(requestData)
            WatcherGlobalLog.debug("AFTER   rawRequest=GsonConfig.gsonLogs.toJson(requestData)")

            WatcherGlobalLog.debug("requestData MODIFIED  rawRequest ${StringUtils.maskJson( rawRequest)}")

            if(httpRequest.requestURI.contains("/vault/save-client-credentials")){
                val json=GsonConfig.gsonLogs.toJson(requestData)
                val dto=requestData as RequestReadClientCredData
                WatcherGlobalLog.debug("saveClientCredentials: [${json}]") 
                var newDto=ClientCredData()
                StringUtils.copyNonEmptyProperties(dto, newDto) 
                val data = PandoraneoManager().saveClientCredentials(newDto)
                val responseDt =  ResponseReadClientCredData() 
                StringUtils.copyNonEmptyProperties(data, response)
                responseDt.errorCode=0
                val finalResponseBody=GsonConfig.gsonLogs.toJson(responseDt)
                WatcherGlobalLog.debug("finish response=> ${finalResponseBody}")
                sendOkResponseHttp(finalResponseBody,httpResponse,wrappedResponse)

                return 
            }       
            if(httpRequest.requestURI.contains("/vault/read-client-credentials")){
              
                val json=GsonConfig.gsonLogs.toJson(requestData)
                val dto=requestData as RequestReadClientCredData
                WatcherGlobalLog.debug("saveClientCredentials: [${json}]")
                val data = PandoraneoManager().readClientCredentials(dto)
                val responseDt =  ResponseReadClientCredData() 
                StringUtils.copyNonEmptyProperties(data, responseDt) 
                responseDt.errorCode=0 
                val finalResponseBody=GsonConfig.gsonLogs.toJson(responseDt)
                WatcherGlobalLog.debug("finish response=> ${finalResponseBody}") 
                sendOkResponseHttp(finalResponseBody,httpResponse,wrappedResponse)

                return  
            }     
    


            //NetworkLogger.debug("rawRequest ${rawRequest}")
        }catch (ex:Exception){
            WatcherGlobalLog.debug("SETTING DATA EXCEPTION: ${ex.message}")
            requestEntity=fillRequestEntity(requestEntity,httpRequest)
            responseEntity.uuid = requestEntity.uuid
            responseEntity.errorCode = ErrorCode.UnHandledException.errorCodeInt
            responseEntity.errorMessage = ErrorCode.UnHandledException.errorCodeMessage +" SETTING DATA EXCEPTION: ${ex.message}"
            sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
            return
        }

        if(watcher!=null) {
            WatcherGlobalLog.debug(" watcher.setAuthToken ")
          //  println(" watcher.setAuthToken ") 
            watcher.setAuthToken(httpRequest.getHeader("Authorization"))
        }else{ 

           WatcherGlobalLog.debug(" WATCHER IS NULL")
           // println(" WATCHER IS NULL")

            requestEntity=fillRequestEntity(requestEntity,httpRequest)
            responseEntity.uuid = requestEntity.uuid
            responseEntity.errorCode = ErrorCode.Undefined.errorCodeInt
            responseEntity.errorMessage = ErrorCode.Undefined.errorCodeMessage +" WATCHER IS NULL"
            sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
            return
        }
        WatcherGlobalLog.debug("BEFORE doFilter(wrappedRequest, wrappedResponse) ")
        // Wrap the request and response so they can be read again
        val wrappedRequest = CustomHttpServletRequestWrapper(httpRequest, rawRequest) 

        chain.doFilter(wrappedRequest, wrappedResponse)
        WatcherGlobalLog.debug("AFTER doFilter(wrappedRequest, wrappedResponse) ")

        val responseBody = wrappedResponse.getCaptureAsString()
        WatcherGlobalLog.debug("Response body: $responseBody")
        val duration = System.currentTimeMillis() - startTime
        WatcherGlobalLog.debug("Completed request for $requestUri with status ${wrappedResponse.status} in $duration ms")
        var finalResponseBody=responseBody
        try { 
            WatcherGlobalLog.debug("BEFORE PARSE TO=> ${responseClass.qualifiedName}")
            WatcherGlobalLog.debug("responseBody=> ${StringUtils.maskJson( responseBody)}")
            //val responseBodyObj = parseJson(responseBody, responseClass::class.java)
            var responseBodyObj = GsonConfig.gson.fromJson(responseBody, responseClass.java)


            watcher.webResponse(responseBodyObj)

            if (responseBodyObj is DescribeISOAsciiResponse) {
                WatcherGlobalLog.debug("responseBodyObj is DescribeISOAsciiResponse")
                httpResponse.contentType =httpRequest.contentType
                httpResponse.characterEncoding = wrappedResponse.characterEncoding
                httpResponse.writer.write(responseBodyObj.data)
                closeWatcher()
                return
            }
            if (responseBodyObj is FileLogResponseDto) {
                WatcherGlobalLog.debug("responseBodyObj is FileLogResponseDto")
                httpResponse.contentType =httpRequest.contentType
                httpResponse.characterEncoding = wrappedResponse.characterEncoding
                httpResponse.writer.write("[${responseBodyObj.errorCodeEnum}(${responseBodyObj.errorCode})] [${responseBodyObj.errorMessage}] \n\n"+responseBodyObj.fileContent)
                closeWatcher()
                return
            }
            if (responseBodyObj is SendTransactionResponse){
                WatcherGlobalLog.debug("requestData is SendTransactionResponse")
                val transactionResponse=(responseBodyObj as SendTransactionResponse ).transactionResponse
                StringUtils.copyNonEmptyProperties(transactionResponse,merchantDataDto)
                WatcherGlobalLog.debug("merchantDataDto ${GsonConfig.gsonLogs.toJson(merchantDataDto)}")

            } 
            if (responseBodyObj is GatewayTransactionResponse) {
                WatcherGlobalLog.debug("responseBodyObj is GatewayTransactionResponse")
                httpResponse.contentType =httpRequest.contentType
                httpResponse.characterEncoding = wrappedResponse.characterEncoding
                responseEntity.operationIdRequest=requestEntity.operationIdRequest
                responseEntity.rrn=requestEntity.rrn

                responseEntity.uuid=requestEntity.uuid
                if(responseBodyObj.gatewayResponse.metaData!=null) {
                    responseEntity.errorMessage = responseBodyObj.gatewayResponse.metaData!!.errorMessage
                    responseEntity.errorCode = responseBodyObj.gatewayResponse.metaData!!.errorCode
                    responseBodyObj.gatewayResponse.metaData!!.uuid = responseEntity.uuid

                    WatcherGlobalLog.debug("requestData is GatewayTransactionResponse")
                    StringUtils.copyNonEmptyProperties( responseBodyObj.gatewayResponse.metaData!!,merchantDataDto)
                    WatcherGlobalLog.debug("merchantDataDto ${GsonConfig.gsonLogs.toJson(merchantDataDto)}")

                }else{
                    responseEntity.errorMessage ="METADATA RESPONSE WAS NULL"
                    responseEntity.errorCode =ErrorCode.Undefined.errorCodeInt
                    WatcherGlobalLog.error("responseEntity.errorMessage ${responseEntity.errorMessage}")
 
                }
                if( responseEntity.errorCode ==-1&&  responseEntity.errorMessage ==""){
                    responseEntity.errorMessage="DIDNT HAVE ERROR MESSAGE"
                }

             

                watcher.updateLogErrorData(responseEntity,merchantDataDto)
                //                //PEGAR LOS ATRIBUTOS QUE AGAN FALTA
                httpResponse.writer.write(GsonConfig.gsonRaw.toJson(responseBodyObj))
                closeWatcher()
                return
            }
            if (responseBodyObj is ResponseEntity) {
                responseBodyObj.errorCodeEnum =ErrorCode.fromId(responseBodyObj.errorCode)
                responseBodyObj.uuid = requestEntity.uuid
                responseBodyObj.startTime = responseEntity.startTime
                responseBodyObj.operationIdRequest=OperationId.fromApiPath(httpRequest.requestURI)
                responseBodyObj.rrn=requestEntity.rrn

                WatcherGlobalLog.debug("responseBodyObj SET UUID->${responseBodyObj.uuid}")
                WatcherGlobalLog.debug("BEFORE updateLogErrorData")
                WatcherGlobalLog.obj(responseBodyObj)
                watcher.updateLogErrorData(responseBodyObj,merchantDataDto)
            }
            else{
                responseEntity.uuid = requestEntity.uuid
                responseEntity.errorCode = ErrorCode.SecurityError.errorCodeInt
                responseEntity.errorMessage = ErrorCode.SecurityError.errorCodeMessage +" INTERNAL ERROR CLASS : [${responseClass.qualifiedName}]=> IS NOT RESPONSE ENTITY"
                sendErrorResponse(merchantDataDto,httpRequest,httpResponse, responseEntity,bodyEncryptResponse,cryptoServerVGUtil,watcher)
                return
            }

            if (isEncrypted) {
                try {
                    finalResponseBody = encryptResponse(responseBodyObj, operationId, cryptoServerVGUtil)
                    WatcherGlobalLog.encryptor("RESPONSE_finalResponseBody ->${finalResponseBody}")

                }catch (ex:Exception){ 
                    httpResponse.status = HttpServletResponse.SC_EXPECTATION_FAILED
                    WatcherGlobalLog.error("BAD REQUEST[RESPONSE]->${ex.message}")
                    sendErrorResponseHttp("BAD REQUEST[RESPONSE]->${ex.message}",httpResponse,wrappedResponse)
                    return 

                }
            }else{
                finalResponseBody=GsonConfig.gsonRawResponse.toJson(responseBodyObj)
              //  httpResponse.writer.write(finalResponseBody)
                WatcherGlobalLog.debug("gsonRawResponse.toJson")


            }

        }catch (ex:Exception){ 
            WatcherGlobalLog.error("ERROR PARSING 01 => ${responseClass.qualifiedName} EXMSG[${ex.message}] ")
            httpResponse.status = HttpServletResponse.SC_EXPECTATION_FAILED
            sendErrorResponseHttp("ERROR PARSING 01 => ${responseClass.qualifiedName} [${ex.message}]",httpResponse,wrappedResponse)
            return
        }
        closeWatcher()
        httpResponse.contentType =httpRequest.contentType
        httpResponse.characterEncoding = wrappedResponse.characterEncoding
        WatcherGlobalLog.debug("finish response=> ${finalResponseBody}")
        watcher.updateLogErrorData(responseEntity,merchantDataDto)

        httpResponse.writer.write(finalResponseBody) 

    }  
/*termina el doFilter original */



    fun fillRequestEntity(requestEntity: RequestEntity,httpRequest: HttpServletRequest):RequestEntity{
        WatcherGlobalLog.debug("fillRequestEntity START")

        if(  requestEntity.clientIP =="")
            requestEntity.clientIP = getClientIpAddress(httpRequest)
        WatcherGlobalLog.debug("httpRequest.requestURI ${httpRequest.requestURI}")
        requestEntity.clientIP = getClientIpAddress(httpRequest)
        requestEntity.serverNameRequest= AppSettings.getInstance().serverName
        requestEntity.operationIdRequest= OperationId.fromApiPath(httpRequest.requestURI)
        requestEntity.moduleIdRequest=requestEntity.operationIdRequest.moduleId
        if( requestEntity.rrn=="")
           requestEntity.rrn=DynamicISOFields.getNewRRN(requestEntity.hostType)

        requestEntity.terminalSerialNumber=requestEntity.terminalSerialNumber.toUpperCase() 
        WatcherGlobalLog.debug("requestEntity.terminalSerialNumber ${requestEntity.terminalSerialNumber}")
        WatcherGlobalLog.debug("httpRequest.operationIdRequest ${requestEntity.operationIdRequest}")
        WatcherGlobalLog.debug("httpRequest.moduleIdRequest ${requestEntity.moduleIdRequest}")
        if(requestEntity.uuid==""){
            requestEntity.uuid=StringUtils.generateUUID()
        } 
        WatcherGlobalLog.debug("fillRequestEntity END ")

        return requestEntity

    }
    fun closeWatcher(){
        WatcherContext.finishOperation()
        WatcherGlobalLog.close()
    }
    private fun sendOkResponseHttp(message:String,httpResponse: HttpServletResponse,wrappedResponse: HttpServletResponse) {
        httpResponse.status = HttpServletResponse.SC_OK
        httpResponse.contentType ="application/json"
        httpResponse.characterEncoding = wrappedResponse.characterEncoding
        httpResponse.writer.write(message)
    }
    private fun sendErrorResponseHttp(message:String,httpResponse: HttpServletResponse,wrappedResponse: HttpServletResponse) {
        httpResponse.status = HttpServletResponse.SC_EXPECTATION_FAILED
        httpResponse.contentType ="text"
        httpResponse.characterEncoding = wrappedResponse.characterEncoding
        httpResponse.writer.write(message)
    }
    private fun sendErrorResponse(merchantDataDto:MerchantDataDto,httpRequest: HttpServletRequest,httpResponse: HttpServletResponse, responseEntity: ResponseEntity,bodyEncryptResponse:BodyEncryptResponse,cryptoServerVGUtil: CryptoServerVGUtil,watcher:Watcher?) {
        val wrappedResponse = CustomHttpServletResponseWrapper(httpResponse)
        responseEntity.errorCodeEnum=ErrorCode.fromId(responseEntity.errorCode)
        responseEntity.operationIdRequest=OperationId.fromApiPath(httpRequest.requestURI)
        httpResponse.status = HttpServletResponse.SC_OK
        httpResponse.contentType =httpRequest.contentType
        httpResponse.characterEncoding = "UTF-8"
        WatcherGlobalLog.error("RESPONSE ERROR=> [${responseEntity.errorCode}][${responseEntity.errorCodeEnum}][${responseEntity.errorMessage}]")
        if(watcher!=null)
          watcher.updateLogErrorData(responseEntity,merchantDataDto)

        if(bodyEncryptResponse.isEncrypted){
            try{
               val encrypted= encryptResponse(GsonConfig.gsonRaw.toJson(responseEntity),bodyEncryptResponse.operationIdRequest,cryptoServerVGUtil)
                WatcherGlobalLog.error("finish response ENCRYPTED[${bodyEncryptResponse.isEncrypted}]=>  [${encrypted}]")
                httpResponse.writer.write(encrypted)
            }catch (ex:Exception){
                httpResponse.status = HttpServletResponse.SC_EXPECTATION_FAILED
                    val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
                httpResponse.writer.write("BAD REQUEST[RESPONSE] ${err} [${ErrorCode.fromId(responseEntity.errorCode)}(${responseEntity.errorCode})] [${responseEntity.errorMessage}]->${ex.message}")
            }finally {
                WatcherContext.finishOperation()
            }
        }else{
          val response=GsonConfig.gsonRawResponse.toJson(responseEntity)
            WatcherGlobalLog.debug("finish response=> ${GsonConfig.gsonLogs.toJson(responseEntity)}")

            httpResponse.writer.write(response)

        }
        WatcherContext.finishOperation()
        WatcherGlobalLog.close()
    }

    override fun destroy() {
        // No cleanup necessary
    }
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val ipAddress = request.getHeader("X-Forwarded-For")
        if (ipAddress.isNullOrEmpty() || "unknown".equals(ipAddress, ignoreCase = true)) {
            return request.remoteAddr
        }
        return ipAddress.split(",").first().trim()
    }
    private fun checkIfEncrypted(
        rawRequest: String,
        operationIdRequest: OperationId,
        cryptoServerVGUtil: CryptoServerVGUtil
    ): BodyEncryptResponse {
        val bodyEncryptResponse=BodyEncryptResponse()
        WatcherGlobalLog.debug("Checking if request is encrypted")
        WatcherGlobalLog.debug("Raw request content: $rawRequest")
        bodyEncryptResponse.operationIdRequest=operationIdRequest

        val isEncrypted = rawRequest.matches(Regex("[a-fA-F0-9.\\n]+"))
        bodyEncryptResponse.isEncrypted = isEncrypted

        WatcherGlobalLog.debug("operationIdRequest $operationIdRequest isEncrypted $isEncrypted")
        if (isEncrypted) {

            WatcherGlobalLog.encryptor("EncryptionFilter_operationIdRequest $operationIdRequest isEncrypted $isEncrypted")

            try {
                WatcherGlobalLog.encryptor("EncryptionFilter_rawRequest ${StringUtils.maskJson( rawRequest)} ")
                WatcherGlobalLog.encryptor(" EncryptionFilter_operationIdRequest.requestDtoClass $operationIdRequest.requestDtoClass.name ")

                val decryptedData = cryptoServerVGUtil.decryptBody(rawRequest, operationIdRequest.requestDtoClass.java)
                if(decryptedData==null){
                    bodyEncryptResponse.responseEntity.errorCode = ErrorCode.SecurityError.errorCodeInt
                    bodyEncryptResponse.responseEntity.errorMessage = ErrorCode.SecurityError.errorCodeMessage+" INAVALID ENCRYPTION "
                    WatcherGlobalLog.encryptor(" EncryptionFilter_bodyEncryptResponse_errorCode  $bodyEncryptResponse.responseEntity.errorCode  ")
                    WatcherGlobalLog.encryptor(" EncryptionFilter_bodyEncryptResponse_errorMessage  $bodyEncryptResponse.responseEntity.errorMessage ")

                    return bodyEncryptResponse
                }
                val decryptedRaw = GsonConfig.gsonRaw.toJson(decryptedData)
                WatcherGlobalLog.encryptor("decryptedRaw ${StringUtils.maskJson( decryptedRaw)}")
                bodyEncryptResponse.rawRequest = decryptedRaw
                bodyEncryptResponse.responseEntity.errorCode = 0
                bodyEncryptResponse.uuid=cryptoServerVGUtil.getUUID()

                WatcherGlobalLog.encryptor("decryptedRaw ${GsonConfig.gsonLogs.toJson(bodyEncryptResponse)}")

                return bodyEncryptResponse
            }catch (ex:Exception){
                WatcherGlobalLog.encryptor("encryptor_exception ${ex.message}")

                bodyEncryptResponse.responseEntity.errorCode = ErrorCode.SecurityError.errorCodeInt
                bodyEncryptResponse.responseEntity.errorMessage = ErrorCode.SecurityError.errorCodeMessage+" ${ex.message}"
            }
        }
        bodyEncryptResponse.rawRequest=rawRequest
        bodyEncryptResponse.responseEntity.errorCode=0
        return bodyEncryptResponse
    }


    private fun buildRequestEntity(httpRequest: HttpServletRequest,rawRequest: String,expectedClass: KClass<out Any>): RequestEntity {
        WatcherGlobalLog.debug("Building RequestEntity. , Raw Request: $rawRequest")
        return     parseJsonRequest(rawRequest,httpRequest,expectedClass)

    }

    private fun decryptRequest(encryptedText: String, cryptoServerVGUtil:CryptoServerVGUtil,requestClass: KClass<out Any>): RequestEntity {

        val decryptedData=cryptoServerVGUtil.decryptBody(encryptedText,requestClass.java)
        val decryptedRaw=GsonConfig.gson.toJson(decryptedData)
        WatcherGlobalLog.debug("decryptedRaw ${StringUtils.maskJson( decryptedRaw)}")
 
      //  NetworkLogger.debug("Decrypting request")
     //   val decryptedJson = decrypt(encryptedText)
     //   NetworkLogger.debug("Decrypted JSON: $decryptedJson")

        return GsonConfig.gson.fromJson(decryptedRaw, RequestEntity::class.java)
    }

    private fun parseJsonRequest(json: String,httpRequest:HttpServletRequest,expectedClass: KClass<out Any>): RequestEntity {
        WatcherGlobalLog.debug("parseJsonRequest  JSON request: $json expectedClass ${expectedClass.qualifiedName}")

            val requestEntity=parseJson(json,RequestEntity::class.java)
            if(  requestEntity.clientIP =="")
                requestEntity.clientIP = getClientIpAddress(httpRequest)
            WatcherGlobalLog.debug("httpRequest.requestURI ${httpRequest.requestURI}")
            requestEntity.clientIP = getClientIpAddress(httpRequest)
            requestEntity.serverNameRequest= AppSettings.getInstance().serverName
            requestEntity.operationIdRequest= OperationId.fromApiPath(httpRequest.requestURI)
            requestEntity.moduleIdRequest=requestEntity.operationIdRequest.moduleId
            WatcherGlobalLog.debug("httpRequest.operationIdRequest ${requestEntity.operationIdRequest}")
            WatcherGlobalLog.debug("httpRequest.moduleIdRequest ${requestEntity.moduleIdRequest}")
            if(requestEntity.uuid==""){
                requestEntity.uuid=StringUtils.generateUUID()
            }
            return requestEntity

    }

  private fun <T> parseJson(json: String, clazz: Class<T>): T {
        WatcherGlobalLog.debug("Parsing JSON to class ${clazz.typeName}")
        return objectMapper.readValue(json, clazz)
    }


    private fun decrypt(encryptedText: String): String {
        WatcherGlobalLog.debug("Decrypting text")
        // Lógica ficticia de desencriptación, reemplaza con tu propia lógica
        return encryptedText.reversed() // Ejemplo: simplemente revertir el texto
    }

    private fun encryptResponse(responseObj: Any,operationIdRequest: OperationId,   cryptoServerVGUtil: CryptoServerVGUtil): String {
        WatcherGlobalLog.debug("Encrypting responseObj")
        val encryptedBody = cryptoServerVGUtil.encryptBody(responseObj)
        WatcherGlobalLog.encryptor("Encrypting encryptedBody [${encryptedBody}]")
        return encryptedBody 
    }

    private fun readRequestBody(request: HttpServletRequest): String {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(request.inputStream, "UTF-8"))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: Exception) {
            WatcherGlobalLog.debug("Error reading request body: ${e.message}")
        }
        return stringBuilder.toString() 
    }

    class CustomHttpServletRequestWrapper(request: HttpServletRequest, private val body: String) : HttpServletRequestWrapper(request) {
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray(StandardCharsets.UTF_8)).let { ServletInputStreamWrapper(it) }
        override fun getReader() = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
    }

    class ServletInputStreamWrapper(private val inputStream: InputStream) : javax.servlet.ServletInputStream() {
        override fun isFinished() = inputStream.available() == 0
        override fun isReady() = true
        override fun setReadListener(readListener: javax.servlet.ReadListener) = throw UnsupportedOperationException()
        override fun read() = inputStream.read()
    }

    class CustomHttpServletResponseWrapper(response: HttpServletResponse) : HttpServletResponseWrapper(response) {
        private val capture = ByteArrayOutputStream()
        private val writer = PrintWriter(capture)

        override fun getOutputStream() = ByteArrayOutputStreamServletOutputStream(capture)
        override fun getWriter(): PrintWriter {
            return writer
        }

        fun getCaptureAsString(): String {
            writer.flush()
            return capture.toString()
        }
    }

    class ByteArrayOutputStreamServletOutputStream(private val outputStream: ByteArrayOutputStream) : javax.servlet.ServletOutputStream() {
        override fun isReady() = true
        override fun setWriteListener(writeListener: javax.servlet.WriteListener) = throw UnsupportedOperationException()
        override fun write(b: Int) = outputStream.write(b)
    }
}
