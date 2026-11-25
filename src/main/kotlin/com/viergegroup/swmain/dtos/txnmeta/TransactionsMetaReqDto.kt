
package com.viergegroup.swmain.dtos.txnmeta

import java.time.ZonedDateTime
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY
import java.time.LocalDateTime
import com.viergegroup.swmain.dbmodels.ReportType 
import com.viergegroup.swmain.entities.RequestEntity

 class TransactionsMetaReqDto:RequestEntity(){ 
    var reportType:ReportType=ReportType.Terminal
    
}