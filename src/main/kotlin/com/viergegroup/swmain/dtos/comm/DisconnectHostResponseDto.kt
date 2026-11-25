package com.viergegroup.swmain.dtos.comm

import com.viergegroup.swmain.entities.ResponseEntity

class DisconnectHostResponseDto : ResponseEntity() {

    var hostIp=""
    var hostPort=0
    var timeOut=0
    var isConnected=false

}