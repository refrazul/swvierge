package com.viergegroup.crypto.pandora.managers

import HsmEncryptor
import HsmKeys
import HsmMessage
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.DukptData

import com.viergegroup.crypto.security.BankRsaKey
import com.viergegroup.crypto.security.Dukpt
import com.viergegroup.crypto.security.RSAUtils
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.dtos.pandora.TDesDto
import com.viergegroup.swmain.entities.catalogs.Authorizer

import com.viergegroup.swmain.webwatcher.WatcherContext

import org.springframework.beans.BeanUtils
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.* 
import com.viergegroup.swmain.entities.catalogs.TransactionType

import com.shared.commons.StringUtils.maskString

class SpecialCipherManager {

    private  val  watcher= WatcherContext.getInstance()



    fun keyLoad(dto: PandoraTransactionDto):PandoraTransactionDto{
 
       watcher.debug("SpecialCipherManager_keyLoad: [${dto.hostType}]  [${dto.terminalSerialNumber}]")
        val response = PandoraTransactionDto()
        val  dtoRSA= com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadRSAData()
        dtoRSA.id=dto.hostType.authorizer.rsaKeyId
        dtoRSA.terminalSerialNumber=dto.terminalSerialNumber
        dtoRSA.rrn=dto.rrn
        dtoRSA.hostType=dto.hostType
        val rsakey= com.viergegroup.crypto.pandora.managers.VaultSecureManager().readRsaKey(dtoRSA)
        if(rsakey.errorCode!=0){
            response.errorCode=rsakey.errorCode
            response.errorMessage="\"${dtoRSA.id}\" "+rsakey.errorMessage
            return response
        }
        try{
            // val ipekBytes=StringUtils.hexStringToByteArray(dto.ipek)
            // val pekBytes=RSAUtils.decryptText(StringUtils.hexStringToByteArray(rsakey.rsaKey),ipekBytes)
            // val pek=StringUtils.encodeHexString(pekBytes)
            val pek = StringUtils.generateRandomHexadecimal(32)
            val ksn = StringUtils.generateRandomHexadecimal(16)+ "0001"
            val dukptRes= Dukpt.saveClearPek(dto.hostType,dto.terminalSerialNumber,dto.keyId,ksn,pek,watcher)
            val ipekBytes=StringUtils.hexStringToByteArray(pek)
            val pekBytes=RSAUtils.encryptText(StringUtils.hexStringToByteArray(rsakey.rsaKey),ipekBytes)

            response.keySerialNumber=ksn
            response.ipek=StringUtils.encodeHexString(pekBytes)
            response.crc32EX=StringUtils.encodeHexString(RSAUtils.CRC32Calculate(ipekBytes))
            if(dukptRes.errorCode!=0){
                response.errorCode=dukptRes.errorCode
                response.errorMessage="ERROR INJECTING KEY | "+dukptRes.errorMessage
               watcher.debug("ERROR SAVING PEK:  ${dukptRes.errorMessage}")
                return response
            }
        }catch (ex:Exception){
            response.errorCode=ErrorCode.RsaFailed.errorCodeInt
            response.errorMessage=ErrorCode.RsaFailed.errorCodeMessage+"| ${ex.message}"
        }
        return response
    }



    fun decryptRsa(dto: PandoraTransactionDto):PandoraTransactionDto{
        val response = PandoraTransactionDto()
        val ipekBytes=StringUtils.hexStringToByteArray(dto.ipek)
        val  dtoRSA= com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadRSAData()
        dtoRSA.id=dto.hostType.authorizer.rsaKeyId+"PRV"
        dtoRSA.terminalSerialNumber=dto.terminalSerialNumber
        dtoRSA.rrn=dto.rrn
        dtoRSA.hostType=dto.hostType
        val rsakey= com.viergegroup.crypto.pandora.managers.VaultSecureManager().readRsaKey(dtoRSA)
        if(rsakey.errorCode!=0){
            response.errorCode=rsakey.errorCode
            response.errorMessage=rsakey.errorMessage
            return response
        }
        try{
            val pekBytes=RSAUtils.decryptText(StringUtils.hexStringToByteArray(rsakey.rsaKey),ipekBytes)
            val pek=StringUtils.encodeHexString(pekBytes)
            response.ipek=pek
            response.errorCode=0
        }catch (ex:Exception){
            response.errorCode=ErrorCode.RsaFailed.errorCodeInt
            response.errorMessage=ErrorCode.RsaFailed.errorCodeMessage+"| ${ex.message}"
        }
        return response
    }

