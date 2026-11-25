package com.viergegroup.swmain.dtos.pandora

import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType

class DBKeyResponseDto : ResponseEntity() {
    var hostType: HostType = HostType.Testing
    var keySerialNumber=""
    var dbKey=""
    var kcv=""
    var creationDateTime: String=""
 
}