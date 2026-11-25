package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import org.springframework.data.jpa.repository.Modifying
import java.math.BigDecimal 



@Repository
interface TransactionsMetaDataRepository : CrudRepository<TransactionsDBMetaData, Long> {

    @Modifying 
    @Transactional
    @Procedure("get_today_transaction_reports")
    fun getTodayTransactionReports(
            @Param("p_report_type") reportType: String  ,
            @Param("p_host_type") hostType: String  

    )    :List<TransactionsDBMetaData>
     
   
   
   

}