    fun doSpecialCipherOpenPay(dto: PandoraTransactionDto): PandoraTransactionDto {
 
       watcher.debug("doSpecialCipherOpenPay")
       watcher.debug(" doSpecialCipherOpenPay_keys ORIGINAL dto.keyId1 ${ dto.keyId1}  dto.keyId2 ${ dto.keyId2} ")

        val response = PandoraTransactionDto() 
        dto.keyId1=dto.keyId1.trim()
        dto.keyId2=dto.keyId2.trim()
        val uniqueKey=dto.keyId1.equals("BCMER001")&&dto.keyId2.equals("")||
                dto.keyId2.equals("")&&dto.keyId2.equals("BCMER001")||
                dto.keyId1.equals("A000OPFT48")&&dto.keyId2.equals("")||
                dto.keyId1.equals("")&&dto.keyId2.equals("A000OPFT48")||

                dto.keyId1.equals("BCMER001")&&dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId2.equals("OPENPUBLIC")&&dto.keyId2.equals("BCMER001")||
                dto.keyId1.equals("A000OPFT48")&&dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId1.equals("OPENPUBLIC")&&dto.keyId2.equals("A000OPFT48")||

                dto.keyId1.equals("BCMER001")&&dto.keyId2.equals("OPENPRIVAD")||
                dto.keyId2.equals("OPENPRIVAD")&&dto.keyId2.equals("BCMER001")||
                dto.keyId1.equals("A000OPFT48")&&dto.keyId2.equals("OPENPRIVAD")||
                dto.keyId1.equals("OPENPRIVAD")&&dto.keyId2.equals("A000OPFT48")

        val mustBuildOpenToken=dto.keyId1.equals("OPENPUBLIC")||
                dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId1.equals("OPENPRIVAD")||
                dto.keyId1.equals("OPENPRIVAD")||
                dto.keyId2.equals("OPENPRIVAD")

        val duplicatedOpenKeys=dto.keyId1.equals("OPENPUBLIC")&&  dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId1.equals("OPENPUBLIC")&&dto.keyId2.equals("OPENPRIVAD")||
                dto.keyId1.equals("OPENPRIVAD")&&  dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId1.equals("OPENPRIVAD")&& dto.keyId2.equals("OPENPRIVAD")

        if(duplicatedOpenKeys){
            dto.keyId1="OPENPUBLIC"
            dto.keyId2=""
        }
        if(dto.keyId1.equals("")&&!dto.keyId2.equals("")){
            dto.keyId1=dto.keyId2
            dto.keyId2=""
        }

        val isOnlyOpenKey=dto.keyId1.equals("OPENPUBLIC")&&dto.keyId2.equals("")||
                dto.keyId2.equals("")&&dto.keyId2.equals("OPENPUBLIC")||
                dto.keyId1.equals("OPENPRIVAD")&&dto.keyId2.equals("")||
                dto.keyId1.equals("")&&dto.keyId2.equals("OPENPRIVAD")


        if(dto.keyId1.equals("OPENPUBLIC")||dto.keyId1.equals("OPENPRIVAD")){
            dto.keyId1=""
        }
        if(dto.keyId2.equals("OPENPUBLIC")||dto.keyId2.equals("OPENPRIVAD")){
            dto.keyId2=""
        }

        if(dto.keyId1.equals("DOUBLEENCR")){ 
            dto.keyId1="BCMER001"
            dto.keyId2="A000OPFT48" 
        } 
        if(dto.keyId2.equals("BCMER001")&&dto.keyId1==""){
            dto.keyId1="BCMER001"
            dto.keyId2=""
        } 
        
        if(dto.keyId2.equals("A000OPFT48")&& dto.keyId1==""){
            dto.keyId1="A000OPFT48"
            dto.keyId2=""  
        } 
        if(dto.keyId1.equals("DOUBLEENCR")||dto.keyId2.equals("DOUBLEENCR")){
            dto.keyId1="BCMER001"
            dto.keyId2="A000OPFT48"

        } 
        watcher.debug("uniqueKey $uniqueKey,mustBuildOpenToken $mustBuildOpenToken ,duplicatedOpenKeys $duplicatedOpenKeys  , isOnlyOpenKey $isOnlyOpenKey")
       watcher.debug("doSpecialCipherOpenPay_newparams dto.merchantId ${ dto.merchantId}  dto.orderId ${ dto.orderId } ")
       watcher.debug(" doSpecialCipherOpenPay_keys NEW dto.keyId1 ${ dto.keyId1}  dto.keyId2 ${ dto.keyId2} ")
 

        if(mustBuildOpenToken){
            val tokenOpenPayRes=buildOpenPayToken(dto)
            if(tokenOpenPayRes.errorCode!=0){
                response.errorCode=tokenOpenPayRes.errorCode
                response.errorMessage="ERROR BUILDING TOKENOPENPAY:"+tokenOpenPayRes.errorMessage
                return response
            }
            response.tokenOpenPay=tokenOpenPayRes.tokenOpenPay
            response.tokenOpenPayClear=tokenOpenPayRes.tokenOpenPayClear

        }

        if( !dto.keyId1.equals("")){
            dto.keyId=dto.keyId1
            val buildTokenEzRes=buildTokenEZ(dto)
            if(buildTokenEzRes.errorCode!=0){
                response.errorCode=buildTokenEzRes.errorCode
                response.errorMessage="ERROR BUILDING TOKENEZ WITH  keyId1 ${dto.keyId1}/"+buildTokenEzRes.errorMessage
                return response
            }
            response.tokenEZ=buildTokenEzRes.tokenEZ

        }
        if( !dto.keyId2.equals("")){
            dto.keyId=dto.keyId2
            val buildTokenEzRes=buildTokenEZ(dto)
            if(buildTokenEzRes.errorCode!=0){
                response.errorCode=buildTokenEzRes.errorCode
                response.errorMessage="ERROR BUILDING TOKENEZ WITH  keyId2 ${dto.keyId2}/"+buildTokenEzRes.errorMessage
                return response
            }
            response.tokenEZ2=buildTokenEzRes.tokenEZ

        }


        return response
    }



 

