package com.viergegroup.swmain.dtos.transactions

import java.time.LocalDateTime

class TransactionHistoryFilterDto {

    var serverName: String=""
    var hostType:String=""
    var terminalSerialNumber: String=""
    var rrn: String=""
    var startDate: LocalDateTime = LocalDateTime.of(1993, 11, 1, 0, 0)
    var endDate: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59)
    var startRow: Int=0
    var  totalRows: Int=10

}