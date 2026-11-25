import com.shared.commons.StringUtils
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

class HsmEncryptor {

    @Throws(Exception::class)
    fun encryptMessage(hsmKeys: HsmKeys, message: ByteArray): HsmMessage {
        var hsmMessage = HsmMessage()
        val aesKeyStatic = StringUtils.encodeHexString(generateAesKey().encoded)  //"79EA9658059341F8296CEAC574BF78A2"
        WatcherGlobalLog.debug("\"*********Aes HEX:" + aesKeyStatic)

        val decodedKey = Base64.getDecoder().decode(aesKeyStatic.toByteArray())

        WatcherGlobalLog.debug(" decodedKey.size: "+ decodedKey.size)
        val aesKey = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
 

        hsmMessage= encryptMessage(hsmMessage, aesKey, message)
        hsmMessage=  encryptKey(hsmMessage, hsmKeys.getServerPublicKey(), aesKey)
        hsmMessage= generateSignature(hsmMessage, hsmKeys.getClientPrivateKey())
        return hsmMessage
    }

    private fun generateAesKey(): SecretKey {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(128)
        return generator.generateKey()
    }

    private fun encryptMessage(hsm: HsmMessage, aesKey: SecretKey, message: ByteArray):HsmMessage {
        val staticIv = ByteArray(16)
        SecureRandom().nextBytes(staticIv)
        WatcherGlobalLog.debug("*********IV ")
        for (j in staticIv.indices) {
            System.out.format("%02X", staticIv[j])
        }

        WatcherGlobalLog.debug("\n\n")
        val ivspec = IvParameterSpec(staticIv)
        val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
        ci.init(Cipher.ENCRYPT_MODE, aesKey, ivspec)
        hsm.iv = toBase64UrlEncoded(ci.iv)
        hsm.encryptedMessage = toBase64UrlEncoded(ci.doFinal(message))

        WatcherGlobalLog.debug("********* ENCRYPTED MESSAGE:" +    hsm.encryptedMessage)

        return hsm

    }

    @Throws(GeneralSecurityException::class)
    private fun encryptKey(hsmMessage: HsmMessage, rsaKey: PublicKey?, aesKey: SecretKey):HsmMessage {

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, rsaKey,  HsmCryptoUtils.getRsaParameterSpec(true))

        val encryptedAesKey = cipher.doFinal(aesKey.encoded) 
        val base64EncodedAesKey = toBase64UrlEncoded(encryptedAesKey)

        WatcherGlobalLog.debug("AES KEY RESULTADO : $base64EncodedAesKey")
        hsmMessage.aesKey=base64EncodedAesKey
        return hsmMessage

    }

    @Throws(GeneralSecurityException::class)
    private fun generateSignature(hsmMessage: HsmMessage, signatureKey: PrivateKey?) :HsmMessage{
        val signature = Signature.getInstance("SHA256withRSA", "SunRsaSign")
        signature.initSign(signatureKey)
        signature.update(hsmMessage.getDataToSign().toByteArray(StandardCharsets.UTF_8))

        hsmMessage.signature = toBase64UrlEncoded(signature.sign())
        WatcherGlobalLog.debug("FIRMA  :" + hsmMessage.signature)

        return hsmMessage
    }

    fun toBase64UrlEncoded(data: ByteArray): String {

        return customBase64Encode( Base64.getUrlEncoder().encodeToString(data))
    }

    fun customBase64Encode(data: String): String {


        return data.replace("+/=", "-_.")
 .replace("/=", "_.")
            .replace("+/", "-_")
 .replace( "+", "-")
.replace( "/", "_")
.replace(  "=", ".");


  }


}
