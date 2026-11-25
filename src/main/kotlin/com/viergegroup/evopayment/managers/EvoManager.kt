package com.viergegroup.evopayment.managers

import com.shared.commons.ErrorCode
import com.shared.commons.GsonConfig
import com.viergegroup.evopayment.dtos.EvoConfirmRequestDto
import com.viergegroup.evopayment.dtos.SwVierge2EvoMapper
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext

import com.viergegroup.evopayment.dtos.EvoKeepAliveResponseDto
import com.viergegroup.evopayment.dtos.EvoKeepAliveDto
import com.shared.commons.AppSettings
import com.viergegroup.swmain.dtos.comm.CommHostRequestDto
/** Clase principal que gestiona la integración con el gateway EvoPayment. */
class EvoManager {
    // Cliente HTTP que se encarga de comunicarse con el gateway Evo
    private val httpClient: EvoHttpClient = EvoHttpClient()
    // Sistema de logging centralizado
    private val watcher: Watcher = WatcherContext.getInstance()

    private fun tryConfirm(confirmDto: EvoConfirmRequestDto) {
        try {
            watcher.debug("Enviando Confirm → ${GsonConfig.gson.toJson(confirmDto)}")
            val evoConfirmRes = httpClient.doConfirmTxn(confirmDto)
            watcher.debug("Confirm OK → ${GsonConfig.gson.toJson(evoConfirmRes)}")
        } catch (e: Exception) {
            watcher.error("Confirm falló (no bloqueante): ${e.message}")
        }
    }

    
    fun keepAlive(dto:CommHostRequestDto): TransactionResponse {
        val requestDto = EvoKeepAliveDto()
        requestDto.KeepAlive.POSIdentification = dto.posIdentification
        requestDto.KeepAlive.BranchIdentification = dto.branchIdentification
        requestDto.KeepAlive.CompanyIdentification = dto.companyIdentification
        requestDto.KeepAlive.SystemIdentification = dto.systemIdentification
    
        val response = TransactionResponse() 
        response.transactionRequest=TransactionDto()
        response.transactionResponse=TransactionDto()

          var baseUrl: String = AppSettings.getInstance().evoUrl

        try {
            val evoResponse = httpClient.doKeepAlive(requestDto)
            if (evoResponse.KeepAliveResponse.ResponseCode == -1) {
                response.errorCode = 0
                 response.transactionResponse!!.responseCode="00"
                response.errorMessage = "URL: ${baseUrl} | KeepAlive successful"
            } else {
                response.errorCode = 0
               response.transactionResponse!!.responseCode="00"

                response.errorMessage = "URL: ${baseUrl} | KeepAlive failed: ${evoResponse.KeepAliveResponse.ResponseMessage}"
            }
        } catch (ex: Exception) {
            response.errorCode = -1
            response.errorMessage = "URL: ${baseUrl} | Exception during KeepAlive: ${ex.message}"
        }

        return  response
    }


