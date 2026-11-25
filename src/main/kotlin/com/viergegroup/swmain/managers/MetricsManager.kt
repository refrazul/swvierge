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

object MetricsManager { 
    private val executor = Executors.newScheduledThreadPool(1)
    private val monitoringActive = AtomicBoolean(false)

    private val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private const val NETWORK_USAGE_THRESHOLD = 97.0 // Umbral de red (%)
    private const val CPU_USAGE_THRESHOLD = 97.0 // Umbral de CPU (%)
    private const val RAM_USAGE_THRESHOLD = 97.0 // Umbral de RAM (%)

    private val systemInfo = SystemInfo()  
    private val operatingSystem = systemInfo.operatingSystem
    private var systemStatsData=SystemStatsData()
   
    var totalMemory :Double=0.0 
    var usedMemory:Double=0.0   
  

    init {
        WatcherGlobalLog.metricsWatcher("MetricsManager inicializado.")
    }    

    fun startMonitoring() {
        if (!monitoringActive.compareAndSet(false, true)) {
            WatcherGlobalLog.metricsWatcher("El monitoreo ya está activo.")
            return
        }

        WatcherGlobalLog.metricsWatcher("Iniciando monitoreo de métricas...")
        executor.scheduleAtFixedRate({
            try {
                monitorMetrics()
            } catch (e: Exception) {
                WatcherGlobalLog.metricsWatcher("Error en monitoreo de métricas: ${e.message}")
            }
        }, 0, 47, java.util.concurrent.TimeUnit.MINUTES)
    }
    fun getSystemRAM(): Double { 
        val systemInfo = SystemInfo()
        val hardware = systemInfo.hardware
        val ramBytes = hardware.memory.total
        return ramBytes.toDouble() / (1024 * 1024 * 1024) // Convertir a GB
    }
    fun getSystemUsedRAM(): Double {

        val systemInfo = SystemInfo()
        val hardware = systemInfo.hardware
        val available = hardware.memory.available
        val totalRam = hardware.memory.total
        return ((totalRam.toDouble()- available.toDouble())/ (1024 * 1024 * 1024)).toDouble() // Convertir a GB

    }
     
    
     
