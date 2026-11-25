package com.viergegroup.crypto.security


import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.StringUtils
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.webwatcher.WatcherOperationLog
import com.viergegroup.swmain.webwatcher.WatcherContext
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.zip.CRC32
import javax.crypto.*
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8
 class KeyPairData{

     var terminalSerialNumber:String=""
     var publicKey: ByteArray= ByteArray(0)
     var privateKey: ByteArray=ByteArray(0)

     var publicKeyData:BankRsaKey=BankRsaKey()
     var privateKeyData:BankRsaKey=BankRsaKey()
 }

 class RSARequestData{
     var terminalSerialNumber:String=""
     var hostType: HostType = HostType.ProsaDUKPT
     var text:String=""
 }

 class RSAResponseData{
     var terminalSerialNumber:String=""
     var result:String=""

 }
 
object RSAUtils {

    // Método para calcular el hash SHA-1
    fun calculateRsaHashSha1(modulus: ByteArray, exponent: ByteArray): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        sha1.update(modulus)
        sha1.update(exponent)
        return StringUtils.encodeHexString(sha1.digest())
    }

    // Método para calcular el hash SHA-256
    fun calculateRsaHashSha256(modulus: ByteArray, exponent: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        sha256.update(modulus)
        sha256.update(exponent)
        return StringUtils.encodeHexString(sha256.digest())
    }

    // Método para calcular el hash SHA-512
    fun calculateRsaHashSha512(modulus: ByteArray, exponent: ByteArray): String {
        val sha512 = MessageDigest.getInstance("SHA-512")
        sha512.update(modulus)
        sha512.update(exponent)
        return StringUtils.encodeHexString(sha512.digest())
    }

  
        // Método para derivar una clave AES usando el módulo y el exponente
        fun deriveAesKey(modulus: ByteArray, exponent: ByteArray): ByteArray {
            val keyData = modulus + exponent // Combina el módulo y el exponente
            val digest = MessageDigest.getInstance("SHA-256").digest(keyData) // Derivación de clave con SHA-256
            return digest.copyOf(16)

        }

   // Método para derivar una clave Triple DES usando el módulo y el exponente
    fun deriveTripleDesKey(modulus: ByteArray, exponent: ByteArray): ByteArray {
        val keyData = modulus + exponent // Combina el módulo y el exponente
        val digest = MessageDigest.getInstance("SHA-256").digest(keyData) // Derivación de clave con SHA-256

        return digest.copyOf(24)

    }
    // Método para calcular el hash AES usando el módulo y el exponente
    fun calculateAesHash(modulus: ByteArray, exponent: ByteArray): String {
        val key = deriveAesKey(modulus, exponent)
        val dataToHash = ByteArray(16) // Array de ceros de 16 bytes
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(dataToHash) // Usando el array de ceros como datos

        return StringUtils.encodeHexString(encrypted)

    }

    // Método para calcular el hash Triple DES usando el módulo y el exponente
    fun calculateTripleDesHash(modulus: ByteArray, exponent: ByteArray): String {
        val key = deriveTripleDesKey(modulus, exponent)
        val dataToHash = ByteArray(24) // Array de ceros de 24 bytes
        val cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "DESede")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(dataToHash) // Usando el array de ceros como datos
        return StringUtils.encodeHexString(encrypted)

    }

    /*fun calculateHashRsa(strModulus: String, strExponent: String, watcher: Watcher): String {
         var rsaMessage = ByteArray(256)  
         val result=this.RSACipher(rsaMessage,strModulus,strExponent,watcher)
         val hexstr=StringUtils.encodeHexString(result) 
         watcher.debug("calculateHashRsa_hexstr ${hexstr}")
         return hexstr
     }*/ 
     

         
    fun calculateHashRsa(modulusHex: String, exponentHex: String): String {

        var modulus = BigInteger(modulusHex, 16)
        var exponent = BigInteger(exponentHex, 16)

        modulus = modulus.abs()
        exponent = exponent.abs()

        val messageBytes = ByteArray(256)
        Arrays.fill(messageBytes, 0x30.toByte())

        val message = BigInteger(1, messageBytes)

        val cipher = message.modPow(exponent, modulus)

        var cipherBytes = cipher.toByteArray()

        val keySizeBytes = (modulus.bitLength() + 7) / 8

        if (cipherBytes.size > keySizeBytes) {
            cipherBytes = Arrays.copyOfRange(cipherBytes, cipherBytes.size - keySizeBytes, cipherBytes.size)
        }

        if (cipherBytes.size < keySizeBytes) {
            val paddedCipher = ByteArray(keySizeBytes)
            System.arraycopy(cipherBytes, 0, paddedCipher, keySizeBytes - cipherBytes.size, cipherBytes.size)
            cipherBytes = paddedCipher
        }

        val result = cipherBytes.take(4).joinToString("") { String.format("%02X", it) }

        return result
    } 

    fun encryptClientCred(message: ByteArray, watcher: Watcher): ByteArray {
        watcher.debug("RSACipher")
 
         return RSACipher(message,"00a0e6768691d52bdb4ee7b929e355b25d8f91946f593c1d16aa3b82016a0b010ffd8544d5f9f333eb374aca985da8b84ccf94952ededfdd15007dc005320051e661c36695e5cc6280c64a61d2f3841b063fed164db5069af727964ccfdc8157c50765e92b0e98b6c936031c7bd5d60d92783bb33e2c44bceb10655e2f78579b12f50e1334f868bb68437e8e5a86f55bbde79b846aca571a5343b5054f99a29a09fe882d1b1d8743e14ba9771b489a64221c588181e5e2338892ee28e29b08fe12327816723337881667efb6aa8026afab7f296fae43d0884ddc93caeb3dbeb047ad39676f4600e1f0f55326e6437d29944fff0e47026134d391a7d8280cc693a5","010001",watcher)
     }
 

    fun RSACipher(message: ByteArray, strModulus: String, strExponent: String, watcher: Watcher): ByteArray {
       watcher.debug("RSACipher")

        val modulus = BigInteger(strModulus, 16)
        val exponent = BigInteger(strExponent, 16)
        val spec = RSAPublicKeySpec(modulus, exponent)
        var rsaMessage = ByteArray(256)  // Inicializar el tamaño de 256 bytes
        try {
        //    val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            rsa.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(spec))
            val encryptedMessage = rsa.doFinal(message)
            rsaMessage = encryptedMessage

        } catch (e: Exception) {
           watcher.error("RSACipher EXCEPTION: ${e.message}")
        }
        return rsaMessage
    }

    fun RSACipherStr(messageStr: String, strModulus: String, strExponent: String): String {
        var rsaMessage = ByteArray(256)  // Inicializar el tamaño de 256 bytes
         try {
            var paddedMessage=messageStr.trim()
             paddedMessage = if (paddedMessage.length % 2 != 0) {
                paddedMessage + "f"
            } else {
                paddedMessage 
            }

            WatcherGlobalLog.debug("RSACipher_messageStr paddedMessage[${paddedMessage}] strModulus [${strModulus}] [${strExponent}]")
            val message: ByteArray=StringUtils.hexStringToByteArrayNeo(paddedMessage.trim()) // 2020AB =>0X20,0X20,0XAB
             val modulus = BigInteger(strModulus.trim(), 16)
             val exponent = BigInteger(strExponent.trim(), 16)
             val spec = RSAPublicKeySpec(modulus, exponent)
             val rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding")
             rsa.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(spec))
             val encryptedMessage = rsa.doFinal(message)
             rsaMessage = encryptedMessage
 
         } catch (e: Exception) {
            WatcherGlobalLog.error("RSACipher_EXCEPTION: [${e.message}]")
            return messageStr+"|"+e.message
         }
         return StringUtils.encodeHexString(rsaMessage) 
     }
 
    // Función para calcular el hash SHA-1 de una llave RSA (módulo y exponente en String)
    fun calculateRsaHashSha1(modulus: String, exponent: String): String {
        // Convertir el módulo y el exponente de hexadecimal a bytes
        val modulusBytes = StringUtils.hexStringToByteArray(modulus)
        val exponentBytes =  StringUtils.hexStringToByteArray(exponent)
        // Combinar módulo y exponente en un solo array de bytes
        val combinedKey = modulusBytes + exponentBytes
        // Calcular el hash usando SHA-1
        val digest = MessageDigest.getInstance("SHA-1")
        val hash = digest.digest(combinedKey)
        return StringUtils.encodeHexString(hash)
    }

    

    fun generateRSAKeyPair(rsarequestData:RSARequestData): KeyPairData {
        val hostType=rsarequestData.hostType
        WatcherGlobalLog.debug("generateRSAKeyPair $hostType ")

        // Generar un nuevo par de llaves RSA
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // Tamaño de la llave: 2048 bits
        val keyPair = keyPairGenerator.generateKeyPair()

        // Obtener las representaciones de bytes de la llave pública y privada
        val publicKeyBytes = keyPair.public.encoded
        // Obtener el módulo y el exponente de la llave pública
        val publicKey = keyPair.public as java.security.interfaces.RSAPublicKey
        val publicKeyModulus = publicKey.modulus.toByteArray()
        val publicKeyExponent = publicKey.publicExponent.toByteArray()

        val privateKeyBytes = keyPair.private.encoded

        // Obtener el módulo y el exponente de la llave privada
        val privateKey = keyPair.private as java.security.interfaces.RSAPrivateKey
        val privateKeyModulus = privateKey.modulus.toByteArray()
        val privateKeyExponent = privateKey.privateExponent.toByteArray()


        // Guardar el par de llaves en un archivo JSON asociado al ID
        val keyPairData = KeyPairData()

        keyPairData.terminalSerialNumber= rsarequestData.terminalSerialNumber
        keyPairData.publicKey=publicKeyBytes
        keyPairData.privateKey=privateKeyBytes

        keyPairData.publicKeyData.terminalSerialNumber= rsarequestData.terminalSerialNumber

        keyPairData.publicKeyData.rsaKey=StringUtils.encodeHexString(  keyPairData.publicKey)
        keyPairData.privateKeyData.rsaKey=StringUtils.encodeHexString(  keyPairData.publicKey)
        keyPairData.publicKeyData.modulus=StringUtils.encodeHexString( publicKeyModulus)
        keyPairData.publicKeyData.exponent=StringUtils.encodeHexString( publicKeyExponent)
        keyPairData.publicKeyData.hostType=rsarequestData.hostType
        keyPairData.publicKeyData.id=rsarequestData.hostType.authorizer.rsaKeyId
        keyPairData.publicKeyData.errorCode=0
        keyPairData.publicKeyData.hostType=rsarequestData.hostType

        keyPairData.privateKeyData.terminalSerialNumber= rsarequestData.terminalSerialNumber
        keyPairData.privateKeyData.hostType=rsarequestData.hostType
        keyPairData.privateKeyData.id=rsarequestData.hostType.authorizer.rsaKeyId+"PRV"
        keyPairData.privateKeyData.errorCode=0
        keyPairData.privateKeyData.hostType=rsarequestData.hostType
        keyPairData.privateKeyData.modulus=StringUtils.encodeHexString( privateKeyModulus)
        keyPairData.privateKeyData.exponent=StringUtils.encodeHexString( privateKeyExponent)

        return keyPairData
    }


    fun encryptText(publicKey: ByteArray, data: ByteArray): ByteArray {
        val publicKeySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKeyObj = keyFactory.generatePublic(publicKeySpec)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj)
        return cipher.doFinal(data)
    }

    fun encryptText(publicKey: ByteArray, text: String): ByteArray {
        val publicKeySpec = X509EncodedKeySpec(publicKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKeyObj = keyFactory.generatePublic(publicKeySpec)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj)
        return cipher.doFinal(text.toByteArray())
    }



    fun decryptText(privateKey: ByteArray, encryptedData: ByteArray): ByteArray {
        val privateKeySpec = PKCS8EncodedKeySpec(privateKey)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKeyObj = keyFactory.generatePrivate(privateKeySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKeyObj)

        val decryptedBytes = cipher.doFinal(encryptedData)
        return decryptedBytes
    }

    fun encrypt3DESAscii(datas: String, keys: String): ByteArray {
        val key: ByteArray=StringUtils.hexStringToByteArray(keys)
        val data: ByteArray=StringUtils.asciiToByteArray(datas)
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun decrypt3DESAscii(datas: String, keys: String): ByteArray {
        val key: ByteArray=StringUtils.hexStringToByteArray(keys)
        val data: ByteArray=StringUtils.hexStringToByteArray(datas)
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun encrypt3DES(datas: String, keys: String): ByteArray {
       val key: ByteArray=StringUtils.hexStringToByteArray(keys)
        val data: ByteArray=StringUtils.hexStringToByteArray(datas)
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun decrypt3DES(datas: String, keys: String): ByteArray {
        val key: ByteArray=StringUtils.hexStringToByteArray(keys)
        val data: ByteArray=StringUtils.hexStringToByteArray(datas)
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun genRandomKey(watcher: Watcher): ByteArray {
        val random = SecureRandom()
       watcher.debug("genRandomKey ")

        val randomKey = ByteArray(16) // 128 bits are converted to 16 bytes;
        random.nextBytes(randomKey)
       watcher.debug("genRandomKey END  ")
        var randomKeyStr=StringUtils.encodeHexString(randomKey)
       watcher.debug("genRandomKey randomKeyStr ${randomKeyStr}  ")

        //  Log.e("RSAUtils", "TK [" + Convert.getInstance().bcdToStr(randomKey) + "]");
        return randomKey
    }

    fun RSADecrypt(buffer: ByteArray): String? {
        try {
            val rsa: Cipher
            rsa = Cipher.getInstance("RSA")
            rsa.init(Cipher.DECRYPT_MODE, getPrivateKey(""))
            val utf8 = rsa.doFinal(buffer)
            return String(utf8, UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    fun getPrivateKey(filename: String?): PrivateKey? {
        val f = File(filename)
        val fis = FileInputStream(f)
        val dis = DataInputStream(fis)
        val keyBytes = ByteArray(f.length().toInt())
        dis.readFully(keyBytes)
        dis.close()
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }


    /**
     * Compute the 3DES of an array of bytes using the provided key.
     * @param message   bytes to cipher
     * @param randomKey It should be 24 bytes to work. If minor length then copy the first part to get 24 bytes
     * @return Array of cipher bytes
     * @throws Exception Cipher Exception
     */
    @Throws(Exception::class)
    fun TDesCipher(message: ByteArray, randomKey: ByteArray): ByteArray {
        val bytesKey = ByteArray(24)
        val tKey: ByteArray
        var j = 0
        for (i in 0..23) {
            if (i < 16) bytesKey[i] = randomKey[i] else {
                bytesKey[i] = randomKey[j]
                j++
            }
        }
        val keySpec = DESedeKeySpec(bytesKey)
        val iv = IvParameterSpec(ByteArray(8))
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        tKey = cipher.doFinal(message)

        //  Log.e("RSAUtils","3DES [" + Convert.getInstance().bcdToStr(tKey) + "]");
        return tKey
    }

    @Throws(Exception::class)
    fun TDesDecrypt(message: ByteArray, randomKey: ByteArray): ByteArray {
        val bytesKey = ByteArray(24)
        var j = 0
        for (i in 0..23) {
            if (i < 16) bytesKey[i] = randomKey[i] else {
                bytesKey[i] = randomKey[j]
                j++
            }
        }
        val key: SecretKey = SecretKeySpec(bytesKey, "DESede")
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        //final IvParameterSpec iv = new IvParameterSpec(new byte[0]);
        //@SuppressLint("GetInstance")
        val decipher = Cipher.getInstance("DESede/ECB/NoPadding")
        //decipher.init(Cipher.DECRYPT_MODE, key, iv);
        decipher.init(Cipher.DECRYPT_MODE, key)
        return decipher.doFinal(message)
    }

   
   /*  fun CRC32Calculate(a: ByteArray): ByteArray {
        val crc = CRC32()
        crc.update(a, 0, a.size)
        val l = crc.value
        var hex = java.lang.Long.toHexString(l).uppercase(Locale.getDefault())
        while (hex.length < 8) {
            hex = "0$hex"
        }
        return StringUtils.hexStringToByteArray(hex)
    } 
*/
    fun CRC32Calculate(byteArray: ByteArray): ByteArray {
        val crc32 = CRC32()
        crc32.update(byteArray)
        val crcValue = crc32.value
        // Convert the CRC32 value to a byte array
        return crcValue.toByteArray()
    } 

    // Extension function to convert a Long to ByteArray
    fun Long.toByteArray(): ByteArray {
        val result = ByteArray(4)
        result[0] = (this shr 24 and 0xFF).toByte()
        result[1] = (this shr 16 and 0xFF).toByte()
        result[2] = (this shr 8 and 0xFF).toByte()
        result[3] = (this and 0xFF).toByte()
        return result
    }


}