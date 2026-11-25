package com.viergegroup.crypto.security

import com.shared.commons.ErrorCode
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.shared.commons.AppSettings

class BankRsaKey : RequestEntity() {
    var id: String = ""
    var modulus: String = ""
    var exponent: String = ""
    var rsaKey: String = "" 
    var kcv: String = ""
    var hash: String = "" 
    var errorCode = -1
    var errorMessage = ""

    companion object {

        fun getBankRsaKeyByHost(hostType: HostType, keyId: String, watcher: Watcher): BankRsaKey {
            watcher.debug("BankRsaKey_getBankRsaKeyByHost ${hostType}")

            if (keyId.equals("")) {
                
                val settings=AppSettings.getHostConfig(hostType)
                watcher.debug("${settings.isDev} [${hostType.authorizer }]")
                if (hostType.authorizer == Authorizer.PROSA&&settings.isDev) {
                    watcher.debug("GETTING RSA: A000VIER48 ")
                    return getBankRsaKey(hostType, "A000VIER48", watcher)
                }     
                if (hostType.authorizer == Authorizer.PROSA&&settings.isDev) {
                    watcher.debug("GETTING RSA: N000ARFT22 ")
                    return getBankRsaKey(hostType, "N000ARFT22", watcher)
                }     

                if (hostType.authorizer == Authorizer.PROSA&&settings.isDev) {
                    watcher.debug("GETTING RSA: A000VIE1 ")
                    return getBankRsaKey(hostType, "A000VIE1", watcher)
                }    
 

                if (hostType.authorizer == Authorizer.PROSA) {
                    watcher.debug("GETTING RSA: A000VIER89 ")
                    return getBankRsaKey(hostType, "A000VIER89", watcher)
                } 
                if (hostType.authorizer == Authorizer.PROSA) {
                    watcher.debug("GETTING RSA: N000ARFT24 ")
                    return getBankRsaKey(hostType, "N000ARFT24", watcher)
                } 
            }
            return getBankRsaKey(hostType, keyId, watcher)
        }

        fun getBankRsaKeyByHost(hostType: HostType, keyId: String): BankRsaKey {
            WatcherGlobalLog.debug("BankRsaKey_getBankRsaKeyByHost ${hostType}")

            if (keyId.equals("")) {
                if (hostType.authorizer == Authorizer.PROSA) {
                    WatcherGlobalLog.debug("GETTING RSA: N000ARFT24 ")
                    return getBankRsaKey(hostType, "N000ARFT24")
                }
            }
            return getBankRsaKey(hostType, keyId)
        }
        fun getBankRsaKey(hostType: HostType, idRsa: String): BankRsaKey {
            WatcherGlobalLog.debug(" BankRsaKey_getBankRsaKey: $idRsa")
            var bankRsaKey = BankRsaKey()
            try {
                val dto = VaultDtos.RequestReadRSAData()
                dto.id = idRsa
                dto.hostType = hostType
                bankRsaKey = VaultSecureManager().readRsaKey(dto)
                bankRsaKey.errorCode = 0
                bankRsaKey.errorMessage = "ok"
            } catch (e: Exception) {
                WatcherGlobalLog.error("BankRsaKey_ERROR_LOADING_RSA: " + e.message)
                bankRsaKey.errorCode = ErrorCode.RSADOesntExists.errorCodeInt
                bankRsaKey.errorMessage =
                        e.message.toString() +
                                " \"${idRsa}\" " +
                                ErrorCode.RSADOesntExists.errorCodeMessage
                bankRsaKey = BankRsaKey()
            }
            return bankRsaKey
        }

        fun getBankRsaKey(hostType: HostType, idRsa: String, watcher: Watcher): BankRsaKey {
            watcher.debug(" getBankRsaKey: $idRsa")
            var bankRsaKey = BankRsaKey()
            try {
                val dto = VaultDtos.RequestReadRSAData()
                dto.id = idRsa
                dto.hostType = hostType
                bankRsaKey = VaultSecureManager().readRsaKey(dto)
                bankRsaKey.errorCode = 0
                bankRsaKey.errorMessage = "ok"
            } catch (e: Exception) {
                watcher.error("ERROR LOADING RSA: " + e.message)
                bankRsaKey.errorCode = ErrorCode.RSADOesntExists.errorCodeInt
                bankRsaKey.errorMessage =
                        e.message.toString() +
                                " \"${idRsa}\" " +
                                ErrorCode.RSADOesntExists.errorCodeMessage
                bankRsaKey = BankRsaKey()
            }
            return bankRsaKey
        }

        private fun parseRsa(rsa: String): RsaKeyData {
            /* var bankRsaKey: BankRsaKey=BankRsaKey()
            val KEY_RSA = "LLAVE RSA PUBLICA"
            val KEY_ID = "ID DE LLAVE:"
            val id = fileContent.substring(fileContent.indexOf(KEY_ID) + KEY_ID.length, fileContent.indexOf(KEY_RSA))
            */
            val keyData = RsaKeyData()

            // val rsa = fileContent.substring(fileContent.indexOf(KEY_RSA) + KEY_RSA.length)
            val modulusLenStr = rsa.substring(12, 16)
            val modulusLen = modulusLenStr.toInt(16) * 2
            val modulus = rsa.substring(16, 16 + modulusLen)
            val expLen = rsa.substring(16 + modulusLen + 2, 16 + modulusLen + 2 + 2)
            val exponent =
                    rsa.substring(
                            16 + modulusLen + 2 + 2,
                            +16 + modulusLen + 2 + 2 + expLen.toInt() * 2
                    )
            keyData.modulus = (modulus)
            keyData.exponent = (exponent)
            return keyData
        }
    }
}
