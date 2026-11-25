package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import java.time.LocalDateTime

class OriginalTransactionDto {


    var affiliation:String=""
    var fiid:String=""
    var merchantName:String=""
  //  var merchantTypeCode:String=""




    var hasChip=false
    var messageType:String=""
    var cardBin: String = "" //FIELD 2
    var validateData: String = "0"

    var cardNumber: String = "" //FIELD 2
    var cardExpirationDate: String = "" //FIELD 14

    var processingCode: String = "" //FIELD 3
    var transactionAmount: String = "" //FIELD 4
    var transmisionDateAndTime: String = "" //FIELD  7

    var systemTraceAuditNumber: String = "" //FIELD 11
    var localTransactionTime: String = "" //FIELD 12
    var localTransactionDate: String = "" //FIELD 12
    var settlementDate: String = "" //FIELD 15


    var captureDate: String = "" //FIELD 17
    var merchantTypeCode: String = "" //FIELD 18
    var merchantCurrencyCode: String = "" //FIELD 19
    var pointOfServiceEntryModeCode: String = "" //FIELD 22
    var pointOfServiceConditionCode: String = "" //FIELD 25
    var keyId: String = ""
    var keyId1: String = ""
    var keyId2: String = ""

    var track2:String=""
    var cvv:String=""

    var acquiringInstitutionIdentificationCode: String = "" //FIELD 32

    var authorizationIdResponse: String = "" //FIELD 38
    var originalRrn: String = ""

    var responseCode: String = "" //FIELD 39
    var cardAcceptorTerminalId: String = "" //FIELD 41
    var cardAcceptorIdCode: String = "" //FIELD 42
    var cardAcceptorNameAndLocation: String = "" //FIELD 43
    var additionalResponseDate: String = "" //FIELD 44

    var retailerData: String = "" //FIELD 48
    var transactionCurrencyCode: String = "" //FIELD 49

    var additionalAmounts: String = "" //FIELD 54
    var terminalData: String = "" //FIELD 60
    var cardIssuerCategoryResponseCodeData: String = "" //FIELD 61

    var postalCode: String = "" //FIELD 62

    var additionalData: String = "" //FIELD 63
    var dataElements: String = "" //FIELD 64
    var networkManagmentInformationCode: String = "" //FIELD 70
    var originalDataElements: String = "" //FIELD 90

    var replacementAmounts: String = "" //FIELD 95

    var receivingInstitutionIdCode: String = "" //FIELD 100

    var accountId: String = "" //FIELD 102

    var terminalAddressBranch: String = "" //FIELD 120

    var authorizationIndicators: String = "" //FIELD 121
    var cardIssuerCode: String = "" //FIELD 122

    var posInvoiceData: String = "" //FIELD 123
    var specialData: String = "" //FIELD 124
    var posSettlementData: String = "" //FIELD 125
    var posPreauthorizationAndChargeBackData: String = "" //FIELD 126
    var commercialReferences: String = ""
    var serverName:String=""



    //TOKEN Q1
    var authorizationModeIdentifier: String = ""
    var userFieldQ1: String = ""
    //TOKEN Q2
    var accessMediumIdentifier: String = ""

    //TOKEN Q6
    var deferral: String = ""
    var numberOfPayments: String = ""
    var paymentPlanCode: String = ""
    var paymentQ6Data: String = ""

    //TOKEN 04
    var additionalErrorFlagRequestInTransaction: String = ""
    var routingCode: String = ""
    var cardVerificationFlag: String = ""
    var cityExension: String = ""
    var completeTrack2Data: String = ""
    var fileFlagUses: String = ""


    //TOKEN C0

    var userField1C0: String = " "

    var cvv2cvc2Result: String = ""
    var statusCodeOfRebroadcastTxn: String = ""
    var counterOfRebroadcastTxn: String = ""
    var merchantPostalCode: String = ""
    var eCommerceIndicator: String = ""
    var saleForcedOrSAF: String = ""
    var cv2PresentIndicator: String = ""

    var additionalInfoIndicatorC0: String = ""
    var authenticationCollectorIndicator: String = ""

    var propensityFlagTradeFraud: String = ""
    var cavvAvvValidationResult: String = ""

