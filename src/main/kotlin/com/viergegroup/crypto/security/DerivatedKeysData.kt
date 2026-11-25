package com.viergegroup.crypto.security

import com.google.gson.GsonBuilder
import com.shared.commons.GsonConfig

class DerivatedKeysData {
    var bdk = ""
    var derivatedIpek = ""
    var derivedPekAsIpek = ""
    var derivedPekAsBdk = ""
    var baseDerivationKey = ""
    var encryptionKey = ""
    var encryptionKeyBytes = ByteArray(24)
    fun toJsonLog(): String? {
      
        return GsonConfig.gsonLogs .toJson(this)
    }  

    fun toJson(): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    } 
}


class DerivatedKeysHsmData {
    var bdk = ""
    var ipekForData = ""
    var pekForData = ""
    var ipekForPin = ""
    var pekForPin = ""
    var pinBlockClear = ""
    var pinClear = ""

    fun toJsonLog(): String? {
      
        return GsonConfig.gsonLogs .toJson(this)
    }  

    fun toJson(): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    } 
}