    fun setMetricsLogs(dbData: LogsMetaData): LogsMetaData {
        val runtime = Runtime.getRuntime() 
        val bytesInGB = 1024 * 1024 * 1024 // 1 GB = 1024 MB = 1024 * 1024 KB = 1024 * 1024 * 1024 bytes
        // Convertir la memoria total y libre a GB
        totalMemory = getSystemRAM()
        usedMemory = getSystemUsedRAM()
        val percentage = 100-((totalMemory - usedMemory) / totalMemory) * 100
    
        try { 
            dbData.ramTotal = BigDecimal(totalMemory)
             dbData.ramUsed = BigDecimal(usedMemory)
            dbData.ramUsagePercentage = BigDecimal(percentage.toString())
      
        } catch (ex: Exception) { 
            WatcherGlobalLog.metricsWatcher("getRamUsage ${ex.message}")
        }
    
        try { 
            val root = File("/")
            val totalSpace = root.totalSpace.toDouble() / bytesInGB // Convertir a GB
            val freeSpace = root.freeSpace.toDouble() / bytesInGB // Convertir a GB
            val usedPercent = ((totalSpace - freeSpace) / totalSpace) * 100
    
            dbData.storageTotal = BigDecimal(totalSpace )
            dbData.storageUsed =BigDecimal (totalSpace - freeSpace)
            dbData.storageUsagePercentage = BigDecimal(usedPercent.toString()) 
    
        } catch (ex: Exception) {
            WatcherGlobalLog.metricsWatcher("checkDiskSpace ${ex.message}")
        }
    
        val allProcesses = operatingSystem.processes
        val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount
    
        val topProcesses = allProcesses
            .sortedByDescending { (it.processCpuLoadCumulative / numberOfCores) * 100 }
        // .take(5)
        var cpuUsage = 0.0
        topProcesses.forEach { process ->
            cpuUsage += (process.processCpuLoadCumulative / numberOfCores) * 100
        }
    
        try {
            dbData.cpuUsagePercentage = BigDecimal(cpuUsage.toString())
    
        } catch (ex: Exception) {
            WatcherGlobalLog.metricsWatcher("getCpuUsage ${ex.message}")
        }
    
        return dbData
    }


    
    fun setMetrics(dbData: TransactionDBData): TransactionDBData {
        val runtime = Runtime.getRuntime()
        val bytesInGB = 1024 * 1024 * 1024 // 1 GB = 1024 MB = 1024 * 1024 KB = 1024 * 1024 * 1024 bytes
        // Convertir la memoria total y libre a GB
        totalMemory = getSystemRAM()
        usedMemory = getSystemUsedRAM()
        val percentage = 100-((totalMemory - usedMemory) / totalMemory) * 100
       
        try { 
            dbData.ramTotal = BigDecimal(totalMemory)
             dbData.ramUsed = BigDecimal(usedMemory)
            dbData.ramUsagePercentage = BigDecimal(percentage.toString())
    
        } catch (ex: Exception) { 
            WatcherGlobalLog.metricsWatcher("getRamUsage ${ex.message}")
        }
    
        try {
            val root = File("/")
            val totalSpace = root.totalSpace.toDouble() / bytesInGB // Convertir a GB
            val freeSpace = root.freeSpace.toDouble() / bytesInGB // Convertir a GB
            val usedPercent = ((totalSpace - freeSpace) / totalSpace) * 100
    
            dbData.storageTotal = BigDecimal(totalSpace )
            dbData.storageUsed =BigDecimal (totalSpace - freeSpace)
            dbData.storageUsagePercentage = BigDecimal(usedPercent.toString()) 
    
        } catch (ex: Exception) {
            WatcherGlobalLog.metricsWatcher("checkDiskSpace ${ex.message}")
        }
    
        val allProcesses = operatingSystem.processes
        val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount
    
        val topProcesses = allProcesses
            .sortedByDescending { (it.processCpuLoadCumulative / numberOfCores) * 100 }
        // .take(5)
        var cpuUsage = 0.0
        topProcesses.forEach { process ->
            cpuUsage += (process.processCpuLoadCumulative / numberOfCores) * 100
        }
    
        try {
            dbData.cpuUsagePercentage = BigDecimal(cpuUsage.toString())
    
        } catch (ex: Exception) {
            WatcherGlobalLog.metricsWatcher("getCpuUsage ${ex.message}")
        }
    
        return dbData
    }


    
    private fun monitorMetrics() {
        WatcherGlobalLog.metricsWatcher("Revisando métricas del sistema...")
        WatcherGlobalLog.metricsWatcher("cpuUsage...")

        val cpuUsage = getCpuUsage()
        WatcherGlobalLog.metricsWatcher("getRamUsage...")

        val ramUsage = getRamUsage()

         
     
        if (cpuUsage > CPU_USAGE_THRESHOLD) {
            val formattedCpuUsage = "%.2f".format(cpuUsage) // Formatear a 2 decimales
            WatcherGlobalLog.metricsWatcher("CPU_USAGE_THRESHOLD excedido: $formattedCpuUsage%")
            sendAlarm(AlarmCause.CPU_WARNING, CPU_USAGE_THRESHOLD, "CPU_USAGE_THRESHOLD excedido: $formattedCpuUsage%")
        }
        
        if (ramUsage > RAM_USAGE_THRESHOLD) {
            val formattedRamUsage = "%.2f".format(ramUsage) // Formatear a 2 decimales
            val formattedusedMemory = "%.2f".format(usedMemory) // Formatear memoria libre
            val formattedTotalMemory = "%.2f".format(totalMemory) // Formatear memoria total
            WatcherGlobalLog.metricsWatcher("RAM_USAGE_THRESHOLD excedido: $formattedRamUsage%")
            sendAlarm(
                AlarmCause.RAM_WARNING,
                RAM_USAGE_THRESHOLD,
                "Uso de RAM excedido: $formattedRamUsage% , USADA: $formattedusedMemory TOTAL: $formattedTotalMemory"
            )
        }
          
      
        checkDiskSpace()
      //  SystemStatsDBManager.insertSystemStats(systemStatsData)


        
    }

