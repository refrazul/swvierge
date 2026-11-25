package com.viergegroup.crypto.security

import com.shared.commons.AppSettings
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog 

import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.nio.file.Files
import java.nio.file.Paths
import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import java.util.Date

object JWTHelper {

    // Recomendación: usar clave en formato PKCS#8 (BEGIN PRIVATE KEY)
    // openssl pkcs8 -topk8 -nocrypt -in jwt_private_key_es256.pem -out jwt_private_key_es256_pkcs8.pem
  
    fun loadPrivateKey(watcher: Watcher): ECPrivateKey {
        val documentPath = "${AppSettings.getInstance().vaultDir}/jwt_private_key_es256_pkcs8.pem"
        watcher.debug("[loadPrivateKey] Reading key from: $documentPath")
        try {
            val keyBytes = Files.readAllBytes(Paths.get(documentPath))
            watcher.debug(" [loadPrivateKey] Key file read: ${keyBytes.size} bytes")

            val pem = String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

            watcher.debug(" [loadPrivateKey] Cleaned PEM length: ${pem.length}")
            val decoded = Base64.getDecoder().decode(pem)
            watcher.debug(" [loadPrivateKey] Decoded key length: ${decoded.size}")

            val keySpec = PKCS8EncodedKeySpec(decoded)
            val keyFactory = KeyFactory.getInstance("EC")
            val ecKey = keyFactory.generatePrivate(keySpec) as ECPrivateKey

            watcher.debug(" [loadPrivateKey] EC Private Key successfully loaded")
            return ecKey
        } catch (e: Exception) {
            watcher.debug("[loadPrivateKey] Error loading private key: ${e.message}")
            throw e
        }
    }

    fun generateSignedJWT(terminalProfileId: String, kid: String, watcher: Watcher): String {
        watcher.debug(" [generateSignedJWT] Starting JWT generation")
        watcher.debug("  terminalProfileId: $terminalProfileId")
        watcher.debug("  keyId (kid): $kid")
        val privateKey = loadPrivateKey(watcher)

        try {
            val signer = ECDSASigner(privateKey)
            watcher.debug(" [generateSignedJWT] Signer created")

            val header = JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(kid)
                .type(JOSEObjectType.JWT)
                .build()

            watcher.debug("[generateSignedJWT] JWT Header: alg=${header.algorithm.name}, kid=$kid")

            val now = Date()
            val exp = Date(now.time + 5 * 60 * 1000) // 5 min
            watcher.debug("🕒 [generateSignedJWT] Issued at: $now | Expires at: $exp")

            val claims = JWTClaimsSet.Builder()
                .subject(terminalProfileId)
                .issuer("viergegroup")
                .issueTime(now)
                .expirationTime(exp)
                .build()

            watcher.debug(" [generateSignedJWT] Claims set created with subject: $terminalProfileId")

           /*val signedJWT = SignedJWT(header, claims)
            signedJWT.sign(signer)
            watcher.debug(" [generateSignedJWT] JWT successfully signed")

            val serialized = signedJWT.serialize()
            watcher.debug(" [generateSignedJWT] Serialized JWT: ${serialized.take(50)}...")
 */ 
            return ""
        } catch (e: Exception) {
            watcher.debug(" [generateSignedJWT] Error generating JWT: ${e.message}")
            throw e
        }
    }
}
