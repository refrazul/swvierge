package com.viergegroup.swmain.controllers



import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.gateway.*
import com.viergegroup.swmain.dtos.transactions.SearchTransactionResponse
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse
import com.viergegroup.swmain.entities.catalogs.HostType

import com.viergegroup.swmain.managers.WebAPIManager


import com.viergegroup.prodhelper.GatewayResponseWrapperPassTrue
import com.viergegroup.prodhelper.SwViergeProdApi
import com.viergegroup.prodhelper.TransactionFullResponseDtoPassTrue

import  com.shared.commons.GsonConfig

import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.entities.catalogs.TransactionType
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import com.viergegroup.swmain.entities.catalogs.Authorizer
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

 
@RestController
@RequestMapping("gatewayevo")
class GatewayEvoController (
       // private val restTemplate: RestTemplate // inyectado

){

    @PostMapping("/transaction") //
    fun transaction(@RequestBody dto: GatewayTransactionDto, request: HttpServletRequest): GatewayTransactionResponse {
        val watcher=WatcherContext.getInstance()
        watcher.info(" \n\n INIT GatewayEvoController UPDATE transaction")
 
 
        val responseApi = GatewayTransactionResponse()
        if(dto.instanceRequest.metaData!=null) {
            watcher.info("dto.instanceRequest.metaData!!.uuid ${dto.instanceRequest.metaData!!.uuid}")
            dto.instanceRequest.metaData!!.transactionIdMpp=dto.instanceRequest.id 
            watcher.info("dto.instanceRequest.metaData.transactionIdMpp ${dto.instanceRequest.metaData!!.transactionIdMpp}")

            watcher.debug("GatewayTransactionDto_instanceRequest_metadata=>")
            watcher.debugObj(dto.instanceRequest.metaData!!) 
                // 👇 AQUÍ EL BRANCH PARA EVOPAYMENTPROD
           /*  if (dto.instanceRequest.metaData!!.hostType == HostType.EvoPaymentProd) {
                val remoteUrl = "https://switchvg.viergegroup.com:8000/gateway/transaction"

                watcher.info("HostType.EvoPaymentProd detectado, reenviando request a $remoteUrl")

                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }

                val entity = HttpEntity(dto, headers)

                return try {
                    val remoteResponse: ResponseEntity<GatewayTransactionResponse> =
                        restTemplate.postForEntity(
                            remoteUrl,
                            entity,
                            GatewayTransactionResponse::class.java
                        )

                    watcher.info("Respuesta recibida desde $remoteUrl status=${remoteResponse.statusCode}")

                    remoteResponse.body ?: run {
                        watcher.info("remoteResponse.body es null, devolviendo GatewayTransactionResponse vacío")
                        GatewayTransactionResponse()
                    } 
                } catch (ex: Exception) {
                    watcher.debug("Error reenviando a $remoteUrl: ${ex.message}")
                    // Manejo de error cuando el servidor remoto falla
                    val errorResp = GatewayTransactionResponse()
                    errorResp.gatewayResponse.status = "ERROR"
                    errorResp.gatewayResponse.actionCode = "9999"
                    errorResp.gatewayResponse.emvResponsePayload = ""
                    errorResp
                }
            }
*/
            if(dto.instanceRequest.metaData!!.transactionType==TransactionType.ReadCard){
               
    

                val response = WebAPIManager().processReadCard(dto.instanceRequest.metaData!!)
                if(dto.instanceRequest.metaData!=null){ 
                    response.transactionResponse.uuid=dto.instanceRequest.metaData!!.uuid
                    response.transactionResponse.errorCode=response.errorCode
                    response.transactionResponse.errorMessage=response.errorMessage
                }
                response.errorMessage = ErrorCode.Approved.errorCodeMessage
                response.errorCode = ErrorCode.Approved.errorCodeInt
                response.errorCodeEnum=ErrorCode.Approved
                responseApi.injectResponse(dto,response) 
                responseApi.gatewayResponse.status="APPROVED"
                responseApi.gatewayResponse.actionCode="0000"
                responseApi.gatewayResponse.emvResponsePayload="8A023030"

                
            }else{    
   
                val response = WebAPIManager().sendTransaction(dto.instanceRequest.metaData!!)
                if(dto.instanceRequest.metaData!=null){
                    response.transactionResponse.uuid=dto.instanceRequest.metaData!!.uuid
                    response.transactionResponse.errorCode=response.errorCode
                    response.transactionResponse.errorMessage=response.errorMessage
                }

                if(response.transactionResponse!=null){

                  if(!response.transactionResponse!!.hostType.authorizer.sendClearData){
                     response.transactionResponse!!.cardNumber=StringUtils.maskCardNumber(  response.transactionResponse!!.cardNumber)
                     response.transactionResponse!!.track2==""
                   response.transactionResponse!!.emvData==""

                  }

           

                  val responseCodeAuthorizer=response.transactionResponse!!.responseCode
                    if(!responseCodeAuthorizer.contains("F")&&!responseCodeAuthorizer.contains("H")
                        &&!responseCodeAuthorizer.contains("D")
                        &&!responseCodeAuthorizer.contains("E") &&!responseCodeAuthorizer.contains("X")&&response.transactionResponse.responseCodeAuthorizer==""){
                            response.transactionResponse.responseCodeAuthorizer=responseCodeAuthorizer

                        }
                }
           

                responseApi.injectResponse(dto,response)
            }
            
          
 
        }else{
            watcher.info(" \n\n dto.instanceRequest.metaData IS_NULL")

        } 
        return responseApi
    }


    
    @PostMapping("/search-txn") //
    fun searchTxn(@RequestBody dto: TransactionTerminalDto): SearchTransactionResponse {

        val response= SearchTransactionResponse()
        val response2=WebAPIManager().searchTxn(dto)
        val watcher=WatcherContext.getInstance()
        watcher.info("searchTxn")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        watcher.info("searchTxn_terminalSerialNumber   dto.terminalSerialNumber")
 
        StringUtils.copyNonEmptyProperties(response2,response)
        if(response2.transactionDto!=null)
            BeanUtils.copyProperties(response2.transactionDto!!,response.transactionDto)
        watcher.info("response2=>") 
         watcher.debugObj(response)
        //  if(response.transactionDto!!.responseCode=="")
         //      val errorCodeEnum = ErrorCode.fromId(response.transactionDto!!.errorCode.errorCodeInt)
           //   response.transactionDto!!.responseCode=errorCodeEnum.responseCode

            val responseCodeAuthorizer=response.transactionDto!!.responseCode

        watcher.info("responseCodeAuthorizer  [$responseCodeAuthorizer]")  

             if(response.transactionDto!=null){
                     
                if(!response.transactionDto!!.hostType.authorizer.sendClearData){
                     response.transactionDto!!.cardNumber=StringUtils.maskCardNumber(  response.transactionDto!!.cardNumber)
                     response.transactionDto!!.track2==""
                      response.transactionDto!!.emvData==""
              
                  }

             

                    if(!responseCodeAuthorizer.contains("F")&&!responseCodeAuthorizer.contains("H")
                    &&!responseCodeAuthorizer.contains("D")
                    &&!responseCodeAuthorizer.contains("E") &&!responseCodeAuthorizer.contains("X")&&response.transactionDto!!.responseCodeAuthorizer==""){
                                     response.transactionDto!!.responseCodeAuthorizer=responseCodeAuthorizer

                                watcher.info("response.transactionDto!!.responseCodeAuthorizer  [${response.transactionDto!!.responseCodeAuthorizer}]")

                    }

             }
       


        //StringUtils.copyNonEmptyProperties(response2.transactionDto!!,response.transactionResponse)
        return response
    }


    @PostMapping("/encode-base64")
    fun encodeBase64(@RequestBody dto: TransactionTerminalDto): GatewayEncodeB64Response {
       val response= GatewayEncodeB64Response()
        response.base64Data=StringUtils.encodeBase64(dto)
        response.errorMessage=""
        response.errorCode=0
        return response
    }

    @PostMapping("/decode-base64")
    fun decodeBase64(@RequestBody dto: GatewayBase64Dto): GatewayDecodeB64Response {
        var response=GatewayDecodeB64Response()
        val resNu=StringUtils.decodeBase64<Any>(dto.base64Data)
        if(resNu!=null){
            response.errorMessage=""
            response.errorCode=0
            response.data= resNu

        }else{
            response.errorMessage="couldnt be parsed"
            response.errorCode=ErrorCode.Undefined.errorCodeInt

        }
        return response
    }



}
