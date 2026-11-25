package com.viergegroup.crypto.security

import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.viergegroup.swmain.webwatcher.Watcher 
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.bitSetToString
import com.shared.commons.StringUtils.encodeHexString
import com.shared.commons.StringUtils.hexStringToByteArray
import com.shared.commons.StringUtils.hexStringToByteArrayNeo
import com.shared.commons.StringUtils.leftPad
import com.shared.commons.StringUtils.toByteArray
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.swmain.dtos.pandora.DukptDataDto
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog


import java.security.InvalidParameterException
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.DESedeKeySpec 
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import com.shared.commons.StringUtils.maskString

object DukptPin {
    private val secureRandom = SecureRandom()
//https://idtechproducts.com/technical-post/how-to-decrypt-credit-card-data-part-ii/ AQUI TODA LA INFO :V
    val NUM_OVERWRITES = 3

    private const val KEY_REGISTER_BITMASK = "C0C0C0C000000000C0C0C0C000000000"
    private val DEFAULT_KEY_REGISTER_BITMASK = toBitSet(hexStringToByteArray(KEY_REGISTER_BITMASK))
    private const val DATA_VARIANT_ENCRIPTION_KEY = "AB04780D555AC104AB04780D555AC104"

    private const val DATA_VARIANT_BITMASK = "0000000000FF00000000000000FF0000"
    //val MAC_VARIANT_BITMASK = "000000000000FF00000000000000FF00" 
    val PIN_VARIANT_BITMASK = "00000000000000FF00000000000000FF"
    private val DEFAULT_VARIANT_BITMASK = toBitSet(hexStringToByteArray(PIN_VARIANT_BITMASK))
    private val DATA_VARIANT_ENCRIPTION_KEY_BIT_SET = toBitSet(hexStringToByteArray(DATA_VARIANT_ENCRIPTION_KEY))

   /* class KeyFileContent {
        var key = ""
        var ksn = ""
        var cipherCounter = 0
    }a
*/
    fun getRandomBytes(numberOfBytes: Int): ByteArray {
        val b = ByteArray(numberOfBytes)
        Random().nextBytes(b)
        return b
    }

