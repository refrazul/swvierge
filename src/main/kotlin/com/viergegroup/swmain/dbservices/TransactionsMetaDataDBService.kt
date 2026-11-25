package com.viergegroup.swmain.dbservices

import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import com.viergegroup.swmain.dbmodels.ReportType

import com.viergegroup.swmain.entities.catalogs.HostType


import com.viergegroup.swmain.dbrepositories.TransactionsMetaDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import java.math.BigDecimal  
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
 
@Service
class TransactionsMetaDataDBService {
  
    @Autowired  
    lateinit var transactionsMetaDataRepository: TransactionsMetaDataRepository
     
    @Transactional
    fun getTodayTransactionReports(reportType: ReportType,hostType:HostType): List<TransactionsDBMetaData> {
        try { 
            WatcherGlobalLog.debug("getTodayTransactionReports - Entrada: reportType=$reportType")
            val results = transactionsMetaDataRepository.getTodayTransactionReports(reportType.name,hostType.name)
            WatcherGlobalLog.debug("getTodayTransactionReports  - Salida: totalResultados=${results.size}")
            return results   
        } catch (e: Exception) {  
            WatcherGlobalLog.error("getTodayTransactionReports - Error: ${e.message}")
            return emptyList() 
        } 
    } 

    


}  