    //TOKEN C4
    var attendedTerminal: String = ""
    var terminalOperationIndex: String = ""
    var terminalLocator: String = ""
    var cardholderPresenceIndicator: String = ""
    var cardPresenceIndicator: String = ""
    var cardCaptureCapacityIndicator: String = ""
    var statusIndicatorC4: String = ""
    var transactionSecurityLevel: String = ""
    var routingIndicator: String = ""
    var terminalActivationByCardholder: String = ""
    var transferDataIndicator: String = ""
    var identificationMethodCardHolder: String = ""
    var fillerC4: String = ""
 

    //TOKEN C6
    var xid: String = ""
    var cavvCardholderAuthVerValue: String = ""
    //TOKEN CE
    var indicatorCE: String = ""
    var cardholderAuthenticationData: String = ""
    //TOKEN CZ
    var atcCZ: String = ""
    var formFactrInd: String = ""
    var userFieldACICZ: String = ""


    //TOKEN EX
    var pekEX: String = ""
    var ksnEX: String = ""
    var checkValueEX: String = ""
    var newKeyRequestStatusEX: String = ""
    var newKeyRequestStatus: String = "0"

    var crc32EX: String = ""
    var tokenEW:String=""
    var tokenEWFormatted:String=""

    //var tokenEWProsa:String=""
    //var tokenEWEglobal:String=""



    //TOKEN EW
    var randomTkCipherEW: String = ""
    var checkValueEW: String = ""

    var rsaVersionEW: String = ""
    var rsaPaddingEW: String = ""

    var crc32EW: String = ""

    var tkEW: String = ""




    //TOKEN ES
    var terminalSoftwareVersion: String = ""
    var cipherConfiguration: String = ""
    var localBinsTableIdPrimary: String = ""
    var localBinsTableIdSecondary: String = ""
    var binsTableVersion: String = ""
    var flagRequestNewKey: String = "0"

    //TOKEN EZ

  // var cipherDataEZ: String = ""
     var clearCipherBlocks= mutableListOf<String>()
     var chipherDataBlocks= mutableListOf<String>()
     var clearCipherDataEZ: String = ""
    var tokenEX: String = ""
    var tokenEZ: String = ""
    var tokenEZ2: String = ""
    var tokenEZFormated: String = ""
    var tokenEZ2Formated: String = ""

    var tokenOpenPay: String = ""


    var keySerialNumber: String = ""
    var cipherCounter: String = ""
    var failCipherCounter: String = ""
    var flagTrack2: String = ""
    var readCardMode: String = ""
    var track2Length: String = ""
    var cvvFlag: String = ""
    var clearCvvLength: String = ""
    var track1Flag: String = ""
    var cipherData: String = ""
    var lastPan4Digits: String = ""
    var crc32EZ: String = ""
    //TOKEN B2
    var bitmapB2: String = ""
    var userField1B2: String = ""
    var criptoInfoData: String = ""
    var tvr: String = ""
    var otherAmount: String = ""
    var aip: String = ""
    var arqc: String = ""

    var atc: String = ""
    var terminalCountryCode: String = ""
    var transDate: String = ""
    var transactionTypeCode: String = ""
    var emvTransactionType:String=""
    var unpredictableNumber:String=""
    var issApplDataLength:String=""
    var issApplData:String=""
    //TOKEN B3
    var bitmapB3: String = "CF00"
    var emvTermSerialNumber: String = ""
    var emvTerminalCapabilities: String = ""
    var userField1B3: String = "0000"
    var userField2B3: String = "00000000"
    var emvLocalTransactionDate:String=""
    var emvTerminalType: String = ""
    var applicationVersionNumber: String = ""
    var cvmResults: String = ""
    var dfNameLength: String = ""
    var dfName: String = ""
    //TOKEN B4

    var posEntryMode: String = ""
    var terminalEntryCapabilities: String = ""

    var lastEmvStat: String = ""
    var dataSuspect: String = ""
    var applPanSeqNum: String = ""
    var devInfo: String = ""
    var rsnOnlyCode: String = ""
    var arqcVerify: String = ""
    var isoRCIndicator: String = ""
    //TOKEN B5

    var userField1B5: String = ""
    var emvIssAuthData:String=""
    var issAuthData:String=""
    var arpc:String=""
    var addlData:String=""
    var issAuthDataLength:String=""

    //TOKEN B6

    var issScriptDataLength: String = ""
    var issScriptData: String = ""




    //TOKEN ER

    var keyUpdateFlag: String = ""
    var binsUpdateFlag: String = ""

     var isoASCII: String = ""

    var emvData: String = ""


}