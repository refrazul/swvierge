package com.viergegroup.swmain.dtos.pandora

import com.fasterxml.jackson.annotation.JsonInclude
import com.shared.commons.AppSettings
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.crypto.security.CryptoPinAlgo

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PandoraTransactionDto: RequestEntity() {

    var errorCode=0
    var errorMessage=""   
    var bdk: String = ""
    var pek: String = ""
    //var ipek: String = ""
 

    var sessionKey: String = ""
    var terminalProfileId: String = "" 

    var rsaKeyId: String = "" 
    var keyId: String = ""
    var keyId1: String = ""
    var keyId2: String = ""
 
    var pinBlockClear: String = ""
    var bankName:String="" 

    var pinBlock: String = ""
    var pinClear: String = ""
    var pan: String = ""
    var ksnPin: String = ""
    var ksnPinTo: String = "" 
    var pinAlgo: CryptoPinAlgo = CryptoPinAlgo.AES
    var pinAlgo2: CryptoPinAlgo = CryptoPinAlgo.TDES

    var merchantId:String="" 
    var orderId:String=""
    
    var cvv: String = ""
    var transactionAmount:String=""
    var validateData: String = "0"

    var track2: String = ""

    var ipek:String=""
    var ipekClear:String=""

    //TOKEN EX
    var tokenEX:String=""
    var pekEX: String = ""
    var ksnEX: String = ""
    var checkValueEX: String = ""
    var newKeyRequestStatusEX: String = ""
    var crc32EX: String = ""



    //TOKEN EZ

    var tokenEZ: String = ""
    var tokenEZ2: String = ""
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

    //TOKEN EW
    var tokenEW: String = ""
    var tkEW: String = ""
    var tkClear: String = ""

    var randomTkCipherEW: String = ""
    var rsaVersionEW: String = ""
    var rsaPaddingEW: String = ""
    var checkValueEW: String = ""
    var crc32EW: String = ""

    var logData:String=""
    var tokenOpenPayClear:String=""

    var tokenOpenPay:String=""

    var pandoraVersion =AppSettings.pandoraVersion
 

    var cardNumber: String = ""
 
     // IOS DATA
   var iosTransactionId: String = ""
    var iosKeyData: String = ""
    var iosPinData: String = ""
    var iosCardholderData: String = ""
    var iosVersionId: String = ""
    var iosMerchantId: String = ""
  var iosCardDataHash: String = ""
    var iosPinDataHash: String = ""
    

}