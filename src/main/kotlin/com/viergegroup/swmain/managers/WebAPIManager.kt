package com.viergegroup.swmain.managers

import com.shared.commons.*
import com.shared.commons.ErrorCode
import com.viergegroup.swmain.dbmanagers.TransactionDBManager
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dtos.transactions.*
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.BeanUtils

class WebAPIManager {
    private var merchantType = MerchantType.Retail
    private lateinit var watcher: Watcher
    init {
        watcher = WatcherContext.getInstance()
    }

    fun saveTransaction(transactionResponse: TransactionResponse) {

        //  watcher.webResponse(transactionResponse)
        watcher.debug("saveTransaction 0")
        if (transactionResponse.transactionRequest != null) {
            watcher.debug("saveTransaction 1")
            watcher.debug("saveTransaction 2")
            watcher.saveTransaction(transactionResponse)
            watcher.debug("saveTransaction 3")
        }
    }

    fun searchTxn(dto: TransactionTerminalDto): SearchTxnResponseDto {

        var response = SearchTxnResponseDto()
        var emvTags = mutableMapOf<String, String>() // Define el mapa mutable con el tipo correcto.

        watcher.debug("searchTxn  ${dto.rrn} ${dto.terminalSerialNumber} ${dto.hostType}")
        try {
            val dbData = watcher.findTransaction(dto.rrn, dto.terminalSerialNumber, dto.hostType)
            if (dbData != null) {
                response.transactionDto = TransactionResponseDto()
                BeanUtils.copyProperties(dbData, response.transactionDto!!)

                if (dbData.transactionDto != null) {
                    watcher.debug("COPYING METADATA =>dbData.transactionDto")
                    try {

                        watcher.debug("StringUtils_maskEMVTags(")
                        dbData.transactionDto!!.emvTagsMap =
                                StringUtils.maskEMVTags(
                                        dto.terminalSerialNumber,
                                        dbData.hostType,
                                        dbData.transactionDto!!.emvTagsMap,
                                        watcher
                                )
                    } catch (e: Exception) {
                        watcher.debug("dbData.transactionDto!!.emvTagsMap ex => ${e.message}")
                    }
                    try {

                        dbData.transactionDto!!.emvData =
                                StringUtils.removeEmvDataSensitive(
                                        dbData.hostType,
                                        dbData.transactionDto!!.emvData,
                                        watcher
                                )

                        watcher.debug(
                                "searchTxn_sendClearData =>[${dbData.transactionDto!!.hostType.authorizer.sendClearData}]"
                        )
                        // watcher.debug("searchTxn_track2
                        // =>[${dbData.transactionDto!!.track2.contains("*")}]")

                        if (dbData.transactionDto!!.hostType.authorizer.sendClearData &&
                                        dbData.transactionDto!!.track2.contains("*")
                        ) {
                            // Asegúrate de que emvTagsMap no sea nulo y sea del tipo esperado
                            for ((key, value) in dbData.transactionDto?.emvTagsMap ?: emptyMap()) {
                                emvTags[key] = value // Agrega cada par clave-valor al mapa mutable.
                            }

                            watcher.debug(
                                    "searchTxn_emvTags?.containsKey(57) => ${emvTags.containsKey("57")}"
                            )
                            //  watcher.debug("searchTxn_emvTags?.containsKey(57) =>
                            // ${dbData.transactionDto?.emvTagsMap.containsKey(57)}")

                            if (emvTags.containsKey("57")) {
                                // Clonamos el valor de la clave 57
                                val clonedTrack2 = emvTags["57"]!!.toString()
                                watcher.debug("searchTxn_emvTags[57]! => $clonedTrack2")

                                // Asignamos el valor clonado a track2
                                dbData.transactionDto!!.track2 = clonedTrack2
                            } else {
                                watcher.debug("No se encontró la clave 57 en emvTags.")
                            }
                        }
                    } catch (e: Exception) {
                        //  dbData.transactionDto!!.emvData = ""
                        watcher.debug("removeEmvDataSensitive ex => ${e.message}")
                    }
                    StringUtils.copyNonEmptyProperties(
                            dbData.transactionDto!!,
                            response.transactionDto!!
                    )

                    BeanUtils.copyProperties(dbData.transactionDto!!, response.transactionDto!!)

                    watcher.debug("dbData.transactionDto!!.emvTagsMap=>")
                    watcher.debugObj(dbData.transactionDto!!.emvTagsMap)
                    response.transactionDto!!.emvTagsMap = dbData.transactionDto!!.emvTagsMap
                    watcher.debug("dbData.")
                    watcher.debugObj(dbData)
                }

                response.errorMessage = ErrorCode.Approved.errorCodeMessage
                response.errorCode = ErrorCode.Approved.errorCodeInt
            } else {
                response.errorCode = ErrorCode.TransactionDoesntExist.errorCodeInt
                response.errorMessage = ErrorCode.TransactionDoesntExist.errorCodeMessage
                watcher.debug("searchTxn ERROR : ${ ErrorCode.fromId(response.errorCode) }")
                return response
            }
            //  if( response.transactionDto!=null)
            //    response.transactionDto!!.apiVersion=versionOriginal
        } catch (e: Exception) {
            watcher.debug("EXCEPTION SEARCHING: ${e.message}")
            response.errorCode = ErrorCode.TransactionDoesntExist.errorCodeInt
            response.errorMessage =
                    ErrorCode.TransactionDoesntExist.errorCodeMessage + " ${e.message}"
            return response
        }
        watcher.debug("EXECUTE removeSensitiveData ")

        watcher.debug(
                "searchTxn_sendClearData =>[${response.transactionDto!!.hostType.authorizer.sendClearData}]"
        )
        watcher.debug("searchTxn_track2 =>[${response.transactionDto!!.track2.contains("*")}]")

        if (response.transactionDto!!.hostType.authorizer.sendClearData &&
                        response.transactionDto!!.track2.contains("*")
        ) {
            emvTags = mutableMapOf<String, String>() // Define el mapa mutable con el tipo correcto.
            for ((key, value) in response.transactionDto?.emvTagsMap ?: emptyMap()) {
                emvTags[key] = value // Agrega cada par clave-valor al mapa mutable.
            }

            watcher.debug("searchTxn_emvTags?.containsKey(57) => ${emvTags.containsKey("57")}")

            if (emvTags?.containsKey("57") == true) {
                response.transactionDto!!.track2 = emvTags["57"]!!
            }
        }
        watcher.debug("searchTxn_track2_clear  => ${response.transactionDto!!.track2}")
        if (response.transactionDto!!.hostType.authorizer.sendClearData) {
            response.transactionDto!!.cardNumber =
                    StringUtils.getCardNumber(response.transactionDto!!.track2)
            watcher.debug(
                    " response_transactionDto_cardNumber  => ${ response.transactionDto!!.cardNumber}"
            )
        }
        //  response.transactionDto=TransactionResponseDto()
        //  StringUtils.copyNonEmptyProperties(dto, response.transactionDto!!)
        // watcher.debug("RETURN  ")
        return response
    }