    fun buildOpenPayToken(dto: PandoraTransactionDto):PandoraTransactionDto{

       watcher.debug("buildOpenPayToken")

        val response = PandoraTransactionDto()
        if(dto.hostType.authorizer!= Authorizer.OPENPAY){
            response.errorCode=-1
            response.errorMessage="Is Not Available"
            return response
        }
        val newCvv = if (dto.cvv.isEmpty()) "0" else dto.cvv
        val newTrack2= dto.track2
        val fechaHoraActual = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
        val date=fechaHoraActual.format(formatter)
       watcher.debug("transactionAmount "+dto.transactionAmount)
      //  var tokenOpenPay="0|${newTrack2}|${newCvv}|${date}|00|07|22|${dto.transactionAmount.toLong()}|"
        var tokenOpenPay="0|${newTrack2}|${newCvv}|${date}|00|07|22|${dto.transactionAmount.toLong()}|${dto.orderId}|${dto.merchantId}|"
        response.tokenOpenPayClear=tokenOpenPay
  
       /* 
       if(dto.merchantId!=""||dto.orderId!=""){
            tokenOpenPay="0|${newTrack2}|${newCvv}|${date}|00|07|22|${dto.transactionAmount.toLong()}|${dto.orderId}|${dto.merchantId}|"
        }*/ 
      
        watcher.debug("buildOpenPayToken_tokenOpenPay : ${maskString(tokenOpenPay)}")
        if(tokenOpenPay.length%16==0){
            tokenOpenPay+= tokenOpenPay+" "
        }
        val hsmEncryptor = HsmEncryptor()
        val message: ByteArray =  tokenOpenPay.toByteArray(StandardCharsets.UTF_8)
        val encrypted: HsmMessage = hsmEncryptor.encryptMessage(HsmKeys(dto.hostType), message) 
      //  tokenOpenPay=encrypted.toString() 
        response.tokenOpenPay=encrypted.toString()

        return response
    }


