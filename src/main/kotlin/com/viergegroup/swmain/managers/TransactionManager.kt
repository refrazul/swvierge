package com.viergegroup.swmain.managers

import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.evopayment.managers.EvoManager
import com.viergegroup.isoprocessor.builder.IsoHostBuilder
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.CardBin
import com.viergegroup.swmain.entities.catalogs.CardType
import com.viergegroup.swmain.entities.catalogs.MerchantType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext

class TransactionManager {
    private lateinit var watcher: Watcher
    init {
        watcher = WatcherContext.getInstance()
    }

    fun processKeyLoadConfirm(
            dto: TransactionDto,
            merchantType: MerchantType
    ): TransactionResponse {
        dto.merchantType = merchantType
        val transactionResponse = TransactionResponse(TransactionDto())
        watcher.debug(
                "processKeyLoadConfirm ${dto.hostType} , ${dto.terminalSerialNumber} ,${dto.keyId}"
        )

        if (dto.terminalSerialNumber.equals("")) {
            transactionResponse.errorCode = ErrorCode.SerialIsEmpty.errorCodeInt
            transactionResponse.errorMessage = ErrorCode.SerialIsEmpty.errorCodeMessage
            return transactionResponse
        }
        if (dto.tokenEX.equals("")) {
            transactionResponse.errorCode = ErrorCode.TokeEXIsEmpty.errorCodeInt
            transactionResponse.errorMessage = ErrorCode.TokeEXIsEmpty.errorCodeMessage
            return transactionResponse
        }

        watcher.debug("EXECUTE PandoraneoManager().injectTokenEX")

        val injectEXResponse = PandoraneoManager().injectTokenEX(dto, watcher.getAuthToken())
        watcher.debug("AFTER PandoraneoManager().injectTokenEX")
        if (injectEXResponse.errorCode != 0) {
            watcher.debug(
                    "ERROR PandoraneoManager().injectTokenEX ${injectEXResponse.errorCode}, ${injectEXResponse.errorMessage}"
            )

            transactionResponse.errorCode = injectEXResponse.errorCode
            transactionResponse.errorMessage = injectEXResponse.errorMessage
        } else {
            transactionResponse.errorCode = 0
            transactionResponse.errorMessage = "keyId=> ${injectEXResponse.keyId}"
        }
        return transactionResponse
    }

