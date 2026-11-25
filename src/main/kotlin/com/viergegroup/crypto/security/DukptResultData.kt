package com.viergegroup.crypto.security

import com.shared.commons.StringUtils.leftPad

class DukptResultData {
    var errorCode=0
    var errorMessage=""
    var clearData = ""
    var clearCipherBlocks= mutableListOf<String>()
    var chipherDataBlocks= mutableListOf<String>()
    var encryptResult = ""
    var derivedKey = ""
    var previousKsn = ""
    var ksn = ""
    var ipek = ""
    var ksnEncrypted = ""

    var crc32 = ""
    var derivatedKeysData: DerivatedKeysData = DerivatedKeysData()
    var previousCipherCounter = 1
    var cipherCounter = 1

    fun setDukptData(dukptData: DukptData){
        this.encryptResult=dukptData.dataEncrypted
        this.ksn=dukptData.ksn
        this.cipherCounter=dukptData.cipherCounter
        this.errorMessage=dukptData.errorMessage
        this.errorCode=dukptData.errorCode


    }
    fun getCipherCounterStr(): String {
        return leftPad(previousCipherCounter.toString(), 7, "0")
    }

    fun getResultText(): String? {
        return """ENCRYPT_DATA: $encryptResult
derivedKey: $derivedKey
KSN: $ksn
cipherCounter : $cipherCounter 
 crc32: $crc32"""
    }
}