package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.isNullOrEmpty
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.entities.catalogs.HostType
import java.io.IOException

class TransactionTerminalDtoGSONAdapter : TypeAdapter<TransactionTerminalDto?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: TransactionTerminalDto?) {
        out.beginObject()
        if (value == null) return
        if (!isNullOrEmpty(value.uuid)) {
            out.name("uuid").value(value.uuid)
        }

        out.name("paymentPlanType").value(value.paymentPlanType.toString())
        out.name("hostType").value(value.hostType.toString())

        if (!isNullOrEmpty(value.clientId)) {
            out.name("clientId").value(value.clientId)
        }

        if (!isNullOrEmpty(value.systemIdentification)) {
            out.name("systemIdentification").value(value.systemIdentification)
        }
        if (!isNullOrEmpty(value.companyIdentification)) {
            out.name("companyIdentification").value(value.companyIdentification)
        }
        if (!isNullOrEmpty(value.branchIdentification)) {
            out.name("branchIdentification").value(value.branchIdentification)
        }

        if (!isNullOrEmpty(value.posIdentification)) {
            out.name("posIdentification").value(value.posIdentification)
        }
        if (!isNullOrEmpty(value.transactionIdMpp)) {
            out.name("transactionIdMpp").value(value.transactionIdMpp)
        }

        if (!isNullOrEmpty(value.affiliation)) {
            out.name("affiliation").value(value.affiliation)
        }

        if (!isNullOrEmpty(value.originalRrn)) {
            out.name("originalRrn").value(value.originalRrn)
        }
        if (!isNullOrEmpty(value.fiid)) {
            out.name("fiid").value(value.fiid)
        }
        if (!isNullOrEmpty(value.merchantName)) {
            out.name("merchantName").value(value.merchantName)
        }
        if (!isNullOrEmpty(value.merchantTypeCode)) {
            out.name("merchantTypeCode").value(value.merchantTypeCode)
        }

        if (!isNullOrEmpty(value.orderId)) {
            out.name("orderId").value(value.orderId)
        }
        if (!isNullOrEmpty(value.merchantId)) {
            out.name("merchantId").value(value.merchantId)
        }
        if (!isNullOrEmpty(value.merchantId)) {
            out.name("merchantId").value(value.merchantId)
        }

        if (!isNullOrEmpty(value.sendToProd)) {
            out.name("sendToProd").value(value.sendToProd)
        }
        if (!isNullOrEmpty(value.noSkipCRC32)) {
            out.name("noSkipCRC32").value(value.noSkipCRC32)
        }
        if (!isNullOrEmpty(value.affiliationNumber)) {
            out.name("affiliationNumber").value(value.affiliationNumber)
        }
        if (!isNullOrEmpty(value.tokenEX)) {
            out.name("tokenEX").value(value.tokenEX)
        }
        if (value.hasChip) {
            out.name("hasChip").value(value.hasChip)
        }
        if (!isNullOrEmpty(value.cardExpirationDate)) {
            out.name("cardExpirationDate").value(value.cardExpirationDate)
        }
        if (!isNullOrEmpty(value.userField1C0)) {
            out.name("userField1C0").value(value.userField1C0)
        }
        if (value.originalTransactionData != null) {
            // Assuming originalTransactionData is another DTO, it might need custom serialization
            // as well
            out.name("originalTransactionData").value(value.originalTransactionData.toString())
        }
        if (!isNullOrEmpty(value.track2) && value.hostType != HostType.Banorte) {
            out.name("track2").value(StringUtils.maskString(value.track2))
        }
        if (!isNullOrEmpty(value.bdk)) {
            out.name("bdk").value(value.bdk)
        }
        if (!isNullOrEmpty(value.ksn)) {
            out.name("ksn").value(value.ksn)
        }
        if (!isNullOrEmpty(value.bank.name)) {
            out.name("bank").value(value.bank.name)
        }
        if (!isNullOrEmpty(value.bankName)) {
            out.name("bankName").value(value.bankName)
        }

