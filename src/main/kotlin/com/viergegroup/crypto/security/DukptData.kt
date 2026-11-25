package com.viergegroup.crypto.security

import com.google.gson.GsonBuilder
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.shared.commons.GsonConfig

class DukptData:RequestEntity() {
    var bdk = ""
    var errorCode = -1
    var errorMessage = "" 
    var errorMessageHSM = ""
   // var pekEncrypted = ""
    var clearCipherBlocks= mutableListOf<String>()
    var chipherDataBlocks= mutableListOf<String>() 
    var clearData = ""
    var dataEncrypted = "" 
    var tk = ""
    var lastModification = ""
    var prevKsn = ""
    var ksn = ""
    var pek = ""

    var ipek = ""
    var ipekClear = ""

    var ipekEncrypted = ""
    var ksnEncrypted = ""
    var hsmCommandEncrypt = ""
    var hsmCommandDecrypt = ""
    var ipekDecrypted= ""
    var previousCounter = 1
    var cipherCounter = 1
    var keyId = ""
    var kcv = ""
    fun toJsonLog(): String? {
        return  GsonConfig.gsonLogs.toJson(this)
    }  
   // var derivatedKeysData = DerivatedKeysData()
    fun toJson(): String? {
        val gson = GsonBuilder().setPrettyPrinting().create()
        //  val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    } 
}