    fun buildTokenEZ(dto: PandoraTransactionDto): PandoraTransactionDto {


       watcher.debug("buildTokenEZ")

        val response = PandoraTransactionDto()
        var computedKeys=false
        if(!dto.readCardMode.equals("")){
            response.readCardMode=dto.readCardMode
        } 
        if(!dto.track2Length.equals("")){
            response.track2Length=dto.track2Length
        }
        val computeInternalKeys=dto.hostType.authorizer.computeInternalKeys
       watcher.debug("computeInternalKeys ${computeInternalKeys}")

        if(!Dukpt.existsKey(dto.hostType,dto.terminalSerialNumber,dto.keyId,dto.rrn,watcher)){
            if(computeInternalKeys){
               watcher.debug("KEYS DOESNT EXIST ${dto.hostType} ${dto.terminalSerialNumber} ${dto.keyId} ${dto.rrn}")
               watcher.debug("COMPUTE KEYS")
                com.viergegroup.crypto.pandora.managers.DukptManager().computeKeys(dto)
                computedKeys=true

            }else{
                response.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
                response.errorMessage=ErrorCode.KeyLoadIsNecesary.errorCodeMessage
                return response
            }
        }

            // Logging de los parámetros enviados a Dukpt.encryptData
          

        val resultDukpt =
            Dukpt.encryptData(dto.hostType,dto.terminalSerialNumber,dto.keyId,dto.rrn,dto.track2, watcher);
        if(resultDukpt.errorCode!=0){
            response.errorCode=resultDukpt.errorCode
            response.errorMessage=resultDukpt.errorMessage
        }else{

            response.errorCode=0
            response.errorMessage="SUCCESS"
            response.cipherData=resultDukpt.encryptResult
            response.crc32EZ=resultDukpt.crc32.padStart(8,'0')
            response.cipherCounter=(resultDukpt.cipherCounter-1).toString().padStart(7,'0')
            response.keySerialNumber=resultDukpt.ksn.uppercase()
            response.clearCvvLength=dto.cvv.length.toString().padStart(2,'0')
         
            if (response.failCipherCounter.isEmpty()) {
                response.failCipherCounter = "00"
            }
            if (response.flagTrack2.isEmpty()) {
                response.flagTrack2 = "1"
            }
            if (response.readCardMode.isEmpty()) {
                response.readCardMode = "07"
            }
            if (response.cvvFlag.isEmpty()) {
                response.cvvFlag = "A"
            }
            if (response.clearCvvLength.isEmpty()) {
                response.clearCvvLength = "00"
            }

            if (response.track1Flag.isEmpty()) {
                response.track1Flag = "0"
            }
            if (response.track1Flag.isEmpty()) {
                response.track1Flag = "0"
            }


            //               restrackresponse.clearCvvLength+response.track1Flag+response.cipherData+response.lastPan4Digits+response.crc32EZ



    // Log the dto and response fields
            watcher.debug("buildTokenEZ_hostType: [${dto.hostType}]")
            watcher.debug("buildTokenEZ_terminalSerialNumber: (${dto.terminalSerialNumber.length})[${dto.terminalSerialNumber}]")
            watcher.debug("buildTokenEZ_keyId: (${dto.keyId.length})[${dto.keyId}]")
            watcher.debug("buildTokenEZ_rrn: (${dto.rrn.length})[${dto.rrn}]")
            watcher.debug("buildTokenEZ_track2: (${dto.track2.length})[${maskString(dto.track2)}]")
            watcher.debug("buildTokenEZ_track1Flag: (${dto.track2.length})[${response.track1Flag}]")
            watcher.debug("buildTokenEZ_track2Length: (${dto.track2.length})[${dto.track2Length}]")
            watcher.debug("buildTokenEZ_lastPan4Digits: (${dto.lastPan4Digits.length})[${dto.lastPan4Digits}]")

            // Log the main response fields
            watcher.debug("buildTokenEZ_cipherData: (${response.cipherData.length})[${response.cipherData}]")
            watcher.debug("buildTokenEZ_crc32EZ: (${response.crc32EZ.length})[${response.crc32EZ}]")
            watcher.debug("buildTokenEZ_cipherCounter: (${response.cipherCounter.length})[${response.cipherCounter}]")
            watcher.debug("buildTokenEZ_keySerialNumber: (${response.keySerialNumber.length})[${response.keySerialNumber}]")
            watcher.debug("buildTokenEZ_clearCvvLength: (${response.clearCvvLength.length})[${response.clearCvvLength}]")

            // Log additional fields that were set to default
            watcher.debug("buildTokenEZ_failCipherCounter: [${response.failCipherCounter}]")
            watcher.debug("buildTokenEZ_flagTrack2: [${response.flagTrack2}]")
            watcher.debug("buildTokenEZ_readCardMode: [${response.readCardMode}]")
            watcher.debug("buildTokenEZ_cvvFlag: [${response.cvvFlag}]")
        
            try{
                var splited=dto.track2.split('D')
                if(splited.size<=1){
                    splited=dto.track2.split('=')
                }
               watcher.debug("ADD splited.size: ${splited.size}")

                if(splited.size>=2) {

                    response.lastPan4Digits = splited[0].substring(splited[0].length-4, splited[0].length)
                }
                if(computedKeys){
                    response.errorMessage="COMPUTED KEYS AUTO !!"
                }
                watcher.debug("buildTokenEZ_lastPan4Digits: [${response.lastPan4Digits }]")

                watcher.debug("buildTokenEZ_lastPan4Digits : (${response.lastPan4Digits.length})[$response.lastPan4Digits");
                val tokenEZValue=response.keySerialNumber+response.cipherCounter+response.failCipherCounter+response.flagTrack2+response.readCardMode+response.track2Length+response.cvvFlag+response.clearCvvLength+response.track1Flag+response.cipherData+response.lastPan4Digits+response.crc32EZ
                response.tokenEZ="! EZ"+tokenEZValue.length.toString().padStart(5,'0')+" "+tokenEZValue
                watcher.debug("buildTokenEZ_tokenEZ: [${ response.tokenEZ}]")

             
                response.track2=""

            }catch (ex:Exception){
               watcher.error("ERROR EXTRACTING LAST4DGITS ${ex.message}")
            }

        }
        return response
    }