        if (!isNullOrEmpty(value.moduleIdRequest.name)) {
            out.name("moduleIdRequest").value(value.moduleIdRequest.name)
        }
        if (!isNullOrEmpty(value.operationIdRequest.name)) {
            out.name("operationIdRequest").value(value.operationIdRequest.name)
        }
        if (!isNullOrEmpty(value.clientIP)) {
            out.name("clientIP").value(value.clientIP)
        }
        if (!isNullOrEmpty(value.clientEmail)) {
            out.name("clientEmail").value(value.clientEmail)
        }
        if (!isNullOrEmpty(value.clientUserName)) {
            out.name("clientUserName").value(value.clientUserName)
        }
        if (!isNullOrEmpty(value.clientGeolocation)) {
            out.name("clientGeolocation").value(value.clientGeolocation)
        }
        if (!isNullOrEmpty(value.deviceModel)) {
            out.name("deviceModel").value(value.deviceModel)
        }

        out.name("deviceType").value(value.deviceType.name)

        if (!isNullOrEmpty(value.merchantType.merchantTypeName)) {
            out.name("merchantType").value(value.merchantType.name)
        }
        if (!isNullOrEmpty(value.transactionType.name)) {
            out.name("transactionType").value(value.transactionType.name)
        }

        if (!isNullOrEmpty(value.keyDirection.name)) {
            out.name("keyDirection").value(value.keyDirection.name)
        }