    /*fun sendTransaction(dto: GatewayTransactionDto): GatewayTransactionResponse {
        val newDto=dto.getTransactionTerminalDto()
        val fullResponse=sendTransaction(newDto)
        val response= GatewayTransactionResponse()
        response.injectResponse(dto,fullResponse)
        return response
    }*/

    fun sendTransaction(dto: TransactionTerminalDto): TransactionFullResponseDto {

        watcher.debug(" DEVICE_TYPE_sendTransaction : ${dto.deviceType} ")
        watcher.debug(
                " LACPI_terminalActivationByCardholder : ${dto.terminalActivationByCardholder} "
        )
        watcher.debug(
                " LACPI_identificationMethodCardHolder : ${dto.identificationMethodCardHolder} "
        )

        if (dto.hostType.authorizer != Authorizer.PROSA) {
            dto.deviceType = DeviceType.MobilePOS
        }
        if (dto.deviceType == DeviceType.MobilePOS) {
            dto.identificationMethodCardHolder = "0"
        }
        val amount = StringUtils.parseAmountToDouble(dto.transactionAmount, watcher)
        watcher.debug(" LACPI_amount: ${amount} ")

        if (dto.deviceType == DeviceType.MobilePOS && amount > 1000) {
            dto.identificationMethodCardHolder = "1"
        }

        if (dto.hostType.authorizer == Authorizer.OPENPAY) {
            watcher.debug(" dto.hostType.authorizer ${dto.hostType.authorizer}")
            if (dto.keyId == "" &&
                            (dto.transactionType == TransactionType.KeyLoad ||
                                    dto.transactionType == TransactionType.KeyLoadConfirm)
            ) {
                dto.keyId = ""
            }
            // else dto.keyId1 = "A000OPFT48"
            watcher.debug(" sendTransaction_keyId [${ dto.keyId}]")
        }
        watcher.debug("sendTransaction [0] dto.uuid ${dto.uuid}")

        watcher.debug("sendTransaction")
        dto.rrn = DynamicISOFields.getNewRRN(dto, dto.hostType)
        //   val originalDto= TransactionTerminalDto()
        //   BeanUtils.copyProperties(dto,originalDto)

        watcher.debug("TransactionTerminalDto.noSkipCRC32 ${dto.noSkipCRC32}")
        watcher.debug("TransactionTerminalDto.transactionType ${dto.transactionType}")
        watcher.debug(
                "TransactionTerminalDto.transactionMessageType ${dto.transactionType.transactionMessageType}"
        )
        watcher.info("BEFORE  watcher.webOriginalAndBaseRequest")

        watcher.webOriginalAndBaseRequest(dto)
        watcher.info("AFTER  watcher.webOriginalAndBaseRequest")
        watcher.debugObj(dto)
        watcher.debug("BEFORE findOriginalTransaction ")
        var originalTxn: TransactionDBData? = null
        val isReverse = dto.transactionType.transactionMessageType == TransactionMessageType.Reverse
        watcher.debug("isReverse ${isReverse} ")

        if (isReverse) {
            watcher.debug("isReverse  ")
            return processReverse(dto)
        }

        if (isReverse || dto.transactionType.transactionMessageType == TransactionMessageType.Advice
        ) {
            watcher.debug("CHECK ORIGINAL BECAUSE IS ADVICE OR IS_REVERSE ")
            watcher.debug("BEFORE findOriginalTransaction")
            watcher.debug("original_param terminalSerialNumber :  ${dto.terminalSerialNumber}")
            watcher.debug(
                    "original_param authorizationIdResponse :  ${dto.authorizationIdResponse}"
            )
            watcher.debug("original_param hostType :  ${ dto.hostType}")

            originalTxn =
                    watcher.findOriginalTransaction(
                            "",
                            dto.terminalSerialNumber,
                            dto.authorizationIdResponse,
                            dto.hostType
                    )

            watcher.debug("AFTER findOriginalTransaction NULL=>[${originalTxn!=null}]")
        } else {
            watcher.debug("ORIGINAL_IS_NULL BECAUSE IS NOT ADVICE")
        }
        watcher.debug("BEFORE checkRequest")

        var response = TransactionResponse(null)

        watcher.debug(
                "LX_dto.authorizationIdResponse: ${dto.authorizationIdResponse} ${dto.transactionType.transactionTypeId}"
        )

        val isAuthMissing = dto.authorizationIdResponse.isNullOrBlank()
        val isCancellationOrRefund =
                dto.transactionType == TransactionType.Cancellation ||
                        dto.transactionType == TransactionType.Refund

        if (isAuthMissing && isCancellationOrRefund) {
            response.transactionResponse = TransactionDto()
            response.transactionRequest = TransactionDto()

            response.apply {
                errorCode = ErrorCode.AuthorizationNumberRequired.errorCodeInt
                errorMessage = "${ErrorCode.AuthorizationNumberRequired.errorCodeMessage}"
                errorCodeEnum = ErrorCode.AuthorizationNumberRequired
            }

            response.transactionResponse?.apply {
                errorCode = ErrorCode.AuthorizationNumberRequired.errorCodeInt
                responseCode = ErrorCode.AuthorizationNumberRequired.responseCode
                errorMessage = "${ErrorCode.AuthorizationNumberRequired.errorCodeMessage}"
                errorCodeEnum = ErrorCode.AuthorizationNumberRequired
            }

            return TransactionFullResponseDto(response)
        }

        var requestDtoNew = DynamicISOFields.checkRequest(dto, originalTxn)

        // -LACPI2
        if (originalTxn != null) {
            watcher.debug(
                    "_LACPI2 NOT_NULL=>[${originalTxn.transactionType}] dto.transactionType.transactionMessageType [${dto.transactionType.transactionMessageType}]"
            )
            if (originalTxn.transactionType == TransactionType.SalePosTip &&
                            dto.transactionType.transactionMessageType ==
                                    TransactionMessageType.Advice
            ) {
                val closePosTip =
                        TransactionDBManager.getTransactionByTypeAndRrn(
                                originalTxn.rrn,
                                TransactionType.ClosePosTip,
                                watcher
                        )
                if (closePosTip != null) {
                    watcher.debug("_LACPI2 closePosTip NOT_NULL=>[${closePosTip.transactionType}]")
                    requestDtoNew.transactionAmount = closePosTip.transactionAmount
                    watcher.debug(
                            "_LACPI2 closePosTip.transactionAmount=>[${closePosTip.transactionAmount}]"
                    )
                } else {
                    watcher.debug("_LACPI2 closePosTip IS_NULL ")
                }
            }
        }

        var authorizationIdResponse = ""

        if (originalTxn != null && originalTxn.authorizationIdResponse != "") {
            watcher.debug("authorizationIdResponse IS NOT  EMPTY ")

            authorizationIdResponse = originalTxn.authorizationIdResponse
            watcher.debug(" authorizationIdResponse [${authorizationIdResponse}] ")
        }

        if (requestDtoNew.errorCode != 0) {

            response.errorCode = requestDtoNew.errorCode
            response.errorMessage = requestDtoNew.errorMessage
            val newRes = TransactionFullResponseDto(response)
            StringUtils.copyNonEmptyProperties(dto, newRes.transactionResponse)
            return newRes
        }
        watcher.debug("sendTransaction [1] requestDtoNew.uuid ${dto.uuid}")
        watcher.debug("requestDtoNew.terminalData ${requestDtoNew.terminalData}")

        try {

            watcher.debug("BEFORE checkEmvData")

            requestDtoNew = DynamicISOFields.checkEmvData(requestDtoNew)
            watcher.debug("BEFORE checkAcquirerData")
            watcher.debug("sendTransaction [2] requestDtoNew.uuid ${dto.uuid}")

            requestDtoNew = DynamicISOFields.checkAcquirerData(requestDtoNew)
            if (isReverse) {
                watcher.reverseDebug("AFTER checkAcquirerData")
            }
            watcher.debug("AFTER checkAcquirerData")
            watcher.debug("sendTransaction [3] requestDtoNew.uuid ${dto.uuid}")

            watcher.debug(" watcher.saveTransactionInitial INIT")
            TransactionDBManager.saveTransactionInitial(watcher, requestDtoNew)
            watcher.debug(" watcher.saveTransactionInitial END")

            if (requestDtoNew.errorCode != 0) {
                response = TransactionResponse(requestDtoNew)
            } else {
                watcher.debug(
                        "============================PROCESS_TRANSACTI0N INI =================================="
                )
                if (isReverse) {
                    watcher.reverseDebug(
                            "============================PROCESS_TRANSACTI0N INI =================================="
                    )
                }
                watcher.debug("transactionType ${dto.transactionType } ")
                if (isReverse) {
                    watcher.reverseDebug("transactionType ${dto.transactionType } ")
                }
                if (dto.transactionType == TransactionType.KeyLoadConfirm ||
                                dto.transactionType == TransactionType.KeyLoadPinConfirm
                ) {
                    watcher.debug("BEFORE processKeyLoadConfirm")
                    response =
                            TransactionManager()
                                    .processKeyLoadConfirm(
                                            requestDtoNew,
                                            requestDtoNew.merchantType
                                    )
                    if (isReverse) {
                        watcher.reverseDebug(
                                "BEFORE processKeyLoadConfirm errorCode: [${response.errorCode}]  [${response.errorMessage}]"
                        )
                    }
                    watcher.debug(
                            "BEFORE processKeyLoadConfirm errorCode: [${response.errorCode}]  [${response.errorMessage}]"
                    )
                } else if (dto.transactionType == TransactionType.KeyLoad ||
                                dto.transactionType == TransactionType.KeyLoadPin
                ) {
                    watcher.debug("BEFORE processKeyLoad hosttype: [${dto.hostType}]")
                    response =
                            TransactionManager()
                                    .processKeyLoad(requestDtoNew, requestDtoNew.merchantType)
                } else {
                    watcher.debug("BEFORE processTransaction")
                    watcher.debug("sendTransaction [4] requestDtoNew.uuid ${dto.uuid}")
                    if (isReverse) {
                        watcher.reverseDebug("BEFORE processTransaction")
                        watcher.reverseDebug("sendTransaction [4] requestDtoNew.uuid ${dto.uuid}")
                    }
                    response = TransactionManager().processTransaction(requestDtoNew)
                }
            }
            if (response.transactionRequest != null) {
                watcher.webRequestSentHost(response.transactionRequest!!)
            }
            watcher.debug(
                    "============================PROCESS_TRANSACTI0N END ==================================\n\n\n"
            )
            if (isReverse) {
                watcher.reverseDebug(
                        "============================PROCESS_TRANSACTI0N END ==================================\n\n\n"
                )
            }
        } catch (webException: WebServiceException) {
            watcher.error(
                    "sendTransaction ERRORCODE: " +
                            response.errorCode +
                            " MESSAGE: " +
                            response.errorMessage
            )
            if (isReverse) {
                watcher.reverseDebug(
                        "sendTransaction ERRORCODE: " +
                                response.errorCode +
                                " MESSAGE: " +
                                response.errorMessage
                )
            }
            response.errorCode = webException.errorCode
            response.errorMessage = webException.errorMessage
        }
        watcher.debug(
                "sendTransaction ERRORCODE: " +
                        response.errorCode +
                        " MESSAGE: " +
                        response.errorMessage
        )

        if (isReverse) {
            watcher.reverseDebug(
                    "sendTransaction ERRORCODE: " +
                            response.errorCode +
                            " MESSAGE: " +
                            response.errorMessage
            )
        }

        val isoRes = response.getIsoResponseASCII()
        if (isoRes.isNotBlank()) {
            watcher.debug("parseIsoHeaderError_isoRes  [$isoRes]")
            val (errorCodeHeader, errorMessageHeader) = StringUtils.parseIsoHeaderError(isoRes)
            watcher.debug(
                    "errorCodeHeader [$errorCodeHeader]  errorMessageHeader [$errorMessageHeader]"
            )
            if (errorCodeHeader != ErrorCode.Approved) {
                response.apply {
                    errorCode = errorCodeHeader.errorCodeInt
                    errorMessage = "${errorCodeHeader.errorCodeMessage} $errorMessageHeader"
                    errorCodeEnum = errorCodeHeader
                }

                response.transactionResponse?.apply {
                    errorCode = errorCodeHeader.errorCodeInt
                    responseCode = errorCodeHeader.responseCode
                    errorMessage = "${errorCodeHeader.errorCodeMessage} $errorMessageHeader"
                    errorCodeEnum = errorCodeHeader
                }
            }
        }

        //  println("SAVING webResponse")
        saveTransaction(response)
        val newRes = TransactionFullResponseDto(response)

        try {
            watcher.debug("sendTransaction_StringUtils_maskEMVTags")
            if (isReverse) {
                watcher.reverseDebug("sendTransaction_StringUtils_maskEMVTags") 
            }

            newRes.transactionResponse.emvTagsMap =
                    StringUtils.maskEMVTags(
                            dto.terminalSerialNumber,
                            newRes.transactionResponse.hostType,
                            newRes.transactionResponse.emvTagsMap,
                            watcher
                    )
        } catch (e: Exception) {
            watcher.debug("dbData.transactionDto!!.emvTagsMap ex => ${e.message}")
        }
        try {
            watcher.debug("   StringUtils.removeEmvDataSensitive")
            newRes.transactionResponse.emvData =
                    StringUtils.removeEmvDataSensitive(
                            newRes.transactionResponse.hostType,
                            newRes.transactionResponse.emvData,
                            watcher
                    )
        } catch (e: Exception) {
            //  dbData.transactionDto!!.emvData = ""
            watcher.debug("removeEmvDataSensitive ex => ${e.message}")
        }

        watcher.debug("newRes=>")
        watcher.debugObj(newRes)
        watcher.debug("authorizationIdResponse [${authorizationIdResponse}]")
        if (isReverse) {
            watcher.reverseDebug("newRes=>")
            watcher.reverseDebug("authorizationIdResponse [${authorizationIdResponse}]")
        }
        if (authorizationIdResponse != "" &&
                        newRes.transactionResponse != null &&
                        newRes.transactionResponse!!.authorizationIdResponse == ""
        ) {
            watcher.debug("dto.authorizationIdResponse IS EMPTY ")
            if (isReverse) {
                watcher.reverseDebug("dto.authorizationIdResponse IS EMPTY ")
            }
            if (originalTxn != null)
                    newRes.transactionResponse!!.authorizationIdResponse = authorizationIdResponse
            if (authorizationIdResponse != "")
                    newRes.transactionResponse!!.authorizationIdResponse = authorizationIdResponse
            if (isReverse) {
                watcher.reverseDebug(
                        " newRes.authorizationIdResponse[${ newRes.transactionResponse!!.authorizationIdResponse}] "
                )
            }
            watcher.debug(
                    " newRes.authorizationIdResponse[${ newRes.transactionResponse!!.authorizationIdResponse}] "
            )
        }

        //  StringUtils.copyNonEmptyProperties(dto,  newRes.transactionResponse)
        return newRes
    }

