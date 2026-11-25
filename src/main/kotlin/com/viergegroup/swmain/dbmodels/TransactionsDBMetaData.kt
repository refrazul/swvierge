
package com.viergegroup.swmain.dbmodels

import java.time.ZonedDateTime
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY
import java.time.LocalDateTime
import java.math.BigDecimal
enum class ReportType {
    Universal,
    TransactionType,
    Merchant,
    Terminal 
}
@Entity
@Table(name = "transactions_meta_data")
 class TransactionsDBMetaData{   
    @Id 
    @GeneratedValue(strategy = IDENTITY) 
    @Column(unique = true, nullable = false)
    var transactionsMetaDataId: Long = 0

    var dateTimeInitial: LocalDateTime = LocalDateTime.now()
 
    var dateTimeEnd: LocalDateTime = LocalDateTime.now()

    var reportTimePeriod: String=""

    var hostType: String=""

    var serverName: String=""

    var timeZone: String =""

    var terminalSerialNumber: String=""
 
    var merchantType: String=""

    var merchantTypeCode: String=""
    var transactionType: String=""

    var totalTransactionApprovedAmount: BigDecimal =BigDecimal(0)
 
    var totalTransactionApprovedCount: Long = 0 

    var totalTransactionRejectedAmount: BigDecimal =BigDecimal(0)

    var totalTransactionRejectedCount: Long = 0

    var creationDateTime: LocalDateTime = LocalDateTime.now()

    var lastModificationDateTime: LocalDateTime = LocalDateTime.now()
    @Enumerated(EnumType.STRING)
    var reportType: ReportType =ReportType.Terminal
 
    var transactionCurrencyCode: String = ""

    var transactionCurrencySymbol: String = "" 

    var transactionCurrencyCodeNumber: Int = 484

    var totalTxnsCommerrCount: Long = 0

    var lastUpdatePmDateTime: LocalDateTime = LocalDateTime.now()

    var totalTxnsRejectedPm: Long = 0

    var totalTxnsApprovedPm: Long = 0

    var totalTransactionCommerrPm: Long = 0

    var ramTotalPm: Double = 0.00

    var ramUsedPm: Double = 0.00 

    var ramUsagePercentagePm: Double = 0.00

    var storageTotalPm: Double = 0.00

    var storageUsedPm: Double = 0.00

    var storageUsagePercentagePm: Double = 0.00

    var networkUsagePercentagePm: Double = 0.00

    var cpuUsagePercentagePm: Double = 0.00

    var connectionCountPm: Int = 0 
}