        if (!isNullOrEmpty(value.pointOfServiceEntryMode.name)) {
            out.name("pointOfServiceEntryMode").value(value.pointOfServiceEntryMode.name)
        }
        if (!isNullOrEmpty(value.keyId)) {
            out.name("keyId").value(value.keyId)
        }
        if (!isNullOrEmpty(value.keyId1)) {
            out.name("keyId1").value(value.keyId1)
        }
        if (!isNullOrEmpty(value.keyId2)) {
            out.name("keyId2").value(value.keyId2)
        }
        if (!isNullOrEmpty(value.transactionAmount)) {
            out.name("transactionAmount").value(value.transactionAmount)
        }
        out.name("cardType").value(value.cardType.toString())
        if (!isNullOrEmpty(value.track2)) {
            out.name("track2").value(value.track2)
        }
        if (!isNullOrEmpty(value.rrn)) {
            out.name("rrn").value(value.rrn)
        }
        if (!isNullOrEmpty(value.authorizationIdResponse)) {
            out.name("authorizationIdResponse").value(value.authorizationIdResponse)
        }
        if (!isNullOrEmpty(value.cardNumber)) {
            out.name("cardNumber").value(value.cardNumber)
        }
        if (!isNullOrEmpty(value.retailerData)) {
            out.name("retailerData").value(value.retailerData)
        }
        if (!isNullOrEmpty(value.terminalData)) {
            out.name("terminalData").value(value.terminalData)
        }
        if (!isNullOrEmpty(value.deferral)) {
            out.name("deferral").value(value.deferral)
        }
        if (!isNullOrEmpty(value.numberOfPayments)) {
            out.name("numberOfPayments").value(value.numberOfPayments)
        }
        if (!isNullOrEmpty(value.cvv2cvc2Result)) {
            out.name("cvv2cvc2Result").value(value.cvv2cvc2Result)
        }
        if (!isNullOrEmpty(value.statusCodeOfRebroadcastTxn)) {
            out.name("statusCodeOfRebroadcastTxn").value(value.statusCodeOfRebroadcastTxn)
        }
        if (!isNullOrEmpty(value.counterOfRebroadcastTxn)) {
            out.name("counterOfRebroadcastTxn").value(value.counterOfRebroadcastTxn)
        }
        if (!isNullOrEmpty(value.merchantPostalCode)) {
            out.name("merchantPostalCode").value(value.merchantPostalCode)
        }
        if (!isNullOrEmpty(value.eCommerceIndicator)) {
            out.name("eCommerceIndicator").value(value.eCommerceIndicator)
        }
        if (!isNullOrEmpty(value.saleForcedOrSAF)) {
            out.name("saleForcedOrSAF").value(value.saleForcedOrSAF)
        }
        if (!isNullOrEmpty(value.cv2PresentIndicator)) {
            out.name("cv2PresentIndicator").value(value.cv2PresentIndicator)
        }
        if (!isNullOrEmpty(value.additionalInfoIndicatorC0)) {
            out.name("additionalInfoIndicatorC0").value(value.additionalInfoIndicatorC0)
        }
        if (!isNullOrEmpty(value.authenticationCollectorIndicator)) {
            out.name("authenticationCollectorIndicator")
                    .value(value.authenticationCollectorIndicator)
        }
        if (!isNullOrEmpty(value.propensityFlagTradeFraud)) {
            out.name("propensityFlagTradeFraud").value(value.propensityFlagTradeFraud)
        }
        if (!isNullOrEmpty(value.cavvAvvValidationResult)) {
            out.name("cavvAvvValidationResult").value(value.cavvAvvValidationResult)
        }
        if (!isNullOrEmpty(value.terminalSoftwareVersion)) {
            out.name("terminalSoftwareVersion").value(value.terminalSoftwareVersion)
        }
        if (!isNullOrEmpty(value.terminalSerialNumber)) {
            out.name("terminalSerialNumber").value(value.terminalSerialNumber)
        }
        if (!isNullOrEmpty(value.cipherConfiguration)) {
            out.name("cipherConfiguration").value(value.cipherConfiguration)
        }
        if (!isNullOrEmpty(value.emvTerminalCapabilities)) {
            out.name("emvTerminalCapabilities").value(value.emvTerminalCapabilities)
        }
        if (!isNullOrEmpty(value.localBinsTableIdPrimary)) {
            out.name("localBinsTableIdPrimary").value(value.localBinsTableIdPrimary)
        }
        if (!isNullOrEmpty(value.localBinsTableIdSecondary)) {
            out.name("localBinsTableIdSecondary").value(value.localBinsTableIdSecondary)
        }
        if (!isNullOrEmpty(value.binsTableVersion)) {
            out.name("binsTableVersion").value(value.binsTableVersion)
        }
        if (!isNullOrEmpty(value.keySerialNumber)) {
            out.name("keySerialNumber").value(value.keySerialNumber)
        }
        if (!isNullOrEmpty(value.cvvFlag)) {
            out.name("cvvFlag").value(value.cvvFlag)
        }
        if (!isNullOrEmpty(value.cipherData)) {
            out.name("cipherData").value(value.cipherData)
        }
        if (!isNullOrEmpty(value.lastPan4Digits)) {
            out.name("lastPan4Digits").value(value.lastPan4Digits)
        }
        if (!isNullOrEmpty(value.criptoInfoData)) {
            out.name("criptoInfoData").value(value.criptoInfoData)
        }
        if (!isNullOrEmpty(value.tvr)) {
            out.name("tvr").value(value.tvr)
        }
        if (!isNullOrEmpty(value.otherAmount)) {
            out.name("otherAmount").value(value.otherAmount)
        }
        if (!isNullOrEmpty(value.aip)) {
            out.name("aip").value(value.aip)
        }
        if (!isNullOrEmpty(value.arqc)) {
            out.name("arqc").value(value.arqc)
        }
        if (!isNullOrEmpty(value.atc)) {
            out.name("atc").value(value.atc)
        }
        if (!isNullOrEmpty(value.terminalCountryCode)) {
            out.name("terminalCountryCode").value(value.terminalCountryCode)
        }
        if (!isNullOrEmpty(value.transactionTypeCode)) {
            out.name("transactionTypeCode").value(value.transactionTypeCode)
        }
        if (!isNullOrEmpty(value.emvTransactionType)) {
            out.name("emvTransactionType").value(value.emvTransactionType)
        }
        if (!isNullOrEmpty(value.unpredictableNumber)) {
            out.name("unpredictableNumber").value(value.unpredictableNumber)
        }
        if (!isNullOrEmpty(value.issApplDataLength)) {
            out.name("issApplDataLength").value(value.issApplDataLength)
        }
        if (!isNullOrEmpty(value.issApplData)) {
            out.name("issApplData").value(value.issApplData)
        }
        if (!isNullOrEmpty(value.emvTermSerialNumber)) {
            out.name("emvTermSerialNumber").value(value.emvTermSerialNumber)
        }
        if (!isNullOrEmpty(value.emvLocalTransactionDate)) {
            out.name("emvLocalTransactionDate").value(value.emvLocalTransactionDate)
        }
        if (!isNullOrEmpty(value.emvTerminalType)) {
            out.name("emvTerminalType").value(value.emvTerminalType)
        }
        if (!isNullOrEmpty(value.applicationVersionNumber)) {
            out.name("applicationVersionNumber").value(value.applicationVersionNumber)
        }
        if (!isNullOrEmpty(value.cvmResults)) {
            out.name("cvmResults").value(value.cvmResults)
        }
        if (!isNullOrEmpty(value.dfNameLength)) {
            out.name("dfNameLength").value(value.dfNameLength)
        }
        if (!isNullOrEmpty(value.dfName)) {
            out.name("dfName").value(value.dfName)
        }
        if (!isNullOrEmpty(value.posEntryMode)) {
            out.name("posEntryMode").value(value.posEntryMode)
        }
        if (!isNullOrEmpty(value.terminalEntryCapabilities)) {
            out.name("terminalEntryCapabilities").value(value.terminalEntryCapabilities)
        }
        if (!isNullOrEmpty(value.lastEmvStat)) {
            out.name("lastEmvStat").value(value.lastEmvStat)
        }
        if (!isNullOrEmpty(value.dataSuspect)) {
            out.name("dataSuspect").value(value.dataSuspect)
        }
        if (!isNullOrEmpty(value.applPanSeqNum)) {
            out.name("applPanSeqNum").value(value.applPanSeqNum)
        }
        if (!isNullOrEmpty(value.devInfo)) {
            out.name("devInfo").value(value.devInfo)
        }
        if (!isNullOrEmpty(value.rsnOnlyCode)) {
            out.name("rsnOnlyCode").value(value.rsnOnlyCode)
        }
        if (!isNullOrEmpty(value.arqcVerify)) {
            out.name("arqcVerify").value(value.arqcVerify)
        }
        if (!isNullOrEmpty(value.isoRCIndicator)) {
            out.name("isoRCIndicator").value(value.isoRCIndicator)
        }

