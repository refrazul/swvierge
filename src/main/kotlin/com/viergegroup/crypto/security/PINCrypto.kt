package com.viergegroup.crypto.security

import com.viergegroup.crypto.security.Dukpt
import com.viergegroup.swmain.webwatcher.WatcherContext
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.shared.commons.ErrorCode
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.swmain.dtos.pandora.DukptDataDto
import com.viergegroup.swmain.entities.catalogs.HostType
import org.springframework.beans.BeanUtils
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto

import  com.viergegroup.crypto.security.DerivatedKeysHsmData

enum class CryptoPinAlgo {
    TDES, AES
}

class PINBlockDataReq: RequestEntity() {

    var bdk: String = ""
    var pek: String = ""
    var ipek: String = ""

    var pinBlock: String = ""
    var pinClear: String = ""
    var pan: String = ""
    var keyId: String = ""
    var ksnPin: String = ""
    var ksnPinTo: String = ""
     var pinAlgo: CryptoPinAlgo = CryptoPinAlgo.AES
    var pinAlgo2: CryptoPinAlgo = CryptoPinAlgo.TDES

}

class PINBlockDataRes: ResponseEntity() {
    var pinBlock: String = ""
    var pinClear: String = "" 
    var pinBlockClear: String = "" 

    var bdk: String = ""
    var pek: String = ""
    var sessionKey: String = ""

    var pan: String = ""
    var ksnPin: String = ""
    var ksnPinTo: String = ""
}
 