    fun buildTokenEW(dto: PandoraTransactionDto): PandoraTransactionDto {

        val response = PandoraTransactionDto()
        BeanUtils.copyProperties(dto,response)
        var logMsg="SpecialCipherManager_buildTokenEW ${dto.hostType} "
        response.logData+=logMsg
    
       watcher.debug(logMsg) 
       val tk= RSAUtils.genRandomKey(watcher)
       // val tkEW= StringUtils.encodeHexString(tk)
        val keyId = dto.keyId.trim().uppercase(Locale.US);
        val bankRsaKey=  BankRsaKey.getBankRsaKeyByHost(dto.hostType,keyId,watcher)
         logMsg="bbuildTokenEW_SpecialCipherManager_keyId  dto.hostType [${dto.hostType}] keyId [${bankRsaKey.id}] tk [${maskString(StringUtils.encodeHexString(tk))}] computeinternalkeys [${AppSettings.getHostConfig(dto.hostType).authorizerId.computeInternalKeys}]"
        response.logData+=logMsg
       watcher.debug(logMsg)
         response.tkClear=StringUtils.encodeHexString(tk)
         watcher.debug("tkClear ${maskString( response.tkClear)}")
 
         watcher.debug("buildTokenEW_dto_transactionType ${dto.transactionType}")

        if(AppSettings.getHostConfig(dto.hostType).authorizerId.computeInternalKeys){
            response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
            response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage+" FOR ${dto.hostType} / ONLY INTERNAL KEYS "
            return response
        }
        if(bankRsaKey.errorCode!=0) {
            response.errorCode=bankRsaKey.errorCode
            response.errorMessage=bankRsaKey.errorMessage+" READ KEY RSA ERROR"
            return response
        }

        var randomTkCipher=ByteArray(0)
        try {

            logMsg="bbuildTokenEW_ankRsaKey .id :[${bankRsaKey.id}] keyId  :[${keyId}] modulus:  [${ maskString( bankRsaKey.modulus)}] \n exponent:  [${bankRsaKey.exponent}] "
            response.logData+=logMsg
           watcher.debug(logMsg)
       
            randomTkCipher = RSAUtils.RSACipher(
                tk,
                bankRsaKey.modulus,
                bankRsaKey.exponent, watcher
            )


            if(randomTkCipher.size<256){
                response.errorCode=ErrorCode.RsaFailed.errorCodeInt
                response.errorMessage=ErrorCode.RsaFailed.errorCodeMessage+" randomTkCipher MUST BE 256 BUT IS ${randomTkCipher.size} BYTES LEN "
                return response
            }
        }catch (ex:Exception){

            logMsg="randomTkCipher exception  [${ ex.message}] "
            response.logData+=logMsg
           watcher.error(logMsg)

            response.errorCode=ErrorCode.RsaFailed.errorCodeInt
            response.errorMessage=ErrorCode.RsaFailed.errorCodeMessage+" ${ex.message} "
            return response
        }

        val randomTkCipherEW= StringUtils.encodeHexString(randomTkCipher)
       // val randomTkCipherEW= StringUtils.encodeHexString(randomTkCipher)

        logMsg=" randomTkCipherEW [${ randomTkCipherEW}] "
        response.logData+=logMsg
       watcher.debug(logMsg) 

       // val crc32 = RSAUtils.CRC32Calculate(StringUtils.asciiToHexByteArray2(randomTkCipherEW))
         val crc32Bytes = RSAUtils.CRC32Calculate(randomTkCipher)
        val crc32 = RSAUtils.CRC32Calculate(StringUtils.stringToByteArray(randomTkCipherEW.toUpperCase()))
        watcher.debug("crc32Bytes ${StringUtils.encodeHexString(crc32Bytes)}")
        watcher.debug("crc32 ${StringUtils.encodeHexString(crc32)}")


        val checkValue = RSAUtils.TDesCipher(ByteArray(8),tk)

        val checkValueEW =StringUtils.encodeHexString(checkValue).substring(0,6)

        logMsg="StringUtils.encodeHexString(checkValue).substring(0,6)  RESULT: checkValueEW [${ checkValueEW}] "
        response.logData+=logMsg
       watcher.error(logMsg)


        val crc32EW= StringUtils.encodeHexString(crc32)
        logMsg="crc32EW [${ crc32EW}] "
        response.logData+=logMsg
       watcher.debug(logMsg)

        val rsaVersionEW= bankRsaKey.id.padEnd(10,' ')
        logMsg="rsaVersionEW [${ rsaVersionEW}] "
        response.logData+=logMsg
       watcher.debug(logMsg)

        val rsaPaddingEW= "01"

        logMsg="rsaPaddingEW [${ rsaPaddingEW}] "
        response.logData+=logMsg
       watcher.debug(logMsg)

 
        var tokenEWValue=randomTkCipherEW+checkValueEW+rsaVersionEW+rsaPaddingEW+crc32EW

        logMsg="randomTkCipherEW+checkValueEW+rsaVersionEW+rsaPaddingEW+crc32EW =>tokenEWValue [${ tokenEWValue}] "
        response.logData+=logMsg
       watcher.debug(logMsg)

        response.randomTkCipherEW=randomTkCipherEW


        response.checkValueEW=checkValueEW
        response.rsaVersionEW=rsaVersionEW
        response.rsaPaddingEW=rsaPaddingEW
        response.crc32EW=crc32EW
        tokenEWValue="! EW"+tokenEWValue.length.toString().padStart(5,'0')+" "+tokenEWValue
        response.tokenEW=tokenEWValue


        logMsg="tokenEWValue FULL TOKEN=>  [${ tokenEWValue}] "
        response.logData+=logMsg
          watcher.debug(logMsg)
      
        var resDukpt=DukptData()
        if(dto.transactionType==TransactionType.KeyLoad){
            resDukpt=Dukpt.saveInitialKey(dto.hostType,StringUtils.encodeHexString(tk),dto.terminalSerialNumber,dto.keyId,"","",watcher)

        }
        if(dto.transactionType==TransactionType.KeyLoadPin){

            resDukpt=Dukpt.saveInitialKey(dto.hostType,StringUtils.encodeHexString(tk),dto.terminalSerialNumber+"_PIN",dto.keyId,"","",watcher)
        }

        if(resDukpt.errorCode!=0){ 
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
        }
        return response
    }

