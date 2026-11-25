package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.isNullOrEmpty
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import java.io.IOException

class TransactionDtoGSONAdapter : TypeAdapter<TransactionDto?>() {
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: TransactionDto?) {
        outstr.beginObject()
        if (valueNull == null) return

        val value = valueNull
        outstr.name("errorCodeEnum")
        outstr.value(value.errorCodeEnum.name)
        outstr.name("errorCode")
        outstr.value(value.errorCode.toLong())

        if (!isNullOrEmpty(value.clientId)) {
            outstr.name("clientId").value(value.clientId)
        }
        if (!isNullOrEmpty(value.systemIdentification)) {
            outstr.name("systemIdentification").value(value.systemIdentification)
        }
        if (!isNullOrEmpty(value.companyIdentification)) {
            outstr.name("companyIdentification").value(value.companyIdentification)
        }
        if (!isNullOrEmpty(value.branchIdentification)) {
            outstr.name("branchIdentification").value(value.branchIdentification)
        }

        if (!isNullOrEmpty(value.posIdentification)) {
            outstr.name("posIdentification").value(value.posIdentification)
        }

        outstr.name("deviceType").value(value.deviceType.name)

        if (!isNullOrEmpty(value.processingCode)) {
            outstr.name("processingCode").value(value.processingCode)
        }

        if (!isNullOrEmpty(value.affiliation)) {
            outstr.name("affiliation").value(value.affiliation)
        }
        if (!isNullOrEmpty(value.fiid)) {
            outstr.name("fiid").value(value.fiid)
        }
        if (!isNullOrEmpty(value.merchantName)) {
            outstr.name("merchantName").value(value.merchantName)
        }

        if (!isNullOrEmpty(value.transactionIdMpp)) {
            outstr.name("transactionIdMpp").value(value.transactionIdMpp)
        }

        if (!isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }

        outstr.name("transactionStatus")
        outstr.value(value.transactionStatus.toString())

        outstr.name("paymentPlanType")
        outstr.value(value.paymentPlanType.toString())

        if (!isNullOrEmpty(value.clientIP)) {
            outstr.name("clientIP").value(value.clientIP)
        }
        if (!isNullOrEmpty(value.clientEmail)) {
            outstr.name("clientEmail").value(value.clientEmail)
        }
        if (!isNullOrEmpty(value.clientUserName)) {
            outstr.name("clientUserName").value(value.clientUserName)
        }
        if (!isNullOrEmpty(value.clientLocation)) {
            outstr.name("clientLocation").value(value.clientLocation)
        }
        if (!isNullOrEmpty(value.deviceModel)) {
            outstr.name("deviceModel").value(value.deviceModel)
        }
        if (!isNullOrEmpty(value.noSkipCRC32)) {
            outstr.name("noSkipCRC32").value(value.noSkipCRC32)
        }
        if (value.hasChip) {
            outstr.name("hasChip").value(value.hasChip)
        }
        if (!isNullOrEmpty(value.messageType)) {
            outstr.name("messageType").value(value.messageType)
        }
        if (!isNullOrEmpty(value.validateData)) {
            outstr.name("validateData").value(value.validateData)
        }
        if (!isNullOrEmpty(value.originalRrn)) {
            outstr.name("originalRrn").value(value.originalRrn)
        }
        if (!isNullOrEmpty(value.commercialReferences)) {
            outstr.name("commercialReferences").value(value.commercialReferences)
        }
        if (!isNullOrEmpty(value.serverName)) {
            outstr.name("serverName").value(value.serverName)
        }

        if (!isNullOrEmpty(value.merchantType.merchantTypeName)) {
            outstr.name("merchantType")
            outstr.value(value.merchantType.merchantTypeName)
        }
        if (!isNullOrEmpty(value.transactionType.transactionTypeName)) {
            outstr.name("transactionType")
            outstr.value(value.transactionType.transactionTypeName)
        }

        if (!isNullOrEmpty(value.pointOfServiceEntryMode.pointOfServiceEntryModeName)) {
            outstr.name("pointOfServiceEntryMode")
            outstr.value(value.pointOfServiceEntryMode.pointOfServiceEntryModeName)
        }

        if (!isNullOrEmpty(value.keyId)) {
            outstr.name("keyId")
            outstr.value(value.keyId)
        }
        if (!isNullOrEmpty(value.keyId1)) {
            outstr.name("keyId1")
            outstr.value(value.keyId1)
        }
        if (!isNullOrEmpty(value.keyId2)) {
            outstr.name("keyId2")
            outstr.value(value.keyId2)
        }
        if (!isNullOrEmpty(value.tokenEZ)) {
            outstr.name("tokenEZ")
            outstr.value(value.tokenEZ)
        }
        if (!isNullOrEmpty(value.tokenOpenPay)) {
            outstr.name("tokenOpenPay")
            outstr.value(value.tokenOpenPay)
        }
        if (!isNullOrEmpty(value.tokenEZ2)) {
            outstr.name("tokenEZ2")
            outstr.value(value.tokenEZ2)
        }
        if (!isNullOrEmpty(value.tokenEZFormated)) {
            outstr.name("tokenEZFormated")
            outstr.value(value.tokenEZFormated)
        }
        if (!isNullOrEmpty(value.tokenEZ2Formated)) {
            outstr.name("tokenEZ2Formated")
            outstr.value(value.tokenEZ2Formated)
        }

        if (!isNullOrEmpty(value.pointOfServiceEntryModeCode)) {
            outstr.name("pointOfServiceEntryModeCode")
            outstr.value(value.pointOfServiceEntryModeCode)
        }
        if (!isNullOrEmpty(value.hostType.hostTypeName)) {
            outstr.name("hostType")
            outstr.value(value.hostType.hostTypeName)
        }
        if (!isNullOrEmpty(value.processingCode)) {
            outstr.name("processingCode")
            outstr.value(value.processingCode)
        }
        if (!isNullOrEmpty(value.transactionAmount)) {
            outstr.name("transactionAmount")
            outstr.value(value.transactionAmount)
        }
        outstr.name("cardType")
        outstr.value(value.cardType.toString())

        if (!isNullOrEmpty(value.transmisionDateAndTime)) {
            outstr.name("transmisionDateAndTime")
            outstr.value(value.transmisionDateAndTime)
        }
        if (!isNullOrEmpty(value.systemTraceAuditNumber)) {
            outstr.name("systemTraceAuditNumber")
            outstr.value(value.systemTraceAuditNumber)
        }
        if (!isNullOrEmpty(value.localTransactionTime)) {
            outstr.name("localTransactionTime")
            outstr.value(value.localTransactionTime)
        }
        if (!isNullOrEmpty(value.localTransactionDate)) {
            outstr.name("localTransactionDate")
            outstr.value(value.localTransactionDate)
        }

        if (!isNullOrEmpty(value.settlementDate)) {
            outstr.name("settlementDate")
            outstr.value(value.settlementDate)
        }
        if (!isNullOrEmpty(value.captureDate)) {
            outstr.name("captureDate")
            outstr.value(value.captureDate)
        }
        if (!isNullOrEmpty(value.merchantTypeCode)) {
            outstr.name("merchantTypeCode")
            outstr.value(value.merchantTypeCode)
        }
        if (!isNullOrEmpty(value.merchantCurrencyCode)) {
            outstr.name("merchantCurrencyCode")
            outstr.value(value.merchantCurrencyCode)
        }
        if (!isNullOrEmpty(value.acquiringInstitutionIdentificationCode)) {
            outstr.name("acquiringInstitutionIdentificationCode")
            outstr.value(value.acquiringInstitutionIdentificationCode)
        }

        if (!isNullOrEmpty(value.pointOfServiceConditionCode)) {
            outstr.name("pointOfServiceConditionCode")
            outstr.value(value.pointOfServiceConditionCode)
        }
        if (!isNullOrEmpty(value.track2) && value.hostType.authorizer.sendClearData) {
            outstr.name("track2")
            outstr.value(value.track2)
        } else {

            outstr.name("track2")
            outstr.value(StringUtils.maskString(value.track2))
        }

        if (!isNullOrEmpty(value.cvv)) {
            outstr.name("cvv")
            outstr.value(value.cvv)
        }
        if (!isNullOrEmpty(value.rrn)) {
            outstr.name("rrn")
            outstr.value(value.rrn)
        }
        if (!isNullOrEmpty(value.authorizationIdResponse)) {
            outstr.name("authorizationIdResponse")
            outstr.value(value.authorizationIdResponse)
        }
        if (!isNullOrEmpty(value.responseCode)) {
            outstr.name("responseCode")
            outstr.value(value.responseCode)
        }
        if (!isNullOrEmpty(value.responseCodeAuthorizer)) {
            outstr.name("responseCodeAuthorizer")
            outstr.value(value.responseCodeAuthorizer)
        }

        if (!isNullOrEmpty(value.cardNumber)) {
            outstr.name("cardNumber")
            outstr.value(value.cardNumber)
        }
        if (!isNullOrEmpty(value.cardBin)) {
            outstr.name("cardBin")
            outstr.value(value.cardBin)
        }
        if (!isNullOrEmpty(value.cardAcceptorTerminalId)) {
            outstr.name("cardAcceptorTerminalId")
            outstr.value(value.cardAcceptorTerminalId)
        }
        if (!isNullOrEmpty(value.cardAcceptorIdCode)) {
            outstr.name("cardAcceptorIdCode")
            outstr.value(value.cardAcceptorIdCode)
        }
        if (!isNullOrEmpty(value.cardAcceptorNameAndLocation)) {
            outstr.name("cardAcceptorNameAndLocation")
            outstr.value(value.cardAcceptorNameAndLocation)
        }
        if (!isNullOrEmpty(value.additionalResponseDate)) {
            outstr.name("additionalResponseDate")
            outstr.value(value.additionalResponseDate)
        }
        if (!isNullOrEmpty(value.retailerData)) {
            outstr.name("retailerData")
            outstr.value(value.retailerData)
        }
        if (!isNullOrEmpty(value.transactionCurrencyCode)) {
            outstr.name("transactionCurrencyCode")
            outstr.value(value.transactionCurrencyCode)
        }
        if (!isNullOrEmpty(value.additionalAmounts)) {
            outstr.name("additionalAmounts")
            outstr.value(value.additionalAmounts)
        }
        if (!isNullOrEmpty(value.terminalData)) {
            outstr.name("terminalData")
            outstr.value(value.terminalData)
        }
        if (!isNullOrEmpty(value.cardIssuerCategoryResponseCodeData)) {
            outstr.name("cardIssuerCategoryResponseCodeData")
            outstr.value(value.cardIssuerCategoryResponseCodeData)
        }
        if (!isNullOrEmpty(value.postalCode)) {
            outstr.name("postalCode")
            outstr.value(value.postalCode)
        }
        if (!isNullOrEmpty(value.additionalData)) {
            outstr.name("additionalData")
            outstr.value(value.additionalData)
        }
        if (!isNullOrEmpty(value.dataElements)) {
            outstr.name("dataElements")
            outstr.value(value.dataElements)
        }
        if (!isNullOrEmpty(value.networkManagmentInformationCode)) {
            outstr.name("networkManagmentInformationCode")
            outstr.value(value.networkManagmentInformationCode)
        }
        if (!isNullOrEmpty(value.originalDataElements)) {
            outstr.name("originalDataElements")
            outstr.value(value.originalDataElements)
        }
        if (!isNullOrEmpty(value.replacementAmounts)) {
            outstr.name("replacementAmounts")
            outstr.value(value.replacementAmounts)
        }
        if (!isNullOrEmpty(value.receivingInstitutionIdCode)) {
            outstr.name("receivingInstitutionIdCode")
            outstr.value(value.receivingInstitutionIdCode)
        }
        if (!isNullOrEmpty(value.accountId)) {
            outstr.name("accountId")
            outstr.value(value.accountId)
        }
        if (!isNullOrEmpty(value.terminalAddressBranch)) {
            outstr.name("terminalAddressBranch")
            outstr.value(value.terminalAddressBranch)
        }

        if (!isNullOrEmpty(value.authorizationIndicators)) {
            outstr.name("authorizationIndicators")
            outstr.value(value.authorizationIndicators)
        }
        if (!isNullOrEmpty(value.cardIssuerCode)) {
            outstr.name("cardIssuerCode")
            outstr.value(value.cardIssuerCode)
        }
        if (!isNullOrEmpty(value.posInvoiceData)) {
            outstr.name("posInvoiceData")
            outstr.value(value.posInvoiceData)
        }
        if (!isNullOrEmpty(value.specialData)) {
            outstr.name("specialData")
            outstr.value(value.specialData)
        }
        if (!isNullOrEmpty(value.posSettlementData)) {
            outstr.name("posSettlementData")
            outstr.value(value.posSettlementData)
        }
        if (!isNullOrEmpty(value.posPreauthorizationAndChargeBackData)) {
            outstr.name("posPreauthorizationAndChargeBackData")
            outstr.value(value.posPreauthorizationAndChargeBackData)
        }
        if (!isNullOrEmpty(value.authorizationModeIdentifier)) {
            outstr.name("authorizationModeIdentifier")
            outstr.value(value.authorizationModeIdentifier)
        }
        if (!isNullOrEmpty(value.userFieldQ1)) {
            outstr.name("userFieldQ1")
            outstr.value(value.userFieldQ1)
        }
        if (!isNullOrEmpty(value.accessMediumIdentifier)) {
            outstr.name("accessMediumIdentifier")
            outstr.value(value.accessMediumIdentifier)
        }
        if (!isNullOrEmpty(value.deferral)) {
            outstr.name("deferral")
            outstr.value(value.deferral)
        }
        if (!isNullOrEmpty(value.numberOfPayments)) {
            outstr.name("numberOfPayments")
            outstr.value(value.numberOfPayments)
        }
        if (!isNullOrEmpty(value.paymentPlanCode)) {
            outstr.name("paymentPlanCode")
            outstr.value(value.paymentPlanCode)
        }

        if (!isNullOrEmpty(value.paymentQ6Data)) {
            outstr.name("paymentQ6Data")
            outstr.value(value.paymentQ6Data)
        }

        if (!isNullOrEmpty(value.additionalErrorFlagRequestInTransaction)) {
            outstr.name("additionalErrorFlagRequestInTransaction")
            outstr.value(value.additionalErrorFlagRequestInTransaction)
        }
        if (!isNullOrEmpty(value.routingCode)) {
            outstr.name("routstringCode")
            outstr.value(value.routingCode)
        }
        if (!isNullOrEmpty(value.cardVerificationFlag)) {
            outstr.name("cardVerificationFlag")
            outstr.value(value.cardVerificationFlag)
        }
        if (!isNullOrEmpty(value.cityExension)) {
            outstr.name("cityExension")
            outstr.value(value.cityExension)
        }
        if (!isNullOrEmpty(value.completeTrack2Data)) {
            outstr.name("completeTrack2Data")
            outstr.value(value.completeTrack2Data)
        }
        if (!isNullOrEmpty(value.fileFlagUses)) {
            outstr.name("fileFlagUses")
            outstr.value(value.fileFlagUses)
        }
        if (!isNullOrEmpty(value.cvv2cvc2Result)) {
            outstr.name("cvv2cvc2Result")
            outstr.value(value.cvv2cvc2Result)
        }
        if (!isNullOrEmpty(value.statusCodeOfRebroadcastTxn)) {
            outstr.name("statusCodeOfRebroadcastTxn")
            outstr.value(value.statusCodeOfRebroadcastTxn)
        }
        if (!isNullOrEmpty(value.counterOfRebroadcastTxn)) {
            outstr.name("counterOfRebroadcastTxn")
            outstr.value(value.counterOfRebroadcastTxn)
        }
        if (!isNullOrEmpty(value.merchantPostalCode)) {
            outstr.name("merchantPostalCode")
            outstr.value(value.merchantPostalCode)
        }
        if (!isNullOrEmpty(value.eCommerceIndicator)) {
            outstr.name("eCommerceIndicator")
            outstr.value(value.eCommerceIndicator)
        }
        if (!isNullOrEmpty(value.saleForcedOrSAF)) {
            outstr.name("saleForcedOrSAF")
            outstr.value(value.saleForcedOrSAF)
        }
        if (!isNullOrEmpty(value.cv2PresentIndicator)) {
            outstr.name("cv2PresentIndicator")
            outstr.value(value.cv2PresentIndicator)
        }
        if (!isNullOrEmpty(value.additionalInfoIndicatorC0)) {
            outstr.name("additionalInfoIndicatorC0")
            outstr.value(value.additionalInfoIndicatorC0)
        }
        if (!isNullOrEmpty(value.authenticationCollectorIndicator)) {
            outstr.name("authenticationCollectorIndicator")
            outstr.value(value.authenticationCollectorIndicator)
        }
        if (!isNullOrEmpty(value.propensityFlagTradeFraud)) {
            outstr.name("propensityFlagTradeFraud")
            outstr.value(value.propensityFlagTradeFraud)
        }
        if (!isNullOrEmpty(value.cavvAvvValidationResult)) {
            outstr.name("cavvAvvValidationResult")
            outstr.value(value.cavvAvvValidationResult)
        }
        if (!isNullOrEmpty(value.attendedTerminal)) {
            outstr.name("attendedTerminal")
            outstr.value(value.attendedTerminal)
        }
        if (!isNullOrEmpty(value.terminalOperationIndex)) {
            outstr.name("terminalOperationIndex")
            outstr.value(value.terminalOperationIndex)
        }
        if (!isNullOrEmpty(value.terminalLocator)) {
            outstr.name("terminalLocator")
            outstr.value(value.terminalLocator)
        }
        if (!isNullOrEmpty(value.cardholderPresenceIndicator)) {
            outstr.name("cardholderPresenceIndicator")
            outstr.value(value.cardholderPresenceIndicator)
        }
        if (!isNullOrEmpty(value.cardPresenceIndicator)) {
            outstr.name("cardPresenceIndicator")
            outstr.value(value.cardPresenceIndicator)
        }
        if (!isNullOrEmpty(value.cardCaptureCapacityIndicator)) {
            outstr.name("cardCaptureCapacityIndicator")
            outstr.value(value.cardCaptureCapacityIndicator)
        }
        if (!isNullOrEmpty(value.statusIndicatorC4)) {
            outstr.name("statusIndicatorC4")
            outstr.value(value.statusIndicatorC4)
        }
        if (!isNullOrEmpty(value.transactionSecurityLevel)) {
            outstr.name("transactionSecurityLevel")
            outstr.value(value.transactionSecurityLevel)
        }
        if (!isNullOrEmpty(value.routingIndicator)) {
            outstr.name("routstringIndicator")
            outstr.value(value.routingIndicator)
        }
        if (!isNullOrEmpty(value.terminalActivationByCardholder)) {
            outstr.name("terminalActivationByCardholder")
            outstr.value(value.terminalActivationByCardholder)
        }
        if (!isNullOrEmpty(value.transferDataIndicator)) {
            outstr.name("transferDataIndicator")
            outstr.value(value.transferDataIndicator)
        }
        if (!isNullOrEmpty(value.identificationMethodCardHolder)) {
            outstr.name("identificationMethodCardHolder")
            outstr.value(value.identificationMethodCardHolder)
        }
        if (!isNullOrEmpty(value.fillerC4)) {
            outstr.name("fillerC4")
            outstr.value(value.fillerC4)
        }
        if (!isNullOrEmpty(value.xid)) {
            outstr.name("xid")
            outstr.value(value.xid)
        }
        if (!isNullOrEmpty(value.cavvCardholderAuthVerValue)) {
            outstr.name("cavvCardholderAuthVerValue")
            outstr.value(value.cavvCardholderAuthVerValue)
        }
        if (!isNullOrEmpty(value.indicatorCE)) {
            outstr.name("indicatorCE")
            outstr.value(value.indicatorCE)
        }
        if (!isNullOrEmpty(value.cardholderAuthenticationData)) {
            outstr.name("cardholderAuthenticationData")
            outstr.value(value.cardholderAuthenticationData)
        }
        if (!isNullOrEmpty(value.atcCZ)) {
            outstr.name("atcCZ")
            outstr.value(value.atcCZ)
        }
        if (!isNullOrEmpty(value.formFactrInd)) {
            outstr.name("formFactrInd")
            outstr.value(value.formFactrInd)
        }
        if (!isNullOrEmpty(value.pekEX)) {
            outstr.name("pekEX")
            outstr.value(value.pekEX)
        }
        if (!isNullOrEmpty(value.ksnEX)) {
            outstr.name("ksnEX")
            outstr.value(value.ksnEX)
        }
        if (!isNullOrEmpty(value.checkValueEX)) {
            outstr.name("checkValueEX")
            outstr.value(value.checkValueEX)
        }
        if (!isNullOrEmpty(value.newKeyRequestStatusEX)) {
            outstr.name("newKeyRequestStatusEX")
            outstr.value(value.newKeyRequestStatusEX)
        }
        if (!isNullOrEmpty(value.crc32EX)) {
            outstr.name("crc32EX")
            outstr.value(value.crc32EX)
        }
        if (!isNullOrEmpty(value.tkEW)) {
            outstr.name("tkEW")
            outstr.value(value.tkEW)
        }
        if (!isNullOrEmpty(value.randomTkCipherEW)) {
            outstr.name("randomTkCipherEW")
            outstr.value(value.randomTkCipherEW)
        }
        if (!isNullOrEmpty(value.checkValueEW)) {
            outstr.name("checkValueEW")
            outstr.value(value.checkValueEW)
        }
        if (!isNullOrEmpty(value.rsaVersionEW)) {
            outstr.name("rsaVersionEW")
            outstr.value(value.rsaVersionEW)
        }
        if (!isNullOrEmpty(value.rsaPaddingEW)) {
            outstr.name("rsaPaddingEW")
            outstr.value(value.rsaPaddingEW)
        }
        if (!isNullOrEmpty(value.rsaVersionEW)) {
            outstr.name("rsaVersionEW")
            outstr.value(value.rsaVersionEW)
        }
        if (!isNullOrEmpty(value.crc32EW)) {
            outstr.name("crc32EW")
            outstr.value(value.crc32EW)
        }
        if (!isNullOrEmpty(value.terminalSoftwareVersion)) {
            outstr.name("terminalSoftwareVersion")
            outstr.value(value.terminalSoftwareVersion)
        }
        if (!isNullOrEmpty(value.terminalSerialNumber)) {
            outstr.name("terminalSerialNumber")
            outstr.value(value.terminalSerialNumber)
        }
        if (!isNullOrEmpty(value.cipherConfiguration)) {
            outstr.name("cipherConfiguration")
            outstr.value(value.cipherConfiguration)
        }
        if (!isNullOrEmpty(value.emvTerminalCapabilities)) {
            outstr.name("emvTerminalCapabilities")
            outstr.value(value.emvTerminalCapabilities)
        }
        if (!isNullOrEmpty(value.localBinsTableIdPrimary)) {
            outstr.name("localBinsTableIdPrimary")
            outstr.value(value.localBinsTableIdPrimary)
        }
        if (!isNullOrEmpty(value.localBinsTableIdSecondary)) {
            outstr.name("localBinsTableIdSecondary")
            outstr.value(value.localBinsTableIdSecondary)
        }
        if (!isNullOrEmpty(value.binsTableVersion)) {
            outstr.name("binsTableVersion")
            outstr.value(value.binsTableVersion)
        }
        if (!isNullOrEmpty(value.flagRequestNewKey)) {
            outstr.name("flagRequestNewKey")
            outstr.value(value.flagRequestNewKey)
        }
        if (!isNullOrEmpty(value.keySerialNumber)) {
            outstr.name("keySerialNumber")
            outstr.value(value.keySerialNumber)
        }
        if (!isNullOrEmpty(value.cipherCounter)) {
            outstr.name("cipherCounter")
            outstr.value(value.cipherCounter)
        }
        if (!isNullOrEmpty(value.failCipherCounter)) {
            outstr.name("failCipherCounter")
            outstr.value(value.failCipherCounter)
        }
        if (!isNullOrEmpty(value.flagTrack2)) {
            outstr.name("flagTrack2")
            outstr.value(value.flagTrack2)
        }
        if (!isNullOrEmpty(value.readCardMode)) {
            outstr.name("readCardMode")
            outstr.value(value.readCardMode)
        }
        if (!isNullOrEmpty(value.track2Length)) {
            outstr.name("track2Length")
            outstr.value(value.track2Length)
        }
        if (!isNullOrEmpty(value.cvvFlag)) {
            outstr.name("cvvFlag")
            outstr.value(value.cvvFlag)
        }
        if (!isNullOrEmpty(value.clearCvvLength)) {
            outstr.name("clearCvvLength")
            outstr.value(value.clearCvvLength)
        }
        if (!isNullOrEmpty(value.track1Flag)) {
            outstr.name("track1Flag")
            outstr.value(value.track1Flag)
        }
        if (!isNullOrEmpty(value.cipherData)) {
            outstr.name("cipherData")
            outstr.value(value.cipherData)
        }
        if (!isNullOrEmpty(value.lastPan4Digits)) {
            outstr.name("lastPan4Digits")
            outstr.value(value.lastPan4Digits)
        }
        if (!isNullOrEmpty(value.crc32EZ)) {
            outstr.name("crc32EZ")
            outstr.value(value.crc32EZ)
        }
        if (!isNullOrEmpty(value.bitmapB2)) {
            outstr.name("bitmapB2")
            outstr.value(value.bitmapB2)
        }
        if (!isNullOrEmpty(value.userField1B2)) {
            outstr.name("userField1B2")
            outstr.value(value.userField1B2)
        }
        if (!isNullOrEmpty(value.criptoInfoData)) {
            outstr.name("criptoInfoData")
            outstr.value(value.criptoInfoData)
        }
        if (!isNullOrEmpty(value.tvr)) {
            outstr.name("tvr")
            outstr.value(value.tvr)
        }
        if (!isNullOrEmpty(value.otherAmount)) {
            outstr.name("otherAmount")
            outstr.value(value.otherAmount)
        }
        if (!isNullOrEmpty(value.aip)) {
            outstr.name("aip")
            outstr.value(value.aip)
        }
        if (!isNullOrEmpty(value.arqc)) {
            outstr.name("arqc")
            outstr.value(value.arqc)
        }
        if (!isNullOrEmpty(value.atc)) {
            outstr.name("atc")
            outstr.value(value.atc)
        }
        if (!isNullOrEmpty(value.terminalCountryCode)) {
            outstr.name("terminalCountryCode")
            outstr.value(value.terminalCountryCode)
        }
        if (!isNullOrEmpty(value.transDate)) {
            outstr.name("transDate")
            outstr.value(value.transDate)
        }
        if (!isNullOrEmpty(value.transactionTypeCode)) {
            outstr.name("transactionTypeCode")
            outstr.value(value.transactionTypeCode)
        }
        if (!isNullOrEmpty(value.emvTransactionType)) {
            outstr.name("emvTransactionType")
            outstr.value(value.emvTransactionType)
        }
        if (!isNullOrEmpty(value.unpredictableNumber)) {
            outstr.name("unpredictableNumber")
            outstr.value(value.unpredictableNumber)
        }
        if (!isNullOrEmpty(value.issApplDataLength)) {
            outstr.name("issApplDataLength")
            outstr.value(value.issApplDataLength)
        }
        if (!isNullOrEmpty(value.issApplData)) {
            outstr.name("issApplData")
            outstr.value(value.issApplData)
        }
        if (!isNullOrEmpty(value.bitmapB3)) {
            outstr.name("bitmapB3")
            outstr.value(value.bitmapB3)
        }
        if (!isNullOrEmpty(value.emvTermSerialNumber)) {
            outstr.name("emvTermSerialNumber")
            outstr.value(value.emvTermSerialNumber)
        }
        if (!isNullOrEmpty(value.userField1B3)) {
            outstr.name("userField1B3")
            outstr.value(value.userField1B3)
        }
        if (!isNullOrEmpty(value.userField2B3)) {
            outstr.name("userField2B3")
            outstr.value(value.userField2B3)
        }
        if (!isNullOrEmpty(value.emvLocalTransactionDate)) {
            outstr.name("emvLocalTransactionDate")
            outstr.value(value.emvLocalTransactionDate)
        }
        if (!isNullOrEmpty(value.emvTerminalType)) {
            outstr.name("emvTerminalType")
            outstr.value(value.emvTerminalType)
        }
        if (!isNullOrEmpty(value.applicationVersionNumber)) {
            outstr.name("applicationVersionNumber")
            outstr.value(value.applicationVersionNumber)
        }
        if (!isNullOrEmpty(value.cvmResults)) {
            outstr.name("cvmResults")
            outstr.value(value.cvmResults)
        }
        if (!isNullOrEmpty(value.dfNameLength)) {
            outstr.name("dfNameLength")
            outstr.value(value.dfNameLength)
        }
        if (!isNullOrEmpty(value.dfName)) {
            outstr.name("dfName")
            outstr.value(value.dfName)
        }
        if (!isNullOrEmpty(value.posEntryMode)) {
            outstr.name("posEntryMode")
            outstr.value(value.posEntryMode)
        }
        if (!isNullOrEmpty(value.terminalEntryCapabilities)) {
            outstr.name("terminalEntryCapabilities")
            outstr.value(value.terminalEntryCapabilities)
        }
        if (!isNullOrEmpty(value.lastEmvStat)) {
            outstr.name("lastEmvStat")
            outstr.value(value.lastEmvStat)
        }
        if (!isNullOrEmpty(value.dataSuspect)) {
            outstr.name("dataSuspect")
            outstr.value(value.dataSuspect)
        }
        if (!isNullOrEmpty(value.applPanSeqNum)) {
            outstr.name("applPanSeqNum")
            outstr.value(value.applPanSeqNum)
        }
        if (!isNullOrEmpty(value.devInfo)) {
            outstr.name("devInfo")
            outstr.value(value.devInfo)
        }
        if (!isNullOrEmpty(value.rsnOnlyCode)) {
            outstr.name("rsnOnlyCode")
            outstr.value(value.rsnOnlyCode)
        }
        if (!isNullOrEmpty(value.arqcVerify)) {
            outstr.name("arqcVerify")
            outstr.value(value.arqcVerify)
        }
        if (!isNullOrEmpty(value.isoRCIndicator)) {
            outstr.name("isoRCIndicator")
            outstr.value(value.isoRCIndicator)
        }
        if (!isNullOrEmpty(value.userField1B5)) {
            outstr.name("userField1B5")
            outstr.value(value.userField1B5)
        }
        if (!isNullOrEmpty(value.emvIssAuthData)) {
            outstr.name("emvIssAuthData")
            outstr.value(value.emvIssAuthData)
        }

        if (!isNullOrEmpty(value.issAuthDataLength)) {
            outstr.name("issAuthDataLength")
            outstr.value(value.issAuthDataLength)
        }

        if (!isNullOrEmpty(value.issAuthData)) {
            outstr.name("issAuthData")
            outstr.value(value.issAuthData)
        }
        if (!isNullOrEmpty(value.arpc)) {
            outstr.name("arpc")
            outstr.value(value.arpc)
        }
        if (!isNullOrEmpty(value.addlData)) {
            outstr.name("addlData")
            outstr.value(value.addlData)
        }
        if (!isNullOrEmpty(value.issScriptDataLength)) {
            outstr.name("issScriptDataLength")
            outstr.value(value.issScriptDataLength)
        }
        if (!isNullOrEmpty(value.issScriptData)) {
            outstr.name("issScriptData")
            outstr.value(value.issScriptData)
        }
        if (!isNullOrEmpty(value.keyUpdateFlag)) {
            outstr.name("keyUpdateFlag")
            outstr.value(value.keyUpdateFlag)
        }
        if (!isNullOrEmpty(value.binsUpdateFlag)) {
            outstr.name("binsUpdateFlag")
            outstr.value(value.binsUpdateFlag)
        }
        if (value.emvTagsMap.isNotEmpty()) {
            outstr.name("emvTags")
            val emvDescriptor = Iso8583Parser.getEmvDescriptor()
            outstr.value(emvDescriptor.describeEmvTagsMap(value.emvTagsMap))
        }

        if (value.emvTagsMap.isNotEmpty()) {
            outstr.name("emvTagsMap")
            // outstr.value(Gson().toJson(value.emvData))
            outstr.jsonValue(Gson().toJson(value.emvTagsMap))

            // outstr.toJson(value.emvTagsMap, Map::class.java, outstr)
        }
        if (value.emvData.isNotEmpty()) {
            outstr.name("emvData")
            outstr.value(value.emvData)
        }




        
        if (!isNullOrEmpty(value.iosTransactionId)) {
            outstr.name("iosTransactionId").value(value.iosTransactionId)
        }
        if (!isNullOrEmpty(value.iosMerchantId)) {
            outstr.name("iosMerchantId").value(value.iosMerchantId)
        }  

        if (!isNullOrEmpty(value.iosTransactionId)) {
            outstr.name("iosTransactionId").value(value.iosTransactionId)
        }

        if (!isNullOrEmpty(value.iosPinData)) {
            outstr.name("iosPinData").value(value.iosPinData)
        }
        if (!isNullOrEmpty(value.iosKeyData)) {
            outstr.name("iosKeyData").value(value.iosKeyData)
        }

        if (!isNullOrEmpty(value.iosCardholderData)) {
            outstr.name("iosCardholderData").value(value.iosCardholderData)
        }
        if (!isNullOrEmpty(value.iosVersionId)) {
            outstr.name("iosVersionId").value(value.iosVersionId)
        }
       
             if (!isNullOrEmpty(value.iosCardDataHash)) {
            outstr.name("iosCardDataHash").value(value.iosCardDataHash)
        }
     if (!isNullOrEmpty(value.iosPinDataHash)) {
            outstr.name("iosPinDataHash").value(value.iosPinDataHash)
        }




        /* similar check for otherObject */ outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): TransactionDto {
        return TransactionDto()
    }
}