    fun processReadCard(dto: TransactionTerminalDto): TransactionFullResponseDto {
        if (dto.hostType.authorizer == Authorizer.OPENPAY) {
            watcher.debug("processReadCard  dto.hostType.authorizer ${dto.hostType.authorizer}")
            if (dto.keyId == "" &&
                            (dto.transactionType == TransactionType.KeyLoad ||
                                    dto.transactionType == TransactionType.KeyLoadConfirm)
            ) {
                dto.keyId = ""
            }
            // else dto.keyId1 = "A000OPFT48"
            watcher.debug(" sendTransaction_keyId [${ dto.keyId}]")
        }
        var response = TransactionResponse(null)

        watcher.debug("sendTransaction [0] dto.uuid ${dto.uuid}")

        watcher.debug("sendTransaction")
        dto.rrn = DynamicISOFields.getNewRRN(dto, dto.hostType)
        //   val originalDto= TransactionTerminalDto()
        //   BeanUtils.copyProperties(dto,originalDto)

        watcher.debug("TransactionTerminalDto.noSkipCRC32 ${dto.noSkipCRC32}")
        watcher.debug("TransactionTerminalDto.transactionType ${dto.transactionType}")
        watcher.debug(
                "TransactionTerminalDto.transactionMessageType ${dto.transactionType.transactionMessageType}"
        )
        watcher.info("BEFORE  watcher.webOriginalAndBaseRequest")

        watcher.webOriginalAndBaseRequest(dto)
        watcher.info("AFTER  watcher.webOriginalAndBaseRequest")
        watcher.debugObj(dto)
        watcher.debug("BEFORE findOriginalTransaction ")
        var originalTxn: TransactionDBData? = null
        var requestDtoNew = DynamicISOFields.checkRequest(dto, originalTxn)

        requestDtoNew.systemIdentification = dto.systemIdentification
        requestDtoNew.companyIdentification = dto.companyIdentification
        requestDtoNew.posIdentification = dto.posIdentification
        requestDtoNew.branchIdentification = dto.branchIdentification

        watcher.debug("requestDtoNew.systemIdentification [${requestDtoNew.systemIdentification}]")
        watcher.debug(
                "requestDtoNew.companyIdentification [${requestDtoNew.companyIdentification}]"
        )
        watcher.debug("requestDtoNew.posIdentification [${requestDtoNew.posIdentification}]")
        watcher.debug("requestDtoNew.branchIdentification [${requestDtoNew.branchIdentification}]")

        try {

            watcher.debug("BEFORE checkEmvData")

            requestDtoNew = DynamicISOFields.checkEmvData(requestDtoNew)
            watcher.debug("BEFORE checkAcquirerData")
            watcher.debug("sendTransaction [2] requestDtoNew.uuid ${dto.uuid}")

            requestDtoNew = DynamicISOFields.checkAcquirerData(requestDtoNew)

            watcher.debug("AFTER checkAcquirerData")
            watcher.debug("sendTransaction [3] requestDtoNew.uuid ${dto.uuid}")

            // watcher.webRequest(requestDtoNew)

            if (requestDtoNew.errorCode != 0) {
                response = TransactionResponse(requestDtoNew)
            } else {}
            if (response.transactionRequest != null) {
                watcher.webRequestSentHost(response.transactionRequest!!)
            }
            watcher.debug(
                    "============================PROCESS_TRANSACTI0N END ==================================\n\n\n"
            )
        } catch (webException: WebServiceException) {
            watcher.error(
                    "sendTransaction ERRORCODE: " +
                            response.errorCode +
                            " MESSAGE: " +
                            response.errorMessage
            )

            response.errorCode = webException.errorCode
            response.errorMessage = webException.errorMessage
        }
        watcher.debug(
                "sendTransaction ERRORCODE: " +
                        response.errorCode +
                        " MESSAGE: " +
                        response.errorMessage
        )

        //  println("SAVING webResponse")
        saveTransaction(response)

        val newRes = TransactionFullResponseDto(response)
        newRes.transactionResponse = requestDtoNew
        watcher.debug(
                "newRes.transactionResponse.emvTagsMap.size: ${newRes.transactionResponse.emvTagsMap.size}"
        )

        try {
            watcher.debug("sendTransaction_StringUtils_maskEMVTags")
            newRes.transactionResponse.emvTagsMap =
                    StringUtils.maskEMVTags(
                            dto.terminalSerialNumber,
                            newRes.transactionResponse.hostType,
                            newRes.transactionResponse.emvTagsMap,
                            watcher
                    )

            newRes.errorMessage = ErrorCode.Approved.errorCodeMessage
            newRes.errorCode = ErrorCode.Approved.errorCodeInt
            newRes.errorCodeEnum = ErrorCode.Approved
        } catch (e: Exception) {
            watcher.debug("dbData.transactionDto!!.emvTagsMap ex => ${e.message}")
        }
        try {
            watcher.debug("   StringUtils.removeEmvDataSensitive")
            newRes.transactionResponse.emvData =
                    StringUtils.removeEmvDataSensitive(
                            newRes.transactionResponse.hostType,
                            newRes.transactionResponse.emvData,
                            watcher
                    )
        } catch (e: Exception) {
            //  dbData.transactionDto!!.emvData = ""
            watcher.debug("removeEmvDataSensitive ex => ${e.message}")
        }

        watcher.debug("newRes=>")
        watcher.debugObj(newRes)
        newRes.errorMessage = ErrorCode.Approved.errorCodeMessage
        newRes.errorCode = ErrorCode.Approved.errorCodeInt
        newRes.errorCodeEnum = ErrorCode.Approved

        return newRes
    }