    fun injectTokenEX(dto: PandoraTransactionDto): PandoraTransactionDto {
        watcher.debug(" injectTokenEX")

        val response = PandoraTransactionDto()
        BeanUtils.copyProperties(dto,response)  
        /*  val dtoRSA= VaultDtos.RequestReadRSAData()
          dtoRSA.terminalSerialNumber=dto.terminalSerialNumber
          dtoRSA.id=dto.keyId
          dtoRSA.hostType=dto.hostType
          dtoRSA.rrn=dto.rrn
          val keyData=VaultSecureManager().readRsaKey(dtoRSA);
          if(keyData.errorCode!=0){
              response.errorCode= ErrorCode.KeyIdDoesntExist.errorCodeInt
              response.errorMessage= ErrorCode.KeyIdDoesntExist.errorCodeMessage
              return response 
          }*/
          watcher.debug(" computeInternalKeys ${AppSettings.getHostConfig(dto.hostType).authorizerId.computeInternalKeys}")
          watcher.debug(" terminalSerialNumber ${dto.terminalSerialNumber}")
          watcher.debug(" transactionType ${dto.transactionType}")

        if(AppSettings.getHostConfig(dto.hostType).authorizerId.computeInternalKeys){
            response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
            response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage+" FOR ${dto.hostType} / ONLY INTERNAL KEYS "
            return response 
        } 
        if(dto.terminalSerialNumber.equals("")){

            response.errorCode= ErrorCode.SerialIsEmpty.errorCodeInt;
            response.errorMessage= ErrorCode.SerialIsEmpty.errorCodeMessage
            return response
        }
        var pek=dto.pekEX
        var ksn=dto.ksnEX 
        var checkValueEX=dto.checkValueEX
        var newKeyRequestStatus=dto.newKeyRequestStatusEX
        var crc32EX=dto.crc32EX

        if(!dto.tokenEX.equals("")){
           watcher.debug("PARSE dto.tokenEX")
            val parts=dto.tokenEX.split(' ')
            if(parts.size<3){
                response.errorCode= ErrorCode.TokeEXBadFormat.errorCodeInt;
                response.errorMessage= ErrorCode.TokeEXBadFormat.errorCodeMessage
                return response
 
            }
            val dataEX=parts[2].toString()
            if(dataEX.length<68){
                response.errorCode= ErrorCode.TokeEXBadFormat.errorCodeInt;
                response.errorMessage= ErrorCode.TokeEXBadFormat.errorCodeMessage
                return response
            }

             pek=dataEX.substring(0,32)
             ksn=dataEX.substring(32,52)
             checkValueEX=dataEX.substring(52,58)
             newKeyRequestStatus=dataEX.substring(58,62)
             crc32EX=dataEX.substring(62)

        }

       watcher.debug("pek $pek")
       watcher.debug("ksn $ksn")
       watcher.debug("newKeyRequestStatus $newKeyRequestStatus")
       watcher.debug("crc32EX $crc32EX")
       watcher.debug("checkValueEX $checkValueEX")


            //dto.validateData=="1"
        if(dto.hostType.authorizer==Authorizer.OPENPAY){
           watcher.debug("injectTokenEX_CRC32Calculate dto.noSkipCRC32 ${dto.validateData}")

            try{
              //  val crc32=RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(pek))
             
              var crc32=RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArrayNeo(pek))
              var crc32S=StringUtils.encodeHexString(crc32)
                if(!crc32S.contains(crc32EX)){
                    response.errorCode= ErrorCode.CRC32Error.errorCodeInt
                    response.errorMessage= ErrorCode.CRC32Error.errorCodeMessage+ " |YOUR CRC32 : $crc32EX  CALCULATED_2 KEY CRC32: $crc32S"
                }
                 crc32=RSAUtils.CRC32Calculate(StringUtils.asciiToByteArray(pek))
                 crc32S=StringUtils.encodeHexString(crc32)
                if(!crc32S.contains(crc32EX)){
                    response.errorCode= ErrorCode.CRC32Error.errorCodeInt
                    response.errorMessage+= ErrorCode.CRC32Error.errorCodeMessage+ " pek: ${pek} ,YOUR CRC32 : $crc32EX  CALCULATED: KEY CRC32: $crc32S"
                    return response

                }
                
                
                response.errorCode==0;


            }catch(e:Exception){
                response.errorCode= ErrorCode.CRC32Error.errorCodeInt
                response.errorMessage= ErrorCode.CRC32Error.errorCodeMessage+" "+ e.message.toString()
                return response

            }

       }


