package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.OperationId
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import com.viergegroup.swmain.entities.catalogs.TransactionMessageType
import org.junit.runner.Request
import java.time.LocalDate
import java.time.LocalDateTime

class TransactionFilterDto:RequestEntity() {
    var transactionId: Long=-1
    var serverName:String=""
    var operationId: OperationId = OperationId.Undefined
    var rrnFilter=""
    var transactionStatus= TransactionStatus.Undefined
    var transactionMessageType= TransactionMessageType.Undefined
    var authorizationIdResponse: String = ""
    var terminalSerialNumberFilter=""
    var startDate: LocalDateTime = LocalDateTime.of(1993, 11, 1, 0, 0)
    var endDate: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59)
    var startRow: Int = 0 
    var totalRows: Int = 10
}