    fun checkDiskSpace() {
      
        val bytesInGB = 1024 * 1024 * 1024 // 1 GB = 1024 MB = 1024 * 1024 KB = 1024 * 1024 * 1024 bytes

        try {  
           
            val root = File("/")
            val totalSpace = root.totalSpace.toDouble()/bytesInGB
            val freeSpace = root.freeSpace.toDouble()/bytesInGB
            val usedPercent = ((totalSpace - freeSpace) / totalSpace) * 100

            systemStatsData.storageTotal= totalSpace.toLong()
            systemStatsData.storageUsed= totalSpace.toLong()-freeSpace.toLong()
            systemStatsData.storageUsagePercentage=BigDecimal(usedPercent.toString())

        } catch(ex:Exception) {
            WatcherGlobalLog.metricsWatcher("checkDiskSpace ${ex.message}")

        }
    }

    private fun getCpuUsage(): Double {
        val allProcesses = operatingSystem.processes
        val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount

        val topProcesses = allProcesses
            .sortedByDescending { (it.processCpuLoadCumulative / numberOfCores) * 100 }
           // .take(5)
            var cpuUsage =0.0
        topProcesses.forEach { process ->
            cpuUsage+= (process.processCpuLoadCumulative / numberOfCores) * 100
          
        }

 

        
        try {  
            systemStatsData.cpuUsagePercentage=  BigDecimal(cpuUsage.toString());


        } catch(ex:Exception) {
            WatcherGlobalLog.metricsWatcher("getCpuUsage ${ex.message}")

        }


        return cpuUsage
    }
    

    private fun getRamUsage(): Double {
        totalMemory = getSystemRAM()
        usedMemory = getSystemUsedRAM()
        val percentage = 100-((totalMemory - usedMemory) / totalMemory) * 100
    
        try {  
            systemStatsData.ramTotal=totalMemory.toLong()
            systemStatsData.ramUsed=( usedMemory).toLong()
           systemStatsData.ramUsagePercentage=  BigDecimal(percentage.toString());
    
        } catch(ex:Exception) {
            WatcherGlobalLog.metricsWatcher("getRamUsage ${ex.message}")

        }

        return percentage 
    }
 
    private fun getNetworkUsage(): Double {
        val networkInterfaces = systemInfo.hardware.networkIFs
        var totalBytesSentStart = 0L
        var totalBytesReceivedStart = 0L
        var totalMaxSpeed = 0L
    
        // Obtener estadísticas iniciales y la capacidad máxima de cada interfaz
        networkInterfaces.forEach { networkIF ->
            networkIF.updateAttributes()
            totalBytesSentStart += networkIF.bytesSent
            totalBytesReceivedStart += networkIF.bytesRecv
            totalMaxSpeed += networkIF.speed // Sumamos las velocidades de las interfaces activas
        }
    
        Thread.sleep(60000) // Esperar 1 minuto
    
        var totalBytesSentEnd = 0L
        var totalBytesReceivedEnd = 0L
    
        // Obtener estadísticas finales
        networkInterfaces.forEach { networkIF ->
            networkIF.updateAttributes()
            totalBytesSentEnd += networkIF.bytesSent
            totalBytesReceivedEnd += networkIF.bytesRecv
        }
    
        // Diferencia de bytes transferidos en un minuto
        val totalBytesSent = totalBytesSentEnd - totalBytesSentStart
        val totalBytesReceived = totalBytesReceivedEnd - totalBytesReceivedStart
    
        // Total de bytes transferidos
        val totalBytes = totalBytesSent + totalBytesReceived
    
        // Convertir a megabytes por segundo
        val totalMBPerMinute = totalBytes / (1024.0 * 1024.0) // Convertir a MB
        val totalMBPerSecond = totalMBPerMinute / 60.0 // MBps
    
        // Convertir la capacidad máxima de bits por segundo a MBps
        val maxMBps = totalMaxSpeed / (8.0 * 1024.0 * 1024.0) // Convertir de bits a MBps
    
        // Evitar divisiones por cero si no hay interfaces con velocidad configurada
        if (maxMBps <= 0) return 0.0
        val usage= (totalMBPerSecond / maxMBps) * 100
        systemStatsData.cpuUsagePercentage=  BigDecimal(usage.toString());

        // Cálculo del porcentaje de uso de red
        return usage
    }
    

