package com.viergegroup.swmain.dbmanagers

import com.shared.commons.ErrorCode 
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.decodeHex
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbservices.ClientActivityDBService
import com.viergegroup.swmain.dbservices.TransactionsMetaDataDBService 
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto 
 
import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData
import com.viergegroup.swmain.dbmodels.ReportType
import com.viergegroup.swmain.entities.catalogs.HostType 

internal object   TransactionsMetaDataDBManager {
    private lateinit var transactionsMetaDataDBService: TransactionsMetaDataDBService
    fun init( transactionsMetaDataDBService: TransactionsMetaDataDBService){
        this.transactionsMetaDataDBService=transactionsMetaDataDBService
    } 
  
    fun getTodayTransactionReports(reportType: ReportType,hostType:HostType): List<TransactionsDBMetaData> {
        val results = transactionsMetaDataDBService.getTodayTransactionReports(reportType,hostType)
           return results 
    }
 
} 