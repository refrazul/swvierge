package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.*

class TransactionTerminalDto : RequestEntity(){

    var fiid:String=""
      var transactionIdMpp: String = ""

    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String="" 
    var merchantAddress:String=""
    var retailerDataAddOns:String="            00010001"
    var bankName:String=""
    var processingCode: String = "" //FIELD 3


    var merchantId:String="" 
    var orderId:String=""  
    var sendToProd:String="0"  
     var terminalActivationByCardholder: String = ""


    var pinBlock: String = "" 
    var pinClear: String = ""
    var pan: String = ""
    var ksnPin: String = ""
    var ksnPinTo: String = ""
 
    
    var serverName=""
   var deviceType: DeviceType = DeviceType.POS 

    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var cardType: CardType = CardType.Undefined
    var paymentPlanType: PaymentPlanType=PaymentPlanType.Undefined
     var keyDirection: KeyDirection = KeyDirection.Undefined

    var noSkipCRC32:String=""

    var transactionAmount: String = ""
    var terminalData:String=""
    var affiliationNumber: String = ""
    var retailerData:String=""
     var clientId:String="VIERGE"

    var track2:String=""

    var cardNumber: String = "" //FIELD 2 
    var cardExpirationDate: String = "" //FIELD 14

    var authorizationIdResponse:String=""
    var originalRrn:String=""
  
    var hasChip:Boolean=false
    var tokenEX: String = ""
    var keyId: String = ""
    var keyId1: String = ""
    var keyId2: String = ""

    //TOKEN ES
    var terminalSoftwareVersion: String = ""

    var cipherConfiguration: String = "5"
    var localBinsTableIdPrimary: String = "" 
    var localBinsTableIdSecondary: String = ""
    var binsTableVersion: String = "00"
    var keySerialNumber: String = ""


    //TOKEN B2
    var criptoInfoData: String = "80"
    var tvr: String = ""
    var otherAmount: String = ""
    var aip: String = ""
    var arqc: String = ""
    var atc: String = ""
    var terminalCountryCode: String = ""
    var transactionTypeCode: String = ""
    var emvTransactionType:String=""
    var unpredictableNumber:String=""
    var issApplDataLength:String=""
    var issApplData:String=""


    //TOKEN B3
    var emvTermSerialNumber: String = ""
    var emvTerminalCapabilities: String = ""

    var emvLocalTransactionDate:String=""
    var emvTerminalType: String = "22"
    var applicationVersionNumber: String = ""
    var cvmResults: String = ""
    var dfNameLength: String = ""
    var dfName: String = ""
    var cvvFlag: String = "A"
    var lastPan4Digits: String = ""
    //TOKEN C0
    var cvv2cvc2Result: String = ""
    var statusCodeOfRebroadcastTxn: String = ""
    var counterOfRebroadcastTxn: String = ""
    var merchantPostalCode: String = ""
    var eCommerceIndicator: String = ""
    var saleForcedOrSAF: String = ""
    var cv2PresentIndicator: String = ""
    var userField1C0: String = ""
    var additionalInfoIndicatorC0: String = "" 
    var authenticationCollectorIndicator: String = ""
    var propensityFlagTradeFraud: String = ""
    var cavvAvvValidationResult: String = ""


    //TOKEN EW
    var randomTkCipherEW: String = ""
    var checkValueEW: String = ""
    var rsaVersionEW: String = ""
    var rsaPaddingEW: String = ""
    var crc32EW: String = ""


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

    var deferral: String = "00"
    var numberOfPayments: String = ""
 var transferDataIndicator: String = ""
    var identificationMethodCardHolder: String = ""

    var emvData: String = ""

    var emvTagsMap= mutableMapOf<String,String>()



    var cipherData: String = ""
    var bdk: String = ""
    var ksn: String = ""


 
     // IOS DATA
    var iosTransactionId: String = ""
    var iosKeyData: String = ""
    var iosPinData: String = ""
    var iosCardholderData: String = ""
    var iosVersionId: String = ""
    var iosMerchantId: String = ""
  var iosCardDataHash: String = ""
    var iosPinDataHash: String = ""
 

    var originalTransactionData: TransactionDto?=null


  /// EVO
    
    var systemIdentification: String = "upay_socket_5g_TAP01"
    var companyIdentification: String = "TAPONPHONE"
    var branchIdentification: String = "1099421"
    var posIdentification: String = "TAPONPAOLA01" 




}