    /**
     *
     * Computes a DUKPT (Derived Unique Key-Per-Transaction).
     *
     *
     * This is derived from the Base Derivation Key, which should
     * have been injected into the device and should remain secret,
     * and the Key Serial Number which is a concatenation of the
     * device's serial number and its encryption (or transaction)
     * counter.
     *
     * @see .getIpek
     *
     * @param baseDerivationKey The Base Derivation Key
     * @param keySerialNumber The Key Serial Number
     * @return A unique key for this set of data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun computeKey(baseDerivationKey: ByteArray, keySerialNumber: ByteArray): ByteArray {
        return computeKey(baseDerivationKey, keySerialNumber, DEFAULT_KEY_REGISTER_BITMASK, DEFAULT_VARIANT_BITMASK)
    }


    fun saveClearPek(hostType: HostType, terminalSerialNumber: String, keyId:String, ksn: String, pekEX: String, watcher: Watcher):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "0"
       watcher.debug("savePek")

        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
                watcher.error("saveDerivatedKey EXCEPTION : " + ex.message)
            }

        }
        val dukptData = DukptData()
        dukptData.terminalSerialNumber=terminalSerialNumber
        dukptData.ksn=ksn
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(pekEX)
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
               watcher.error("error saving dukpt ERRORMESSAGE: " +dukptData.errorMessage)
            }
            dukptData.errorCode=res.errorCode

        } catch (e: Exception) {
            dukptData.errorMessage=ErrorCode.VaultError.errorCodeMessage+" EX: "+e.message.toString()
            dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
           watcher.error("ERROR SAVING DUKPT ex: " +dukptData.errorMessage)

            return dukptData
        }
        return dukptData
    }

    fun readDukptData(hostType: HostType, terminalSerialNumber: String, keyId:String, rrn:String, watcher: Watcher): DukptData {
        var result = DukptData()
        try{
           val dto= VaultDtos.RequestReadDukptData()
            dto.rrn=rrn
            dto.keyId=keyId
            dto.terminalSerialNumber=terminalSerialNumber
            dto.hostType=hostType
            result= VaultSecureManager().readDukptData(dto)
            result.errorCode=0
            result.errorMessage="READ SUCCESS"

        }catch (e:Exception){
            result.errorMessage=e.message.toString()
            result.errorCode=-1
        }
        return result
    }

 
    fun existsKey(hostType: HostType, terminalSerialNumber: String, keyId:String, rrn:String, watcher: Watcher): Boolean {
        val dto= VaultDtos.RequestReadDukptData()
       watcher.debug("existsKey? ${hostType}  ${terminalSerialNumber} ${keyId} ${rrn}")

        dto.rrn=rrn
        dto.keyId=keyId
        dto.terminalSerialNumber=terminalSerialNumber
        dto.hostType=hostType
        val dukptData= VaultSecureManager().readDukptData(dto)

       watcher.debug("dukptData RESULT IN COMPUTE KEYS = ${dukptData.errorCode}  ${dukptData.errorMessage} ${dukptData.errorMessageHSM}")

        return dukptData.errorCode==0
    }


    fun saveDerivatedKey(hostType: HostType, data: DukptData, watcher: Watcher) {
        data.lastModification = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        try {
            data.hostType=hostType
            val res= VaultSecureManager().saveDukptData(data)
            data.errorMessage=res.errorMessage
            data.errorCode=res.errorCode
        } catch (e: Exception) {
            data.errorMessage=e.message.toString()
            data.errorCode=-1

        }
    }


    fun saveComputedKeys(hostType: HostType, tk: String, terminalSerialNumber: String, keyId:String, ksn: String, key: String, watcher: Watcher):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "1"
       watcher.debug("saveComputedKeys : $hostType")

        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
               watcher.error("saveDerivatedKey EXCEPTION : " + ex.message)
            }
        }
        val dukptData = DukptData()
        dukptData.terminalSerialNumber=terminalSerialNumber
        dukptData.ksn=ksn
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(key)
        dukptData.cipherCounter=(cipherCounter)
        dukptData.previousCounter=0

        //dukptData.pekEncrypted = key
        dukptData.tk = tk
        dukptData.ipek=key
        try {
            dukptData.hostType=hostType
            val res= VaultSecureManager().saveDukptData(dukptData)
            dukptData.errorMessage=res.errorMessage
            dukptData.errorCode=res.errorCode
            dukptData.ipekEncrypted=res.ipekEncrypted
        } catch (e: Exception) {
            dukptData.errorMessage=e.message.toString()
            dukptData.errorCode=-1

        }
        return dukptData
    }



    fun saveInitialKey(hostType: HostType, tk: String, terminalSerialNumber: String, keyId:String, ksn: String, key: String, watcher: Watcher):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "0"
       watcher.debug("saveInitialKey")
        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
               watcher.error("saveDerivatedKey EXCEPTION : " + ex.message)
            }

        }
        val dukptData = DukptData()
        dukptData.terminalSerialNumber=terminalSerialNumber
        dukptData.ksn=ksn
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(key)
        dukptData.cipherCounter=(cipherCounter)
        //dukptData.pekEncrypted = key
        dukptData.tk = tk
        if( dukptData.tk.equals("")){
            dukptData.errorMessage="TK IS EMPTY"
            dukptData.errorCode=-1

            return dukptData
        }else{
           watcher.debug("dukptData.tk: ${maskString(dukptData.tk)}")

        }

        try {
            dukptData.hostType=hostType
            val res= VaultSecureManager().saveDukptData(dukptData)
            if( res.errorCode!=0){
                dukptData.errorMessage="ERROR SAVING DUKPT DATA ${res.errorCode} "+res.errorMessage+" "
               watcher.error("error saving dukpt ERRORMESSAGE: " +dukptData.errorMessage)
            }
            dukptData.errorCode=res.errorCode

        } catch (e: Exception) {
            dukptData.errorMessage=ErrorCode.VaultError.errorCodeMessage+" EX: "+e.message.toString()
            dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
           watcher.error("ERROR SAVING DUKPT ex: " +dukptData.errorMessage)

            return dukptData
        }
        return dukptData
    }
    fun savePek(hostType: HostType, tk: String, terminalSerialNumber: String, keyId:String, ksn: String, pekEX: String, watcher: Watcher):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "0"
         watcher.debug("savePek")
  
        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
               watcher.error("saveDerivatedKey EXCEPTION : " + ex.message)
            }

        }
        val dukptData = DukptData()
        dukptData.terminalSerialNumber=terminalSerialNumber
        dukptData.ksn=ksn
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(pekEX)
        dukptData.cipherCounter=(cipherCounter)
        //dukptData.pekEncrypted = key
        dukptData.ipekClear=" INITIAL VALUE"

        
        dukptData.tk = tk
        if( pekEX.equals("")){
            dukptData.errorMessage=" savePek ->PEK IS EMPTY"

            dukptData.ipekClear=" savePek ->PEK IS EMPTY"

            dukptData.errorCode=ErrorCode.HSMError.errorCodeInt
            return dukptData
        }
        if( dukptData.tk.equals("")){
            dukptData.ipekClear="TK IS EMPTY"

            dukptData.errorMessage="TK IS EMPTY"
            dukptData.errorCode=ErrorCode.HSMError.errorCodeInt
            return dukptData
        }else{
           watcher.debug("dukptData.tk: ${dukptData.tk}")
        }
        if(tk==""){
            watcher.debug("DECRYPT_PEK ${pekEX} ")

            
        }

        dukptData.tk=tk

        if (tk != ""&&pekEX!="") {
            dukptData.errorMessage = "NO ERRORS"
            try {
                watcher.debug("DECRYPT_PEK ${pekEX} ")

                if(pekEX!="44DE53EA308484F312A44FFA6520EAC0"){

                    try {
                        val PEKDES2: ByteArray = RSAUtils.TDesDecrypt(
                            hexStringToByteArray(pekEX), 
                            hexStringToByteArray(tk)
                        )

                        val pekClear=encodeHexString(PEKDES2)
                        watcher.debug("DECRYPT PEK SUCCESS  maskString($pekClear)")
                        dukptData.ipek = pekClear
                        dukptData.errorMessage = "IPEK INJECTED"
                       watcher.debug("DECRYPT PEK SUCCESS ")
                       dukptData.ipekClear=pekClear

                    }catch (ex:Exception){
                       watcher.error("ERROR DECRYPTING PEK  ${ex.message}")
                       dukptData.ipekClear="ERROR DECRYPTING PEK  ${ex.message}"

                        dukptData.ipek=pekEX
                        dukptData.errorCode=ErrorCode.TKFailed.errorCodeInt
                        dukptData.errorMessage="TK FAILED BUT WE USE PEK"
                        return dukptData
                    }
                }else{
                    dukptData.ipekClear="IPEK HARDCODE  ${maskString(pekEX)}  ONLY FOR DEV"

                   watcher.debug("IPEK HARDCODE ${ maskString(pekEX)}  ONLY FOR DEV")
                    dukptData.ipek=pekEX
                    dukptData.errorMessage="IPEK HARDCODE ! ONLY FOR DEV"
                }

                watcher.debug("CHECK_KEY ${ maskString(dukptData.ipek)}) ")

                dukptData.errorCode=0

            } catch (ex: Exception) {
                dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
                dukptData.errorMessage = "ERROR DESENCRIPTANDO IPEK:" + ex.message
               watcher.error("saveDerivatedKey ex: " + ex.message)
                return dukptData
            }

        }else{
            dukptData.ipekClear="NO CALCULADO"

        }
        try {
            dukptData.hostType=hostType
            val res= VaultSecureManager().saveDukptData(dukptData)
            if( res.errorCode!=0){
                dukptData.errorMessage="ERROR SAVING DUKPT DATA ${res.errorCode} "+res.errorMessage+" "
               watcher.error("error saving dukpt ERRORMESSAGE: " +dukptData.errorMessage)
            }
            dukptData.errorCode=res.errorCode

        } catch (e: Exception) {
            dukptData.errorMessage=ErrorCode.VaultError.errorCodeMessage+" EX: "+e.message.toString()
            dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
           watcher.error("ERROR SAVING DUKPT ex: " +dukptData.errorMessage)

            return dukptData
        }
        return dukptData
    }

    fun saveDerivatedKey(hostType: HostType, tk: String, terminalSerialNumber: String, keyId:String, ksn: String, key: String, watcher: Watcher):DukptData {
        var cipherCounter = 1
        var cipherCounterStr = "0"
       watcher.debug("saveDerivatedKey")

        if(!ksn.equals("")){
            cipherCounterStr = ksn.substring(ksn.length - 2, ksn.length)
            try {
               watcher.debug("cipherCounterStr : $cipherCounterStr")
                cipherCounter = Integer.decode(cipherCounterStr)
               watcher.debug("saveDerivatedKey cipherCounter : $cipherCounter")
            } catch (ex: Exception) {
               watcher.error("saveDerivatedKey EXCEPTION : " + ex.message)
            }

        }
        val dukptData = DukptData()
        dukptData.terminalSerialNumber=terminalSerialNumber
        dukptData.ksn=ksn
        dukptData.keyId=keyId
        dukptData.ksn=ksn
        dukptData.ipek=(key)
        dukptData.cipherCounter=(cipherCounter)
        //dukptData.pekEncrypted = key
        dukptData.tk = tk
        if( dukptData.tk.equals(""))
            watcher.debug("TK IS EMPTY!!")

        if (tk != ""&&key!="") {
            dukptData.errorMessage = "NO ERRORS"
            try {
                if(key!="44DE53EA308484F312A44FFA6520EAC0"){

                    try {
                        val PEKDES2: ByteArray = RSAUtils.TDesDecrypt(
                            hexStringToByteArray(key),
                            hexStringToByteArray(tk)
                        )
                        dukptData.ipek = (encodeHexString(PEKDES2))
                        dukptData.errorMessage = "IPEK INJECTED"
                    }catch (ex:Exception){
                        dukptData.ipek=key
                        dukptData.errorCode=ErrorCode.TKFailed.errorCodeInt
                        dukptData.errorMessage="TK FAILED BUT WE USE PEK"
                        return dukptData
                    }
                }else{
                    dukptData.ipek=key
                    dukptData.errorMessage="IPEK HARDCODE ! ONLY FOR DEV"
                }
                dukptData.errorCode=0

            } catch (ex: Exception) {
                dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
                dukptData.errorMessage = "ERROR DESENCRIPTANDO IPEK:" + ex.message
               watcher.error("saveDerivatedKey ex: " + ex.message)
                return dukptData
            }

        }
        try {
            dukptData.hostType=hostType
            val res= VaultSecureManager().saveDukptData(dukptData)
            if( res.errorCode!=0){
                dukptData.errorMessage="ERROR SAVING DUKPT DATA ${res.errorCode} "+res.errorMessage+" "
               watcher.error("error saving dukpt ERRORMESSAGE: " +dukptData.errorMessage)
            }
            dukptData.errorCode=res.errorCode

        } catch (e: Exception) {
            dukptData.errorMessage=ErrorCode.VaultError.errorCodeMessage+" EX: "+e.message.toString()
            dukptData.errorCode=ErrorCode.VaultError.errorCodeInt
           watcher.error("ERROR SAVING DUKPT ex: " +dukptData.errorMessage)

            return dukptData
        }
        return dukptData
    }





    fun toEncryptionKey(derivedKey: ByteArray): ByteArray {
       // var resultKey=ByteArray(derivedKey.size)
        val derivedKeyBitSet=StringUtils.byteArrayToBitSet(derivedKey)
        val resultBitSet=BitSet(derivedKeyBitSet.size())
        val originalKeyBitStr=StringUtils.byteArrayToBitString(hexStringToByteArray(DATA_VARIANT_ENCRIPTION_KEY) )
        val keyBitSetSTR=StringUtils.byteArrayToBitString(derivedKey)
        val expectedKey="0CCB5E8794BA527B0642AB8F535CB21D"
        val expectedResult=StringUtils.byteArrayToBitString(hexStringToByteArray(expectedKey))
        WatcherGlobalLog.debug("INPUT:\t ${encodeHexString(derivedKey)}\n FF_KEY:\t ${DATA_VARIANT_ENCRIPTION_KEY}\n EXPECTED:\t ${expectedKey}")


        WatcherGlobalLog.debug("OPERATOR1:\t ${keyBitSetSTR}\n OPERATOR2:\t ${maskString(originalKeyBitStr)}\n OPERATOR3:\t ${expectedResult}")
        var counter=1
        for(i in 0 until resultBitSet.size()){
            var originalBit=derivedKeyBitSet[i]
            val encriptionBit= DATA_VARIANT_ENCRIPTION_KEY_BIT_SET[i]
            if(counter<=4){
                originalBit=originalBit xor encriptionBit
            }else{
                originalBit=!(originalBit xor encriptionBit)

            }
            counter++
            if(counter>8){
                counter=1
            }
            resultBitSet[i]=originalBit

        }

        WatcherGlobalLog.debug("RESULT:\t ${bitSetToString(resultBitSet)}")


        return toByteArray(resultBitSet)
    }
    @Throws(Exception::class)
    fun computeKeys(
        baseDerivationKey: ByteArray,
        keySerialNumber: ByteArray, 
        keyRegisterBitmask: BitSet,
        dataVariantBitmask: BitSet,
        watcher: Watcher
    ): DerivatedKeysData {
        val result = DerivatedKeysData()
        val bdk = toBitSet(baseDerivationKey)
        val ksn = toBitSet(keySerialNumber)
        val ipek = getIpek(bdk, ksn, keyRegisterBitmask)
        result.bdk = encodeHexString(baseDerivationKey)
       watcher.debug("baseDerivationKey : " + result.bdk)

        // convert key for returning
        val key = _getCurrentKey(ipek, ksn, keyRegisterBitmask, dataVariantBitmask)
        // val rkey = toByteArray(key)
        val derivedPekAsBdkBytes = toByteArray(key)
        val keyipek = _getCurrentKey(bdk, ksn, keyRegisterBitmask, dataVariantBitmask)

        val derivedPekAsIpekBytes = toByteArray(keyipek)
        derivedPekAsIpekBytes[5] = (derivedPekAsIpekBytes[5] xor 0xFF.toByte())
        derivedPekAsIpekBytes[13] = (derivedPekAsIpekBytes[13] xor 0xFF.toByte())


        derivedPekAsIpekBytes[5] = (derivedPekAsIpekBytes[5] xor 0xFF.toByte())
        derivedPekAsIpekBytes[13] = (derivedPekAsIpekBytes[13] xor 0xFF.toByte())

        val derivedPekAsIpek = encodeHexString(derivedPekAsIpekBytes)
        val derivedPekAsBdk = encodeHexString(derivedPekAsBdkBytes)
        //val rkeyStr = encodeHexString(rkey)
        result.derivedPekAsIpek = derivedPekAsIpek
        result.derivedPekAsBdk = derivedPekAsBdk
        val ipekStr = encodeHexString(toByteArray(ipek))
        //val sesskey = toDataKey(rkey)
        //val sesskeystr = encodeHexString(sesskey)
        //println("sesskeystr: $sesskeystr rkeyStr2:  IPEK $ipekStr\n derivedPekAsIpek $derivedPekAsIpek derivedPekAsBdk: $derivedPekAsBdk")
        result.derivatedIpek = ipekStr
        baseDerivationKey[7] = (baseDerivationKey[7] xor 0xFF.toByte())
        baseDerivationKey[baseDerivationKey.size - 1] = (baseDerivationKey[baseDerivationKey.size - 1] xor 0xFF.toByte())

        // derivedPekAsBdkBytes[7] = (derivedPekAsBdkBytes[7] xor 0xFF.toByte()) as Byte
        // derivedPekAsBdkBytes[derivedPekAsBdkBytes.size - 1] = (derivedPekAsBdkBytes[baseDerivationKey.size - 1] xor 0xFF.toByte()) as Byte
        derivedPekAsBdkBytes[5] = (derivedPekAsBdkBytes[5] xor 0xFF.toByte())
        derivedPekAsBdkBytes[13] = (derivedPekAsBdkBytes[13] xor 0xFF.toByte())


        //System.out.println("xorb: "+String.format("%02X ", xorb));

        /*  result.encryptionKeyBytes=baseDerivationKey;
        result.encryptionKey=StringUtils.encodeHexString(baseDerivationKey);
        */
 