        if (!isNullOrEmpty(value.randomTkCipherEW)) {
            out.name("randomTkCipherEW").value(value.randomTkCipherEW)
        }
        if (!isNullOrEmpty(value.checkValueEW)) {
            out.name("checkValueEW").value(value.checkValueEW)
        }

        if (!isNullOrEmpty(value.rsaVersionEW)) {
            out.name("rsaVersionEW").value(value.rsaVersionEW)
        }
        if (!isNullOrEmpty(value.rsaPaddingEW)) {
            out.name("rsaPaddingEW").value(value.rsaPaddingEW)
        }
        if (!isNullOrEmpty(value.crc32EW)) {
            out.name("crc32EW").value(value.crc32EW)
        }
        if (!isNullOrEmpty(value.transferDataIndicator)) {
            out.name("transferDataIndicator").value(value.transferDataIndicator)
        }
        if (!isNullOrEmpty(value.identificationMethodCardHolder)) {
            out.name("identificationMethodCardHolder").value(value.identificationMethodCardHolder)
        }
        if (!isNullOrEmpty(value.terminalActivationByCardholder)) {
            out.name("terminalActivationByCardholder").value(value.terminalActivationByCardholder)
        }

        if (!isNullOrEmpty(value.processingCode)) {
            out.name("processingCode").value(value.processingCode)
        }

        if (!isNullOrEmpty(value.iosTransactionId)) {
            out.name("iosTransactionId").value(value.iosTransactionId)
        }
        if (!isNullOrEmpty(value.iosMerchantId)) {
            out.name("iosMerchantId").value(value.iosMerchantId)
        } 

        if (!isNullOrEmpty(value.iosTransactionId)) {
            out.name("iosTransactionId").value(value.iosTransactionId)
        }

        if (!isNullOrEmpty(value.iosPinData)) {
            out.name("iosPinData").value(value.iosPinData)
        }
        if (!isNullOrEmpty(value.iosKeyData)) {
            out.name("iosKeyData").value(value.iosKeyData)
        }

        if (!isNullOrEmpty(value.iosCardholderData)) {
            out.name("iosCardholderData").value(value.iosCardholderData)
        }
        if (!isNullOrEmpty(value.iosVersionId)) {
            out.name("iosVersionId").value(value.iosVersionId)
        }


             if (!isNullOrEmpty(value.iosCardDataHash)) {
            out.name("iosCardDataHash").value(value.iosCardDataHash)
        }
     if (!isNullOrEmpty(value.iosPinDataHash)) {
            out.name("iosPinDataHash").value(value.iosPinDataHash)
        }


        
        if (!value.emvData.isEmpty()) {
            out.name("emvData").value(value.emvData)
        }
        if (value.emvTagsMap.isNotEmpty()) {
            out.name("emvTagsMap")
            out.jsonValue(Gson().toJson(value.emvTagsMap))
        }
        out.endObject()
    }
    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): TransactionTerminalDto {
        return TransactionTerminalDto()
    }
}
