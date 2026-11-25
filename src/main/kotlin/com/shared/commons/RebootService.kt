package com.shared.commons

import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.BufferedReader
import java.io.InputStreamReader
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class RebootService {  

    private val lastRebootFile = "${AppSettings.getInstance().dataPath}/lastReboot.txt"
    private val minutesBetweenReboots = AtomicInteger(11111117) // Tiempo mínimo entre reboots (en minutos)
   
    private val rebootPipeCounter = AtomicInteger(0)
    //var isRebooting=false 
    private val isRebooting = AtomicBoolean(false)
    fun canRebootSystem(): String { 
        val lastRebootTime = getLastRebootTime()
        val now = LocalDateTime.now()
        val currentMinutesBetweenReboots = AppSettings.getInstance().minutesBetweenReboots
        this.minutesBetweenReboots.set(currentMinutesBetweenReboots)
        val canReboot = AppSettings.getInstance().canReboot
        WatcherGlobalLog.rebootDebug(" canRebootSystem_minutesBetweenReboots [${currentMinutesBetweenReboots}]")
        WatcherGlobalLog.rebootDebug(" canRebootSystem_canReboot [${canReboot}]")
        if (!canReboot) return "NO canReboot"
    
        if (lastRebootTime != null && ChronoUnit.MINUTES.between(lastRebootTime, now) < currentMinutesBetweenReboots) {
            val msg = "You Must wait $currentMinutesBetweenReboots minutes to reboot"
            WatcherGlobalLog.rebootDebug(msg)
            return msg 
        }

        return executeReboot(now)
    }

   fun canRebootSystem(details:String): String { 
        val lastRebootTime = getLastRebootTime()
        val now = LocalDateTime.now()
        val currentMinutesBetweenReboots = AppSettings.getInstance().minutesBetweenReboots
        this.minutesBetweenReboots.set(currentMinutesBetweenReboots)
        val canReboot = AppSettings.getInstance().canReboot
        WatcherGlobalLog.rebootDebug(" canRebootSystem_minutesBetweenReboots [${currentMinutesBetweenReboots}]")
        WatcherGlobalLog.rebootDebug(" canRebootSystem_canReboot [${canReboot}]")
        if (!canReboot) return "NO canReboot"
    
        if (lastRebootTime != null && ChronoUnit.MINUTES.between(lastRebootTime, now) < currentMinutesBetweenReboots) {
            val msg = "You Must wait $currentMinutesBetweenReboots minutes to reboot"
            WatcherGlobalLog.rebootDebug(msg)
            return msg 
        }

        return executeReboot(now)
    }




    fun forceReboot(): String {
        val now = LocalDateTime.now()
        WatcherGlobalLog.rebootDebug("forceReboot STARTED...")
       // return "false"
        return executeReboot(now)
    }

    fun rebootByPipeBroken(): String {
        val previousCount = rebootPipeCounter.getAndIncrement()
        val currentCount = previousCount + 1
        WatcherGlobalLog.rebootDebug("rebootByPipeBroken rebootPipeCounter $previousCount")

        return if (currentCount >= 2) {
            forceReboot()
        } else { 
            forceReboot()
        }
    }

     private fun executeReboot(details:String,now: LocalDateTime): String {
       
      /*   if(isRebooting) return  "IS_BUSSY"

        isRebooting=true
        val serviceName = AppSettings.getInstance().initScript // Nombre del servicio en systemd */
            if (!isRebooting.compareAndSet(false, true)) return "IS_BUSSY"

            val serviceName = AppSettings.getInstance().initScript

        AlarmEmailService.sendAlarmEmail(AlarmCause.SYSTEM_RESTART,"Restarting service [$serviceName] using systemctl DETAILS [${details}]")
        return try {
            // Reiniciar el servicio iniswtrp.service
            val processBuilder = ProcessBuilder("sudo", "systemctl", "restart", serviceName)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            WatcherGlobalLog.rebootDebug("Restarting service [$serviceName] using systemctl")
            val exitCode = process.waitFor()
            if (exitCode == 0) { 
                saveLastRebootTime(now) // Guarda la fecha y hora del reinicio
                val msg = "Service [$serviceName] restarted successfully"
                WatcherGlobalLog.rebootDebug(msg)
                msg
            } else {
                val msg = "Error restarting service [$serviceName]: exit code $exitCode"
                WatcherGlobalLog.rebootDebug(msg)
                msg
            }
        } catch (e: IOException) {
            e.printStackTrace()
            val msg = "Error restarting service EXCEPTION: ${e.message}"
            WatcherGlobalLog.rebootDebug(msg)
            msg
        } catch (e: InterruptedException) {
            e.printStackTrace()
            val msg = "Error restarting service EXCEPTION: ${e.message}"
            WatcherGlobalLog.rebootDebug(msg)
            msg
        } finally {
            isRebooting.set(false)
        }
    }
    
    private fun executeReboot(now: LocalDateTime): String {
       
       /*  if(isRebooting) return  "IS_BUSSY"

        isRebooting=true
        val serviceName = AppSettings.getInstance().initScript // Nombre del servicio en systemd */

        if (!isRebooting.compareAndSet(false, true)) return "IS_BUSSY"

        val serviceName = AppSettings.getInstance().initScript
        AlarmEmailService.sendAlarmEmail(AlarmCause.SYSTEM_RESTART,"Restarting service [$serviceName] using systemctl")
        return try {
            // Reiniciar el servicio iniswtrp.service
            val processBuilder = ProcessBuilder("sudo", "systemctl", "restart", serviceName)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            WatcherGlobalLog.rebootDebug("Restarting service [$serviceName] using systemctl")
            val exitCode = process.waitFor()
            if (exitCode == 0) { 
                saveLastRebootTime(now) // Guarda la fecha y hora del reinicio
                val msg = "Service [$serviceName] restarted successfully"
                WatcherGlobalLog.rebootDebug(msg)
                msg
            } else {
                val msg = "Error restarting service [$serviceName]: exit code $exitCode"
                WatcherGlobalLog.rebootDebug(msg)
                msg
            }
        } catch (e: IOException) {
            e.printStackTrace()
            val msg = "Error restarting service EXCEPTION: ${e.message}"
            WatcherGlobalLog.rebootDebug(msg)
            msg
        } catch (e: InterruptedException) {
            e.printStackTrace()
            val msg = "Error restarting service EXCEPTION: ${e.message}"
            WatcherGlobalLog.rebootDebug(msg)
            msg
        } finally {
            isRebooting.set(false)
        }
    }
    
    private fun getLastRebootTime(): LocalDateTime? {
        return try {
            if (File(lastRebootFile).exists()) {
                val lastReboot = Files.readString(Paths.get(lastRebootFile))
                LocalDateTime.parse(lastReboot)
            } else {
                null   
            } 
        } catch (e: IOException) {
            e.printStackTrace()
            null 
        }
    }

    private fun saveLastRebootTime(time: LocalDateTime) {
        try {
            Files.writeString(Paths.get(lastRebootFile), time.toString(), StandardOpenOption.CREATE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