    fun processTransaction(dtoOrg: TransactionDto): TransactionResponse {
        try {
            return when (dtoOrg.transactionType) {
                TransactionType.Sale -> {
                    val saleDto = SwVierge2EvoMapper.txnDto2PaymentDto(dtoOrg, watcher)
                    val evoResponse = httpClient.doSale(saleDto)

                    val confirmDto =
                            SwVierge2EvoMapper.saleRsp2Confirm(
                                    saleDto.Sale.Reference,
                                    evoResponse,
                                    watcher
                            )
                    tryConfirm(confirmDto)

                    SwVierge2EvoMapper.resPayment2TxnResDto(dtoOrg, evoResponse, watcher)
                }
                TransactionType.SaleWithTip -> {
                    val saleDto = SwVierge2EvoMapper.txnDto2SaleTipDto(dtoOrg, watcher)
                    val evoResponse = httpClient.doSaleTip(saleDto)
                    // --- CONFIRM ---
                    val confirmDto =
                            SwVierge2EvoMapper.saleRsp2Confirm(
                                    saleDto.Sale.Reference,
                                    evoResponse,
                                    watcher
                            )
                    tryConfirm(confirmDto)
                    SwVierge2EvoMapper.resPayment2TxnResDto(dtoOrg, evoResponse, watcher)
                }
                TransactionType.Reverse -> {
                    watcher.debug("dtoOrg.originalWasRefund ${dtoOrg.originalWasRefund}")
                    if (dtoOrg.originalWasRefund) {
                        val reverseDto =
                                SwVierge2EvoMapper.txnDto2ReturnReverseDtoV2(dtoOrg, watcher)
                        val evoResponse = httpClient.doReturnReverse(reverseDto)
                        val confirmDto =
                                SwVierge2EvoMapper.returnRsp2Confirm(
                                        reverseDto.Return.Reference,
                                        evoResponse,
                                        watcher
                                )
                        tryConfirm(confirmDto)
                        SwVierge2EvoMapper.resReturn2TxnResDto(dtoOrg, evoResponse, watcher)
                    } else {

                        val reverseDto = SwVierge2EvoMapper.txnDto2ReverseDtoV2(dtoOrg, watcher)
                        val evoResponse = httpClient.doReverse(reverseDto)
                        val confirmDto =
                                SwVierge2EvoMapper.saleRsp2Confirm(
                                        reverseDto.Sale.Reference,
                                        evoResponse,
                                        watcher
                                )
                        tryConfirm(confirmDto)
                        SwVierge2EvoMapper.resPayment2TxnResDto(dtoOrg, evoResponse, watcher)
                    }
                }
                TransactionType.Cancellation -> {
                    val cancelDto = SwVierge2EvoMapper.txnDto2VoidDto(dtoOrg, watcher)
                    val evoResponse = httpClient.doVoid(cancelDto)
                    // --- CONFIRM ---
                    val confirmDto =
                            SwVierge2EvoMapper.voidRsp2Confirm(
                                    cancelDto.Void.Reference,
                                    evoResponse,
                                    watcher
                            )
                    tryConfirm(confirmDto)

                    SwVierge2EvoMapper.resVoid2TxnResDto(dtoOrg, evoResponse, watcher)
                }
                TransactionType.Refund -> {
                    val returnDto = SwVierge2EvoMapper.txnDto2Return(dtoOrg, watcher)
                    val evoResponse = httpClient.doReturn(returnDto)
                    val confirmDto =
                            SwVierge2EvoMapper.returnRsp2Confirm(
                                    returnDto.Return.Reference,
                                    evoResponse,
                                    watcher
                            )
                    tryConfirm(confirmDto)
                    SwVierge2EvoMapper.resReturn2TxnResDto(dtoOrg, evoResponse, watcher)
                }
                else -> TransactionResponse()
            }
        } catch (exp: EvoException) {
            val response = TransactionResponse()
            response.errorCode = ErrorCode.UnHandledException.errorCodeInt
            response.errorCodeEnum = ErrorCode.UnHandledException
            response.errorMessage = exp.message.toString()
            watcher.error("EvoException => ${exp.message}")
            response.transactionResponse = TransactionDto()
            response.transactionRequest = TransactionDto()
            var responseCode = ErrorCode.UnHandledException.responseCode
            if (exp.message!!.lowercase().contains("time out") ||
                            exp.message!!.lowercase().contains("timed out")
            ) {
                response.errorCode = ErrorCode.ClientTimeOut.errorCodeInt
                response.errorCodeEnum = ErrorCode.ClientTimeOut
                response.errorMessage = exp.message.toString()
            }
            if (exp.message!!.lowercase().contains("EOFException")) {
                response.errorCode = ErrorCode.ClientTimeOut.errorCodeInt
                response.errorCodeEnum = ErrorCode.ClientTimeOut
                response.errorMessage = exp.message.toString()
                responseCode = ErrorCode.ClientTimeOut.responseCode
            }
            response.transactionRequest = dtoOrg
            response.transactionResponse = dtoOrg
            response.transactionRequest!!.responseCode = responseCode
            response.transactionResponse!!.responseCode = responseCode
            response.transactionRequest!!.responseCodeAuthorizer = responseCode
            response.transactionResponse!!.responseCodeAuthorizer = responseCode
            watcher.error("EvoException_responseCode => ${responseCode}")

            response.transactionResponse!!.errorCode = response.errorCode
            response.transactionResponse!!.errorCodeEnum = response.errorCodeEnum
            response.transactionResponse!!.errorMessage = response.errorMessage
            response.transactionRequest!!.errorCode = response.errorCode
            response.transactionRequest!!.errorCodeEnum = response.errorCodeEnum
            response.transactionRequest!!.errorMessage = response.errorMessage

            return response
        }
    }
}
