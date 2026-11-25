package com.viergegroup.prodhelper

import com.fasterxml.jackson.annotation.JsonInclude
import com.google.gson.annotations.SerializedName
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import java.util.*
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TransactionDtoPassTrue {
    @SerializedName("apiVersion")
    var apiVersion: String = "Version1"

    @SerializedName("errorCode")
    var errorCode: Int = 0

    @SerializedName("errorMessage")
    var errorMessage: String = ""

    @SerializedName("hostType")
    var hostType: String = "ProsaDUKPT"

    @SerializedName("merchantType")
    var merchantType: String = "Retail"

    @SerializedName("transactionType")
    var transactionType: String = "KeyLoad"

    @SerializedName("pointOfServiceEntryMode")
    var pointOfServiceEntryMode: String = "KeyLoad"

    @SerializedName("transactionDirection")
    var transactionDirection: String = "Request"

    @SerializedName("cardType")
    var cardType: String = "Undefined"

    @SerializedName("paymentPlanType")
    var paymentPlanType: String = "Undefined"

    @SerializedName("noSkipCRC32")
    var noSkipCRC32: String = ""

    @SerializedName("transactionStatus")
    var transactionStatus: String = "ReceivedInWebService"

    @SerializedName("hasChip")
    var hasChip: Boolean = false

    @SerializedName("messageType")
    var messageType: String = "0210"

    @SerializedName("cardBin")
    var cardBin: String = "" //FIELD 2

    @SerializedName("cardNumber")
    var cardNumber: String = "" //FIELD 2

    @SerializedName("cardExpirationDate")
    var cardExpirationDate: String = "" //FIELD 14

    @SerializedName("processingCode")
    var processingCode: String = "" //FIELD 3

    @SerializedName("transactionAmount")
    var transactionAmount: String = "" //FIELD 4

    @SerializedName("transmisionDateAndTime")
    var transmisionDateAndTime: String = "" //FIELD  7

    @SerializedName("systemTraceAuditNumber")
    var systemTraceAuditNumber: String = "" //FIELD 11

    @SerializedName("localTransactionTime")
    var localTransactionTime: String = "" //FIELD 12

    @SerializedName("localTransactionDate")
    var localTransactionDate: String = "" //FIELD 12

    @SerializedName("settlementDate")
    var settlementDate: String = "" //FIELD 15

    @SerializedName("captureDate")
    var captureDate: String = "" //FIELD 17

    @SerializedName("merchantTypeCode")
    var merchantTypeCode: String = "" //FIELD 18

    @SerializedName("merchantCurrencyCode")
    var merchantCurrencyCode: String = "" //FIELD 19

    @SerializedName("pointOfServiceEntryModeCode")
    var pointOfServiceEntryModeCode: String = "" //FIELD 22

    @SerializedName("pointOfServiceConditionCode")
    var pointOfServiceConditionCode: String = "" //FIELD 25

    @SerializedName("clearTrack2")
    var clearTrack2: String = ""

    @SerializedName("keyId")
    var keyId: String = ""

    @SerializedName("keyId1")
    var keyId1: String = ""

    @SerializedName("keyId2")
    var keyId2: String = ""

    @SerializedName("track2")
    var track2: String = ""

    @SerializedName("cvv")
    var cvv: String = ""

    @SerializedName("acquiringInstitutionIdentificationCode")
    var acquiringInstitutionIdentificationCode: String = "" //FIELD 32

    @SerializedName("rrn")
    var rrn: String = "" //FIELD 37

    @SerializedName("authorizationIdResponse")
    var authorizationIdResponse: String = "" //FIELD 38

    @SerializedName("originalRrn")
    var originalRrn: String = ""

    @SerializedName("responseCode")
    var responseCode: String = "" //FIELD 39

    @SerializedName("cardAcceptorTerminalId")
    var cardAcceptorTerminalId: String = "" //FIELD 41

    @SerializedName("cardAcceptorIdCode")
    var cardAcceptorIdCode: String = "" //FIELD 42

    @SerializedName("cardAcceptorNameAndLocation")
    var cardAcceptorNameAndLocation: String = "" //FIELD 43

    @SerializedName("additionalResponseDate")
    var additionalResponseDate: String = "" //FIELD 44

    @SerializedName("retailerData")
    var retailerData: String = "" //FIELD 48

    @SerializedName("transactionCurrencyCode")
    var transactionCurrencyCode: String = "" //FIELD 49

    @SerializedName("additionalAmounts")
    var additionalAmounts: String = "" //FIELD 54

    @SerializedName("terminalData")
    var terminalData: String = "" //FIELD 60

    @SerializedName("cardIssuerCategoryResponseCodeData")
    var cardIssuerCategoryResponseCodeData: String = "" //FIELD 61

    @SerializedName("postalCode")
    var postalCode: String = "" //FIELD 62

    @SerializedName("additionalData")
    var additionalData: String = "" //FIELD 63

    @SerializedName("dataElements")
    var dataElements: String = "" //FIELD 64

    @SerializedName("networkManagmentInformationCode")
    var networkManagmentInformationCode: String = "" //FIELD 70

    @SerializedName("originalDataElements")
    var originalDataElements: String = "" //FIELD 90

    @SerializedName("replacementAmounts")
    var replacementAmounts: String = "" //FIELD 95

    @SerializedName("receivingInstitutionIdCode")
    var receivingInstitutionIdCode: String = "" //FIELD 100

    @SerializedName("accountId")
    var accountId: String = "" //FIELD 102

    @SerializedName("terminalAddressBranch")
    var terminalAddressBranch: String = "" //FIELD 120

    @SerializedName("authorizationIndicators")
    var authorizationIndicators: String = "" //FIELD 121

    @SerializedName("cardIssuerCode")
    var cardIssuerCode: String = "" //FIELD 122

    @SerializedName("posInvoiceData")
    var posInvoiceData: String = "" //FIELD 123

    @SerializedName("specialData")
    var specialData: String = "" //FIELD 124

    @SerializedName("posSettlementData")
    var posSettlementData: String = "" //FIELD 125

    @SerializedName("posPreauthorizationAndChargeBackData")
    var posPreauthorizationAndChargeBackData: String = "" //FIELD 126

    @SerializedName("commercialReferences")
    var commercialReferences: String = ""

    //TOKEN Q1
    @SerializedName("authorizationModeIdentifier")
    var authorizationModeIdentifier: String = ""

    @SerializedName("userFieldQ1")
    var userFieldQ1: String = " "

    //TOKEN Q2
    @SerializedName("accessMediumIdentifier")
    var accessMediumIdentifier: String = ""

    //TOKEN Q6
    @SerializedName("deferral")
    var deferral: String = ""

    @SerializedName("numberOfPayments")
    var numberOfPayments: String = ""

    @SerializedName("paymentPlanCode")
    var paymentPlanCode: String = ""

    @SerializedName("paymentQ6Data")
    var paymentQ6Data: String = ""

    //TOKEN 04
    @SerializedName("additionalErrorFlagRequestInTransaction")
    var additionalErrorFlagRequestInTransaction: String = ""

    @SerializedName("routingCode")
    var routingCode: String = ""

    @SerializedName("cardVerificationFlag")
    var cardVerificationFlag: String = ""

    @SerializedName("cityExension")
    var cityExension: String = ""

    @SerializedName("completeTrack2Data")
    var completeTrack2Data: String = ""

    @SerializedName("fileFlagUses")
    var fileFlagUses: String = ""

    //TOKEN C0
    @SerializedName("userField1C0")
    var userField1C0: String = " "

    @SerializedName("cvv2cvc2Result")
    var cvv2cvc2Result: String = ""

    @SerializedName("statusCodeOfRebroadcastTxn")
    var statusCodeOfRebroadcastTxn: String = ""

    @SerializedName("counterOfRebroadcastTxn")
    var counterOfRebroadcastTxn: String = ""

    @SerializedName("merchantPostalCode")
    var merchantPostalCode: String = ""

    @SerializedName("eCommerceIndicator")
    var eCommerceIndicator: String = ""

    @SerializedName("saleForcedOrSAF")
    var saleForcedOrSAF: String = ""

    @SerializedName("cv2PresentIndicator")
    var cv2PresentIndicator: String = ""

    @SerializedName("additionalInfoIndicatorC0")
    var additionalInfoIndicatorC0: String = ""

    @SerializedName("authenticationCollectorIndicator")
    var authenticationCollectorIndicator: String = ""

    @SerializedName("propensityFlagTradeFraud")
    var propensityFlagTradeFraud: String = ""

    @SerializedName("cavvAvvValidationResult")
    var cavvAvvValidationResult: String = ""

    //TOKEN C4
    @SerializedName("attendedTerminal")
    var attendedTerminal: String = ""

    @SerializedName("terminalOperationIndex")
    var terminalOperationIndex: String = ""

    @SerializedName("terminalLocator")
    var terminalLocator: String = ""

    @SerializedName("cardholderPresenceIndicator")
    var cardholderPresenceIndicator: String = ""

    @SerializedName("cardPresenceIndicator")
    var cardPresenceIndicator: String = ""

    @SerializedName("cardCaptureCapacityIndicator")
    var cardCaptureCapacityIndicator: String = ""

    @SerializedName("statusIndicatorC4")
    var statusIndicatorC4: String = ""

    @SerializedName("transactionSecurityLevel")
    var transactionSecurityLevel: String = ""

    @SerializedName("routingIndicator")
    var routingIndicator: String = ""

    @SerializedName("terminalActivationByCardholder")
    var terminalActivationByCardholder: String = ""

    @SerializedName("transferDataIndicator")
    var transferDataIndicator: String = ""

    @SerializedName("identificationMethodCardHolder")
    var identificationMethodCardHolder: String = ""

    @SerializedName("fillerC4")
    var fillerC4: String = ""

    //TOKEN C6
    @SerializedName("xid")
    var xid: String = ""

    @SerializedName("cavvCardholderAuthVerValue")
    var cavvCardholderAuthVerValue: String = ""

    //TOKEN CE
    @SerializedName("indicatorCE")
    var indicatorCE: String = ""

    @SerializedName("cardholderAuthenticationData")
    var cardholderAuthenticationData: String = ""

    //TOKEN CZ
    @SerializedName("atcCZ")
    var atcCZ: String = ""

    @SerializedName("formFactrInd")
    var formFactrInd: String = ""

    @SerializedName("userFieldACICZ")
    var userFieldACICZ: String = ""

    //TOKEN EX
    @SerializedName("pekEX")
    var pekEX: String = ""

    @SerializedName("ksnEX")
    var ksnEX: String = ""

    @SerializedName("checkValueEX")
    var checkValueEX: String = ""

    @SerializedName("newKeyRequestStatusEX")
    var newKeyRequestStatusEX: String = ""

    @SerializedName("newKeyRequestStatus")
    var newKeyRequestStatus: String = "0"

    @SerializedName("crc32EX")
    var crc32EX: String = ""

    @SerializedName("tokenEW")
    var tokenEW: String = ""

    @SerializedName("tokenEWFormatted")
    var tokenEWFormatted: String = ""

    @SerializedName("tkEW")
    var tkEW: String = ""

    @SerializedName("randomTkCipherEW")
    var randomTkCipherEW: String = ""

    @SerializedName("rsaVersionEW")
    var rsaVersionEW: String = ""

    @SerializedName("rsaPaddingEW")
    var rsaPaddingEW: String = ""

    @SerializedName("checkValueEW")
    var checkValueEW: String = ""

    @SerializedName("crc32EW")
    var crc32EW: String = ""

    //TOKEN ES
    @SerializedName("terminalSoftwareVersion")
    var terminalSoftwareVersion: String = ""

    @SerializedName("terminalSerialNumber")
    var terminalSerialNumber: String = ""

    @SerializedName("cipherConfiguration")
    var cipherConfiguration: String = ""

    @SerializedName("localBinsTableIdPrimary")
    var localBinsTableIdPrimary: String = ""

    @SerializedName("localBinsTableIdSecondary")
    var localBinsTableIdSecondary: String = ""

    @SerializedName("binsTableVersion")
    var binsTableVersion: String = ""

    @SerializedName("flagRequestNewKey")
    var flagRequestNewKey: String = "0"

    //TOKEN EZ
    @SerializedName("clearCipherBlocks")
    var clearCipherBlocks = mutableListOf<String>()

    @SerializedName("chipherDataBlocks")
    var chipherDataBlocks = mutableListOf<String>()

    @SerializedName("clearCipherDataEZ")
    var clearCipherDataEZ: String = ""

    @SerializedName("tokenEX")
    var tokenEX: String = ""

    @SerializedName("tokenEZ")
    var tokenEZ: String = ""

    @SerializedName("tokenEZ2")
    var tokenEZ2: String = ""

    @SerializedName("tokenEZFormated")
    var tokenEZFormated: String = ""

    @SerializedName("tokenEZ2Formated")
    var tokenEZ2Formated: String = ""

    @SerializedName("tokenOpenPay")
    var tokenOpenPay: String = ""

    @SerializedName("keySerialNumber")
    var keySerialNumber: String = ""

    @SerializedName("cipherCounter")
    var cipherCounter: String = ""

    @SerializedName("failCipherCounter")
    var failCipherCounter: String = ""

    @SerializedName("flagTrack2")
    var flagTrack2: String = ""

    @SerializedName("readCardMode")
    var readCardMode: String = ""

    @SerializedName("track2Length")
    var track2Length: String = ""

    @SerializedName("cvvFlag")
    var cvvFlag: String = ""

    @SerializedName("clearCvvLength")
    var clearCvvLength: String = ""

    @SerializedName("track1Flag")
    var track1Flag: String = ""

    @SerializedName("cipherData")
    var cipherData: String = ""

    @SerializedName("lastPan4Digits")
    var lastPan4Digits: String = ""

    @SerializedName("crc32EZ")
    var crc32EZ: String = ""

    //TOKEN B2
    @SerializedName("bitmapB2")
    var bitmapB2: String = ""

    @SerializedName("userField1B2")
    var userField1B2: String = ""

    @SerializedName("criptoInfoData")
    var criptoInfoData: String = ""

    @SerializedName("tvr")
    var tvr: String = ""

    @SerializedName("otherAmount")
    var otherAmount: String = ""

    @SerializedName("aip")
    var aip: String = ""

    @SerializedName("arqc")
    var arqc: String = ""

    @SerializedName("atc")
    var atc: String = ""

    @SerializedName("terminalCountryCode")
    var terminalCountryCode: String = ""

    @SerializedName("transDate")
    var transDate: String = ""

    @SerializedName("transactionTypeCode")
    var transactionTypeCode: String = ""

    @SerializedName("emvTransactionType")
    var emvTransactionType: String = ""

    @SerializedName("unpredictableNumber")
    var unpredictableNumber: String = ""

    @SerializedName("issApplDataLength")
    var issApplDataLength: String = ""

    @SerializedName("issApplData")
    var issApplData: String = ""

    //TOKEN B3
    @SerializedName("bitmapB3")
    var bitmapB3: String = ""

    @SerializedName("emvTermSerialNumber")
    var emvTermSerialNumber: String = ""

    @SerializedName("emvTerminalCapabilities")
    var emvTerminalCapabilities: String = ""

    @SerializedName("userField1B3")
    var userField1B3: String = ""

    @SerializedName("userField2B3")
    var userField2B3: String = ""

    @SerializedName("emvLocalTransactionDate")
    var emvLocalTransactionDate: String = ""

    @SerializedName("emvTerminalType")
    var emvTerminalType: String = ""

    @SerializedName("applicationVersionNumber")
    var applicationVersionNumber: String = ""

    @SerializedName("cvmResults")
    var cvmResults: String = ""

    @SerializedName("dfNameLength")
    var dfNameLength: String = ""

    @SerializedName("dfName")
    var dfName: String = ""

    //TOKEN B4
    @SerializedName("posEntryMode")
    var posEntryMode: String = ""

    @SerializedName("terminalEntryCapabilities")
    var terminalEntryCapabilities: String = ""

    @SerializedName("lastEmvStat")
    var lastEmvStat: String = ""

    @SerializedName("dataSuspect")
    var dataSuspect: String = ""

    @SerializedName("applPanSeqNum")
    var applPanSeqNum: String = ""

    @SerializedName("devInfo")
    var devInfo: String = ""

    @SerializedName("rsnOnlyCode")
    var rsnOnlyCode: String = ""

    @SerializedName("arqcVerify")
    var arqcVerify: String = ""

    @SerializedName("isoRCIndicator")
    var isoRCIndicator: String = ""

    //TOKEN B5
    @SerializedName("userField1B5")
    var userField1B5: String = ""

    @SerializedName("emvIssAuthData")
    var emvIssAuthData: String = ""

    @SerializedName("issAuthData")
    var issAuthData: String = ""

    @SerializedName("arpc")
    var arpc: String = ""

    @SerializedName("addlData")
    var addlData: String = ""

    @SerializedName("issAuthDataLength")
    var issAuthDataLength: String = ""

    //TOKEN B6
    @SerializedName("issScriptDataLength")
    var issScriptDataLength: String = ""

    @SerializedName("issScriptData")
    var issScriptData: String = ""

    //TOKEN ER
    @SerializedName("keyUpdateFlag")
    var keyUpdateFlag: String = ""

    @SerializedName("binsUpdateFlag")
    var binsUpdateFlag: String = ""

    @SerializedName("isoASCII")
    var isoASCII: String = ""

    @SerializedName("emvData")
    var emvData: String = ""

    @SerializedName("emvTagsMap")
    var emvTagsMap = mutableMapOf<String, String>()

    @SerializedName("transactionDateTime")
    var transactionDateTime: Long =0L


}