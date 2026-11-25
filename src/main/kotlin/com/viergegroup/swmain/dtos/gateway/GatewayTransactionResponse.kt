package com.viergegroup.swmain.dtos.gateway
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.transactions.TransactionFullResponseDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.util.*

import com.viergegroup.swmain.entities.catalogs.CardType
import com.viergegroup.swmain.entities.catalogs.HostType

import com.viergegroup.swmain.entities.catalogs.DeviceType
import com.viergegroup.swmain.entities.catalogs.PointOfServiceEntryMode
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.catalogs.MerchantType

import com.viergegroup.swmain.entities.catalogs.TransactionStatus


class GatewayResponseMeta: ResponseEntity() {
   
    var ksn: String = ""

    // --- TODOS LOS PARÁMETROS COPIADOS DE TransactionDto ---
    var fiid:String=""   
    var affiliation:String=""  
    var merchantName:String=""
    var merchantAddress:String=""
    var retailerDataAddOns:String="            00010001"
    var bankName:String="" 
    var merchantId:String="" 
    var orderId:String="" 
    var clientIP:String=""  
    var clientEmail:String=""
    var clientUserName:String=""
    var clientLocation:String="" 
    var clientId:String="VIERGE"

    var pinBlock: String = ""
    var pinClear: String = ""
    var pan: String = ""
    var ksnPin: String = ""
    var ksnPinTo: String = ""
    var deviceModel:String=""
    var hostType:HostType= HostType.Undefined
    var terminalSerialNumber: String = ""
    
    var deviceType: DeviceType = DeviceType.POS 
    var merchantType :MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var cardType: CardType = CardType.Undefined
 
    var noSkipCRC32:String=""
    var transactionStatus= TransactionStatus.ReceivedInWebService
    var originalWasRefund:Boolean=false
    var hasChip=false 
    var messageType:String=""
    var cardBin: String = "" 
    var validateData: String = "0"

    var cardNumber: String = "" 
    var cardExpirationDate: String = "" 
    var processingCode: String = "" 
    var transactionAmount: String = "" 
    var transmisionDateAndTime: String = "" 
 
    var systemTraceAuditNumber: String = "" 
    var localTransactionTime: String = "" 
    var localTransactionDate: String = "" 
    var settlementDate: String = "" 
    
    var transactionIdMpp: String = ""
    var captureDate: String = "" 
    var merchantTypeCode: String = "" 
    var merchantCurrencyCode: String = "" 
    var pointOfServiceEntryModeCode: String = "071" 
    var pointOfServiceConditionCode: String = "" 
    var keyId: String = ""
    var keyId1: String = ""
    var keyId2: String = ""
    var track2Data:String=""
    var track2:String=""
    var cvv:String=""

    var acquiringInstitutionIdentificationCode: String = "" 
    var authorizationIdResponse: String = "" 
    var originalRrn: String = ""
 
    var responseCode: String = "" 
    var responseCodeAuthorizer: String = ""  

    var cardAcceptorTerminalId: String = "" 
    var cardAcceptorIdCode: String = "" 
    var cardAcceptorNameAndLocation: String = "" 
    var additionalResponseDate: String = "" 

    var retailerData: String = "" 
    var transactionCurrencyCode: String = "" 
    var additionalAmounts: String = "" 
    var terminalData: String = "" 
    var cardIssuerCategoryResponseCodeData: String = "" 
    var postalCode: String = "" 
    var additionalData: String = "" 
    var expandedTokenData: String = "" 
    var forwardingInstitutionIdCode: String = "" 
    
    var dataElements: String = "" 
    var networkManagmentInformationCode: String = "" 
    var originalDataElements: String = "" 
    var replacementAmounts: String = "" 
    var receivingInstitutionIdCode: String = "" 
    var accountId: String = "" 
    var terminalAddressBranch: String = "" 
    var authorizationIndicators: String = "" 
    var cardIssuerCode: String = "" 

    var posInvoiceData: String = "" 
    var specialData: String = "" 
    var posSettlementData: String = "" 
    var posPreauthorizationAndChargeBackData: String = "" 
    var commercialReferences: String = ""
    var serverName:String=""

