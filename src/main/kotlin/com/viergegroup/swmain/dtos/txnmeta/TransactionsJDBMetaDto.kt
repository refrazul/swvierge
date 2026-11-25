package com.viergegroup.swmain.dtos.txnmeta

import com.viergegroup.swmain.dbmodels.ReportType
import com.viergegroup.swmain.dbmodels.TransactionsDBMetaData


import java.math.BigDecimal
import java.text.DecimalFormat

class TransactionsJDBMetaDto {

    var reportType: ReportType = ReportType.Terminal
    var servers:  List<ServerDto>   = listOf()

    class ServerDto {
        var serverName: String = "" 
        var metrics: MetricsDto = MetricsDto()
        var hostSummary: List<HostDto> = listOf()
    }
    class TransactionsPerMinDto {
        var lastUpdateDateTime: String = ""
        var totalTxnsRejected: Long = 0
        var totalTxnsApproved: Long = 0
        var totalTransactionCommerr: Long = 0
    }

    class MetricsDto {
        var ramTotal: Double = 0.00
        var ramUsed: Double = 0.00
        var ramUsagePercentage: Double = 0.00
        var storageTotal: Double = 0.00
        var storageUsed: Double = 0.00
        var storageUsagePercentage: Double = 0.00
        var networkUsagePercentage: Double = 0.00
        var cpuUsagePercentage: Double = 0.00
        var dbConnectionsCount: Int = 0
    }

    class HostDto {
        var hostType: String = ""
        var terminalSerialNumber: String = ""
        var merchantType: String = ""
        var merchantTypeCode: String = ""
        var transactionType: String = ""
        var txnsPerMin: TransactionsPerMinDto= TransactionsPerMinDto()
        var transactionsSummary: List<TransactionsDto> = listOf()

    }

    class TransactionsDto {
        var reportTimePeriod: String=""
        
        var dateTimeInitialStr: String = ""
        var dateTimeEndStr: String = ""
        var timeZone: String = ""
        var totalTransactionApprovedAmount: String = ""
        var totalTransactionApprovedCount: Long = 0
        var totalTransactionRejectedAmount: String = ""
        var totalTransactionRejectedCount: Long = 0
    
        var transactionCurrencyCodeNumber: Int = 484
        var totalTxnsCommerrCount: Long = 0
      
    }

    
    companion object {
                                    
// Función para formatear BigDecimal con separadores de miles y dos decimales
fun formatBigDecimal(value: BigDecimal): String {
    val formatter = DecimalFormat("#,##0.00") // Define el formato
    return formatter.format(value)
}

        fun toTransactionsJDBMetaDto(list: List<TransactionsDBMetaData>): TransactionsJDBMetaDto {
            return TransactionsJDBMetaDto().apply {
                // Asignar el reportType (se toma del primer elemento de la lista, asumiendo que es el mismo para todos)
                reportType = list.firstOrNull()?.reportType ?: ReportType.Terminal

                // Agrupar los datos por servidor
                servers = list.groupBy { it.serverName }
                    .map { (serverName, serverData) ->
                        ServerDto().apply {
                            this.serverName = serverName

                            // Mapear las métricas del servidor (se toman del primer elemento del grupo)
                            metrics = MetricsDto().apply {
                                val firstServerData = serverData.first()
                                ramTotal = firstServerData.ramTotalPm
                                ramUsed = firstServerData.ramUsedPm
                                ramUsagePercentage = firstServerData.ramUsagePercentagePm
                                storageTotal = firstServerData.storageTotalPm
                                storageUsed = firstServerData.storageUsedPm
                                storageUsagePercentage = firstServerData.storageUsagePercentagePm
                                networkUsagePercentage = firstServerData.networkUsagePercentagePm
                                cpuUsagePercentage = firstServerData.cpuUsagePercentagePm
                                dbConnectionsCount = firstServerData.connectionCountPm
                            }

                            // Agrupar los datos por hostType
                            hostSummary = serverData.groupBy { it.hostType }
                                .map { (hostType, hostData) ->
                                    HostDto().apply {
                                        this.hostType = hostType
                                        terminalSerialNumber = hostData.firstOrNull()?.terminalSerialNumber ?: ""
                                        merchantType = hostData.firstOrNull()?.merchantType ?: ""
                                        merchantTypeCode = hostData.firstOrNull()?.merchantTypeCode ?: ""
                                        transactionType = hostData.firstOrNull()?.transactionType ?: ""

                                        // Mapear txnsPerMin
                                        txnsPerMin = TransactionsPerMinDto().apply {
                                            lastUpdateDateTime = hostData.firstOrNull()?.lastUpdatePmDateTime.toString() ?: ""
                                            totalTxnsRejected = hostData.map { it.totalTxnsRejectedPm }.average().toLong()
                                            totalTxnsApproved = hostData.map { it.totalTxnsApprovedPm }.average().toLong()
                                            totalTransactionCommerr = hostData.map { it.totalTransactionCommerrPm }.average().toLong()
                                        } 
                                        
                    
                                        // Mapear las transacciones
                                        transactionsSummary = hostData.map { data ->
                                            TransactionsDto().apply {
                                                reportTimePeriod = data.reportTimePeriod
                                                dateTimeInitialStr = data.dateTimeInitial.toString()
                                                dateTimeEndStr = data.dateTimeEnd.toString()
                                                timeZone = data.timeZone
                                        

                                                    // Uso en tu código
                                                    totalTransactionApprovedAmount = 
                                                        "${data.transactionCurrencySymbol} ${data.transactionCurrencyCode} ${formatBigDecimal(data.totalTransactionApprovedAmount)}"

                                                    totalTransactionApprovedCount = data.totalTransactionApprovedCount

                                                    totalTransactionRejectedAmount = 
                                                        "${data.transactionCurrencySymbol} ${data.transactionCurrencyCode} ${formatBigDecimal(data.totalTransactionRejectedAmount)}"
                                                        
                                                   totalTransactionRejectedCount = data.totalTransactionRejectedCount
                                                transactionCurrencyCodeNumber = data.transactionCurrencyCodeNumber
                                                totalTxnsCommerrCount = data.totalTxnsCommerrCount
                                            }
                                        }
                                    }
                                }
                        }
                    }
            }
        }
    }
} 
