package com.viergegroup.swmain.websecurity.bodyencryptor

import com.google.gson.GsonBuilder
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPrivateKeySpec
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random
import com.shared.commons.StringUtils
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.dtosgsonadapters.LocalDateTimeGSONAdapter
import java.time.LocalDateTime
import com.google.gson.Gson

internal class CryptoServerVGUtil {
    private val gson: Gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeGSONAdapter())
    .setPrettyPrinting()
    .create()

    private var aesKey: ByteArray = ByteArray(0)
    private var uuid: String = ""


    fun encryptBody(data: Any): String {
        WatcherGlobalLog.encryptor(uuid,"encryptBody ")
        WatcherGlobalLog.encryptor(uuid,"encryptBody_request : " + gson.toJson(data))
        val response = encryptMessage(data)
        WatcherGlobalLog.encryptor(uuid,"encryptBody_response: " + gson.toJson(response))
        val dataRawEncrypted= response.uuid + ".\n" + response.metaData + ".\n" + response.hash
        WatcherGlobalLog.encryptor(uuid,"encryptBody_dataRawEncrypted : " + dataRawEncrypted)
        return dataRawEncrypted
    }

    fun getUUID(): String {
        return uuid
    }

  
    private fun encryptAES(data: ByteArray): ByteArray {
        WatcherGlobalLog.encryptor(uuid,"encryptAES")

        val keySpec = SecretKeySpec(aesKey.copyOf(16), "AES")
        WatcherGlobalLog.encryptor(uuid,"aes_uuid: ${uuid} AES_KEY: ${StringUtils.encodeHexString(aesKey)}")

        val iv = ByteArray(16).also { Random.nextBytes(it) }
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData // prepend IV for later decryption
    }
    fun encryptMessage(data: Any): ServerSecureResponse {
        WatcherGlobalLog.encryptor(uuid,"encryptMessage")
    
        val debugData = EncryptionDebugData()
        debugData.encryptOrDecrypt = EncryptionDebugEnum.Encrypt
    
        val response = ServerSecureResponse()
        response.uuid = uuid
        debugData.uuid = uuid
    
        val uuidBytes = StringUtils.hexStringToByteArrayNeo(uuid)
        debugData.uuidBytesHex = StringUtils.encodeHexString(uuidBytes)
    
        val json = gson.toJson(data)
        val jsonByteArray = json.toByteArray()
        debugData.originalJson = json
        debugData.originalJsonByteArray = jsonByteArray

        val iv = ByteArray(16).also { Random.nextBytes(it) }
        debugData.ivHex = StringUtils.encodeHexString(iv)

        val keySpec = SecretKeySpec(aesKey.copyOf(16), "AES")
        debugData.aesKeyClearHex = StringUtils.encodeHexString(aesKey)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        val encryptedData = cipher.doFinal(jsonByteArray)
        val finalEncrypted = iv + encryptedData
    
        response.metaData = StringUtils.encodeHexString(finalEncrypted)
        debugData.encryptedMetaData = response.metaData
    
        val hmac = calculateHMACSHA256(jsonByteArray + uuidBytes)
        response.hash = StringUtils.encodeHexString(hmac)
        debugData.hmacHex = response.hash
        debugData.calculatedHmacHex = debugData.hmacHex
    
        CryptoDebugLogger.logDebugData(debugData)
    
        aesKey = ByteArray(0) // limpiar
    
        WatcherGlobalLog.encryptor(uuid,"Datos encriptados: " + gson.toJson(response))
        return response
    }
    
    fun <T> decryptMessage(serializedData: SerializedData, clazz: Class<T>): T? {
        val debugData = EncryptionDebugData()
        debugData.encryptOrDecrypt = EncryptionDebugEnum.Decrypt
        debugData.uuid = serializedData.uuid
        debugData.serialKey = serializedData.serialKey
        try {
            WatcherGlobalLog.encryptor(uuid,"decryptMessage")
            val (encryptedKeyHex, twoChars) = recoverTwoChars(serializedData.serialKey)
            WatcherGlobalLog.encryptor(uuid,"decryptMessage_rsa_index [${twoChars}] encryptedKeyHex [${encryptedKeyHex}] ")
            WatcherGlobalLog.encryptor(uuid,"decryptMessage_clazz [${clazz.name}] ")
    
            val encryptedKey = StringUtils.hexStringToByteArrayNeo(encryptedKeyHex)
            val middleKey = String(decryptRSA(encryptedKey, twoChars))
            val fullKeyClear = getStampPreffix() + middleKey + getStampSuffix()
            WatcherGlobalLog.encryptor(uuid,"uuid: ${uuid} middleKey: $middleKey")
            WatcherGlobalLog.encryptor(uuid,"uuid: ${uuid}  Llave AES completa desencriptada: $fullKeyClear")

            aesKey = fullKeyClear.toByteArray()
           // debugData.aesKeyClearHex = StringUtils.encodeHexString(aesKey)
    
            val encryptedData = StringUtils.hexStringToByteArrayNeo(serializedData.metaData)
            debugData.encryptedMetaData = serializedData.metaData
            debugData.ivHex = StringUtils.encodeHexString(encryptedData.copyOfRange(0, 16))
    
            val decryptedJson = decryptAES(encryptedData)
            debugData.originalJson = String(decryptedJson)
            debugData.originalJsonByteArray = decryptedJson
    
            WatcherGlobalLog.encryptor(uuid,"uuid: ${uuid} decryptedJson : ${debugData.originalJson}") 
    
            val uuidBytes = StringUtils.hexStringToByteArrayNeo(serializedData.uuid)
            debugData.uuidBytesHex = StringUtils.encodeHexString(uuidBytes)
            WatcherGlobalLog.encryptor(uuid,"uuidBytes : ${debugData.uuidBytesHex}") 
    
            val receivedHmac = serializedData.hash.toUpperCase()
            debugData.aesKeyClearHex = StringUtils.encodeHexString(aesKey)
            val calculatedHmac = StringUtils.encodeHexString(calculateHMACSHA256(decryptedJson + uuidBytes)).toUpperCase()
            debugData.hmacHex = receivedHmac
            debugData.calculatedHmacHex = calculatedHmac
    
            WatcherGlobalLog.encryptor(uuid,"HMAC calculado: $calculatedHmac")
            WatcherGlobalLog.encryptor(uuid,"HMAC recibido: $receivedHmac")
            debugData.receivedHmacHex = receivedHmac

            val result = if (receivedHmac == calculatedHmac) {
                val parsed = gson.fromJson(String(decryptedJson), clazz)
                WatcherGlobalLog.encryptor(uuid,"Datos desencriptados: " + gson.toJson(parsed))
                CryptoDebugLogger.logDebugData(debugData)

                parsed 
            } else {
                WatcherGlobalLog.encryptor(uuid,"Fallo en la verificación HMAC")
                 debugData.error = "Fallo en la verificación HMAC"
                 CryptoDebugLogger.logErrorData(debugData)

                 null

             //   val parsed = gson.fromJson(String(decryptedJson), clazz)
               // WatcherGlobalLog.encryptor(uuid,"Datos desencriptados: " + gson.toJson(parsed))
                //CryptoDebugLogger.logDebugData(debugData)

               // parsed
            }
    
            return result
    
        } catch (e: Exception) {
            debugData.error = e.message
            CryptoDebugLogger.logErrorData(debugData)
    
            WatcherGlobalLog.encryptor(uuid,"Error al desencriptar: ${e.message}")
            e.printStackTrace()
        }
    
        return null
    }
    
    fun <T> decryptBody(serializedData: String, clazz: Class<T>): T? {
        WatcherGlobalLog.encryptor(uuid,"Datos recibidos para desencriptar: $serializedData")

        val split = serializedData.replace("\n", "").split(".")
        if (split.size < 4) {
            throw Exception("Bad Data")
            return null
        }
        val serializedDataObj = SerializedData()
        serializedDataObj.uuid = split[0].trim()
        uuid = serializedDataObj.uuid
        serializedDataObj.serialKey = split[1].trim()
        serializedDataObj.metaData = split[2].trim()
        serializedDataObj.hash = split[3].trim()
        WatcherGlobalLog.encryptor(uuid,"Datos recibidos para desencriptar: $serializedData")
        WatcherGlobalLog.encryptor(uuid,"Datos serializados parseados: " + gson.toJson(serializedDataObj))

        return decryptMessage(serializedDataObj, clazz)
    }

    private fun decryptRSA(encryptedKey: ByteArray, twoChars: String): ByteArray {
        val modulus = BigInteger(Offuscator.desofuscate(PrivateRSATx.values().first { it.id == twoChars }.modulus), 16)
        val exponent = BigInteger(Offuscator.desofuscate(PrivateRSATx.values().first { it.id == twoChars }.exponent), 16)
        val spec = RSAPrivateKeySpec(modulus, exponent)
        return try {
            val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            rsa.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(spec))
            val decryptedKey = rsa.doFinal(encryptedKey)
            WatcherGlobalLog.encryptor(uuid,"Llave RSA desencriptada: ${StringUtils.encodeHexString(decryptedKey)}")
            decryptedKey
        } catch (e: Exception) {
            WatcherGlobalLog.encryptor(uuid,"Error al desencriptar la llave RSA: ${e.message}")
            ByteArray(0)
        }
    }

    private fun getStampSuffix(): String {
        val zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
        val suffix = zonedDateTime.format(DateTimeFormatter.ofPattern("MMyyHHdd"))
        return Offuscator.offuscate(suffix)
    }

    private fun getStampPreffix(): String {
        val zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
        val prefix = zonedDateTime.format(DateTimeFormatter.ofPattern("HHddMMyy"))
        return Offuscator.offuscate(prefix)
    }

    private fun calculateHMACSHA256(data: ByteArray): ByteArray {
        try{
            WatcherGlobalLog.encryptor(uuid,"Calculando HMAC SHA-256| calculateHMACSHA256_data=${StringUtils.encodeHexString(data)},calculateHMACSHA256_aesKey=${StringUtils.encodeHexString(aesKey)}")
        }catch(ex:Exception){

        }
        val hmacSha256 = "HmacSHA256"
        val secretKey = SecretKeySpec(aesKey, hmacSha256)
        val mac = Mac.getInstance(hmacSha256)
        mac.init(secretKey)
        val result=mac.doFinal(data)
        try{
            WatcherGlobalLog.encryptor(uuid,"calculateHMACSHA256_result=${StringUtils.encodeHexString(result)}")

        }catch(ex:Exception){

        }
        return result
    }

    private fun decryptAES(data: ByteArray): ByteArray {
        WatcherGlobalLog.encryptor(uuid,"decryptAES_data: ${StringUtils.encodeHexString(data)}")
        if (data.size < 16 || data.size % 16 != 0) {
            WatcherGlobalLog.encryptor(uuid,"Input data length must be a multiple of 16 bytes ${data.size} RESIDUO= ${data.size % 16 != 0}")
        }

        val iv = data.copyOfRange(0, 16)
        val encryptedData = data.copyOfRange(16, data.size)
        val keySpec = SecretKeySpec(aesKey.copyOf(16), "AES")
        WatcherGlobalLog.encryptor(uuid,"AES_KEY: ${StringUtils.encodeHexString(aesKey)}")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(encryptedData)
        WatcherGlobalLog.encryptor(uuid,"decryptAES_decrypted: ${StringUtils.encodeHexString(decrypted)}")
        return decrypted
    }

    fun recoverTwoChars(serial: String): Pair<String, String> {
        val positionHex = serial.substring(0, 4)
        val position = positionHex.toInt(16)
        val hexString = serial.substring(4)
        val twoChars = hexString.substring(position, position + 2)
        val originalString = hexString.removeRange(position, position + 2)
        return Pair(originalString, twoChars)
    }
}