object PINCrypto { 


 

    
    fun transferPinBlock(dto: PandoraTransactionDto,watcher:Watcher):PandoraTransactionDto{
        val response=PandoraTransactionDto()  
        val requestPandora=PINBlockDataReq() 
        BeanUtils.copyProperties(dto,requestPandora)
        try {
            watcher.debug("dto.hostType.authorizer.computeInternalKeys: [${dto.hostType.authorizer.computeInternalKeys}]")
            if(dto.hostType.authorizer.computeInternalKeys){
                response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }
            val resDukpt = transferPinBlock(requestPandora,watcher)
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
            response.pinBlock=resDukpt.pinBlock

        }catch (e:Exception){
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        return response
    }

    



    fun clearTransactionCounter(ksnHex: String): String {
        return if (ksnHex.length == 20) {
            ksnHex.substring(0, 14) + "000000"
        } else {
            ksnHex // Lo devuelve tal cual si no tiene longitud válida
        }
    }
    


    fun transferPinBlock(from: PINBlockDataReq,watcher:Watcher): PINBlockDataRes {
        val response = PINBlockDataRes()
        var clearPinBlockBytes: ByteArray? = null
        watcher.debug("INI TransferPinBlock")
        watcher.debug("Request: pinBlock=${from.pinBlock}, pinClear=${from.pinClear}, pan=${from.pan}, ksn=${from.ksnPin}, ksnTo=${from.ksnPinTo}, algo=${from.pinAlgo}, algo=${from.pinAlgo2}")
        try {

            val rrequestReadKeyval=VaultDtos.ReadKeyRequest()
            rrequestReadKeyval.keyId="bdkmppvg"
            rrequestReadKeyval.hostType=HostType.ViergeTestProd
            BeanUtils.copyProperties(from,rrequestReadKeyval)
            var responseKeyVal= VaultSecureManager().readKeyValue(rrequestReadKeyval)
            if(responseKeyVal.data==""){
                response.errorCode = ErrorCode.KeyLoadPinIsNecesary.errorCodeInt
                response.errorMessage ="BDK MPP-VG NOT FOUND"
                watcher.debug("decryptPinBlock ERROR: ${  response.errorCode }| ${ response.errorMessage}")
                return response 
            } 
            watcher.debug("bdkmppvg  [${ responseKeyVal.data}] ")
            val ksnBkd=clearTransactionCounter(from.ksnPin)
            watcher.debug("ksnBkd  [${ ksnBkd}] ")
             val pekMpp =calculatePinPek(responseKeyVal.data,ksnBkd,true,watcher)
             watcher.debug("pekMpp  [${ pekMpp}] ")
            // Desencriptar el PIN block de origen
            val responseDecrypt = decryptPinBlock(from,pekMpp,watcher)
            watcher.debug("Decrypted pinClear=${responseDecrypt.pinClear}, error=${responseDecrypt.errorCode}")
                if(responseDecrypt.errorCode!=0){
                    return responseDecrypt
                }
            // Validación del PAN si aplica
            if (from.pan.length < 13) {
                response.errorMessage = "Invalid PAN length"
                watcher.debug("Invalid PAN: ${from.pan}")
                return response
            }

            from.pinClear = responseDecrypt.pinClear
            from.pinAlgo=from.pinAlgo2
            // Encriptar el PIN block limpio hacia el destino
            val responseEncrypt = encryptPinBlock(from,watcher,true)
            watcher.debug("Encrypted pinBlock=${responseEncrypt.pinBlock}, error=${responseEncrypt.errorCode}")
            if(responseEncrypt.errorCode!=0){
                return responseEncrypt
            }
            response.errorCode = ErrorCode.Approved.errorCodeInt
            response.pinBlock = responseEncrypt.pinBlock

        } catch (e: Exception) {
            response.errorCode = ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage = e.message ?: "Unknown error"
            watcher.debug("TransferPinBlock ERROR: ${e.message}")
        } finally {
            clearPinBlockBytes?.fill(0x00)
        }
        watcher.debug("FIN TransferPinBlock with result: pinBlock=${response.pinBlock}, error=${response.errorCode}, msg=${response.errorMessage}")
        return response
    }
  

    /*  fun calculatePinPek(dto: PandoraTransactionDto,watcher:Watcher):PandoraTransactionDto{
        val response=PandoraTransactionDto()  
        val requestPandora=PINBlockDataReq() 
        BeanUtils.copyProperties(dto,requestPandora)
        try { 
            watcher.debug("dto.hostType.authorizer.computeInternalKeys: [${dto.hostType.authorizer.computeInternalKeys}]")
            if(dto.hostType.authorizer.computeInternalKeys){
                response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }
            val resDukpt = calculatePinPek(requestPandora,watcher)
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
            response.pek=resDukpt.pek 
    
        }catch (e:Exception){
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        return response
    }*/
   


    fun calculatePinPek(key:String,ksnPin:String,isBdk:Boolean,watcher:Watcher): String {
        var pekResponse = ""
      //  var response=DerivatedKeysHsmData()  

        watcher.debug("INI calculatePinPek")
        watcher.debug("Request: key=${key}, ksnPin=${ksnPin},")
        try {
            val response= Dukpt.computeKeysAll(hexToByteArray(key),hexToByteArray(ksnPin),isBdk,watcher)
            pekResponse=response.pekForPin
            watcher.debug("pekResponse=${pekResponse}")

        } catch (e: Exception) {
           
            watcher.debug("TransferPinBlock ERROR: ${e.message}")
        } finally {
        }
        return pekResponse
    }

    fun calculatePinPek(from: PandoraTransactionDto,watcher:Watcher): PandoraTransactionDto {
        val response = PandoraTransactionDto()
        var clearPinBlockBytes: ByteArray? = null
        watcher.debug("INI calculatePinPek")
        watcher.debug("Request: pinBlock=${from.pinBlock}, pinClear=${from.pinClear}, pan=${from.pan}, ksn=${from.ksnPin}, ksnTo=${from.ksnPinTo}, algo=${from.pinAlgo}, algo=${from.pinAlgo2}")
        try {
             val keys =computePinKeys(from,watcher)
             watcher.debug(" keys.pekForPin  [${ keys.pekForPin}] ")
            response.pek=keys.pekForPin
            response.errorCode = ErrorCode.Approved.errorCodeInt
        } catch (e: Exception) {
            response.errorCode = ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage = e.message ?: "Unknown error"
            watcher.debug("TransferPinBlock ERROR: ${e.message}")
        } finally {
            clearPinBlockBytes?.fill(0x00) 
        }
        watcher.debug("FIN TransferPinBlock with result: pinBlock=${response.pinBlock}, error=${response.errorCode}, msg=${response.errorMessage}")
        return response
    }


    
    
    fun computePinKeys(dto: PandoraTransactionDto,watcher:Watcher):DerivatedKeysHsmData{
        var response=DerivatedKeysHsmData()  
        val requestPandora=PINBlockDataReq() 
        BeanUtils.copyProperties(dto,requestPandora)
        try {
            watcher.debug("dto.hostType.authorizer.computeInternalKeys: [${dto.hostType.authorizer.computeInternalKeys}]")
            if(dto.hostType.authorizer.computeInternalKeys){
            //   response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
             //   response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }  

       
            if(dto.bdk!="")
             response= Dukpt.computeKeysAll(hexToByteArray(dto.bdk),hexToByteArray(dto.ksnPin),true,watcher)
             if(dto.ipek!="")
             response= Dukpt.computeKeysAll(hexToByteArray(dto.ipek),hexToByteArray(dto.ksnPin),false,watcher)
             if(dto.pinClear!=""&&dto.pan!=""){
                response.pinBlockClear=buildIsoPinBlock(dto.pinClear,dto.pan,watcher)
                response.pinClear=extractPinFromIsoBlock(response.pinBlockClear,dto.pan,watcher)
            }

        }catch (e:Exception){
           // response.errorCode=ErrorCode.VaultError.errorCodeInt
            //response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        return response
    }



    fun encryptPinBlock(dto: PandoraTransactionDto,watcher:Watcher,addCipherCounter:Boolean= false):PandoraTransactionDto{

        val response=PandoraTransactionDto() 
        val requestPandora=PINBlockDataReq() 
        BeanUtils.copyProperties(dto,requestPandora) 
        try {
            watcher.debug("dto.hostType.authorizer.computeInternalKeys: [${dto.hostType.authorizer.computeInternalKeys}]")

            if(dto.hostType.authorizer.computeInternalKeys){
                response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }
            
            val resDukpt = encryptPinBlock(requestPandora,watcher,addCipherCounter)
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
            response.pinBlock=resDukpt.pinBlock 
            response.pinClear=resDukpt.pinClear 
            response.pinBlockClear=resDukpt.pinBlockClear 
            response.pek=resDukpt.pek 

        }catch (e:Exception){
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        return response
    }

    fun applyPaddingIfNeeded(data: ByteArray, blockSize: Int): ByteArray {
        val remainder = data.size % blockSize
        return if (remainder == 0) {
            data
        } else {
            val paddingLength = blockSize - remainder
            data + ByteArray(paddingLength) { 0x00 }
        }
    }

    fun buildIsoPinBlock(pin: String, pan: String, watcher: Watcher): String {
        // Paso 1: construir bloque PIN
        val pinBlock = StringBuilder()
            .append(pin.length.toString(16).padStart(2, '0'))
            .append(pin)
            .append("F".repeat(14 - pin.length))
            .toString()
    
        // Paso 2: construir bloque PAN
        val pan12 = pan.filter { it.isDigit() }.dropLast(1).takeLast(12).padStart(16, '0')
    
        watcher.debug("PIN block raw: $pinBlock")
        watcher.debug("PAN block raw: $pan12")
    
        // Paso 3: hacer XOR
        val pinBytes = hexToByteArray(pinBlock)
        val panBytes = hexToByteArray(pan12)
        val result = pinBytes.zip(panBytes) { a, b -> a xor b }.toByteArray()
    
        val finalBlock = byteArrayToHex(result)
        watcher.debug("Resultado XOR final: $finalBlock")
        return finalBlock
    }
    fun extractPinFromIsoBlock(pinBlockHex: String, pan: String, watcher: Watcher): String {
        // Paso 1: Formatear el PAN para ISO-0
        val pan12 = pan.filter { it.isDigit() }.dropLast(1).takeLast(12).padStart(16, '0')
    
        // Paso 2: Convertir PIN block y PAN a bytes
        val pinBlockBytes = hexToByteArray(pinBlockHex)
        val panBytes = hexToByteArray(pan12)
    
        // Paso 3: XOR inverso
        val xorResult = ByteArray(pinBlockBytes.size) { i -> (pinBlockBytes[i].toInt() xor panBytes[i].toInt()).toByte() }
        val resultHex = byteArrayToHex(xorResult)
    
        watcher.debug("[extractPinFromIsoBlock] Resultado XOR inverso: $resultHex")
    
        // Paso 4: Extraer longitud y PIN
        val pinLengthHex = resultHex.substring(0, 2)
        val pinLength = pinLengthHex.toInt(16)
    
        if (pinLength <= 0 || pinLength > 12) {
         //   throw IllegalArgumentException("PIN length inválido: $pinLength")
            watcher.debug("PIN length inválido: $pinLength")

        }
    
        val pin = resultHex.substring(2, 2 + pinLength)
        watcher.debug("[extractPinFromIsoBlock] PIN extraído: $pin")
        return pin
    }
    
    fun encryptPinBlock(requestDto: PINBlockDataReq, watcher: Watcher, addCipherCounter: Boolean = false): PINBlockDataRes {
        val res = PINBlockDataRes()
        watcher.debug("[EncryptPinBlock] Iniciando proceso de encriptación")
        val dukpt = readDukptDataPIN(requestDto.hostType, requestDto.terminalSerialNumber, requestDto.keyId, requestDto.rrn, watcher)
        if (dukpt.ipek.isEmpty()) {
            res.errorCode = ErrorCode.KeyLoadPinIsNecesary.errorCodeInt
            res.errorMessage = "PEK NOT FOUND"
            watcher.debug("[EncryptPinBlock] ERROR: ${res.errorMessage}")
            return res
        }
        watcher.debug("encryptPinBlock_dukpt_pek [${dukpt.pek}]")
        var pekPin = ""
        if(requestDto.ipek!=""){
            watcher.debug("CALCULATING_WITH_IPEK")
            pekPin=calculatePinPek(requestDto.ipek, requestDto.ksnPin, false, watcher)
            watcher.debug("IPEK_pekPin [${pekPin}]")

        }
        else if(requestDto.bdk!=""){
            watcher.debug("CALCULATING_WITH_BDK")
            pekPin=calculatePinPek(requestDto.bdk, requestDto.ksnPin, true, watcher)
            watcher.debug("BDK_pekPin [${pekPin}]")

        }else{
           watcher.debug("CALCULATING_WITH_SAVED_IPEK")
            pekPin=calculatePinPek(dukpt.ipek, dukpt.ksn, false, watcher)
            watcher.debug("SAVED_IPEK_pekPin [${pekPin}]")


        }

        watcher.debug("pekPin [${pekPin}]")
        res.pek=pekPin
        requestDto.ksnPin = dukpt.ksn
        try { 
            val isoBlock = buildIsoPinBlock(requestDto.pinClear, requestDto.pan, watcher)
            val plainData = hexToByteArray(isoBlock)
            res.pinBlockClear = isoBlock

            val paddedData = when (requestDto.pinAlgo) {
                CryptoPinAlgo.TDES -> applyPaddingIfNeeded(plainData, 8)
                CryptoPinAlgo.AES -> applyPaddingIfNeeded(plainData, 16)
            }
            watcher.debug("[EncryptPinBlock] PIN listo para cifrado (padded): ${byteArrayToHex(paddedData)}")
            val cipherBytes = when (requestDto.pinAlgo) {
                CryptoPinAlgo.TDES -> Dukpt.encryptTripleDes(hexToByteArray(pekPin), paddedData, false)
                CryptoPinAlgo.AES -> Dukpt.encryptAes(hexToByteArray(pekPin), paddedData, false)!!
            }
            res.pinBlock = byteArrayToHex(cipherBytes)
            res.pinClear = byteArrayToHex(paddedData)
            res.errorCode = ErrorCode.Approved.errorCodeInt
            watcher.debug("[EncryptPinBlock] PIN Encriptado: ${res.pinBlock}")
            saveClearPekPIN(requestDto.hostType, requestDto.terminalSerialNumber, requestDto.keyId, dukpt.ksn, dukpt.ipek, watcher, addCipherCounter)
        } catch (ex: Exception) {
            res.errorCode = ErrorCode.UnHandledException.errorCodeInt
            res.errorMessage = ex.message.toString()
            watcher.error("[EncryptPinBlock] EXCEPCIÓN: ${ex.message}")
        }
        return res
    }



 
    fun decryptPinBlock(requestDto: PINBlockDataReq, pek: String, watcher: Watcher): PINBlockDataRes {
        val res = PINBlockDataRes()
        watcher.debug("pek: $pek")
    
        try {
            watcher.debug("Decrypting with algo=${requestDto.pinAlgo}, pinBlock=${requestDto.pinBlock}, ksn=${requestDto.ksnPin}")
            //val sessionKey = deriveSessionKey(ipek, requestDto.ksnPin, requestDto.pinAlgo, watcher)
            //watcher.debug("SessionKey: $sessionKey")
            watcher.debug("decryptPinBlock_pek [${pek}]")

            val decrypted = when (requestDto.pinAlgo) {
                CryptoPinAlgo.TDES -> Dukpt.decryptTripleDes(hexToByteArray(pek), hexToByteArray(requestDto.pinBlock), false)
                CryptoPinAlgo.AES -> Dukpt.decryptAes(hexToByteArray(pek), hexToByteArray(requestDto.pinBlock), false)
            }
    
            watcher.debug("byteArrayToHex(decrypted) =${byteArrayToHex(decrypted)}")
            res.pinBlock =byteArrayToHex(decrypted)
           // val unpadded = removePadding(decrypted)
            res.pinClear =extractPinFromIsoBlock( byteArrayToHex(decrypted),requestDto.pan,watcher)
            res.errorCode = ErrorCode.Approved.errorCodeInt
            watcher.debug(" res.pinClear : ${ res.pinClear }")

        } catch (ex: Exception) {
            res.errorCode = ErrorCode.UnHandledException.errorCodeInt
            res.errorMessage = ex.message.toString()
            watcher.debug("decryptPinBlock ERROR: ${ex.message}")
        }
    
        return res
    }
    
    fun applyPadding(data: ByteArray, blockSize: Int): ByteArray {
        val paddingLength = blockSize - (data.size % blockSize)
        val padding = ByteArray(paddingLength) { paddingLength.toByte() }
        return data + padding
    }
    
    fun removePadding(data: ByteArray): ByteArray {
        if (data.isEmpty()) return data
        val paddingLength = data.last().toInt()
        if (paddingLength <= 0 || paddingLength > data.size) return data
        return data.copyOf(data.size - paddingLength)
    }
    
    
   
    fun decryptPinBlock(dto: PandoraTransactionDto,watcher:Watcher):PandoraTransactionDto{

        val response=PandoraTransactionDto() 
        val requestPandora=PINBlockDataReq()   
        BeanUtils.copyProperties(dto,requestPandora)
        try { 
            watcher.debug("dto.hostType.authorizer.computeInternalKeys: [${dto.hostType.authorizer.computeInternalKeys}]")

            if(dto.hostType.authorizer.computeInternalKeys){
                response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }
          
            val resDukpt = decryptPinBlock(requestPandora,watcher)
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
            response.pinClear=resDukpt.pinClear
            response.pek=resDukpt.pek 

        }catch (e:Exception){
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        return response
    }


    
    fun decryptPinBlock(requestDto: PINBlockDataReq,watcher:Watcher): PINBlockDataRes {
        var pek = "" // Add logic to get actual IPEK if needed
        val res = PINBlockDataRes() 
        val dukpt=readDukptDataPIN(requestDto.hostType,requestDto.terminalSerialNumber,requestDto.keyId,requestDto.rrn,watcher)
        if (dukpt.ipek.isEmpty()) {
            res.errorCode = ErrorCode.KeyLoadPinIsNecesary.errorCodeInt
            res.errorMessage = "PEK NOT FOUND"
            watcher.debug("[EncryptPinBlock] ERROR: ${res.errorMessage}")
            return res
        }
        watcher.debug("encryptPinBlock_dukpt_pek [${dukpt.pek}]")

   
        pek=dukpt.pek
       
        watcher.debug("ipek: ${pek} , dukpt.ksn:${dukpt.ksn}")
        try { 
            watcher.debug("Decrypting with algo=${requestDto.pinAlgo}, pinBlock=${requestDto.pinBlock}, ksn=${requestDto.ksnPin}")
            var pekPin =""


            if(requestDto.ipek!=""){
                watcher.debug("CALCULATING_WITH_IPEK")
                pekPin=calculatePinPek(requestDto.ipek, requestDto.ksnPin, false, watcher)
                watcher.debug("IPEK_pekPin [${pekPin}]")

            }
            else if(requestDto.bdk!=""){
                watcher.debug("CALCULATING_WITH_BDK")
                pekPin=calculatePinPek(requestDto.bdk, requestDto.ksnPin, true, watcher)
                watcher.debug("BDK_pekPin [${pekPin}]")

            }else{
               watcher.debug("CALCULATING_WITH_SAVED_IPEK")
               requestDto.ksnPin=dukpt.ksn
                pekPin=calculatePinPek(dukpt.ipek, dukpt.ksn, false, watcher)
                watcher.debug("SAVED_IPEK_pekPin [${pekPin}]")

            }

            

            watcher.debug("pekPin [${pekPin}]")
            res.pek=pekPin
            //    watcher.debug("SessionKey: $sessionKey")

            val clearBytes = when (requestDto.pinAlgo) {
                CryptoPinAlgo.TDES -> Dukpt.decryptTripleDes(hexToByteArray(pekPin), hexToByteArray(requestDto.pinBlock), false)
                CryptoPinAlgo.AES -> Dukpt.decryptAes(hexToByteArray(pekPin), hexToByteArray(requestDto.pinBlock), false)
            } 

            res.pinClear = byteArrayToHex(clearBytes)
            res.errorCode = ErrorCode.Approved.errorCodeInt

        } catch (ex: Exception) {
            res.errorCode = ErrorCode.UnHandledException.errorCodeInt
            res.errorMessage = ex.message.toString()
            watcher.debug("decryptPinBlock ERROR: ${ex.message}")
        }
        return res
    }


 
    private fun hexToByteArray(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    private fun byteArrayToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) } 
    }
 
    

    fun saveClearPekPIN(hostType: HostType, terminalSerialNumber: String, keyId:String, ksn: String, pekEX: String,watcher:Watcher,addCipherCounter:Boolean= false):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "0"
       watcher.debug("PINCrypto saveClearPekPIN")  
       
        val dukptData = DukptData() 
        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("PINCrypto cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("PINCrypto saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
                watcher.error("PINCrypto saveDerivatedKey EXCEPTION : " + ex.message)
            }
        }
        watcher.debug("PINCrypto addCipherCounter [${addCipherCounter}] cipherCounter : [$cipherCounter]")
        if(addCipherCounter){
            cipherCounter++ 
            val hexCounter = leftPad(Integer.toHexString(cipherCounter), 2, "0")
            watcher.debug("PINCrypto hexCounter cipherCounter: $hexCounter")
            watcher.debug("PINCrypto hexCounter dukptData.ksn: " + dukptData.ksn)
             dukptData.ksn = dukptData.ksn.substring(0, dukptData.ksn.length - 2) + hexCounter
            watcher.debug("PINCrypto hexCounter MODIFIED dukptData.ksn: " + dukptData.ksn)
        }
        watcher.debug("PINCrypto addCipherCounter [${addCipherCounter}] cipherCounter : [$cipherCounter]")

        dukptData.terminalSerialNumber=terminalSerialNumber+"_PIN"
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(pekEX)
        dukptData.pek=(pekEX)

        dukptData.cipherCounter=(cipherCounter)
        //dukptData.pekEncrypted = key

        if( pekEX.equals("")){
            dukptData.errorMessage="PEK IS EMPTY"
            dukptData.errorCode=ErrorCode.HSMError.errorCodeInt
            return dukptData
        }


        try { 
            dukptData.hostType=hostType
            val res= VaultSecureManager().saveDukptData(dukptData)
            if( res.errorCode!=0){
                dukptData.errorMessage="ERROR SAVING DUKPT DATA ${res.errorCode} "+res.errorMessage+" "
               watcher.error("PINCrypto error saving dukpt ERRORMESSAGE: " +dukptData.errorMessage)
            }
            dukptData.errorCode=res.errorCode

        } catch (e: Exception) {
            dukptData.errorMessage=ErrorCode.VaultError.errorCodeMessage+" EX: "+e.message.toString()
            dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
           watcher.error("PINCrypto ERROR SAVING DUKPT ex: " +dukptData.errorMessage)

            return dukptData
        }
        return dukptData
    }

    fun leftPad(strParam: String, size: Int, padStr: String): String {
        var str = strParam
        val total = size - str.length
        for (i in 0 until total) {
            str = padStr + str
        }
        return str
    }


    fun readDukptDataPIN(hostType: HostType, terminalSerialNumber: String, keyId:String, rrn:String,watcher:Watcher): DukptData {
        var result = DukptData()
        try{
           val dto= VaultDtos.RequestReadDukptData() 
            dto.rrn=rrn
            dto.keyId=keyId 
            dto.terminalSerialNumber=terminalSerialNumber+"_PIN"
            dto.hostType=hostType
            result= VaultSecureManager().readDukptData(dto)  
            result.errorCode=0 
            result.errorMessage="READ SUCCESS"
            watcher.debug( result.errorMessage+"  dto.rrn ${ dto.rrn} /  dto.terminalSerialNumber ${   dto.terminalSerialNumber}")

        }catch (e:Exception){
            result.errorMessage=e.message.toString()
            result.errorCode=-1
            watcher.error("readDukptDataPIN ex: " +e.message)

        }
        return result 
    }


}