       watcher.debug("pek: $pek  ksn $ksn checkValueEX $checkValueEX  newKeyRequestStatus $newKeyRequestStatus crc32EX $crc32EX")
       val dtoDUKPT= com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadDukptData()
        dtoDUKPT.terminalSerialNumber=dto.terminalSerialNumber
        dtoDUKPT.keyId=dto.keyId
        dtoDUKPT.hostType=dto.hostType
        dtoDUKPT.rrn=dto.rrn


        val dukptData= com.viergegroup.crypto.pandora.managers.VaultSecureManager().readDukptData(dtoDUKPT)

        watcher.debug("dukptData.tk ${dukptData.tk}")
        response.errorMessage=dukptData.errorMessage
 
        if(dukptData.errorCode!=0){
            response.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
            response.errorMessage=ErrorCode.KeyLoadIsNecesary.errorCodeMessage
            return response
        }else{


        }  

         var dukptRes=DukptData()
        if(dto.transactionType==TransactionType.KeyLoadPin||dto.transactionType==TransactionType.KeyLoadPinConfirm){
            watcher.debug("INJECT PIN KEY : ${dto.terminalSerialNumber+"_PIN"}")
            dukptRes= Dukpt.savePek(dto.hostType,dukptData.tk,dto.terminalSerialNumber+"_PIN",dto.keyId,ksn,pek,watcher)

        }else{
            watcher.debug("INJECT PIN KEY : ${dto.terminalSerialNumber+""}")
            dukptRes= Dukpt.savePek(dto.hostType,dukptData.tk,dto.terminalSerialNumber,dto.keyId,ksn,pek,watcher)

        } 