    fun processReverse(dto: TransactionTerminalDto): TransactionFullResponseDto {
        var rrn = dto.rrn

        if (dto.originalRrn != "") {
            watcher.debug(" processReverse_originalRrn : $rrn ")
            rrn = dto.originalRrn
        }
        watcher.debug(" DEVICE_TYPE_processReverse : ${dto.deviceType} ")

        val terminalSerialNumber = dto.terminalSerialNumber
        val hostType = dto.hostType
        val transactionDto =
                TransactionDto().apply { StringUtils.copyNonEmptyProperties(dto, this) }
        dto.cardNumber = StringUtils.maskCardNumber(dto.cardNumber)

        var response = TransactionResponse(transactionDto)
        watcher.debug(
                "Processing reverse for reverse_originalRrn ${dto.originalRrn} | rrn=$rrn, terminalSerialNumber=$terminalSerialNumber, hostType=$hostType"
        )
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                "Processing reverse for rrn=$rrn, terminalSerialNumber=$terminalSerialNumber, hostType=$hostType"
        )

        val reverseDb: TransactionDBData =
                TransactionDBManager.findTxn(rrn, terminalSerialNumber, hostType, watcher)
                        ?: run {
                            watcher.debug("reverseDb IS_NULL")
                            response.errorCode = ErrorCode.TransactionDoesntExist.errorCodeInt
                            response.errorMessage =
                                    ErrorCode.TransactionDoesntExist.errorCodeMessage
                            return TransactionFullResponseDto(response)
                        }

