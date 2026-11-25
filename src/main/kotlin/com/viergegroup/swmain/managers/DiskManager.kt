package com.viergegroup.swmain.managers

import com.shared.commons.AppSettings
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.Executors

object DiskManager {
    private val lock = ReentrantLock() // Bloqueo para evitar empalmes

    fun manageDiskSpace() {
        if (!lock.tryLock()) { 
            WatcherGlobalLog.diskWatcher("Otro proceso de limpieza está en ejecución. Saliendo...")
            return
        }
 
        try {  
            val thresholdPercent = AppSettings.getInstance().deleteThresholdPercent
            WatcherGlobalLog.diskWatcher("manageDiskSpace=>thresholdPercent:$thresholdPercent")

            val root = File("/")
            val totalSpace = root.totalSpace.toDouble()
            val freeSpace = root.freeSpace.toDouble()
            val usedPercent = ((totalSpace - freeSpace) / totalSpace) * 100

            WatcherGlobalLog.diskWatcher("Espacio usado: ${"%.2f".format(usedPercent)}%")

            if (usedPercent > thresholdPercent) {
                WatcherGlobalLog.diskWatcher("El espacio utilizado supera el $thresholdPercent%. Ejecutando script de limpieza...")
                executeCleanupScript() 
            } else {
                WatcherGlobalLog.diskWatcher("El espacio utilizado está por debajo del umbral.")
            }
        } finally {
            lock.unlock()
        }
    }

    private fun executeCleanupScript() {
        val startTime = LocalDateTime.now()
        WatcherGlobalLog.diskWatcher("Inicio de ejecución del script: $startTime")

        try {
            val process = ProcessBuilder("sudo", "/bin/bash", "/home/ec2-user/movelogs.sh")
                .redirectErrorStream(true) // Combina salida estándar y de error
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor() // Espera a que termine sin límite de tiempo

            val endTime = LocalDateTime.now()
            val duration = Duration.between(startTime, endTime).toHours()

            if (exitCode == 0) {
                WatcherGlobalLog.diskWatcher("Script ejecutado con éxito.")
                sendDiskSpaceFinishedAlarm(output, startTime, endTime, duration)
            } else {
                WatcherGlobalLog.diskWatcher("Error al ejecutar el script. Código de salida: $exitCode")
                sendDiskSpaceErrorAlarm(output, startTime, endTime, duration)
            }
        } catch (e: Exception) {
            WatcherGlobalLog.diskWatcher("Excepción al intentar ejecutar el script: ${e.message}")
            sendDiskSpaceErrorAlarm("Excepción: ${e.message}", LocalDateTime.now(), LocalDateTime.now(), 0)
        }
    }

    private fun sendDiskSpaceFinishedAlarm(output: String, startTime: LocalDateTime, endTime: LocalDateTime, duration: Long) {
        val message = """
            <b>Script de limpieza ejecutado con éxito.</b>
            Inicio: $startTime
            Fin: $endTime
            Duración: $duration horas
            Salida del script:
            <pre>$output</pre>
        """.trimIndent()

        AlarmEmailService.sendAlarmEmail(
            AlarmCause.DISK_SPACE_FINISHED,
            message
        )
        WatcherGlobalLog.diskWatcher("Alarma DISK_SPACE_FINISHED enviada.")
    }

    private fun sendDiskSpaceErrorAlarm(output: String, startTime: LocalDateTime, endTime: LocalDateTime, duration: Long) {
        val message = """
            <b>Error al ejecutar el script de limpieza.</b>
            Inicio: $startTime
            Fin: $endTime
            Duración: $duration horas
            Salida del script:
            <pre>$output</pre>
        """.trimIndent()

        AlarmEmailService.sendAlarmEmail(
            AlarmCause.DISK_SPACE_ERROR,
            message
        )
        WatcherGlobalLog.diskWatcher("Alarma DISK_SPACE_ERROR enviada.")
    }

    fun startDiskSpaceChecker() {
        WatcherGlobalLog.diskWatcher("startDiskSpaceChecker")

        val scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate({ manageDiskSpace() }, 0, 167, java.util.concurrent.TimeUnit.MINUTES)
    }
}
