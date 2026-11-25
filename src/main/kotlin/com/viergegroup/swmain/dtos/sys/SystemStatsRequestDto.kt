package com.viergegroup.swmain.dtos.sys

import java.time.LocalDateTime
import com.viergegroup.swmain.entities.RequestEntity

 class SystemStatsRequestDto:RequestEntity() {
    var startDate: LocalDateTime= LocalDateTime.of(1993, 11, 1, 0, 0)
    var endDate: LocalDateTime= LocalDateTime.of(1993, 11, 1, 0, 0)
    var startRow: Int = 0
    var totalRows: Int = 10   
 }
    
  