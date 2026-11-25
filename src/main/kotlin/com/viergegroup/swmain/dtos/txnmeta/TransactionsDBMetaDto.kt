
package com.viergegroup.swmain.dtos.txnmeta

import java.time.ZonedDateTime
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY
import java.time.LocalDateTime
import com.viergegroup.swmain.dbmodels.ReportType 
 
 class TransactionsDBMetaDto{  

    var dateTimeInitialStr:  String=""

    var dateTimeEndStr:  String=""

    var reportTimePeriod: String=""

    var hostType: String=""

    var serverName: String=""

    var timeZone: String =""

    var terminalSerialNumber: String=""
 
    var merchantType: String=""

    var merchantTypeCode: String=""
    
    var transactionType: String=""

    var totalTransactionApprovedAmount: Double = 0.0

    var totalTransactionApprovedCount: Long = 0

    var totalTransactionRejectedAmount: Double = 0.0

    var totalTransactionRejectedCount: Long = 0

    //var creationDateTime: LocalDateTime = LocalDateTime.now()

  //  var lastModificationDateTime: LocalDateTime = LocalDateTime.now()
    var reportType: ReportType =ReportType.Terminal
 
    var transactionCurrencyCode: String = ""

    var transactionCurrencySymbol: String = "" 

    var transactionCurrencyCodeNumber: Int = 484

    var totalTxnsCommerrCount: Long = 0

  //  var lastUpdatePmDateTime: LocalDateTime = LocalDateTime.now()

    var totalTxnsRejectedPm: Long = 0

    var totalTxnsApprovedPm: Long = 0

    var totalTransactionCommerrPm: Long = 0

    var ramTotalPm: Long = 0

    var ramUsedPm: Long = 0

    var ramUsagePercentagePm: Double = 0.00

    var storageTotalPm: Long = 0

    var storageUsedPm: Long = 0

    var storageUsagePercentagePm: Double = 0.00

    var networkUsagePercentagePm: Double = 0.00

    var cpuUsagePercentagePm: Double = 0.00

    var connectionCountPm: Int = 0
}