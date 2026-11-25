package com.viergegroup.swmain.dtos.sys

import com.viergegroup.swmain.entities.ResponseEntity

class SystemStatsResponseDto : ResponseEntity() {
    var totalPages: Int = 0
    var id: Long? = 0
    var list:MutableList<SystemStatsDataDto> =mutableListOf()
} 
  