    var authorizationModeIdentifier: String = ""
    var userFieldQ1: String = ""
    var accessMediumIdentifier: String = ""
    var deferral: String = ""
    var numberOfPayments: String = ""
    var paymentPlanCode: String = ""
    var paymentQ6Data: String = ""
 
    var additionalErrorFlagRequestInTransaction: String = ""
    var routingCode: String = ""
    var cardVerificationFlag: String = ""
    var cityExension: String = ""
    var completeTrack2Data: String = ""
    var fileFlagUses: String = ""

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
 
    var xid: String = ""
    var cavvCardholderAuthVerValue: String = ""
    var indicatorCE: String = "" 
    var cardholderAuthenticationData: String = ""
    var atcCZ: String = ""
    var formFactrInd: String = ""
    var userFieldACICZ: String = ""

    var pekEX: String = ""
    var ksnEX: String = ""
    var checkValueEX: String = ""
    var newKeyRequestStatusEX: String = ""
    var newKeyRequestStatus: String = "0"
    var crc32EX: String = ""
    var tokenEW:String="" 
    var tokenEWFormatted:String="" 

    var randomTkCipherEW: String = ""
    var checkValueEW: String = ""
    var rsaVersionEW: String = ""
    var rsaPaddingEW: String = ""
    var crc32EW: String = ""
    var tkEW: String = ""

    var terminalSoftwareVersion: String = ""
    var cipherConfiguration: String = ""
    var localBinsTableIdPrimary: String = ""
    var localBinsTableIdSecondary: String = ""
    var binsTableVersion: String = ""
    var flagRequestNewKey: String = "0"

    var clearCipherBlocks= mutableListOf<String>()
    var chipherDataBlocks= mutableListOf<String>()
    var clearCipherDataEZ: String = ""
    var tokenEX: String = ""
    var tokenEZ: String = ""
    var tokenEZ2: String = ""
    var tokenEZFormated: String = ""
    var tokenEZ2Formated: String = ""
    var tokenOpenPayClear:String=""
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

    var posEntryMode: String = ""
    var terminalEntryCapabilities: String = ""
    var lastEmvStat: String = ""
    var dataSuspect: String = ""
    var applPanSeqNum: String = ""
    var devInfo: String = ""
    var rsnOnlyCode: String = ""
    var arqcVerify: String = ""
    var isoRCIndicator: String = ""

    var userField1B5: String = ""
    var emvIssAuthData:String=""
    var issAuthData:String=""
    var arpc:String=""
    var addlData:String=""
    var issAuthDataLength:String=""

    var issScriptDataLength: String = ""
    var issScriptData: String = ""

    var securityControlInfo: String = ""
    var additionalDataIva: String = ""
    var isoMac: String = ""
    var cryptographicServiceMessage: String = ""
    
    var dsTxnIdRJ: String = ""
    var authnRespRJ: String = ""
    var pgmProtoRJ: String = ""
    var userFldAciRJ: String = ""
    
    var chTokenData: String = ""
    var keyUpdateFlag: String = ""
    var binsUpdateFlag: String = ""
    var isoASCII: String = ""
    var emvData: String = ""
    var emvTagsMap= mutableMapOf<String,String>()

    var iosTransactionId: String = ""
    var iosKeyData: String = ""
    var iosPinData: String = ""
    var iosCardholderData: String = ""
    var iosVersionId: String = ""
    var iosMerchantId: String = ""
    
    var systemIdentification: String = "QATAPON"
    var companyIdentification: String = "QATAPON"
    var branchIdentification: String = "QATAPON02"
    var posIdentification: String = "QATAPON0201"

    var messageClassTR31: String=""               
    var receiverIdTR31: String=""          
    var originatorIdTR31: String=""        
    var serviceRequestCodeTR31: String=""  
    var keyDataHex: String=""         
    var expectedKeyCountHexTR31: String="" 
    var sentKeyCountHexTR31: String=""    
    var errorCodeTR31: String=""          
    var extrasRawTR31: String=""          

    var cardTypeInfo: String=""
}



class GatewayResponseDto
{
    var GatewayResponse= GatewayResponse()
    var Status=""

}

