
package com.viergegroup.swmain.entities.transactions

import com.google.gson.GsonBuilder
import com.shared.commons.*
import com.viergegroup.isoprocessor.models.iso.Iso8583Message
import com.viergegroup.isoprocessor.models.iso.Iso8583MessageType
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtosgsonadapters.TransactionDtoGSONAdapter
import com.viergegroup.swmain.entities.catalogs.*

import com.viergegroup.swmain.webwatcher.Watcher
import org.springframework.beans.BeanUtils 
import java.lang.reflect.Field


class TransactionData {
 
    fun constructor() {}
    fun constructor(transactionDto: TransactionDto){
        BeanUtils.copyProperties( this,transactionDto);

    } 
    var uuid: String=""

    
  
    var fiid:String=""  
            var transactionIdMpp: String = ""
   
    var merchantName:String=""
  // var merchantTypeCode:String=""
    var merchantAddress:String=""
    var retailerDataAddOns:String="            00010001"
    var bankName:String=""  


    var transactionStatus=TransactionStatus.ReceivedInWebService
    var originalAuthorizationId:String=""
       var deviceType: DeviceType = DeviceType.POS 

    var hostType:HostType= HostType.TestingDUKPTHost

    var merchantType :MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var keyDirection: KeyDirection = KeyDirection.Undefined

    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined

    var transactionDirection: TransactionDirection = TransactionDirection.Request
    var paymentPlanType: PaymentPlanType= PaymentPlanType.Undefined

   // var cardTypeEnum:CardType=CardType.Undefined
    var cardType: CardType = CardType.Undefined

    private var isoMessage: Iso8583Message?=null 
    var affiliation:String=""
    
    var keyId:String=""
    var keyId1:String=""
    var keyId2:String=""
    var noSkipCRC32:String=""

    //var cardType:String=""
    var track2:String=""

    var cvv:String=""
    var cardNumber: String = "" //FIELD 3
    var cardExpirationDate: String = "" //FIELD 14
    var cardBin: String = "" //FIELD 3


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

    var acquiringInstitutionIdentificationCode: String = "" //FIELD 32

    var rrn: String = "" //FIELD 37
    var originalRrn: String = ""
    var authorizationIdResponse: String = "" //FIELD 38


    
    var responseCodeAuthorizer: String = "" //FIELD 39


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
    var expandedTokenData: String = "" //FIELD 57

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
    var crc32EX: String = ""
    var tokenEX: String = ""



    //TOKEN EW
    var tkEW: String = ""
    var randomTkCipherEW: String = ""
    var rsaVersionEW: String = ""
    var rsaPaddingEW: String = ""
    var checkValueEW: String = ""
    var crc32EW: String = ""
    var tokenEW:String=""



    //TOKEN ES
    var terminalSoftwareVersion: String = ""
    var terminalSerialNumber: String = ""
    var cipherConfiguration: String = ""
    var localBinsTableIdPrimary: String = ""
    var localBinsTableIdSecondary: String = ""
    var binsTableVersion: String = ""
    var flagRequestNewKey: String = "0"
    //TOKEN EZ
    var cipherDataEZ: String = ""
    var clearCipherBlocks= mutableListOf<String>()
    var chipherDataBlocks= mutableListOf<String>()
    var clearCipherDataEZ: String = ""
    var tokenEZ: String = ""
    var tokenEZ2: String = ""
    var tokenEZFormated: String = ""
    var tokenEZ2Formated: String = ""

    var tokenEZProsa: String = ""
    var tokenEZEglobal: String = ""
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
    var bitmapB3: String = ""
    var emvTermSerialNumber: String = ""
    var emvTerminalCapabilities: String = ""
    var userField1B3: String = ""
    var userField2B3: String = ""
    var emvLocalTransactionDate:String=""
    var emvTerminalType: String = ""
    var applicationVersionNumber: String = ""
    var cvmResults: String = ""
    var dfNameLength: String = ""
    var dfName: String = ""

           //CH
    var chTokenData: String = ""

    
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

 


    

   
     // IOS DATA
    var iosTransactionId: String = ""
    var iosKeyData: String = ""
    var iosPinData: String = ""
    var iosCardholderData: String = ""
    var iosVersionId: String = ""




    //TOKEN ER

    var keyUpdateFlag: String = ""
    var binsUpdateFlag: String = ""


    var emvData: String = ""
    var emvTagsMap= mutableMapOf<String,String>()


 //CREDIBANCO ONLY 
 
    var securityControlInfo: String = ""
    var additionalDataIva: String = ""
    var isoMac: String = ""
    var cryptographicServiceMessage: String = ""