    fun processKeyLoad(dto: TransactionDto, merchantType: MerchantType): TransactionResponse {
        dto.merchantType = merchantType
        watcher.debug("processKeyLoad")
        var transactionResponse = TransactionResponse()
        watcher.debug(
                "Host type ${dto.hostType} printDebugData ${AppSettings.getHostConfig(dto.hostType).printDebugData}"
        )
        watcher.debug(
                "///////////////////////  buildTokenEW INI ////////////////////////////////////////////"
        )
        var pandoraNeoManager = PandoraneoManager()
        watcher.debug("tokenEX=> ${dto.tokenEX}")

        watcher.debug(" TOKEN_EW:[${dto.tokenEW}] ")

        if (dto.tokenEW == "") {
            if (dto.randomTkCipherEW != "") {
                watcher.debug("HARDCODE_randomTkCipherEW ")

                //  transactionResponse.errorCode = 0
                // transactionResponse.errorMessage = ""
                transactionResponse.transactionResponse = TransactionDto()
                StringUtils.copyNonEmptyProperties(dto, transactionResponse.transactionResponse!!)
                watcher.debug("PRINT transactionResponse")
                watcher.debugObj(transactionResponse.transactionResponse!!)
                watcher.debug("dto.rsaVersionEW " + dto.rsaVersionEW)
                watcher.debug("dto.randomTkCipherEW " + dto.randomTkCipherEW)
                watcher.debug("dto.checkValueEW " + dto.checkValueEW)
                watcher.debug("dto.crc32EW " + dto.crc32EW)
                // return transactionResponse

            } else {

                watcher.debug("BEFORE pandoraNeoManager.buildTokenEW")
                val ewRes = pandoraNeoManager.buildTokenEW(dto)
                watcher.debug("AFTER pandoraNeoManager.buildTokenEW")
                watcher.debug("tk_clear  [" + ewRes.tkClear + "]")
                if (ewRes.errorCode != 0) {
                    watcher.debug("buildTokenEW ERROR: ${ewRes.errorCode} ${ewRes.errorMessage} ")
                    transactionResponse.errorCode = ewRes.errorCode
                    transactionResponse.errorMessage =
                            "PANDORA_ERROR processKeyLoad: " + ewRes.errorMessage
                    return transactionResponse
                }
                watcher.debug(
                        "///////////////////////  buildTokenEW END ////////////////////////////////////////////"
                )
                dto.rsaVersionEW = ewRes.rsaVersionEW
                dto.tkEW = ewRes.tkEW
                dto.randomTkCipherEW = ewRes.randomTkCipherEW
                dto.checkValueEW = ewRes.checkValueEW
                dto.crc32EW = ewRes.crc32EW
                dto.tokenEW = ewRes.tokenEW
            }
        } else {

            val ewRes = StringUtils.parseTokenEW(dto, watcher)
            dto.rsaVersionEW = ewRes.rsaVersionEW
            dto.tkEW = ewRes.tkEW
            dto.randomTkCipherEW = ewRes.randomTkCipherEW
            dto.checkValueEW = ewRes.checkValueEW
            dto.crc32EW = ewRes.crc32EW
            dto.tokenEW = ewRes.tokenEW
        }

        watcher.debug("dto.rsaVersionEW " + dto.rsaVersionEW)
        watcher.debug("dto.tkEW  [" + dto.tkEW + "]")
        watcher.debug("dto.randomTkCipherEW " + dto.randomTkCipherEW)
        watcher.debug("dto.checkValueEW " + dto.checkValueEW)
        watcher.debug("dto.crc32EW " + dto.crc32EW)
        val computeInternalKeys =
                AppSettings.getAuthorizerConfig(dto.hostType).authorizerId.computeInternalKeys
        watcher.debug("dto.tokenEW " + dto.tokenEW)
        val mustSendTxnToHost = AppSettings.getHostConfig(dto.hostType).mustSendTxnToHost
        watcher.debug(
                "computeInternalKeys [${computeInternalKeys}] mustSendTxnToHost [$mustSendTxnToHost] "
        )

        if (!mustSendTxnToHost) {
            transactionResponse.errorCode = 0
            transactionResponse.errorMessage = ""
            transactionResponse.transactionResponse = TransactionDto()
            StringUtils.copyNonEmptyProperties(dto, transactionResponse.transactionResponse!!)
            watcher.debug("PRINT transactionResponse")

            watcher.debugObj(transactionResponse.transactionResponse!!)
        }

        if (mustSendTxnToHost) {

            val isoMetaData = IsoHostBuilder.buildIso(TransactionType.KeyLoad, dto)
            transactionResponse = HostMasterManager.sendIsoAndWaitForResponse(isoMetaData, watcher)

            transactionResponse.setIsoResponseASCII(isoMetaData.isoAscii)
            watcher.debug("isoMetaData.isoAscii ${isoMetaData.isoAscii}")

            if (transactionResponse.transactionResponse == null) {
                transactionResponse.errorCode = ErrorCode.HostError.errorCodeInt
                transactionResponse.errorMessage =
                        "ERROR IN RESPONSE FROM HOST DIDN'T RECEIVE PEK  "
            }
            if (transactionResponse.transactionResponse != null && !computeInternalKeys) {
                val transResponse = transactionResponse.transactionResponse!!
                watcher.debug("processKeyLoad responseCode ${transResponse.responseCode}")
                watcher.debug("processKeyLoad ksnEX ${transResponse.ksnEX}")
                watcher.debug("processKeyLoad pekEX ${transResponse.pekEX}")
                watcher.debug("processKeyLoad tokenEX ${transResponse.tokenEX}")

                if (!dto.keyId.isEmpty()) {
                    dto.terminalSerialNumber = dto.terminalSerialNumber + dto.keyId
                }
                if (transResponse.ksnEX != "") {

                    watcher.debug(
                            "///////////////////////  injectTokenEX INI ////////////////////////////////////////////"
                    )

                    val injectTokenEx = PandoraTransactionDto()
                    StringUtils.copyNonEmptyProperties(dto, injectTokenEx)
                    injectTokenEx.tokenEW = ""
                    injectTokenEx.randomTkCipherEW = ""
                    injectTokenEx.randomTkCipherEW = ""

                    injectTokenEx.tkEW = transResponse.tkEW
                    watcher.debug(
                            "///////////////////////  injectTokenEX INI 2 ////////////////////////////////////////////"
                    )

                    injectTokenEx.pekEX = transResponse.pekEX
                    injectTokenEx.ksnEX = transResponse.ksnEX
                    injectTokenEx.checkValueEX = transResponse.checkValueEX
                    injectTokenEx.newKeyRequestStatusEX = transResponse.newKeyRequestStatusEX
                    injectTokenEx.crc32EX = transResponse.crc32EX
                    watcher.debug("injectTokenExObj")
                    watcher.debugObj(injectTokenEx)
                    watcher.debug("ipek_encrypted: [${injectTokenEx.pekEX}]")
                    watcher.debug("ksn: [${injectTokenEx.ksnEX}]")

                    watcher.debug("BEFORE PandoraneoManager().injectTokenEX")
                    val responseInject =
                            pandoraNeoManager.injectTokenEX(injectTokenEx, watcher.getAuthToken())
                    watcher.debug("AFTER PandoraneoManager().injectTokenEX")
                    if (responseInject.errorCode != 0) {
                        watcher.debug(
                                "injectTokenEX ERROR: ${responseInject.errorCode}, ${responseInject.errorMessage} "
                        )
                        if (AppSettings.getHostConfig(dto.hostType).printDebugData) {
                            watcher.debug("transResponse.pekEX ${transResponse.pekEX}")
                            watcher.debug("  injectDto.ksnEX ${  dto.ksnEX}")
                        }
                        transactionResponse.errorCode = responseInject.errorCode
                        transactionResponse.errorMessage =
                                "PANDORA_ERROR injectTokenEX: " + responseInject.errorMessage
                        transactionResponse.transactionResponse = null
                        transactionResponse.transactionRequest = null
                    } else {
                        watcher.debug(
                                "injectTokenEX OK: ${responseInject.errorCode}, ${responseInject.errorMessage} "
                        )
                    }

                    watcher.debug("tk_clear [${responseInject.tkClear}]")
                    //  watcher.debug("ipek_clear [${responseInject.ipek}]")
                    watcher.debug("ipek_clear [${responseInject.ipekClear}]")
                    // watcher.debug("ipek_clear [${responseInject.errorMessage}]")

                    watcher.debug("responseInject")
                    watcher.debugObj(responseInject)

                    watcher.debug(
                            "///////////////////////  injectTokenEX END ////////////////////////////////////////////"
                    )
                }
            }
        }

        return transactionResponse
    }