        if(dukptRes.errorCode!=0){  
            response.errorCode=dukptRes.errorCode
            response.errorMessage=dukptRes.errorMessage
            return response 
        } 
        response.errorMessage="" 
        response.errorCode=0
        response.ipekClear=dukptRes.ipekClear;
        response.tkClear=dukptRes.tk;

        return response
    }
    fun injectDUKPTKey(dto: PandoraTransactionDto): PandoraTransactionDto {

        val response = PandoraTransactionDto()


        if(dto.terminalSerialNumber.equals("")){
            response.errorCode= ErrorCode.SerialIsEmpty.errorCodeInt;
            response.errorMessage= ErrorCode.SerialIsEmpty.errorCodeMessage
            return response
        }

        val pek=dto.pekEX
        val ksn=dto.ksnEX
        val checkValueEX=dto.checkValueEX
        val crc32EX=dto.crc32EX

        if(dto.validateData=="1"){
           watcher.debug("dto.noSkipCRC32 ${dto.validateData}")
            try{ 
                val crc32=RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(pek))
                val crc32S=StringUtils.encodeHexString(crc32)
                if(crc32S!=crc32EX){
                    response.errorCode= ErrorCode.CRC32Error.errorCodeInt
                    response.errorMessage= ErrorCode.CRC32Error.errorCodeMessage+ " YOUR CRC32 : $crc32EX  KEY CRC32: $crc32S"
                    return response


                }

            }catch(e:Exception){
                response.errorCode= ErrorCode.CRC32Error.errorCodeInt
                response.errorMessage= ErrorCode.CRC32Error.errorCodeMessage+" "+ e.message.toString()
                return response

            }

        }


       watcher.debug("pek: $pek  ksn $ksn checkValueEX $checkValueEX crc32EX $crc32EX")
        val dtoDUKPT= com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadDukptData()
        dtoDUKPT.terminalSerialNumber=dto.terminalSerialNumber
        dtoDUKPT.keyId=dto.keyId
        dtoDUKPT.hostType=dto.hostType
        dtoDUKPT.rrn=dto.rrn


        val dukptData= com.viergegroup.crypto.pandora.managers.VaultSecureManager().readDukptData(dtoDUKPT)
        if(dukptData.errorCode!=0){
            response.errorCode=dukptData.errorCode;
            response.errorMessage=dukptData.errorMessage
            return response
        }
        Dukpt.saveDerivatedKey(dto.hostType,dukptData.tk,dto.terminalSerialNumber,dto.keyId,ksn,pek,watcher)
        response.errorMessage=""
        response.errorCode=0
        return response
    }


 
    fun encryptTDesAscii(dto: TDesDto): TDesDto {
        try {
            val bytesData = RSAUtils.encrypt3DESAscii(
                dto.data,
                dto.key.padStart(48,'0')
            )
            dto.data= StringUtils.encodeHexString(bytesData)
        }catch (e:Exception){

        }
        return dto
    }


    fun decryptTDesAscii(dto: TDesDto): TDesDto {
        try { 
            val bytesData = RSAUtils.decrypt3DESAscii(
                dto.data,
                dto.key.padStart(48,'0')
            )
            dto.data= StringUtils.byteArrayToASCII(bytesData)
        }catch (e:Exception){

        }
        return dto
    }



    fun encryptTDes(dto: TDesDto): TDesDto {
        try {
            val bytesData = RSAUtils.encrypt3DES(
                dto.data,
                dto.key.padStart(48,'0')
            )
            dto.data= StringUtils.encodeHexString(bytesData)
        }catch (e:Exception){

        }
        return dto
    }
    fun decryptTDes(dto: TDesDto): TDesDto {
        try {
            val bytesData = RSAUtils.decrypt3DES(
                dto.data,
                dto.key.padStart(48,'0')
            )
            dto.data= StringUtils.encodeHexString(bytesData)
        }catch (e:Exception){

        }
        return dto
    } 




}