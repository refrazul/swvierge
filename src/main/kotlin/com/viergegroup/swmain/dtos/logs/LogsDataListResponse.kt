package com.viergegroup.swmain.dtos.logs

import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.entities.ResponseEntity

class LogsDataListResponse : ResponseEntity() {
    var data: List<LogsMetaDataDBDto> = emptyList()
}
    