class GatewayResponse {
    var id="-1" 
    var source="ISSUER"
    var status="DECLINED"
    var actionCode="0001"
    var emvResponsePayload=""
    var meta=""
    var metaData:GatewayResponseMeta?=null
}


class GatewayTransactionResponse{

   /* var Response=GatewayResponseDto()
    var Status=""
    var TransactionId="" 
*/ 
   var gatewayResponse= GatewayResponse()


    fun injectResponse(transactionResponse: TransactionResponse){


 
    }
    fun injectResponse(dto: GatewayTransactionDto, transactionResponse: TransactionFullResponseDto){
    val GatewayResponse = this.gatewayResponse
    WatcherGlobalLog.info(" injectResponse ")

    GatewayResponse.id = dto.instanceRequest.id
    val codeEnum = ErrorCode.fromResponseCode(transactionResponse.transactionResponse.responseCode)
    var responseCode = transactionResponse.transactionResponse.responseCode
    var actionCode = responseCode
    if(StringUtils.isLetters(responseCode)){
        actionCode = "77" 
    }  

    val Status = codeEnum.toString().uppercase(Locale.getDefault()).replace(" ","_")
    
    val meta = GatewayResponseMeta()  
    GatewayResponse.actionCode = actionCode.padStart(4,'0') 
    
    // Referencia corta al TransactionDto para no repetir código
    val tr = transactionResponse.transactionResponse 

    meta.apply {
        // Datos de error y extras propios de Meta
        errorMessage = transactionResponse.errorMessage 
        errorCode = transactionResponse.errorCode
        
        // Mapeo exhaustivo de TODOS los campos de TransactionDto
        // (Nota: rrn lo asigne de originalRrn porque rrn estaba comentado en tu DTO)
        rrn = tr.rrn  
        ksn = tr.ksnEX 
        
        fiid = tr.fiid
        affiliation = tr.affiliation
        merchantName = tr.merchantName
        merchantAddress = tr.merchantAddress
        retailerDataAddOns = tr.retailerDataAddOns
        bankName = tr.bankName
        merchantId = tr.merchantId
        orderId = tr.orderId
        clientIP = tr.clientIP
        clientEmail = tr.clientEmail
        clientUserName = tr.clientUserName
        clientLocation = tr.clientLocation
        clientId = tr.clientId
        pinBlock = tr.pinBlock
        pinClear = tr.pinClear
        pan = tr.pan
        ksnPin = tr.ksnPin
        ksnPinTo = tr.ksnPinTo
        deviceModel = tr.deviceModel
        hostType = tr.hostType
        terminalSerialNumber = tr.terminalSerialNumber
        deviceType = tr.deviceType
        merchantType = tr.merchantType
        transactionType = tr.transactionType
        pointOfServiceEntryMode = tr.pointOfServiceEntryMode
        cardType = tr.cardType
        noSkipCRC32 = tr.noSkipCRC32
        transactionStatus = tr.transactionStatus 
        originalWasRefund = tr.originalWasRefund
        hasChip = tr.hasChip
        messageType = tr.messageType 
        cardBin = tr.cardBin
        validateData = tr.validateData
        cardNumber = tr.cardNumber
        cardExpirationDate = tr.cardExpirationDate
        processingCode = tr.processingCode
        transactionAmount = tr.transactionAmount
        transmisionDateAndTime = tr.transmisionDateAndTime
        systemTraceAuditNumber = tr.systemTraceAuditNumber
        localTransactionTime = tr.localTransactionTime
        localTransactionDate = tr.localTransactionDate
        settlementDate = tr.settlementDate
        transactionIdMpp = tr.transactionIdMpp
        captureDate = tr.captureDate
        merchantTypeCode = tr.merchantTypeCode
        merchantCurrencyCode = tr.merchantCurrencyCode
        pointOfServiceEntryModeCode = tr.pointOfServiceEntryModeCode  //correccion 6 
        pointOfServiceConditionCode = tr.pointOfServiceConditionCode 
        keyId = tr.keyId
        keyId1 = tr.keyId1
        keyId2 = tr.keyId2
       // track2Data = tr.track2Data
        track2 = tr.track2
        cvv = tr.cvv
        acquiringInstitutionIdentificationCode = tr.acquiringInstitutionIdentificationCode
        authorizationIdResponse = tr.authorizationIdResponse
        originalRrn = tr.originalRrn

        // -------- correccion 2
        this.responseCode = tr.responseCode 
        // -------------------------------------------------------------------------------

        responseCodeAuthorizer = tr.responseCodeAuthorizer
        cardAcceptorTerminalId = tr.cardAcceptorTerminalId
        cardAcceptorIdCode = tr.cardAcceptorIdCode
        cardAcceptorNameAndLocation = tr.cardAcceptorNameAndLocation
        additionalResponseDate = tr.additionalResponseDate
        retailerData = tr.retailerData
        transactionCurrencyCode = tr.transactionCurrencyCode
        additionalAmounts = tr.additionalAmounts
        terminalData = tr.terminalData
        cardIssuerCategoryResponseCodeData = tr.cardIssuerCategoryResponseCodeData
        postalCode = tr.postalCode
        additionalData = tr.additionalData
        expandedTokenData = tr.expandedTokenData
       // forwardingInstitutionIdCode = tr.forwardingInstitutionIdCode
        dataElements = tr.dataElements
        networkManagmentInformationCode = tr.networkManagmentInformationCode
        originalDataElements = tr.originalDataElements
        replacementAmounts = tr.replacementAmounts
        receivingInstitutionIdCode = tr.receivingInstitutionIdCode
        accountId = tr.accountId
        terminalAddressBranch = tr.terminalAddressBranch
        authorizationIndicators = tr.authorizationIndicators
        cardIssuerCode = tr.cardIssuerCode
        posInvoiceData = tr.posInvoiceData
        specialData = tr.specialData
        posSettlementData = tr.posSettlementData
        posPreauthorizationAndChargeBackData = tr.posPreauthorizationAndChargeBackData
        commercialReferences = tr.commercialReferences
        serverName = tr.serverName

        authorizationModeIdentifier = tr.authorizationModeIdentifier
        userFieldQ1 = tr.userFieldQ1
        accessMediumIdentifier = tr.accessMediumIdentifier
        deferral = tr.deferral
        numberOfPayments = tr.numberOfPayments
        paymentPlanCode = tr.paymentPlanCode
        paymentQ6Data = tr.paymentQ6Data
        additionalErrorFlagRequestInTransaction = tr.additionalErrorFlagRequestInTransaction
        routingCode = tr.routingCode
        cardVerificationFlag = tr.cardVerificationFlag
        cityExension = tr.cityExension
        completeTrack2Data = tr.completeTrack2Data
        fileFlagUses = tr.fileFlagUses

        userField1C0 = tr.userField1C0
        cvv2cvc2Result = tr.cvv2cvc2Result
        statusCodeOfRebroadcastTxn = tr.statusCodeOfRebroadcastTxn
        counterOfRebroadcastTxn = tr.counterOfRebroadcastTxn
        merchantPostalCode = tr.merchantPostalCode
        eCommerceIndicator = tr.eCommerceIndicator
        saleForcedOrSAF = tr.saleForcedOrSAF
        cv2PresentIndicator = tr.cv2PresentIndicator
        additionalInfoIndicatorC0 = tr.additionalInfoIndicatorC0
        authenticationCollectorIndicator = tr.authenticationCollectorIndicator
        propensityFlagTradeFraud = tr.propensityFlagTradeFraud
        cavvAvvValidationResult = tr.cavvAvvValidationResult

        attendedTerminal = tr.attendedTerminal
        terminalOperationIndex = tr.terminalOperationIndex
        terminalLocator = tr.terminalLocator
        cardholderPresenceIndicator = tr.cardholderPresenceIndicator
        cardPresenceIndicator = tr.cardPresenceIndicator
        cardCaptureCapacityIndicator = tr.cardCaptureCapacityIndicator
        statusIndicatorC4 = tr.statusIndicatorC4
        transactionSecurityLevel = tr.transactionSecurityLevel
        routingIndicator = tr.routingIndicator
        terminalActivationByCardholder = tr.terminalActivationByCardholder
        transferDataIndicator = tr.transferDataIndicator
        identificationMethodCardHolder = tr.identificationMethodCardHolder
        fillerC4 = tr.fillerC4

        xid = tr.xid
        cavvCardholderAuthVerValue = tr.cavvCardholderAuthVerValue
        indicatorCE = tr.indicatorCE
        cardholderAuthenticationData = tr.cardholderAuthenticationData
        atcCZ = tr.atcCZ
        formFactrInd = tr.formFactrInd
        userFieldACICZ = tr.userFieldACICZ

        pekEX = tr.pekEX
        ksnEX = tr.ksnEX
        checkValueEX = tr.checkValueEX
        newKeyRequestStatusEX = tr.newKeyRequestStatusEX
        newKeyRequestStatus = tr.newKeyRequestStatus
        crc32EX = tr.crc32EX
        tokenEW = tr.tokenEW
        tokenEWFormatted = tr.tokenEWFormatted

        randomTkCipherEW = tr.randomTkCipherEW
        checkValueEW = tr.checkValueEW
        rsaVersionEW = tr.rsaVersionEW
        rsaPaddingEW = tr.rsaPaddingEW
        crc32EW = tr.crc32EW
        tkEW = tr.tkEW

        terminalSoftwareVersion = tr.terminalSoftwareVersion
        cipherConfiguration = tr.cipherConfiguration
        localBinsTableIdPrimary = tr.localBinsTableIdPrimary
        localBinsTableIdSecondary = tr.localBinsTableIdSecondary
        binsTableVersion = tr.binsTableVersion
        flagRequestNewKey = tr.flagRequestNewKey

        clearCipherBlocks = ArrayList(tr.clearCipherBlocks)
        chipherDataBlocks = ArrayList(tr.chipherDataBlocks)
        clearCipherDataEZ = tr.clearCipherDataEZ
        tokenEX = tr.tokenEX
        tokenEZ = tr.tokenEZ
        tokenEZ2 = tr.tokenEZ2
        tokenEZFormated = tr.tokenEZFormated
        tokenEZ2Formated = tr.tokenEZ2Formated
        tokenOpenPayClear = tr.tokenOpenPayClear
        tokenOpenPay = tr.tokenOpenPay

        keySerialNumber = tr.keySerialNumber
        cipherCounter = tr.cipherCounter
        failCipherCounter = tr.failCipherCounter
        flagTrack2 = tr.flagTrack2
        readCardMode = tr.readCardMode
        track2Length = tr.track2Length
        cvvFlag = tr.cvvFlag
        clearCvvLength = tr.clearCvvLength
        track1Flag = tr.track1Flag
        cipherData = tr.cipherData
        lastPan4Digits = tr.lastPan4Digits
        crc32EZ = tr.crc32EZ
        
        bitmapB2 = tr.bitmapB2
        userField1B2 = tr.userField1B2
        criptoInfoData = tr.criptoInfoData
        tvr = tr.tvr
        otherAmount = tr.otherAmount
        aip = tr.aip
        arqc = tr.arqc
        atc = tr.atc
        terminalCountryCode = tr.terminalCountryCode
        transDate = tr.transDate
        transactionTypeCode = tr.transactionTypeCode
        emvTransactionType = tr.emvTransactionType
        transactionType = tr.transactionType

        unpredictableNumber = tr.unpredictableNumber
        issApplDataLength = tr.issApplDataLength
        issApplData = tr.issApplData  

        bitmapB3 = tr.bitmapB3
        emvTermSerialNumber = tr.emvTermSerialNumber
        emvTerminalCapabilities = tr.emvTerminalCapabilities
        userField1B3 = tr.userField1B3
        userField2B3 = tr.userField2B3
        emvLocalTransactionDate = tr.emvLocalTransactionDate
        emvTerminalType = tr.emvTerminalType
        applicationVersionNumber = tr.applicationVersionNumber
        cvmResults = tr.cvmResults
        dfNameLength = tr.dfNameLength
        dfName = tr.dfName

        posEntryMode = tr.posEntryMode
        terminalEntryCapabilities = tr.terminalEntryCapabilities
        lastEmvStat = tr.lastEmvStat
        dataSuspect = tr.dataSuspect
        applPanSeqNum = tr.applPanSeqNum
        devInfo = tr.devInfo
        rsnOnlyCode = tr.rsnOnlyCode
        arqcVerify = tr.arqcVerify
        isoRCIndicator = tr.isoRCIndicator

        userField1B5 = tr.userField1B5
        emvIssAuthData = tr.emvIssAuthData
        issAuthData = tr.issAuthData
        arpc = tr.arpc
        addlData = tr.addlData
        issAuthDataLength = tr.issAuthDataLength

        issScriptDataLength = tr.issScriptDataLength
        issScriptData = tr.issScriptData

        securityControlInfo = tr.securityControlInfo
        additionalDataIva = tr.additionalDataIva
        isoMac = tr.isoMac
        cryptographicServiceMessage = tr.cryptographicServiceMessage

        //dsTxnIdRJ = tr.dsTxnIdRJ
    //    authnRespRJ = tr.authnRespRJ
      //  pgmProtoRJ = tr.pgmProtoRJ
       // userFldAciRJ = tr.userFldAciRJ
        chTokenData = tr.chTokenData

        keyUpdateFlag = tr.keyUpdateFlag
        binsUpdateFlag = tr.binsUpdateFlag
        isoASCII = tr.isoASCII
        emvData = tr.emvData

        emvTagsMap = HashMap(tr.emvTagsMap)

        iosTransactionId = tr.iosTransactionId
        iosKeyData = tr.iosKeyData
        iosPinData = tr.iosPinData
        iosCardholderData = tr.iosCardholderData
        iosVersionId = tr.iosVersionId
        iosMerchantId = tr.iosMerchantId

        systemIdentification = tr.systemIdentification 
        companyIdentification = tr.companyIdentification
        branchIdentification = tr.branchIdentification
        posIdentification = tr.posIdentification

      //  messageClassTR31 = tr.messageClassTR31
      //  receiverIdTR31 = tr.receiverIdTR31
     //   originatorIdTR31 = tr.originatorIdTR31
     //   serviceRequestCodeTR31 = tr.serviceRequestCodeTR31
      //  keyDataHex = tr.keyDataHex
      // expectedKeyCountHexTR31 = tr.expectedKeyCountHexTR31
      //  sentKeyCountHexTR31 = tr.sentKeyCountHexTR31
     //   errorCodeTR31 = tr.errorCodeTR31
      //  extrasRawTR31 = tr.extrasRawTR31
      //  cardTypeInfo = tr.cardTypeInfo
    }

    // ---- correccion 2
    if (meta.responseCode == "" || (meta.responseCode == "00" && transactionResponse.errorCode != 0)) {
        val errorCodeEnum = ErrorCode.fromId(transactionResponse.errorCode)
        meta.responseCode = errorCodeEnum.responseCode 
        
        if (meta.responseCode == "") {
            val fallbackEnum = ErrorCode.fromId(transactionResponse.transactionResponse.errorCode )
            meta.responseCode = fallbackEnum.responseCode // <- Faltaba realizar la asignación
        }  
    }      
    // ---------------------------------------------------------------------

    try { 
        val resMeta = StringUtils.encodeBase64(meta)
        GatewayResponse.meta = resMeta
        GatewayResponse.metaData = meta
        WatcherGlobalLog.debug(" GatewayResponse_meta   ${resMeta}")
        if(GatewayResponse.metaData!=null){
            WatcherGlobalLog.debug(" GatewayResponse_metaData ")
            WatcherGlobalLog.obj(GatewayResponse.metaData!!)
        }
    } catch (e:Exception){
        WatcherGlobalLog.error("ERROR_DECODING_META  ${e.message}")
    }

    val emvRequestPayloadAscii = responseCode.toCharArray().joinToString("") {
        it.toInt().toString(16).padStart(2, '0')
    }
    val responseCodeLength = responseCode.length.toString(16).padStart(2, '0')
    GatewayResponse.emvResponsePayload = "8A$responseCodeLength$emvRequestPayloadAscii"

    GatewayResponse.status=Status
    GatewayResponse.metaData=meta
    GatewayResponse.id= GatewayResponse.id
    this.gatewayResponse=GatewayResponse
}

}