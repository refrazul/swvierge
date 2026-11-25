package com.viergegroup.swmain.dtos.pandora

import com.viergegroup.crypto.security.DukptResultData
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType


class DukptDataDto : ResponseEntity() {
    var hostType= HostType.TestingDUKPTHost

    var clearData = ""
    var dataEncrypted = "" 
    var lastModification = ""
    var ksn = ""
    var ksnEncrypted = ""
    var ipek=""
    var derivedKey=""
    var previousCounter = 1
    var cipherCounter = 1
    var terminalSerialNumber = ""
    var versionName=""
    fun setDukptData(dukptData: DukptResultData){
        this.errorMessage=dukptData.errorMessage
        this.ksn=dukptData.ksn
        this.cipherCounter=dukptData.cipherCounter
        this.clearData=dukptData.clearData
        this.derivedKey=dukptData.derivedKey
        this.ksnEncrypted=dukptData.ksnEncrypted

        this.dataEncrypted=dukptData.encryptResult
        this.ipek=dukptData.ipek


    }

}