        result.baseDerivationKey = encodeHexString(baseDerivationKey)
        result.encryptionKeyBytes = derivedPekAsIpekBytes
        result.encryptionKey = encodeHexString( result.encryptionKeyBytes)
        val expansionTopKeyStr=result.encryptionKey.substring(16,result.encryptionKey.length)

        val expansionBottomKeyStr=result.encryptionKey.substring(0,16)
        val expansionKey=result.encryptionKey+expansionBottomKeyStr

        WatcherGlobalLog.debug("expansionKey $expansionKey expansionBottomKeyStr $expansionBottomKeyStr  expansionTopKeyStr $expansionTopKeyStr")

        val keyExpansionBottomBytes= hexStringToByteArray(expansionBottomKeyStr)
        val expansionTopKeyBytes= hexStringToByteArray(expansionTopKeyStr)

        val expansionKeyBytes= hexStringToByteArray(expansionKey)

        // var encryptedKeyBytesRight= encryptDes( expansionKeyBytes,keyExpansionBottomBytes,true )
        // var encryptedKeyBytesLeft= encryptDes( expansionKeyBytes,expansionTopKeyBytes,true )



        // var encryptedKeyBytesRightStr= encodeHexString(encryptedKeyBytesRight)
        //  var encryptedKeyBytesLeftStr= encodeHexString(encryptedKeyBytesLeft)

        // println("encryptedKeyBytesLeftStr $encryptedKeyBytesLeftStr encryptedKeyBytesRightStr  $encryptedKeyBytesRightStr")


        val encryptedKeyBytesRight= encryptTripleDes( expansionKeyBytes,keyExpansionBottomBytes,true )
        val encryptedKeyBytesLeft= encryptTripleDes( expansionKeyBytes,expansionTopKeyBytes,true )



        val encryptedKeyBytesRightStr= encodeHexString(encryptedKeyBytesRight)
        val encryptedKeyBytesLeftStr= encodeHexString(encryptedKeyBytesLeft)

        // println("encryptedKeyBytesLeftStr $encryptedKeyBytesLeftStr encryptedKeyBytesRightStr  $encryptedKeyBytesRightStr")

        val finalEncryptionKey=encryptedKeyBytesRightStr.substring(0,16)+encryptedKeyBytesLeftStr.substring(0,16)


        result.encryptionKey=finalEncryptionKey
        // println("finalEncryptionKey $finalEncryptionKey ")

        result.encryptionKeyBytes= hexStringToByteArray( result.encryptionKey)
        //    derivedPekAsIpekBytes[7] = (derivedPekAsIpekBytes[7] xor 0xFF.toByte()) as Byte
        //    derivedPekAsIpekBytes[baseDerivationKey.size - 1] = (derivedPekAsIpekBytes[baseDerivationKey.size - 1] xor 0xFF.toByte()) as Byte





        //    result.encryptionKeyBytes = toDataKey(derivedPekAsIpekBytes)
        //   result.encryptionKey = encodeHexString( result.encryptionKeyBytes)

        // println(result.toJson())

