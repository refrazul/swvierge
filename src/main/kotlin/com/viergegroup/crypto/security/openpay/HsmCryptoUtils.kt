
import java.security.GeneralSecurityException
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.MGF1ParameterSpec

import javax.crypto.Cipher
import java.security.Signature
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource


object HsmCryptoUtils {


    /** Algoritmo a usar para encriptar mensaje. */
    @Throws(GeneralSecurityException::class)
    fun getAesCipher(): Cipher {
        return Cipher.getInstance("AES/CBC/PKCS5Padding")
    }

    /** Algoritmo a usar para firmar mensaje. */
    @Throws(GeneralSecurityException::class)
    fun getSignatureAlgorithm(): Signature {
        return Signature.getInstance("SHA256withRSA", "SunRsaSign")
    }

    /** Algoritmo a usar para encriptar llave simetrica. */
    @Throws(GeneralSecurityException::class)
    fun getRsaCipher(): Cipher {
        return Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING")
    }

    /** Parámetros del algoritmo de llave simétrica. */
    fun getRsaParameterSpec(isEncryption: Boolean): AlgorithmParameterSpec {
        return if (isEncryption) {
            OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
        } else {
            OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
        }
    }
}