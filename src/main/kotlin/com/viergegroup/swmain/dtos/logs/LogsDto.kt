package com.viergegroup.swmain.dtos.logs

import com.fasterxml.jackson.annotation.JsonInclude
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LogsDto : RequestEntity(){
    var logData: String = ""


}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LogsResponseDto : ResponseEntity(){


}