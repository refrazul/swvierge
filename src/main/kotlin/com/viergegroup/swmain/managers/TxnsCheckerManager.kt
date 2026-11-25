package com.viergegroup.swmain.managers

import com.shared.commons.AppSettings
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import oshi.SystemInfo
import oshi.software.os.OperatingSystem
import oshi.software.os.OSProcess
import com.viergegroup.swmain.dbmodels.SystemStatsData
import java.math.BigDecimal 
import java.io.File
import com.viergegroup.swmain.dbmanagers.SystemStatsDBManager
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dbmodels.LogsMetaData
import java.io.InputStreamReader
import java.io.BufferedReader
import com.viergegroup.swmain.dtos.txnmeta.TransactionsMetaReqDto
import com.viergegroup.swmain.dbmanagers.TransactionsMetaDataDBManager
import com.viergegroup.swmain.dbmodels.ReportType
import com.viergegroup.swmain.entities.catalogs.HostType
object TxnsCheckerManager {  
    private val executor = Executors.newScheduledThreadPool(1)
    private val monitoringActive = AtomicBoolean(false)

    private val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private const val TXNS_DAY_REJECTED_THRESHOLD = 98.0 // Umbral de CPU (%)
    private const val TXNS_MIN_REJECTED_THRESHOLD = 97.0 // Umbral de RAM (%)

    private val systemInfo = SystemInfo()  
    private val operatingSystem = systemInfo.operatingSystem
    private var systemStatsData=SystemStatsData()
   
    var totalMemory :Double=0.0 
    var usedMemory:Double=0.0     

    private val lastDailyAlarmTime = mutableMapOf<String, Long>()
    private val lastMinuteAlarmTime = mutableMapOf<String, Long>()
    private const val DAILY_ALARM_DELAY = 10 * 60 * 60 * 1000L // 10 horas en milisegundos
    private const val MINUTE_ALARM_DELAY = 10 * 60 * 1000L // 10 minutos en milisegundos

    fun checkTxns() {
        val report = TransactionsMetaDataDBManager.getTodayTransactionReports(ReportType.Universal, HostType.Undefined)

        if (report.isNullOrEmpty()) {
            WatcherGlobalLog.txnsWatcher("No hay reportes para el día de hoy.")
            return
        }

        report.forEach { txn ->
            val currentTime = System.currentTimeMillis()

            if (txn.reportTimePeriod == "Daily") {
                val totalTxns = txn.totalTransactionApprovedCount + txn.totalTransactionRejectedCount
                if (totalTxns > 0) {
                    val rejectedPercentage = (txn.totalTransactionRejectedCount.toDouble() / totalTxns.toDouble()) * 100

                    WatcherGlobalLog.txnsWatcher("serverName: ${txn.serverName}/${txn.hostType} - Porcentaje de Rechazo Diario: $rejectedPercentage%")

                    if (rejectedPercentage >= TXNS_DAY_REJECTED_THRESHOLD) {
                        val lastAlarmTime = lastDailyAlarmTime[txn.terminalSerialNumber] ?: 0L
                        if (currentTime - lastAlarmTime >= DAILY_ALARM_DELAY) {
                            sendAlarm(
                                AlarmCause.TXNS_DAY_WARNING,
                                "Alta tasa de rechazo diario (${rejectedPercentage}%) [Total:${totalTxns}, Aprobadas:${txn.totalTransactionApprovedCount},Rechazadas:${ txn.totalTransactionRejectedCount}] para el Servidor ${txn.serverName} y cliente ${txn.hostType}"
                            )
                            WatcherGlobalLog.txnsWatcher("Alta tasa de rechazo diario (${rejectedPercentage}%) [Total:${totalTxns}, Aprobadas:${txn.totalTransactionApprovedCount},Rechazadas:${ txn.totalTransactionRejectedCount}] para el Servidor ${txn.serverName} y cliente ${txn.hostType}")
                
                            lastDailyAlarmTime[txn.terminalSerialNumber] = currentTime
                        }
                    }
                } else {
                    WatcherGlobalLog.txnsWatcher("Terminal: ${txn.terminalSerialNumber} - Sin transacciones diarias para calcular porcentaje.")
                }
            } else {
                val totalTxnsPm = txn.totalTxnsApprovedPm + txn.totalTxnsRejectedPm
                if (totalTxnsPm > 0) {
                    val rejectedPercentagePm = (txn.totalTxnsRejectedPm.toDouble() / totalTxnsPm.toDouble()) * 100

                    WatcherGlobalLog.txnsWatcher("serverName: ${txn.serverName}/${txn.hostType} - Porcentaje de Rechazo por Minuto: $rejectedPercentagePm%")

                    if (rejectedPercentagePm >= TXNS_MIN_REJECTED_THRESHOLD) {
                        val lastAlarmTime = lastMinuteAlarmTime[txn.terminalSerialNumber] ?: 0L
                        if (currentTime - lastAlarmTime >= MINUTE_ALARM_DELAY) {
                            sendAlarm(
                                AlarmCause.TXNS_MIN_WARNING,
                                "Alta tasa de rechazo por minuto (${rejectedPercentagePm}%) [Total:${totalTxnsPm}, Aprobadas:${txn.totalTxnsApprovedPm},Rechazadas:${ txn.totalTxnsRejectedPm}]  para el Servidor ${txn.serverName} y cliente ${txn.hostType}"
                            )
                            WatcherGlobalLog.txnsWatcher("Alta tasa de rechazo por minuto (${rejectedPercentagePm}%) [Total:${totalTxnsPm}, Aprobadas:${txn.totalTxnsApprovedPm},Rechazadas:${ txn.totalTxnsRejectedPm}]  para el Servidor ${txn.serverName} y cliente ${txn.hostType}")

                            lastMinuteAlarmTime[txn.terminalSerialNumber] = currentTime
                        }
                    }
                } else {
                    WatcherGlobalLog.txnsWatcher("Terminal: ${txn.terminalSerialNumber} - Sin transacciones por minuto para calcular porcentaje.")
                }
            }
        }
    }

 

     private fun sendAlarm(cause: AlarmCause, msg: String) {
        // Obtener el número de procesadores lógicos
        val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount
        val emailBody = StringBuilder()
        emailBody.append("<b>$msg</b><br>")
        AlarmEmailService.sendAlarmEmail(cause, emailBody.toString())
        WatcherGlobalLog.metricsWatcher("Correo enviado con detalles para $cause")
    }  
    fun startMonitoring() {
        if (!monitoringActive.compareAndSet(false, true)) {
            WatcherGlobalLog.txnsWatcher("TxnsCheckerManager ACTIVO.")
            return
        }

        WatcherGlobalLog.txnsWatcher("TxnsCheckerManager Iniciando .")
        executor.scheduleAtFixedRate({
            try {
                checkTxns()
            } catch (e: Exception) {
                WatcherGlobalLog.txnsWatcher("TxnsCheckerManager Error  ${e.message}")
            }
        }, 0, 2, java.util.concurrent.TimeUnit.MINUTES)
    } 
 
}