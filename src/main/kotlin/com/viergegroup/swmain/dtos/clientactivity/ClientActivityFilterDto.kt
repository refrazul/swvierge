package com.viergegroup.swmain.dtos.clientactivity
import com.viergegroup.swmain.entities.RequestEntity
import java.time.LocalDate
import java.time.LocalDateTime

class ClientActivityFilterDto :RequestEntity(){
    var startDate: LocalDateTime = LocalDateTime.of(1993, 11, 1, 0, 0)
    var endDate: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59)
    var startRow: Int = 0
    var totalRows: Int = 15
    var terminalSerialNumberFilter=""
}