    fun processReverse(dto: TransactionDto, merchantType: MerchantType): TransactionResponse {
        dto.merchantType = merchantType
        val isoHostRaw = IsoHostBuilder.buildIso(TransactionType.Reverse, dto)
        return HostMasterManager.sendIsoAndWaitForResponse(isoHostRaw, watcher)
    }

    fun processTransaction(dtoOrg: TransactionDto): TransactionResponse {
        var response = TransactionResponse()
        val dto = processSensitiveData(dtoOrg)

 

        watcher.debug("PANDORA_get_CARDNUMBER_1 [${dto.cardNumber}]")
        watcher.debug("PANDORA_get_CARDBIN_1 ${dto.cardBin}")
 


        val mustSendTXN = AppSettings.getHostConfig(dto.hostType).mustSendTxnToHost
        watcher.debug(" mustSendTXN ${mustSendTXN}")
        if (dto.errorCode != 0) {
            response.transactionRequest = dto
            watcher.debug("processSensitiveData error ${dto.errorMessage}")
            response.errorCode = dto.errorCode
            response.errorMessage = dto.errorMessage
            return response
        }
        if (mustSendTXN) {

            // RDM EVOPAYMENT
            if (dto.hostType.usesIso8583) {
                val isoMetaDataRequest = IsoHostBuilder.buildIso(dto.transactionType, dto)
                if (isoMetaDataRequest.transactionData == null) {
                    watcher.debug("processTransaction  isoMetaDataRequest.transactionData IS_NULL")
                } else {
                    watcher.debug(
                            "processTransaction  isoMetaDataRequest.transactionData.hostType: " +
                                    isoMetaDataRequest.transactionData!!.hostType
                    )
                    watcher.debugObj(isoMetaDataRequest.transactionData!!)
                }

                watcher.debug(
                        " processTransaction errorCode: ${ isoMetaDataRequest.errorCode} msg: " +
                                isoMetaDataRequest.errorMessage
                )
                response = HostMasterManager.sendIsoAndWaitForResponse(isoMetaDataRequest, watcher)
                
       //  println("PANDORA_get_CARDNUMBER_2 [${dto.cardNumber}]")
       //    println("PANDORA_get_CARDBIN_2 ${dto.cardBin}")

                StringUtils.copyNonEmptyProperties(dto, response.transactionRequest!!)

                
                response.transactionRequest!!.cardNumber=dto.cardNumber
             response.transactionRequest!!.cardBin=dto.cardBin   

 

                watcher.debug("TRANSACTION_MANAGER sendIsoAndWaitForResponse dto.uuid  ${dto.uuid}")
                watcher.debug(
                        "TRANSACTION_MANAGER sendIsoAndWaitForResponse response.transactionRequest!!.uuid  ${response.transactionRequest!!.uuid}"
                )
 
                      
       //  println("PANDORA_get_CARDNUMBER_3 [${dto.cardNumber}]")
       //   println("PANDORA_get_CARDBIN_3 ${dto.cardBin}")


            } else {
                if (dto.hostType.authorizer == Authorizer.EVOPAYMENT) {
                    watcher.debug("INVOCAR FLUJO DE EVOPAYMENT=>EvoManager().processTransaction ")
                    return EvoManager().processTransaction(dto)
                } else {}
            }
        } else {
            watcher.debug("COPY DTO REQUEST AND RESPONSE")
            dto.errorCode = 0
            response.transactionRequest = TransactionDto()
            response.transactionResponse = TransactionDto()
            response.errorCode = 0
            dto.responseCode = "00"
            if (dto.rrn.length >= 6) dto.authorizationIdResponse = dto.rrn.substring(6)
            response.errorMessage = "SUCCESS OFFLINE"
            //  val newDto=StringUtils.removeSensitiveData(dto,watcher)
            // watcher.debug("TRANSACTION_MANAGER processTransaction newDto.uuid  ${newDto.uuid}")
             
        watcher.debug("PANDORA_get_CARDNUMBER_4 [${dto.cardNumber}]")
          watcher.debug("PANDORA_get_CARDBIN_4 ${dto.cardBin}")
            StringUtils.copyNonEmptyProperties(dto, response.transactionRequest!!)
     
 
            StringUtils.copyNonEmptyProperties(dto, response.transactionResponse!!)

             response.transactionResponse!!.cardNumber=dto.cardNumber
             response.transactionResponse!!.cardBin=dto.cardBin   


            watcher.debug("dto=>")
            watcher.debugObj(dto)
              
        watcher.debug("PANDORA_get_CARDNUMBER_5 [${dto.cardNumber}]")
          watcher.debug("PANDORA_get_CARDBIN_5 ${dto.cardBin}")
            // watcher.debugObj(response)

            watcher.debug(
                    "TRANSACTION_MANAGER processTransaction response.transactionRequest!!.uuid  ${response.transactionRequest!!.uuid}"
            )
            watcher.debug("SUCCESS OFFLINE")
        }

        return response
    }