        dto.uuid = StringUtils.generateUUID()
        val requestUUID = dto.uuid
        reverseDb.uuid = StringUtils.hexStringToByteArrayNeo(requestUUID)
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                "Processing reverse for rrn=$rrn, terminalSerialNumber=$terminalSerialNumber, hostType=$hostType"
        )

        val requestResponseCode = reverseDb.responseCode
        watcher.debug(
                "Processing transactionId=${reverseDb.transactionId} requestUUID=[${requestUUID}]"
        )
        val transactionId = reverseDb.transactionId
        watcher.debug("Processing transactionId=${reverseDb.transactionId}, rrn=${reverseDb.rrn}")
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                "Processing transactionId=${reverseDb.transactionId}, rrn=${reverseDb.rrn}"
        )
        TransactionDBManager.updateTransactionStatus(
                transactionId,
                TransactionStatus.WaitingForResponse
        )
        watcher.debug("Updated transactionId=${reverseDb.transactionId} to WaitingForResponse")
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                "Updated transactionId=${reverseDb.transactionId} to WaitingForResponse"
        )
        val reverseDto = reverseDb.buildIso8583Reverse(watcher)

        val affiliation = reverseDb.affiliation

        val isoMetaData = reverseDto.iso8583MetaData
        response = TransactionResponse(reverseDto.transactionDto)
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                " reverseDto.reverseDbData.uuid =${ reverseDto.reverseDbData.uuid}, requestUUID=${requestUUID}"
        )
        reverseDto.reverseDbData.uuid = StringUtils.hexStringToByteArrayNeo(requestUUID)
        reverseDto.transactionDto.uuid = requestUUID
        reverseDto.transactionData.uuid = requestUUID
        reverseDto.iso8583MetaData.guid = StringUtils.hexStr2UUID(requestUUID)
        reverseDto.iso8583MetaData.setUUID(requestUUID)

        // reverseDto.uuid= StringUtils.hexStringToByteArrayNeo( requestUUID)

        var headerIso = ""
        var responseCode = ""
        var responseCodeOrg = reverseDb.responseCode
        var rrnOrg = reverseDb.rrn
        var termSerOrg = reverseDb.terminalSerialNumber
        var txnAmount = reverseDb.transactionAmount
        val hostSettings = AppSettings.getHostConfig(hostType)
        val errorCodeSimulated = hostSettings.getErrorCodeForTransaction(TransactionType.Reverse)

        val sendHardcode = hostSettings.hostResponseHardcode || affiliation == "0000000"

        TransactionDBManager.insertTransactionData(watcher, reverseDto.reverseDbData)

        try {
            reverseDto.reverseDbData.isoASCIIRequest =
                    StringUtils.asciiToByteArray(isoMetaData.isoAscii)
            watcher.debug(" reverseDto.reverseDbData.isoASCIIRequest ${isoMetaData.isoAscii}")
            WatcherGlobalLog.reverseDebugRrn(
                    rrn,
                    " reverseDto.reverseDbData.isoASCIIRequest ${isoMetaData.isoAscii}"
            )
        } catch (ex: Exception) {
            watcher.debug("EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")
            WatcherGlobalLog.reverseDebugRrn(rrn, "EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")
        }
        if (isoMetaData.errorCode != 0) {
            val errorMsg =
                    "ERROR BUILDING REVERSE for transactionId=${reverseDb.transactionId}: ${isoMetaData.errorMessage}"
            watcher.debug(errorMsg)
            WatcherGlobalLog.reverseDebugRrn(rrn, errorMsg)
            reverseDto.reverseDbData.apply {
                errorCode = ErrorCode.fromId(isoMetaData.errorCode)
                errorCodeNumber = isoMetaData.errorCode

                errorMessage = isoMetaData.errorMessage
            }

            reverseDto.reverseDbData.responseCode =
                    ErrorCode.fromId(isoMetaData.errorCode).responseCode
            AlarmEmailService.sendAlarmEmail(
                    AlarmCause.REVERSE_ERROR,
                    "requestUUID=${requestUUID} ERROR BUILDING REVERSE for transactionId=${reverseDb.transactionId}: ${isoMetaData.errorMessage}"
            )
        } else {

            if (sendHardcode) {
                responseCode = errorCodeSimulated.responseCode
                if (responseCode == "") {
                    responseCode = "00"
                }
                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "REVERSE_SIMULATED responseCode : [${responseCode}]"
                )

                if (errorCodeSimulated == ErrorCode.Approved) {
                    TransactionDBManager.updateTransactionStatus(
                            transactionId,
                            TransactionStatus.Reversed
                    )
                    reverseDto.reverseDbData.transactionStatus = TransactionStatus.Approved
                    watcher.debug(
                            "REVERSE_SIMULATED TransactionId=${reverseDb.transactionId} successfully reversed"
                    )
                    WatcherGlobalLog.reverseDebugRrn(
                            rrn,
                            "REVERSE_SIMULATED  affiliation [${affiliation}] TransactionId=${reverseDb.transactionId} successfully reversed"
                    )
                } else {

                    TransactionDBManager.updateTransactionStatus(
                            transactionId,
                            TransactionStatus.MustBeReversed
                    )
                    AlarmEmailService.sendAlarmEmail(
                            AlarmCause.COMM_WARN,
                            "REVERSE_SIMULATED ERROR:   rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] affiliation [${affiliation}] TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage} errorCode: ${response.errorCode} resascii: [${response.getIsoRequestASCII()}]"
                    )
                    if (response.getIsoRequestASCII() == "" || response.errorCode == -1) {
                        reverseDto.reverseDbData.transactionStatus = TransactionStatus.Approved
                    } else {
                        reverseDto.reverseDbData.transactionStatus = TransactionStatus.Rejected
                    }

                    watcher.debug(
                            "REVERSE_SIMULATED ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage} errorCode: ${response.errorCode} resascii: ${response.getIsoRequestASCII()}"
                    )
                    WatcherGlobalLog.reverseDebugRrn(
                            rrn,
                            "REVERSE_SIMULATED ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}"
                    )
                }
                response.errorCodeEnum = ErrorCode.fromResponseCode(responseCode)
                response.errorCode = response.errorCodeEnum.errorCodeInt
                response.errorMessage = response.errorCodeEnum.errorCodeMessage

                reverseDto.reverseDbData.apply {
                    errorCode = response.errorCodeEnum
                    errorCodeNumber = response.errorCode
                    errorMessage = response.errorMessage
                    responseCode = responseCode
                }
                TransactionDBManager.insertTransactionData(watcher, reverseDto.reverseDbData)

                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "REVERSE_SIMULATED :  reverseDto.reverseDbData=${GsonConfig.gson.toJson(reverseDto.reverseDbData)} "
                )

                return TransactionFullResponseDto(response)
            }

            try {

                watcher.debug(
                        "Sending ISO 8583 request for transactionId=${reverseDb.transactionId}"
                )
                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "Sending ISO 8583 request for transactionId=${reverseDb.transactionId}"
                )
                val responseCode1 = response.getResponseCode()
                response =
                        HostMasterManager.sendIsoAndWaitForResponse(hostType, isoMetaData, watcher)
                WatcherGlobalLog.reverseDebugRrn(rrn, "AFTER_sendIsoAndWaitForResponse ")
                reverseDto.reverseDbData.isoASCIIResponse =
                        StringUtils.asciiToByteArray("NO RESPONSE [" + responseCode1 + "]")
                if (response.getIsoRequestASCII() != "") {
                    reverseDto.reverseDbData.isoASCIIRequest =
                            StringUtils.asciiToByteArray(response.getIsoRequestASCII())
                    watcher.debug(
                            " reverseDto.reverseDbData.getIsoRequestASCII ${response.getIsoRequestASCII()}"
                    )
                    WatcherGlobalLog.reverseDebugRrn(
                            rrn,
                            " reverseDto.reverseDbData.getIsoRequestASCII ${response.getIsoRequestASCII()}"
                    )
                }

                if (response.getIsoResponseASCII() != "") {
                    var iscorespascii =
                            response.getIsoResponseASCII()
                                    .replace("ISO0260000501111", "ISO0260000509420")
                                    .replace("ISO0260038501111", "ISO0260038509420")
                                    .replace("ISO0260039501111", "ISO0260039509420")
                    reverseDto.reverseDbData.isoASCIIResponse =
                            StringUtils.asciiToByteArray(iscorespascii)
                    watcher.debug(
                            " reverseDto.reverseDbData.isoASCIIResponse ${response.getIsoResponseASCII()}"
                    )
                    WatcherGlobalLog.reverseDebugRrn(
                            rrn,
                            " reverseDto.reverseDbData.isoASCIIResponse ${response.getIsoResponseASCII()}"
                    )
                }

                if (response.getIsoResponseASCII() != "" &&
                                !response.getIsoResponseASCII().contains("0430")
                ) {
                    // reverseDto.reverseDbData.isoASCIIRequest=ByteArray(0)
                    reverseDto.reverseDbData.cardNumber =
                            StringUtils.maskCardNumber(reverseDto.reverseDbData.cardNumber)
                    val responseCode = "HA"
                    response.errorCodeEnum = ErrorCode.fromResponseCode(responseCode)
                    response.errorCode = response.errorCodeEnum.errorCodeInt
                    headerIso = response.getIsoResponseASCII().take(12)

                    TransactionDBManager.updateTransactionStatus(
                            transactionId,
                            TransactionStatus.Rejected
                    )
                    reverseDto.reverseDbData.transactionStatus = TransactionStatus.Rejected

                    if (response.transactionResponse != null) {
                        response.transactionResponse!!.responseCode = responseCode
                        responseCodeOrg = responseCode
                        rrnOrg = response.transactionResponse!!.rrn
                        termSerOrg = response.transactionResponse!!.terminalSerialNumber
                    }
                    response.errorMessage =
                            response.errorCodeEnum.errorCodeMessage +
                                    "   headerIso [${headerIso}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}]"

                    AlarmEmailService.sendAlarmEmail(
                            AlarmCause.REVERSE_ERROR,
                            "requestUUID=${requestUUID} log_path [${watcher.getLogsOpPath()}] RESPONSE_CODE_ORIGINAL [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] \n ISO_ASCII [${response.getIsoResponseASCII()}]  "
                    )

                    reverseDto.reverseDbData.errorCode = ErrorCode.fromResponseCode(responseCode)
                    reverseDto.reverseDbData.errorCodeNumber = response.errorCode
                    reverseDto.reverseDbData.errorMessage = response.errorMessage
                    reverseDto.reverseDbData.responseCode = responseCode
                    TransactionDBManager.insertTransactionData(watcher, reverseDto.reverseDbData)
                    return TransactionFullResponseDto(response)
                }
            } catch (ex: Exception) {
                val err =
                        "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

                watcher.debug("EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message}")
                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "EXCEPTION IN REVERSE_ISO_REQUEST: ${ex.message} [${err}]"
                )
                AlarmEmailService.sendAlarmEmail(
                        AlarmCause.REVERSE_ERROR,
                        " log_path [${watcher.getLogsOpPath()}]  requestUUID=${requestUUID} EXCEPTION   [${err}] transactionAmount [${txnAmount}]  rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}]  affiliation [${affiliation}] IN REVERSE_ISO_REQUEST: ${ex.message}  "
                ) 
            }

            responseCode = response.getResponseCode()
            if (responseCode == "") {
                responseCode = "HB"
            }
            response.errorCodeEnum = ErrorCode.fromResponseCode(responseCode)
            response.errorCode = response.errorCodeEnum.errorCodeInt
            response.errorMessage = response.errorCodeEnum.errorCodeMessage

            watcher.debug(
                    "REVERSE RESPONSE for responseCode =${responseCode}, transactionId=${reverseDb.transactionId}: errorCode=${response.errorCode}, errorMessage=${response.errorMessage}, isOk=${response.isOk()}"
            )
            WatcherGlobalLog.reverseDebugRrn(
                    rrn,
                    "REVERSE RESPONSE for   rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] affiliation [${affiliation}] responseCode =${responseCode}, transactionId=${reverseDb.transactionId}: errorCode=${response.errorCode}, errorMessage=${response.errorMessage}, isOk=${response.isOk()}"
            )

            if (response.getIsoResponseASCII() != "" && responseCode == "00") {
                TransactionDBManager.updateTransactionStatus(
                        transactionId,
                        TransactionStatus.Reversed
                )
                reverseDto.reverseDbData.transactionStatus = TransactionStatus.Approved
                watcher.debug("TransactionId=${reverseDb.transactionId} successfully reversed")
                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "TransactionId=${reverseDb.transactionId} successfully reversed"
                )
            } else {
                TransactionDBManager.updateTransactionStatus(
                        transactionId,
                        TransactionStatus.MustBeReversed
                )
                AlarmEmailService.sendAlarmEmail(
                        AlarmCause.COMM_WARN,
                        "ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}  resascii: ${response.getIsoRequestASCII()} errorCode: ${response.errorCode} resascii: [${response.getIsoRequestASCII()}]"
                )
                /*if(response.getIsoRequestASCII()==""||response.errorCode == -1){
                }else{
                 reverseDto.reverseDbData.transactionStatus = TransactionStatus.Rejected
                }  */
                reverseDto.reverseDbData.transactionStatus = TransactionStatus.Rejected

                AlarmEmailService.sendAlarmEmail(
                        AlarmCause.REVERSE_ERROR,
                        " requestUUID=${requestUUID} GSON[${GsonConfig.gson.toJson(reverseDto.reverseDbData) }]  resascii: ${response.getIsoRequestASCII()}  RESPONSE_CODE_ORIGINAL  responseCode [${responseCode}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] affiliation [${affiliation}] [${responseCodeOrg}] rrn [${rrnOrg}] terminalSerialNumber [${termSerOrg}] HEADER_ISO [${headerIso}] \n ISO_ASCII [${response.getIsoResponseASCII()}]  "
                )

                watcher.debug(
                        "ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage} errorCode: ${response.errorCode} resascii: ${response.getIsoRequestASCII()}"
                )
                WatcherGlobalLog.reverseDebugRrn(
                        rrn,
                        "ERROR: TransactionId=${reverseDb.transactionId} could not be reversed: ${response.errorMessage}"
                )
            }

            reverseDto.reverseDbData.errorCode = ErrorCode.fromResponseCode(responseCode)
            reverseDto.reverseDbData.errorCodeNumber =
                    reverseDto.reverseDbData.errorCode.errorCodeInt

            reverseDto.reverseDbData.errorMessage =
                    reverseDto.reverseDbData.errorCode.errorCodeMessage +
                            " EXTERNAL_REVERSE  headerIso [$headerIso] requestResponseCode [${requestResponseCode}] response_responseCode [${responseCode}]"
            reverseDto.reverseDbData.responseCode = responseCode

            watcher.debug("Updated reverse data for transactionId=${reverseDb.transactionId}")
            WatcherGlobalLog.reverseDebugRrn(
                    rrn,
                    "Updated reverse data for transactionId=${reverseDb.transactionId}"
            )
        }
        reverseDto.reverseDbData.cardNumber =
                StringUtils.maskCardNumber(reverseDto.reverseDbData.cardNumber)

        watcher.debug("reverseDb_uuiid =${StringUtils.encodeHexString(reverseDb.uuid)}")
        reverseDb.uuid = StringUtils.hexStringToByteArrayNeo(requestUUID)

        TransactionDBManager.insertTransactionDataEnd(watcher, reverseDto.reverseDbData)
        watcher.debug("Saved updated transaction for transactionId=${reverseDb.transactionId}")
        WatcherGlobalLog.reverseDebugRrn(
                rrn,
                "Saved updated transaction for transactionId=${reverseDb.transactionId}"
        )

        return TransactionFullResponseDto(response)
    }

    fun reverse(dto: TransactionDto): TransactionFullResponseDto {
        var response = TransactionResponse(dto)
        val requestDtoNew = DynamicISOFields.checkRequest(dto)

        try {

            response = TransactionManager().processReverse(requestDtoNew, merchantType)
        } catch (webException: WebServiceException) {
            response.errorCode = webException.errorCode
            response.errorMessage = webException.errorMessage
        }

        return TransactionFullResponseDto(response)
    }
}
