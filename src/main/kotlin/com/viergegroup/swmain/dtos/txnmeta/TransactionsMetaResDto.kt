
package com.viergegroup.swmain.dtos.txnmeta

import java.time.ZonedDateTime
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY
import java.time.LocalDateTime
import com.viergegroup.swmain.dbmodels.ReportType 
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.dtos.txnmeta.TransactionsDBMetaDto
import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import com.viergegroup.swmain.dtos.txnmeta.TransactionsJDBMetaDto

 class TransactionsMetaResDto:ResponseEntity(){ 
    var reportType:ReportType=ReportType.Terminal
    var data:TransactionsJDBMetaDto=TransactionsJDBMetaDto()
}    