        // secure memory
        obliviate(ksn)
        obliviate(bdk)
        obliviate(ipek)
        obliviate(key)
        return result
    }
   

    fun encryptData(hostType: HostType, terminalSerialNumber: String, keyId: String, rrn:String, data: String, watcher: Watcher): DukptResultData {
        return encryptData(hostType,terminalSerialNumber,keyId,rrn, data, "", "", watcher)
    }  

    @Throws(Exception::class)
    fun computeKeys(baseDerivationKey: ByteArray, keySerialNumber: ByteArray, watcher: Watcher): DerivatedKeysData {
        return computeKeys(
            baseDerivationKey,
            keySerialNumber,
            DEFAULT_KEY_REGISTER_BITMASK,
            DEFAULT_VARIANT_BITMASK,
            watcher
        )
    }


    fun deriveKeys(hostType: HostType,
                   terminalSerialNumber: String, keySerialNumber:String, keyId:String, rrn:String, watcher: Watcher
    ): DukptDataDto {
        val result= DukptDataDto()
       val dukptData = readDukptData(hostType,terminalSerialNumber,keyId,rrn, watcher)
        val finalIpek = dukptData.bdk
        val derivatedKeysData =
            computeKeys(hexStringToByteArray(finalIpek), hexStringToByteArray(keySerialNumber), watcher)
        result.ipek=derivatedKeysData.encryptionKey
        result.terminalSerialNumber=terminalSerialNumber
        result.ksn=keySerialNumber

        return result
    }

    fun decryptData(hostType: HostType,
                    terminalSerialNumber: String,
                    keyId:String,
                    rrn:String,
                    ksn:String,
                    cipherData:String,
                    watcher: Watcher
    ): DukptDataDto {
        val result= DukptDataDto()
        val dukptData = readDukptData(hostType,terminalSerialNumber,keyId,rrn, watcher)
        val finalIpek = dukptData.ipek

        if(dukptData.errorCode!=0){
            result.errorCode=dukptData.errorCode
            result.errorMessage=dukptData.errorMessage
           watcher.error("ERROR READING KEY, ${hostType} ksn $ksn ${terminalSerialNumber} , ERR:"+  result.errorMessage)
            return result
        }

        if(dukptData.cipherCounter>=9999){
           watcher.error("MUST LOAD OTHER KEYS COUNTER: ${dukptData.cipherCounter}" )
            result.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
            result.errorMessage=ErrorCode.KeyLoadIsNecesary.errorCodeMessage
            return result
        }
        result.ksn=dukptData.prevKsn
       watcher.error("PREVKSN: ${dukptData.prevKsn}" )

        val derivatedKeysData =
            computeKeys(hexStringToByteArray(finalIpek), hexStringToByteArray(ksn), watcher)
        result.ipek=derivatedKeysData.encryptionKey
        result.terminalSerialNumber=terminalSerialNumber
        result.previousCounter=dukptData.previousCounter
        result.cipherCounter=dukptData.cipherCounter
        result.ksnEncrypted=dukptData.ksnEncrypted
        //result.bdk=derivatedKeysData.bdk
        try{

            val cipherBlocks= mutableListOf<String>()
            try {
                val blocksNumber = (cipherData.length.toFloat() / 16).toInt()
                val residual = (cipherData.length.toFloat()%16).toInt()
               watcher.debug("blocksNumber $blocksNumber  residual $residual")
               watcher.debug("finalText $cipherData ")
                for (index in 0 until blocksNumber) {
                    var corrimiento=16
                    try {
                        if (residual != 0 && index == blocksNumber - 1) {
                            corrimiento = residual
                        }
                        cipherBlocks.add(cipherData.substring(index * 16, index * 16 + corrimiento))
                       watcher.debug("clearBlocks ($index):  ${cipherBlocks.get(index)} corrimiento: $corrimiento")
                    }catch (e:Exception){
                       watcher.debug("ERROR GETING  CIPHER BLOCK ${e.message}")
                    }
                }

                var finalEncryptionResult = ""
                result.ipek =StringUtils.encodeHexString(derivatedKeysData.encryptionKeyBytes)
                for (textCipher in cipherBlocks) {
                    val bytes=hexStringToByteArrayNeo(textCipher)
                   watcher.debug("textClear $textCipher TEXT SIZE:${textCipher.length}  BYTES SIZE: ${bytes.size}")
                    val resultDecrypt = decryptTripleDes(
                        derivatedKeysData.encryptionKeyBytes,
                        bytes ,
                        AppSettings.getInstance().useTDESPadding
                    )
                    val resultEncryptStr = encodeHexString(resultDecrypt)

                   watcher.debug("resultEncrypt $resultEncryptStr")
                    finalEncryptionResult += resultEncryptStr
                }
                result.clearData=finalEncryptionResult
                result.errorCode=0
                result.errorMessage="DECRYPTED OK"
            //encryptedData = hexStringToByteArray(finalEncryptionResult)
            }catch (e:Exception){
                result.errorCode=-1

               watcher.error("ERROR DECRYPTING DATA ${e.message}")
                result.errorMessage=e.message.toString()
            }
        }catch (e:Exception){
            result.errorCode=-1
           watcher.error("ERROR DECRYPTING  DATA ${e.message}")
            result.errorMessage=e.message.toString()
        }


        return result
    }

    fun encryptData(hostType: HostType,
                    terminalSerialNumber: String,
                    keyId:String,
                    rrn:String,
                    text: String,
                    ipek: String,
                    keySerialNumberFromParameter: String,
                    watcher: Watcher
    ): DukptResultData {
        var keySerialNumber = keySerialNumberFromParameter
        val result = DukptResultData()
        var finalIpek = ipek
        val dukptData: DukptData
       watcher.debug("encryptData terminalSerialNumber:  $terminalSerialNumber text ${maskString(text)} ipek ${maskString(ipek)} keySerialNumber $keySerialNumber")
        if (ipek == "") {
            dukptData = readDukptData(hostType,terminalSerialNumber,keyId,rrn, watcher)
            finalIpek=dukptData.ipek
            result.ksn = dukptData.ksn
            keySerialNumber = result.ksn
           watcher.debug("encryptData terminalSerialNumber:  $terminalSerialNumber  ${maskString(text)} ipek ${maskString(ipek)} keySerialNumber $keySerialNumber")

        } else {
            if(!keySerialNumber.equals(""))
                saveDerivatedKey(hostType,"",terminalSerialNumber,keyId,keySerialNumber,ipek,watcher)
            dukptData = readDukptData(hostType,terminalSerialNumber,keyId,rrn, watcher)
            if (!dukptData.ksn.equals("")) result.ksn = dukptData.ksn

        }



        if (keySerialNumber != "" && result.ksn != keySerialNumber) {
            result.ksn = keySerialNumber
        }
        result.previousKsn=dukptData.ksn
        dukptData.prevKsn=dukptData.ksn
        result.clearData=text
        result.previousCipherCounter=dukptData.cipherCounter

        //  byte[] ipekBytes= StringUtils.hexStringToByteArray(finalIpek);
        try {
           // val ipekBytes=hexStringToByteArray(finalIpek)
          //  val ksnBytes=hexStringToByteArray(keySerialNumber)

           // val keyBytes = computeKey(ipekBytes,ksnBytes )
           keySerialNumber=keySerialNumber.toUpperCase()

            val derivatedKeysData =
                computeKeys(hexStringToByteArray(finalIpek), hexStringToByteArray(keySerialNumber), watcher)
           watcher.debug("finalIpek ${finalIpek} keySerialNumber ${keySerialNumber} " )

           watcher.debug("useTDESPadding: ${AppSettings.getInstance().useTDESPadding}")
           watcher.debug("finalIpek: $finalIpek")
           watcher.debug("derivatedKeysData: " + derivatedKeysData.toJsonLog())
 


            var encryptedData=ByteArray(0)
           watcher.debug("dukptEncryptionByParts ${AppSettings.getInstance().dukptEncryptionByParts} ")
           watcher.debug("hexPaddingChar ${AppSettings.getInstance().hexPaddingChar} ")
            var finalText=text

            if(!AppSettings.getInstance().useTDESPadding) {

                finalText =
                    text.padEnd(48, AppSettings.getInstance().hexPaddingChar.toCharArray()[0])

            }
            val textBytes = hexStringToByteArray(finalText)
            result.clearData=finalText
           watcher.debug("ENCRYPT_DATA dukptEncryptionByParts${AppSettings.getInstance().dukptEncryptionByParts}")


                val clearBlocks= mutableListOf<String>()

                try {
                    val blocksNumber = (finalText.length.toFloat() / 16).toInt()
                    val residual = (finalText.length.toFloat()%16).toInt()
                   watcher.debug("blocksNumber $blocksNumber  residual $residual")
                   watcher.debug("finalText $finalText ")
                    for (index in 0 until blocksNumber) {
                        var corrimiento=16
                        try {
                            if (residual != 0 && index == blocksNumber - 1) {
                                corrimiento = residual
                            }
                            clearBlocks.add(finalText.substring(index * 16, index * 16 + corrimiento))
                           watcher.debug("clearBlocks ($index):  ${maskString(clearBlocks.get(index))} corrimiento: $corrimiento")
                        }catch (e:Exception){
                           watcher.debug("ERROR GETING  CIPHER BLOCK ${e.message}")
                        }
                    }


                    result.clearCipherBlocks=clearBlocks
                    var derivedPekAsBdkBytes=StringUtils.hexStringToByteArrayNeo( derivatedKeysData.derivedPekAsBdk)
                    var derivedPekAsIpekBytes=StringUtils.hexStringToByteArrayNeo( derivatedKeysData.derivedPekAsIpek)
                    var derivatedIpekBytes=StringUtils.hexStringToByteArrayNeo( derivatedKeysData.derivatedIpek)

                    var finalEncryptionResult = ""
                    result.ipek =StringUtils.encodeHexString(derivatedKeysData.encryptionKeyBytes)
                    result.derivedKey =StringUtils.encodeHexString(derivatedKeysData.encryptionKeyBytes)
                    watcher.debug("result.derivedKey: ${maskString(result.derivedKey)}")
                    watcher.debug("CHECK_KEY finalIpek " +maskString( finalIpek ))
                    watcher.debug("CHECK_KEY ipek " +maskString( result.ipek ))
                    watcher.debug("CHECK_KEY derivedPekAsBdk " +maskString( derivatedKeysData.derivedPekAsBdk))
                    watcher.debug("CHECK_KEY derivedPekAsIpek " + maskString(derivatedKeysData.derivedPekAsIpek))
                    watcher.debug("CHECK_KEY derivatedIpek " +  maskString(derivatedKeysData.derivatedIpek))


                    watcher.debug("CHECK_KEY keySerialNumber " + keySerialNumber)



                    watcher.debug("ENCRYPT WITH derivatedIpekBytes ini \n\n")

                    for (textClear in clearBlocks) {
                        val bytes=hexStringToByteArrayNeo(textClear)
                       watcher.debug("textClear $textClear TEXT SIZE:${textClear.length}  BYTES SIZE: ${bytes.size}")
                        val resultEncrypt = encryptTripleDes(
                            derivatedIpekBytes,
                            bytes ,
                            AppSettings.getInstance().useTDESPadding
                        )
                        val resultEncryptStr = encodeHexString(resultEncrypt)
                        result.chipherDataBlocks.add(resultEncryptStr)

                       watcher.debug("resultEncrypt $resultEncryptStr")
                        //finalEncryptionResult += resultEncryptStr

                    }
                    watcher.debug("ENCRYPT WITH derivatedIpekBytes ENND \n\n")




                    watcher.debug("ENCRYPT WITH derivedPekAsBdk ini \n\n")

                    for (textClear in clearBlocks) {
                        val bytes=hexStringToByteArrayNeo(textClear)
                       watcher.debug("textClear $textClear TEXT SIZE:${textClear.length}  BYTES SIZE: ${bytes.size}")
                        val resultEncrypt = encryptTripleDes(
                            derivedPekAsBdkBytes,
                            bytes ,
                            AppSettings.getInstance().useTDESPadding
                        )
                        val resultEncryptStr = encodeHexString(resultEncrypt)
                        result.chipherDataBlocks.add(resultEncryptStr)

                       watcher.debug("resultEncrypt $resultEncryptStr")
                        //finalEncryptionResult += resultEncryptStr

                    }
                    watcher.debug("ENCRYPT WITH derivedPekAsBdk ENND \n\n")

                    
                    watcher.debug("ENCRYPT WITH derivedPekAsIpek ini \n\n")

                    for (textClear in clearBlocks) {
                        val bytes=hexStringToByteArrayNeo(textClear)
                       watcher.debug("textClear ${maskString(textClear)} TEXT SIZE:${textClear.length}  BYTES SIZE: ${bytes.size}")
                        val resultEncrypt = encryptTripleDes(
                            derivedPekAsIpekBytes,
                            bytes ,
                            AppSettings.getInstance().useTDESPadding
                        )
                        val resultEncryptStr = encodeHexString(resultEncrypt)
                        result.chipherDataBlocks.add(resultEncryptStr)

                       watcher.debug("resultEncrypt $resultEncryptStr")
                       // finalEncryptionResult += resultEncryptStr

                    }
                    watcher.debug("ENCRYPT WITH derivedPekAsIpek ENND \n\n")





                    for (textClear in clearBlocks) {
                        val bytes=hexStringToByteArrayNeo(textClear)
                       watcher.debug("textClear $textClear TEXT SIZE:${textClear.length}  BYTES SIZE: ${bytes.size}")
                        val resultEncrypt = encryptTripleDes(
                            derivatedKeysData.encryptionKeyBytes,
                            bytes ,
                            AppSettings.getInstance().useTDESPadding
                        )
                        val resultEncryptStr = encodeHexString(resultEncrypt)
                        result.chipherDataBlocks.add(resultEncryptStr)

                       watcher.debug("resultEncrypt $resultEncryptStr")
                        finalEncryptionResult += resultEncryptStr

                    }



                    encryptedData = hexStringToByteArray(finalEncryptionResult)
                    watcher.debug(" encryptedData ${encryptedData} ")

                }catch (e:Exception){
                   watcher.error("ERROR ENCRIPTING DATA ${e.message}")

                }

            dukptData.chipherDataBlocks=result.chipherDataBlocks
            dukptData.clearCipherBlocks=result.clearCipherBlocks
            result.encryptResult = encodeHexString(encryptedData)
            watcher.debug("CHECK_KEY " + derivatedKeysData.encryptionKey)

           watcher.debug("encryptTripleDes DATA: " + result.encryptResult + " key: " + derivatedKeysData.encryptionKey)
            try {
                //val crc32: ByteArray = RSAUtils.CRC32Calculate(encryptedData)
               // val crc32: ByteArray = RSAUtils.CRC32Calculate(encryptedData)

                val datastr=StringUtils.encodeHexString(encryptedData)
                val crc32Bytes = RSAUtils.CRC32Calculate(encryptedData)
                val crc32 = RSAUtils.CRC32Calculate(StringUtils.stringToByteArray(datastr.toUpperCase()))
                watcher.debug("datastr HEX: " + datastr)

                watcher.debug("crc32Bytes HEX: " + StringUtils.encodeHexString(crc32Bytes))
                watcher.debug("crc32 HEX: " + StringUtils.encodeHexString(crc32))



                result.crc32 = encodeHexString(crc32)
            } catch (ex: Exception) {
               watcher.error("encryptData " + ex.message)
            }
         //  val derivedKey = encodeHexString(keyBytes)
            if (ipek != "") {
                dukptData.cipherCounter=(1)
            }
            var counter: Int = dukptData.cipherCounter
            /*result.ipek = derivatedKeysData.bdk
            if (counter == 1) {
                dukptData.bdk = finalIpek
            }
*/

            /*if(AppSettings.getInstance().useIPEKAfterBdk){
                if(counter==0){
                    result.derivedKey=derivatedKeysData.derivedPekAsBdk;

                }else{
                    result.derivedKey=derivatedKeysData.derivedPekAsIpek;

                }
            }else{
                result.derivedKey=derivatedKeysData.derivedPekAsBdk;

            }*/
         //   result.derivedKey = derivatedKeysData.derivedPekAsIpek
            dukptData.clearData = text
           // dukptData.derivatedKeysData = derivatedKeysData
            dukptData.clearData = text
            dukptData.dataEncrypted = result.encryptResult
            result.derivatedKeysData = derivatedKeysData
            counter++
            val hexCounter = leftPad(Integer.toHexString(counter), 2, "0")
           watcher.debug("hexCounter cipherCounter: $hexCounter")
           watcher.debug("hexCounter dukptData.ksn: " + dukptData.ksn)
            dukptData.ksn = dukptData.ksn.substring(0, dukptData.ksn.length - 2) + hexCounter
           watcher.debug("hexCounter MODIFIED dukptData.ksn: " + dukptData.ksn)
           watcher.debug("hexCounter MODIFIED dukptData.ipek: " + maskString(dukptData.ipek))
            result.ipek=dukptData.ipek
            dukptData.previousCounter=counter-1
            dukptData.cipherCounter=(counter)
            result.cipherCounter = dukptData.cipherCounter
            result.ksnEncrypted=dukptData.ksnEncrypted
          //  dukptData.derivatedKeysData = derivatedKeysData
            // dukptData.currentKey=(result.derivedKey)
            dukptData.terminalSerialNumber=(terminalSerialNumber)
            saveDerivatedKey(hostType,dukptData, watcher)
            result.ksn=dukptData.prevKsn 

        } catch (ex: Exception) {
            result.errorMessage="ENCRYPTDATA ERROR EN ENCRIPCION  " + ex.message +" "+ErrorCode.KeyLoadIsNecesary.errorCodeMessage
            result.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
           watcher.error("ENCRYPTDATA ERROR EN ENCRIPCION  " + ex.message)
        }
        return result
    }


    /**
     *
     * Computes a DUKPT (Derived Unique Key-Per-Transaction) using the provided key register bitmask and data variant
     * bitmask.
     *
     * @see .computeKey
     * @param baseDerivationKey
     * @param keySerialNumber
     * @param keyRegisterBitmask
     * @param dataVariantBitmask
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun computeKey(
        baseDerivationKey: ByteArray,
        keySerialNumber: ByteArray,
        keyRegisterBitmask: BitSet,
        dataVariantBitmask: BitSet
    ): ByteArray{
        val bdk = toBitSet(baseDerivationKey)
        val ksn = toBitSet(keySerialNumber)
        //var bdkSize=bdk.size()
        //var ksnSize=ksn.size()

        val ipek = getIpek(bdk, ksn, keyRegisterBitmask)

        // convert key for returning
        val key = _getCurrentKey(ipek, ksn, keyRegisterBitmask, dataVariantBitmask)
        val rkey = toByteArray(key)

        // secure memory
        obliviate(ksn)
        obliviate(bdk)
        obliviate(ipek)
        obliviate(key)
        return rkey
    }

    /**
     *
     * Computes the Initial PIN Encryption Key (Sometimes referred to as
     * the Initial PIN Entry Device Key).
     *
     *
     * Within the function, the transaction counter is removed.
     * This is because the IPEK should be seen as the Dukpt
     * (Derived Unique Key-Per-Transaction) corresponding to a brand
     * new transaction counter (assuming it starts at 0).
     *
     *
     * Due to the process under which one key is derived from a subset of
     * those before it, the IPEK can be used to quickly calculate the
     * DUKPT for any Key Serial Number, or more specifically, any
     * encryption count.
     *
     *
     * This algorithm was found in Annex A, section 6 on page 69
     * of the ANSI X9.24-1:2009 document.
     *

     */
   /* @Throws(Exception::class)
    fun getIpek(key: BitSet, ksn: BitSet): BitSet? {
        return getIpek(key, ksn, DEFAULT_KEY_REGISTER_BITMASK)
    }*/

    /**
     *
     * Computes the Initial PIN Encryption Key using the provided key register bitmask.
     *
     * @see .getIpek
     * @param key
     * @param ksn
     * @param keyRegisterBitmask
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getIpek(key: BitSet, ksn: BitSet, keyRegisterBitmask: BitSet): BitSet {
        val ipek = arrayOf<ByteArray>(ByteArray(0),ByteArray(0))

        val keyRegister:BitSet = key[0, key.bitSize()]
        val data = ksn[0, ksn.bitSize()]
        data.clear(59, 80)
        ipek[0] = encryptTripleDes(toByteArray(keyRegister), toByteArray(data[0, 64]))
        keyRegister.xor(keyRegisterBitmask)
        ipek[1] = encryptTripleDes(toByteArray(keyRegister), toByteArray(data[0, 64]))
        val bipek = concat(ipek[0], ipek[1])
        val bsipek = toBitSet(bipek)

        // secure memory
        obliviate(ipek[0])
        obliviate(ipek[1])

        obliviate(bipek)
        obliviate(keyRegister)
        obliviate(data)
        return bsipek
    }

    /**
     *
     * Computes a Dukpt (Derived Unique Key-Per-Transaction) given an IPEK
     * and Key Serial Number.
     *
     *
     * Here, a non-reversible operation is used to find one key from
     * another.  This is where the transaction counter comes in.  In order
     * to have the desired number of possible unique keys (over 1 million)
     * for a given device, a transaction counter size of 20 bits would
     * suffice.  However, by adding an extra bit and a constraint (that
     * keys must have AT MOST 9* bits set) the same number of values can be
     * achieved while allowing a user to calculate the key in at most 9
     * steps.
     *
     *
     * We have reason to believe that is actually 10 bits (as the
     * sum of the 21 choose i for i from 0 to 9 is only around 700,000 while
     * taking i from 0 to 10 yields exactly 2^20 (just over 1,000,000) values)
     * but regardless of the truth, our algorithm is not dependent upon this
     * figure and will work no matter how it is implemented in the encrypting
     * device or application.
     *
     *
     * This algorithm was found in Annex A, section 3 on pages 50-54
     * of the ANSI X9.24-1:2009 document.
     *
     * @param ipek The Initial PIN Encryption Key.
     * @param ksn The Key Serial Number.
     * @return The Dukpt that corresponds to this combination of values.
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun _getCurrentKey(
        ipek: BitSet,
        ksn: BitSet,
        keyRegisterBitmask: BitSet,
        dataVariantBitmask: BitSet
    ): BitSet{
        var key = ipek[0, ipek.bitSize()]
        val counter = ksn[0, ksn.bitSize()]
        counter.clear(59, ksn.bitSize())
        for (i in 59 until ksn.bitSize()) {
            if (ksn[i]) {
                counter.set(i)
                val tmp = _nonReversibleKeyGenerationProcess(key, counter[16, 80], keyRegisterBitmask)
                // secure memory
                obliviate(key)
                key = tmp
            }
        }
        key.xor(dataVariantBitmask) // data encryption variant (e.g. To PIN)

        // secure memory
        obliviate(counter)
        return key
    }

    /**
     *
     * Creates a new key from a previous key and the right 64 bits of the
     * Key Serial Number for the desired transaction.
     *
     *
     * This algorithm was found in Annex A, section 2 on page 50
     * of the ANSI X9.24-1:2009 document.
     *
     * @param p_key The previous key to be used for derivation.
     * @param data The data to encrypt it with, usually the right 64 bits of the transaction counter.
     * @return A key that cannot be traced back to p_key.
     * @throws Exception
     */
    private fun _nonReversibleKeyGenerationProcess(p_key: BitSet, data: BitSet, keyRegisterBitmask: BitSet): BitSet {
        val keyreg = p_key[0, p_key.bitSize()]
        var reg1 = data[0, data.bitSize()]
        // step 1: Crypto Register-1 XORed with the right half of the Key Register goes to Crypto Register-2.
        var reg2 = reg1[0, 64] // reg2 is being used like a temp here
        reg2.xor(keyreg[64, 128]) // and here, too, kind of
        // step 2: Crypto Register-2 DEA-encrypted using, as the key, the left half of the Key Register goes to Crypto Register-2

        try {
            val key=toByteArray(keyreg[0, 64])
            val regBytes=toByteArray(reg2)
            val result=encryptDes(key,regBytes )
            reg2 = toBitSet(result)
        }catch (e:Exception){
            println("encryptDes: "+e.message)
        }
        // step 3: Crypto Register-2 XORed with the right half of the Key Register goes to Crypto Register-2
        reg2.xor(keyreg[64, 128])
        // done messing with reg2

        // step 4: XOR the Key Register with hexadecimal C0C0 C0C0 0000 0000 C0C0 C0C0 0000 0000
        keyreg.xor(keyRegisterBitmask)
        // step 5: Crypto Register-1 XORed with the right half of the Key Register goes to Crypto Register-1
        reg1.xor(keyreg[64, 128])
        // step 6: Crypto Register-1 DEA-encrypted using, as the key, the left half of the Key Register goes to Crypto Register-1
        reg1 = toBitSet(encryptDes(toByteArray(keyreg[0, 64]), toByteArray(reg1)))
        // step 7: Crypto Register-1 XORed with the right half of the Key Register goes to Crypto Register-1
        reg1.xor(keyreg[64, 128])
        // done
        val reg1b = toByteArray(reg1)
        val reg2b = toByteArray(reg2)
        val key = concat(reg1b, reg2b)
        val rkey = toBitSet(key)




        // secure memory
        obliviate(reg1)
        obliviate(reg2)
        obliviate(reg1b)
        obliviate(reg2b)
        obliviate(key)
        obliviate(keyreg)



        return rkey
    }

    /**
     *
     * Performs Single DES Encryption.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @param padding When true, PKCS5 Padding will be used.  This is most likely not desirable.
     * @return The encrypted.
     * @throws Exception
     */
    fun encryptDes(key: ByteArray, data: ByteArray, padding: Boolean): ByteArray {
        val iv = IvParameterSpec(ByteArray(8))
        val encryptKey = SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(key))
        val encryptor: Cipher
        encryptor = if (padding) {
            Cipher.getInstance("DES/CBC/PKCS5Padding")
        } else {
            Cipher.getInstance("DES/CBC/NoPadding")
        }
        encryptor.init(Cipher.ENCRYPT_MODE, encryptKey, iv)
        val result=encryptor.doFinal(data)

        return result
    }

    /**
     *
     * Creates a data key, as described [here](https://idtechproducts.com/support/technical-blog/id/how-to-decrypt-credit-card-data-part-ii/).
     *
     *
     * NOTE: for standard usage, the derived key provided to this method must be generated using the [.DATA_VARIANT_BITMASK].
     *
     *
     * ### The following content is copied from the above-linked page ###
     *
     * <h3>Creating Data, PIN, and MAC Key Variants</h3>
     *
     *
     * ANSI X9.24 allows a DUKPT key to take on one of three final forms, called variants. The forms are MAC, PIN,
     * and Data. Let’s defer any discussion of what these various key types are used for in order to concentrate on how
     * they’re created.
     *
     *
     * The starting point for any of the variants is a DUKPT basis key (the derived key that we called curKey in Step
     * 5 further above). To get the MAC variant, you simply need to XOR the basis key (the “derived key”) with a special
     * constant:
     *
     * <pre>MACkey = derivedKey ^ 0x000000000000FF00000000000000FF00;</pre>
     *
     * The PIN variant, likewise, is created in similar fashion, but using a different constant:
     *
     * <pre>PINkey = derivedKey ^ 0x00000000000000FF00000000000000FF;</pre>
     *
     * The Data variant requires yet another constant:
     *
     * <pre>Datakey = derivedKey ^ 0x0000000000FF00000000000000FF0000;</pre>
     *
     * For MAC and PIN variants, the XOR operation constitutes the final step in creating the relevant session key. For the Data variant, it’s customary to perform one additional step, involving a one-way hash (to preclude any possibility of someone back-transforming a Data key into a MAC key). In pseudocode:
     *
     * <pre>
     * // left half:
     * var left = des(  EDE3KeyExpand( derivedKey ),
     * top8bytes( derivedKey ),
     * true,
     * CBC,
     * iv );
     *
     * // right half:
     * var right = des( EDE3KeyExpand( derivedKey ),
     * bottom8bytes( derivedKey ),
     * true,
     * CBC,
     * iv );
     *
     * finalDataKey = (left << 64) | right;  // combine halves
    </pre> *
     *
     *
     * In English: First, obtain a 24-byte version of your derived key, by using the EDE3 expansion method.
     * (This simply means copying the first 8 bytes of a 16-byte key onto the tail end of the key, creating a 24-byte
     * key in which the first and last 8 bytes are the same.) Use that key to TDES-encrypt the first 8 bytes of your
     * 16-byte derived key, thereby creating an 8-byte cipher. That’s the left half of the eventual data key. To create
     * the right half, use the same 24-byte key to encrypt the bottom 8 bytes of the derivedKey. Combine the two 8-byte
     * ciphers (left and right pieces), and you’re done.
     *
     * @param derivedKey
     * @return
     */
    fun toDataKey(derivedKey: ByteArray?): ByteArray {
        require(!(derivedKey == null || derivedKey.size != 16)) { "Invalid key provided: " + if (derivedKey == null) "null" else "length " + derivedKey.size }
        val left = Arrays.copyOfRange(derivedKey, 0, 8)
        val right = Arrays.copyOfRange(derivedKey, 8, 16)
        val leftEncrypted: ByteArray = encryptTripleDes(derivedKey, left)
        val rightEncrypted: ByteArray = encryptTripleDes(derivedKey, right)
        val dataKey: ByteArray = concat(leftEncrypted, rightEncrypted)
        obliviate(left)
        obliviate(right)
        obliviate(leftEncrypted)
        obliviate(rightEncrypted)
        return dataKey
    }

    /**
     *
     * Performs Single DES Decryption.
     *
     * @param key The key for decryption.
     * @param data The data to decrypt.
     * @param padding When true, PKCS5 Padding will be assumed.  This is most likely not desirable.
     * @return The decrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptDes(key: ByteArray?, data: ByteArray?, padding: Boolean): ByteArray? {
        val iv = IvParameterSpec(ByteArray(8))
        val decryptKey = SecretKeyFactory.getInstance("DES").generateSecret(DESKeySpec(key))
        val decryptor: Cipher
        decryptor = if (padding) {
            Cipher.getInstance("DES/CBC/PKCS5Padding")
        } else {
            Cipher.getInstance("DES/CBC/NoPadding")
        }
        decryptor.init(Cipher.DECRYPT_MODE, decryptKey, iv)
        return decryptor.doFinal(data)
    }

    /**
     *
     * Performs Single DEA Encryption without padding.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @return The encrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptDes(key: ByteArray, data: ByteArray): ByteArray {
        return encryptDes(key, data, false)
    }

    /**
     *
     * Performs Single DES Decryption assuming no padding was used.
     *
     * @param key The key for decryption.
     * @param data The data to decrypt.
     * @return The decrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptDes(key: ByteArray?, data: ByteArray?): ByteArray? {
        return decryptDes(key, data, false)
    }

    /**
     *
     * Performs Triple DES Encryption.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @param padding When true, PKCS5 Padding will be used.  This is most likely not desirable.
     * @return The encrypted data.
     * @throws Exception
     */
    fun encryptTripleDes(key: ByteArray, data: ByteArray, padding: Boolean): ByteArray {
        val bskey = toBitSet(key)
        val k1: BitSet
        val k2: BitSet
        val k3: BitSet
        if (bskey.bitSize() == 64) {
            // single length
            k1 = bskey[0, 64]
            k2 = k1
            k3 = k1
        } else if (bskey.bitSize() == 128) {
            // double length
            k1 = bskey[0, 64]
            k2 = bskey[64, 128]
            k3 = k1
        } else {
            // triple length
            if (bskey.bitSize() != 192) {
                throw InvalidParameterException("Key is not 8/16/24 bytes long.")
            }
            k1 = bskey[0, 64]
            k2 = bskey[64, 128]
            k3 = bskey[128, 192]
        }
        val kb1 = toByteArray(k1)
        val kb2 = toByteArray(k2)
        val kb3 = toByteArray(k3)
        val key16 = concat(kb1, kb2)
        val key24 = concat(key16, kb3)
        val iv = IvParameterSpec(ByteArray(8))
        val encryptKey = SecretKeyFactory.getInstance("DESede").generateSecret(DESedeKeySpec(key24))
        val encryptor: Cipher
        encryptor = if (padding) {
            Cipher.getInstance("DESede/CBC/PKCS5Padding")
        } else {
            Cipher.getInstance("DESede/CBC/NoPadding")
        }
        encryptor.init(Cipher.ENCRYPT_MODE, encryptKey, iv)
        val bytes = encryptor.doFinal(data)

        // secure memory
        obliviate(k1)
        obliviate(k2)
        obliviate(k3)
        obliviate(kb1)
        obliviate(kb2)
        obliviate(kb3)
        obliviate(key16)
        obliviate(key24)
        obliviate(bskey)
        return bytes
    }

    /**
     *
     * Performs Triple DES Decryption.
     *
     * @param key The key for decryption.
     * @param data The data to decrypt.
     * @param padding When true, PKCS5 Padding will be assumed.  This is most likely not desirable.
     * @return The decrypted data.
     * @throws Exception
     */
    fun decryptTripleDes(key: ByteArray, data: ByteArray, padding: Boolean): ByteArray {
        val bskey = toBitSet(key)
        val k1: BitSet
        val k2: BitSet
        val k3: BitSet
        if (bskey.bitSize() == 64) {
            // single length
            k1 = bskey[0, 64]
            k2 = k1
            k3 = k1
        } else if (bskey.bitSize() == 128) {
            // double length
            k1 = bskey[0, 64]
            k2 = bskey[64, 128]
            k3 = k1
        } else {
            // triple length
            if (bskey.bitSize() != 192) {
                throw InvalidParameterException("Key is not 8/16/24 bytes long.")
            }
            k1 = bskey[0, 64]
            k2 = bskey[64, 128]
            k3 = bskey[128, 192]
        }
        val kb1 = toByteArray(k1)
        val kb2 = toByteArray(k2)
        val kb3 = toByteArray(k3)
        val key16 = concat(kb1, kb2)
        val key24 = concat(key16, kb3)
        val iv = IvParameterSpec(ByteArray(8))
        val encryptKey = SecretKeyFactory.getInstance("DESede").generateSecret(DESedeKeySpec(key24))
        val decryptor: Cipher
        decryptor =
            if (padding) Cipher.getInstance("DESede/CBC/PKCS5Padding") else Cipher.getInstance("DESede/CBC/NoPadding")
        decryptor.init(Cipher.DECRYPT_MODE, encryptKey, iv)
        val bytes = decryptor.doFinal(data)

        // secure memory
        obliviate(k1)
        obliviate(k2)
        obliviate(k3)
        obliviate(kb1)
        obliviate(kb2)
        obliviate(kb3)
        obliviate(key16)
        obliviate(key24)
        obliviate(bskey)
        return bytes
    }

    /**
     *
     * Performs Single DEA Encryption without padding.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @return The encrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptTripleDes(key: ByteArray, data: ByteArray): ByteArray {
        return encryptTripleDes(key, data, false)
    }

    /**
     * Performs Triple DEA Decryption without padding.
     *
    fun decryptTripleDes(key: ByteArray, data: ByteArray): ByteArray? {
    return decryptTripleDes(key, data, false)
    }
     */


    /**
     *
     * Performs Single AES Encryption.
     *
     *
     * This is supplied for use generic encryption and decryption purposes, but is not a part of the Dukpt algorithm.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @param padding When true, PKCS5 Padding will be used.  This is most likely not desirable.
     * @return The encrypted.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptAes(key: ByteArray?, data: ByteArray?, padding: Boolean): ByteArray? {
        val iv = IvParameterSpec(ByteArray(16))
        val encryptKey = SecretKeySpec(key, "AES")
        val encryptor: Cipher
        encryptor = if (padding) {
            Cipher.getInstance("AES/CBC/PKCS5Padding")
        } else {
            Cipher.getInstance("AES/CBC/NoPadding")
        }
        encryptor.init(Cipher.ENCRYPT_MODE, encryptKey, iv)
        return encryptor.doFinal(data)
    }

    /**
     *
     * Performs Single AES Decryption.
     *
     *
     * This is supplied for use generic encryption and decryption purposes, but is not a part of the Dukpt algorithm.
     *
     * @param key The key for decryption.
     * @param data The data to decrypt.
     * @param padding When true, PKCS5 Padding will be assumed.  This is most likely not desirable.
     * @return The decrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptAes(key: ByteArray, data: ByteArray, padding: Boolean): ByteArray {
        val iv = IvParameterSpec(ByteArray(16))
        val decryptKey = SecretKeySpec(key, "AES")
        val decryptor: Cipher
        decryptor = if (padding) {
            Cipher.getInstance("AES/CBC/PKCS5Padding")
        } else {
            Cipher.getInstance("AES/CBC/NoPadding")
        }
        decryptor.init(Cipher.DECRYPT_MODE, decryptKey, iv)
        return decryptor.doFinal(data)
    }

    /**
     *
     * Performs Single AES Encryption without padding.
     *
     *
     * This is supplied for use generic encryption and decryption purposes, but is not a part of the Dukpt algorithm.
     *
     * @param key The key for encryption.
     * @param data The data to encrypt.
     * @return The encrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptAes(key: ByteArray?, data: ByteArray?): ByteArray? {
        return encryptAes(key, data, false)
    }
 
    /**
     *
     * Performs Triple AES Decryption without padding.
     *
     *
     * This is supplied for use generic encryption and decryption purposes, but is not a part of the Dukpt algorithm.
     *
     * @param key The key for decryption.
     * @param data The data to decrypt.
     * @return The decrypted data.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptAes(key: ByteArray, data: ByteArray): ByteArray {
        return decryptAes(key, data, false)
    }

    /**
     *
     * Converts a byte into an extended BitSet.
     */
    fun toBitSet(b: Byte): BitSet {
        val bs = BitSet(8)
        for (i in 0..7) {
            if (b and ((1L shl i).toByte()) > 0) {
                bs.set(7 - i)
            }
        }
        return bs
    }

    /**
     *
     * Converts a byte array to an extended BitSet.
     */
    /* fun toBitSet(b: ByteArray): BitSet {
         val bs = BitSet(8 * b.size)
         for (i in b.indices) {
             for (j in 0..7) {
                 if (b[i] and ((1L shl j).toByte()) > 0) {
                     bs.set(8 * i + (7 - j))
                 }
             }
         }
         return bs
     }*/
    fun toBitSet(b: ByteArray): BitSet {
        val bs = BitSet(8 * b.size)
        for (i in b.indices) {
            for (j in 0..7) {
                if (b[i] and (1L shl j).toByte() > 0) {
                    bs.set(8 * i + (7 - j))
                }
            }
        }
        return StringUtils.byteArrayToBitSet(b)
    }

    /*fun toBitSet(bytes: ByteArray): BitSet {
        val bits = BitSet()
        for (i in 0 until bytes.size * 8) {
            if (bytes[bytes.size - i / 8 - 1] and (1 shl i % 8).toByte() > 0) {
                bits.set(i)
            }
        }
        return bits
    }

*/



    /**
     *
     * Converts an extended BitSet into a byte.
     *
     *
     * Requires that the BitSet be exactly 8 bits long.
     */
    fun toByte(b: BitSet): Byte {
        var value: Byte = 0
        for (i in 0 until b.bitSize()) {
            if (b[i]) value = (value or ((1L shl 7 - i).toByte()))
        }
        return value
    }

    /**
     *
     * Converts a BitSet into a byte array.
     *
     *
     * Pads to the left with zeroes.
     *
     *
     * Note: this is different from [BitSet.toByteArray].
     */
    /* fun toByteArray(b: BitSet?): ByteArray {
         val size = Math.ceil(b.bitSize() / 8.0).toInt()
         val value = ByteArray(size)
         for (i in 0 until size) {
             value[i] = toByte(b[i * 8, Math.min(b.bitSize(), (i + 1) * 8)])
         }
         return value
     }*/
    /* fun toByteArray(bits: BitSet): ByteArray {
         val bytes = ByteArray(bits.length() / 8 + 1)
         for (i in 0 until bits.length()) {
             if (bits[i]) {
                 bytes[bytes.size - i / 8 - 1] = bytes[bytes.size - i / 8 - 1] or (1 shl i % 8).toByte()
             }
         }
         return bytes
     }*/



    /**
     *
     * Converts a hexadecimal String into a byte array (Big-Endian).
     *
     */
    /*fun toByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }*/

    /**
     *
     * Converts a byte array into a hexadecimal string (Big-Endian).
     *
    fun toHex(bytes: ByteArray): String? {
    val bi = BigInteger(1, bytes)
    return String.format("%0" + (bytes.size shl 1) + "X", bi)
    }
     */


    /**
     *
     * Concatenates two byte arrays.
     *
     */
    fun concat(a: ByteArray, b: ByteArray): ByteArray {
        val c = ByteArray(a.size + b.size)
        for (i in a.indices) {
            c[i] = a[i]
        }
        for (i in b.indices) {
            c[a.size + i] = b[i]
        }
        return c
    }

    /**
     *
     * Overwrites the extended BitSet NUM_OVERWRITES times with random data for security purposes.
     */
    fun obliviate(b: BitSet) {
        val r = secureRandom
        for (i in 0 until NUM_OVERWRITES) {
            for (j in 0 until b.bitSize()) {
                b[j] = r.nextBoolean()
            }
        }
    }

    /**
     *
     * Overwrites the byte array NUM_OVERWRITES times with random data for security purposes.
     */
    fun obliviate(b: ByteArray) {
        for (i in 0 until NUM_OVERWRITES) {
            b[i] = 0x00
            b[i] = 0x01
        }
        val r = secureRandom
        for (i in 0 until NUM_OVERWRITES) {
            r.nextBytes(b)
        }
    }

    /**
     *
     * Overwrites the extended BitSet with random data for security purposes.
     */
   /* fun obliviate(b: BitSet, n: Int) {
        val r = SecureRandom()
        for (i in 0 until NUM_OVERWRITES) {
            for (j in 0 until b.bitSize()) {
                b[j] = r.nextBoolean()
            }
        }
    }*/

    /**
     *
     * Overwrites the byte array with random data for security purposes.
     */
    fun obliviate(b: ByteArray, n: Int) {
        for (i in 0 until n) {
            b[i] = 0x00
            b[i] = 0x01
        }
        val r = secureRandom
        for (i in 0 until n) {
            r.nextBytes(b)
        }
    }
}