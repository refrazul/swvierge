package com.viergegroup.swmain.dtos.clientactivity

import com.viergegroup.swmain.dbmodels.ClientActivityDBData
import com.viergegroup.swmain.entities.ResponseEntity

class ClientActivityDataListResponse : ResponseEntity() {
    var data: List<ClientActivityDBDataDto> = emptyList()
}
