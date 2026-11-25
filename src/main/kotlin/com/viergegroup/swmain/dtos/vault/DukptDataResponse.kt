package com.viergegroup.swmain.dtos.vault

import com.viergegroup.swmain.entities.ResponseEntity

class DukptDataResponse:ResponseEntity() {
    var bdk = ""

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
    var ipek = ""
    var kcv = ""
    var ipekEncrypted = ""
    var ksnEncrypted = ""
    var hsmCommandEncrypt = ""
    var hsmCommandDecrypt = ""
    var ipekDecrypted= ""
    var previousCounter = 1
    var cipherCounter = 1
    var keyId = ""
}