    fun getValueTagEmv(tag:String):String{

        if(emvTagsMap.containsKey(tag)){
            return emvTagsMap[tag]!!
        }
        return ""

    }
    fun dataApplyToBuildTOKENEZ():Boolean{

        return transactionType!=TransactionType.EchoTest
                &&transactionType!=TransactionType.KeyLoad
                &&transactionType!=TransactionType.Cancellation
                &&transactionType!=TransactionType.Reverse
                &&transactionType!=TransactionType.Refund
                &&transactionType!=TransactionType.LogOn
                &&(
                pointOfServiceEntryMode==PointOfServiceEntryMode.Contactless
                ||pointOfServiceEntryMode==PointOfServiceEntryMode.Inserted
                ||pointOfServiceEntryMode==PointOfServiceEntryMode.Swiped
                ||pointOfServiceEntryMode==PointOfServiceEntryMode.KeyLoad

                )&&transactionDirection==TransactionDirection.Request
    }

    fun applyForReverse():Boolean{

        return transactionType.transactionMessageType== TransactionMessageType.Financial

    }


    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(fieldName: String?, value: String) {
        val field: Field = javaClass.getDeclaredField(fieldName)


        field.set(this, value)
    }
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getField(fieldName: String):String {
        val field: Field = javaClass.getDeclaredField(fieldName)
        field.isAccessible=true

        return  field.get(this) as String
    }

    fun getIsoBuffer():String{
        if(isoMessage==null) return ""
        return isoMessage!!.isoMessageBuffer
    }


    fun getMessageTypeString():String{
        if(isoMessage==null) return ""
        return isoMessage!!.isoMessageType.messageTypeId
    }

    fun getMessageType():Iso8583MessageType{
        if(isoMessage==null) return  Iso8583MessageType.Message0200
        return isoMessage!!.isoMessageType
    }
    fun getIsoResponseCode():String{
        if(isoMessage==null) return  "NU"
        val strCode=isoMessage!!.getFieldValue(39)
        if(strCode == "") return "00"
        return isoMessage!!.getFieldValue(39).padStart(2,'0')
    }

    fun getIsoErrorDescriptionResponseCode():String{
        if(isoMessage==null) return  ErrorCode.Undefined.errorCodeMessage

       val stri= ErrorCode.fromResponseCode(getIsoResponseCode()).errorCodeMessage

        return stri

    }

    fun isLogOnRequestMessage(watcher: Watcher):Boolean{
       watcher.debug("isLogOnMessage "+getMessageType()+" "+networkManagmentInformationCode)
        return (getMessageType()==Iso8583MessageType.Message0800&&networkManagmentInformationCode.equals("001"))
    }
    fun isEchoTestRequestMessage(watcher: Watcher):Boolean{
       watcher.debug("isEchoTestMessage "+getMessageType()+" "+networkManagmentInformationCode)

        return (getMessageType()==Iso8583MessageType.Message0800&&networkManagmentInformationCode.equals("301"))
    }

    fun getIsoASCII():String{


        if(isoMessage==null) return ""


        return isoMessage!!.buildIsoBuffer()

    }
    fun describe():String{

        if(isoMessage==null) return ""


        return isoMessage!!.describe()

    }
    fun describeJSON():String{

       // if(isoMessage==null) return ""

       val transactioDto=TransactionDto()
       StringUtils.copyNonEmptyProperties(this, transactioDto)
        val jsonString = GsonConfig.gsonLogs.toJson(transactioDto)
        return jsonString;
       // return jsonString+"\n\n\n"

    }
    fun describeJSONInLine():String{

        if(isoMessage==null) return ""

        val transactioDto=TransactionDto()
        
        BeanUtils.copyProperties(this,transactioDto)

        val jsonString = GsonConfig.gsonLogs.toJson(transactioDto)
        return jsonString; 
        // return jsonString+"\n\n\n"

    }

    fun injectIso8583(isoMessage:Iso8583Message){
        this.isoMessage=isoMessage
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0200){
            transactionDirection=TransactionDirection.Request
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0210){
            transactionDirection=TransactionDirection.Response
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0100){
            transactionDirection=TransactionDirection.Request
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0110){
            transactionDirection=TransactionDirection.Response
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0400){
            transactionDirection=TransactionDirection.Request
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0410){
            transactionDirection=TransactionDirection.Response
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0800){
            transactionDirection=TransactionDirection.Request
        }
        if(isoMessage.isoMessageType==Iso8583MessageType.Message0810){
            transactionDirection=TransactionDirection.Response
        }



    }
}