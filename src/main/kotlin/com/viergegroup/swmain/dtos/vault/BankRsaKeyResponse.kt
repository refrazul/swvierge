package com.viergegroup.swmain.dtos.vault

import com.viergegroup.swmain.entities.ResponseEntity

class BankRsaKeyResponse:ResponseEntity() {
    var id: String=""
    var modulus: String=""
    var exponent: String=""
    var rsaKey: String=""
    var kcv: String=""


}