    private fun sendAlarm(cause: AlarmCause, threshold: Double, msg: String) {
        // Obtener el número de procesadores lógicos
        val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount
    
        val emailBody = StringBuilder()
        emailBody.append("<b>$msg</b><br>")
    
        when (cause) {
            AlarmCause.CPU_WARNING, AlarmCause.RAM_WARNING -> {
                emailBody.append("<b>Procesos con mayor uso de recursos:</b><br>")
                emailBody.append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
                emailBody.append("<thead><tr><th>PID</th><th>Nombre</th><th>CPU (%)</th><th>RAM (MB)</th></tr></thead>")
                emailBody.append("<tbody>")
        
                val allProcesses = operatingSystem.processes
                val numberOfCores = systemInfo.hardware.processor.logicalProcessorCount
        
                val topProcesses = allProcesses
                    .sortedByDescending { (it.processCpuLoadCumulative / numberOfCores) * 100 }
                   // .take(5)
        
                topProcesses.forEach { process ->
                    val cpuUsage = (process.processCpuLoadCumulative / numberOfCores) * 100
                    val ramUsage = process.residentSetSize / (1024 * 1024)
                    if(cpuUsage>0){
                        emailBody.append("<tr>")
                        emailBody.append("<td>${process.processID}</td>")
                        emailBody.append("<td>${process.name}</td>")
                        emailBody.append("<td>${"%.2f".format(cpuUsage)}</td>")
                        emailBody.append("<td>${ramUsage}</td>")
                        emailBody.append("</tr>")
                    }
                 
                }
        
                emailBody.append("</tbody></table>")
            }
            AlarmCause.NETWORK_WARNING -> {
                emailBody.append("<b>Estadísticas de Red:</b><br>")
                emailBody.append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
                emailBody.append("<thead><tr><th>Interfaz</th><th>Bytes Enviados</th><th>Bytes Recibidos</th><th>Uso (%)</th></tr></thead>")
                emailBody.append("<tbody>")
            
                val networkInterfaces = systemInfo.hardware.networkIFs
            
                // Guardar las estadísticas iniciales
                val initialStats = networkInterfaces.associateBy(
                    { it.name },
                    { Pair(it.bytesSent, it.bytesRecv) }
                )
            
                Thread.sleep(60000) // Esperar 1 minuto
            
                networkInterfaces.forEach { networkIF ->
                    networkIF.updateAttributes()
            
                    // Obtener estadísticas iniciales
                    val (initialBytesSent, initialBytesReceived) = initialStats[networkIF.name] ?: Pair(0L, 0L)
            
                    // Calcular los datos transferidos durante el intervalo
                    val bytesSent = (networkIF.bytesSent - initialBytesSent).toDouble()
                    val bytesReceived = (networkIF.bytesRecv - initialBytesReceived).toDouble()
                    val totalBytes = bytesSent + bytesReceived
            
                    // Convertir a MB
                    val bytesSentMB = bytesSent / (1024 * 1024)
                    val bytesReceivedMB = bytesReceived / (1024 * 1024)
                    val totalMB = totalBytes / (1024 * 1024)
            
                    // Calcular la capacidad máxima en MBps
                    val maxMBps = if (networkIF.speed > 0) networkIF.speed / (8.0 * 1024 * 1024) else 0.0
            
                    // Calcular el uso de red como porcentaje
                    val usagePercentage = if (maxMBps > 0) (totalMB / 60.0 / maxMBps) * 100 else 0.0
            
                    // Agregar los datos a la tabla
                    emailBody.append("<tr>")
                    emailBody.append("<td>${networkIF.name}</td>")
                    emailBody.append("<td>${"%.2f".format(bytesSentMB)} MB</td>")
                    emailBody.append("<td>${"%.2f".format(bytesReceivedMB)} MB</td>")
                    emailBody.append("<td>${"%.2f".format(usagePercentage)}%</td>")
                    emailBody.append("</tr>")
                } 
            
                emailBody.append("</tbody></table>")
            } 
              
            
        }
    
        AlarmEmailService.sendAlarmEmail(cause, emailBody.toString())
        WatcherGlobalLog.metricsWatcher("Correo enviado con detalles para $cause")
    }  
    
  
    fun stopMonitoring() {
        if (!monitoringActive.compareAndSet(true, false)) {
            WatcherGlobalLog.metricsWatcher("El monitoreo ya está detenido.")
            return
        }
        executor.shutdown()
        WatcherGlobalLog.metricsWatcher("Monitoreo de métricas detenido.")
    }
}