    private fun processSensitiveData(dto: TransactionDto): TransactionDto {
        watcher.debug("processSensitiveData")
        watcher.debug(
                "processSensitiveData HOSTTYPE [${dto.hostType}] AUTHORIZER [${dto.hostType.authorizer}] "
        )

        if (dto.hostType.authorizer == Authorizer.EVOPAYMENT) {
            watcher.debug(" Iniciando flujo EVOPAYMENT: processSensitiveData")
            watcher.debug(" TRACK2 recibido: [${dto.track2}]")

            val request =
                    MyHsmDto.DUKPTRequest().apply {
                        ksn = dto.keySerialNumber // si ya lo tienes
                        terminalSerialNumber = dto.terminalSerialNumber
                        data = dto.track2
                        rrn = dto.rrn
                        hostType = dto.hostType
                    }

            watcher.debug(
                    " DUKPTRequest generado: serial=[${request.terminalSerialNumber}] ksn=[${request.ksn}], rrn=[${request.rrn}], hostType=[${request.hostType.name}]"
            )

            val myhsmres = PandoraneoManager().encryptWithDUKPT(request)
            watcher.debug(
                    "    - errorCode=[${myhsmres.errorCode}], errorMessage=[${myhsmres.errorMessage}]"
            )

            watcher.debug(" Respuesta de encryptWithDUKPT:")
            watcher.debug("    - cipherData=[${myhsmres.data}]")
            watcher.debug("    - lastPan4Digits=[${myhsmres.lastPan4Digits}]")
            watcher.debug("    - cipherCounter=[${myhsmres.cipherCounter}]")
            watcher.debug("    - ksn=[${myhsmres.ksn}]")
            watcher.debug("    - crc32=[${myhsmres.crc32}]")

            dto.keySerialNumber = myhsmres.ksn
            dto.cipherCounter = myhsmres.cipherCounter
            dto.lastPan4Digits = myhsmres.lastPan4Digits
            dto.cipherData = myhsmres.data
            dto.crc32EZ = myhsmres.crc32 // este ya lo asignaste arriba también
            dto.cardNumber = myhsmres.cardNumber

            watcher.debug(" dto actualizado con datos de cifrado DUKPT")

            return dto
        }

        if (dto.hostType.authorizer == Authorizer.YOLOPAGO ||
                        dto.hostType.authorizer == Authorizer.CLAROPAY ||
                        dto.hostType.authorizer == Authorizer.PAYNET
        ) {
            watcher.debug(
                    "doing_YOLOPAGO_CIPHER SPECIAL rsaKeyId[${dto.hostType.authorizer.rsaKeyId}]"
            )
            watcher.debug(
                    "doing_PAYNET_CIPHER SPECIAL rsaKeyId[${dto.hostType.authorizer.rsaKeyId}]"
            )
            watcher.debug(" emvTagsMap=> [${dto.getEmvTagsMapStr()}]")
            return dto
        }

        val dataApplyToBuildTOKENEZ = dto.dataApplyToBuildTOKENEZ()
        watcher.debug(
                "coputecipher [${!dto.hostType.authorizer.computeInternalKeys}] dataApplyToBuildTOKENEZ [$dataApplyToBuildTOKENEZ]    [${dto.transactionType}/${dto.pointOfServiceEntryMode}/${dto.transactionDirection}] "
        )

        if (dataApplyToBuildTOKENEZ && !dto.hostType.authorizer.computeInternalKeys) {
            if (dto.track2 != "" && dto.terminalSerialNumber == "") {
                dto.errorCode = ErrorCode.InvalidFormatField.errorCodeInt
                dto.errorMessage = " terminalSerialNumber is mandatory when track2 is present"
                return dto
            }

            watcher.debug("EXECUTE PandoraneoManager(logger).encryptData  ")

            var resDukpt = PandoraneoManager().encryptData(dto) // "PANDORA_ERROR: "+
            watcher.debug("AFTER EXECUTE PandoraneoManager(logger).encryptData  ")

            if (resDukpt.errorCode != 0) {
                watcher.debug(
                        "computeInternalKeys [${dto.hostType.authorizer.computeInternalKeys}] "
                )

                if (dto.hostType.authorizer.computeInternalKeys) {
                    watcher.debug("EXECUTE PandoraneoManager().buildTokenEW ")
                    val computeKeysRes = PandoraneoManager().buildTokenEW(dto)
                    watcher.debug("AFTER EXECUTE PandoraneoManager().buildTokenEW ")
                    if (computeKeysRes.errorCode != 0) {
                        dto.errorMessage = "PANDORA_ERROR COMPUTING_KEYS: " + resDukpt.errorMessage
                        dto.errorCode = resDukpt.errorCode
                    } else {
                        watcher.debug("EXECUTE PandoraneoManager().encryptData")
                        resDukpt = PandoraneoManager().encryptData(dto)
                        watcher.debug("AFTER EXECUTE PandoraneoManager().encryptData")
                    }
                } else {
                    dto.errorMessage = "PANDORA_ERROR encryptData: " + resDukpt.errorMessage
                    dto.errorCode = resDukpt.errorCode
                    watcher.debug("ERROR FROM PANDORA ${dto.errorMessage}")
                    return dto
                }
            }

            watcher.debug("PANDORA_CHECK_CARDNUMBER [${dto.cardNumber}]")

            if (dto.cardNumber == "") {
                dto.cardNumber = resDukpt.cardNumber
                watcher.debug("PANDORA_get_CARDNUMBER [${dto.cardNumber}]")
                if (dto.cardNumber != "") {
                    dto.cardBin = dto.cardNumber.substring(0, 6).replace("*","0")  
                }  
                watcher.debug("PANDORA_get_CARDBIN ${dto.cardBin}")
                try{
                    dto.cardType = CardBin.getCardType(dto.cardBin.toLong())
                    if( dto.cardType ==CardType.Undefined){
                        dto.cardType = CardType.VISA 
                    }

                }catch(ex:Exception)
                {
                     watcher.debug("ex: ${ex.message} ")
 
                }
                dto.cardNumber=StringUtils.maskCardNumber(dto.cardNumber)
                watcher.debug("PANDORA_get_CARDType  ${dto.cardType}")
            } 

            dto.cipherData = resDukpt.cipherData
            dto.tokenEZ = resDukpt.tokenEZ
            dto.tokenEZ2 = resDukpt.tokenEZ2
            dto.tokenOpenPay = resDukpt.tokenOpenPay
            dto.tokenOpenPayClear = resDukpt.tokenOpenPayClear

            dto.crc32EZ = resDukpt.crc32EZ
            dto.keySerialNumber = resDukpt.keySerialNumber
            dto.failCipherCounter = resDukpt.failCipherCounter 
            dto.flagTrack2 = resDukpt.flagTrack2
            dto.track2Length = resDukpt.track2Length
            dto.cvvFlag = resDukpt.cvvFlag
            dto.track1Flag = resDukpt.track1Flag
            dto.cipherCounter = resDukpt.cipherCounter
            dto.clearCvvLength = resDukpt.clearCvvLength
            dto.lastPan4Digits = resDukpt.lastPan4Digits

            watcher.debug(
                    " dto.keySerialNumber [${ dto.keySerialNumber}] dto.cipherData [${ dto.cipherData}]   dto.crc32EZ [${ dto.crc32EZ}] dto.failCipherCounter [${dto.failCipherCounter}]   dto.track2Length [${ dto.track2Length}]"
            )
           // --- correccion 3

            if (dto.cardNumber == "") { 
                dto.cardNumber = resDukpt.cardNumber
                if (dto.cardNumber != "") {
                    
                    if (dto.cardNumber.length >= 4) { //los bines son de 4  
                        dto.cardBin = dto.cardNumber.substring(0, 4) 
                    } else {
                        dto.cardBin = dto.cardNumber
                    }
                }
                dto.cardType = CardBin.getCardType(dto, watcher)
            }

            watcher.debug(
                    " dto.keySerialNumber [${ dto.keySerialNumber}] dto.cipherData [${ dto.cipherData}]   dto.crc32EZ [${ dto.crc32EZ}] dto.failCipherCounter [${dto.failCipherCounter}]   dto.track2Length [${ dto.track2Length}]"
            )
            watcher.debug(" dto.track1Flag [${ dto.track1Flag}] dto.cvvFlag [${ dto.cvvFlag}]   ")
            watcher.debug(
                    "    dto.cipherCounter [${ dto.cipherCounter}] dto.clearCvvLength [${dto.clearCvvLength}]   dto.lastPan4Digits [${ dto.lastPan4Digits}]"
            )
        }

        dto.errorMessage = ""
        dto.errorCode = 0
        return dto
    }
}
