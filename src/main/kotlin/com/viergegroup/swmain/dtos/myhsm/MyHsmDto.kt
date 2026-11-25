package com.viergegroup.swmain.dtos.myhsm

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType


class MyHsmDto {
    class DUKPTRequest: RequestEntity(){
        var bdk=""
        var ksn="" 
        var data=""


    }

    class DUKPTResponse: ResponseEntity() {
        var data=""
        var command=""
        var errorMsg=""


    }

}