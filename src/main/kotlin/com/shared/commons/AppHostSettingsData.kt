package com.shared.commons

import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.LogLevel
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime 
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import com.viergegroup.swmain.entities.catalogs.TransactionType
import kotlin.random.Random

class AppHostSettingsData {
    var authorizerId: Authorizer = Authorizer.PROSA
    var hostName="TestingDUKPTHost"
    var hostIp = "127.0.0.1"

    var logsHostLevel= listOf<String>()
    var logsOperationLevel= listOf<String>()

    var simulateHSM = true
    var enableConsoleLogs = true
    var enableKeyLoadHarcode = false
    var maxBuferResponseSize=17096
    
    var numberOfLogicalNetworks: Int = 1
    
    var balancePolicy: String = "ROUND_ROBIN"     // luego puedes meter "LEAST_IN_FLIGHT"


    var maxConnectionTries:Int=12
    var emulateErrors=listOf<ErrorEmulateSettingData>()
    var hostPort = 8080
    var hostConnectionTimeOut =300000
    var checkMessageReceptionEvery:Long = 3000
    var sendEchoTestEvery :Long= 300000
    var checkReversesEvery: Long = 1000
    var hostMessageCheckDelay:Long = 1000
    var usesDukpt = false
    var usesInternalDukpt = false

    var keepConnectionAlive = true
    var enableEchoTests = false 
    var enableLogOns = false
    var hostResponseHardcode = false
    var useSystemTraceNumberHardcode = true
    var responseCodeHardcode=""
    var errorMessageHardcode=""
    var hostIsActiveOnWindows = false
    var hostIsActiveOnLinux=false
    var enableTls = false 
    var hostCertPath = ""
    var certPassword = "123456" 
    var tlsProtocol = "TLSv1.2"
    var printDebugData=false

     var  telegramBotName: String="vierge_bot"
     var  telegramAdminChatId: Long=7777777
     
    var mustSendTxnToHost=false 
    var isDev=false 
    var simulateTimeout=false
    var syncReceiveHostMessage=false
    var syncSendHostMessage=false
    var commLogsPath = ""
    var hostLogsFilePath = ""
    private var hostLogsWithoutDateFilePath = ""
    var clientWaitAnswerTimeOut: Long = 60000
    var clientWaitDeltaSeconds: Long = 1000

    var isLinux=false
    var zoneTime = "America/Mexico_City"

    fun logHostIsActive(logLevel: LogLevel):Boolean{
        return logsHostLevel.contains(logLevel.code)
    }

    fun logsOperationIsActive(logLevel: LogLevel):Boolean{
        return logsOperationLevel.contains(logLevel.code)
    }
    fun getErrorCodeForTransaction(
        transactionType: TransactionType
    ): ErrorCode { 
        // Busca la configuración de error para el tipo de transacción dado
        val errorSetting = emulateErrors.find { it.transactionType == transactionType }
        // Si no se encuentra configuración o está inactiva, retorna Undefined
        if (errorSetting == null || !errorSetting.isActive) {
            return ErrorCode.Approved 
        }
          // Verifica si hay códigos de respuesta permitidos
            return if (errorSetting.responseCodes.isNotEmpty()) {
                // Escoge un código de respuesta aleatorio de la lista de códigos permitidos
                val randomResponseCode = errorSetting.responseCodes[Random.nextInt(errorSetting.responseCodes.size)]
                ErrorCode.fromResponseCode(randomResponseCode)
            } else {
                ErrorCode.UnHandledException
            }
    }

 
     fun getUpdatedFields(updated: AppHostSettingsData): String {

         val updatedFields = mutableListOf<String>()
        val properties = AppHostSettingsData::class.memberProperties
        val original = this
        for (property in properties) {
            val originalValue = (property as KProperty1<AppHostSettingsData, *>).get(original)
            val updatedValue = property.get(updated)
            if (originalValue != updatedValue) {
                updatedFields.add("${property.name}: $originalValue => $updatedValue")
            }
        }
        return updatedFields.joinToString(separator = "\n")
    }
    fun getCurrentDateTime(): String { 
        val  watcher=WatcherContext.getInstance()

        watcher.debug("getCurrentDateTime zoneTime $zoneTime")
        return try {
            val zoneId = ZoneId.of(zoneTime) 
            val zonedDateTime = ZonedDateTime.now(zoneId)
            watcher.debug(" getCurrentDateTime zonedDateTime $zonedDateTime")
   
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
           val date= zonedDateTime.format(formatter)
            watcher.debug(" getCurrentDateTime date $date")
             date
        } catch (ex: Exception) {
           println("Error with zone time: ${ex.message}, using default UTC zone.")
            val zoneId = ZoneId.of("UTC")
            val zonedDateTime = ZonedDateTime.now(zoneId)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            zonedDateTime.format(formatter)
        }
    }
    fun getCurrentDateTimeLogFile(): String {

        return try {
            val zoneId = ZoneId.of(zoneTime)
            val zonedDateTime = ZonedDateTime.now(zoneId)

            val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
            zonedDateTime.format(formatter)
        } catch (ex: Exception) {
            println("Error with zone time: ${ex.message}, using default UTC zone.")
            val zoneId = ZoneId.of("UTC")
            val zonedDateTime = ZonedDateTime.now(zoneId)
            val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
            zonedDateTime.format(formatter)
        }
    }
    fun getCurrentDateTime(pattern: String): String {
        val watcher= WatcherContext.getInstance()
        watcher.debug("getCurrentDateTime zoneTime $zoneTime")

        return try {
            val zoneId = ZoneId.of(zoneTime)
            val zonedDateTime = ZonedDateTime.now(zoneId)
            watcher.debug("getCurrentDateTime zonedDateTime $zonedDateTime")

            val formatter = DateTimeFormatter.ofPattern(pattern)
            val date= zonedDateTime.format(formatter)

            watcher.debug(" getCurrentDateTime date $date pattern $pattern")
            date

        } catch (ex: Exception) {
            println("Error with zone time: ${ex.message}, using default UTC zone.")
            val zoneId = ZoneId.of("UTC")
            val zonedDateTime = ZonedDateTime.now(zoneId)
            val formatter = DateTimeFormatter.ofPattern(pattern)
            zonedDateTime.format(formatter)
        }
    }

    fun getCurrentDateTime(date: Date, pattern: String): String {
        val watcher=WatcherContext.getInstance()

        watcher.debug("getCurrentDateTime zoneTime $zoneTime")

        return try {
            val zoneId = ZoneId.of(zoneTime)
            val instant = date.toInstant()

            val zonedDateTime = instant.atZone(zoneId)
            watcher.debug("getCurrentDateTime zonedDateTime $zonedDateTime")

            val formatter = DateTimeFormatter.ofPattern(pattern)
            zonedDateTime.format(formatter)
        } catch (ex: Exception) {
            println("Error with zone time: ${ex.message}, using default UTC zone.")
            val zoneId = ZoneId.of("UTC")
            val instant = date.toInstant()
            val zonedDateTime = instant.atZone(zoneId)
            val formatter = DateTimeFormatter.ofPattern(pattern)
            zonedDateTime.format(formatter)
        }
    }
    fun getCurrentDateTime(date: LocalDateTime, pattern: String): String {
       val watcher=WatcherContext.getInstance()

        watcher.debug("getCurrentDateTime zoneTime $zoneTime")

        return try {
            val zoneId = ZoneId.of(zoneTime)
            val zonedDateTime = date.atZone(zoneId)
            watcher.debug("getCurrentDateTime zonedDateTime $zonedDateTime")

            val formatter = DateTimeFormatter.ofPattern(pattern)

            zonedDateTime.format(formatter)

        } catch (ex: Exception) {
            println("Error with zone time: ${ex.message}, using default UTC zone.")
            val zoneId = ZoneId.of("UTC")
            val zonedDateTime = date.atZone(zoneId)
            val formatter = DateTimeFormatter.ofPattern(pattern)
            zonedDateTime.format(formatter)
        }
    }

    fun getRequestStamp(): String {

        val zoneId = ZoneId.of(zoneTime)
        val zonedDateTime = ZonedDateTime.now(zoneId)
        val formatter = DateTimeFormatter.ofPattern("HHmmyyMMdd")
        return zonedDateTime.format(formatter)
    }

    fun getCurrentDate(): String {

        val zoneId = ZoneId.of(zoneTime)
        val zonedDateTime = ZonedDateTime.now(zoneId)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return zonedDateTime.format(formatter)
    }
    fun hostIsActive():Boolean
    {
       // Watcher.console.debug(HostType.fromName(hostName), " hostIsActiveOnWindows [${hostIsActiveOnWindows}] isLinux [$isLinux]")
        return (hostIsActiveOnWindows&&!isLinux || hostIsActiveOnLinux&&isLinux)
    }
    fun convertHostFilePath(date: String): String {
        return hostLogsWithoutDateFilePath.replace("@date",date.replace("-","_"))
    }
    fun convertHostFilePath(date: LocalDate): String {
        val dateStr: String = date.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
        return hostLogsWithoutDateFilePath.replace("@date",dateStr)
    }


    fun injectDataPath(dataPath:String,logsPath:String,authorizerIsEnabled:Boolean,isLinux:Boolean){
        val dateStr: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
        if(!authorizerIsEnabled){
            hostIsActiveOnWindows=false
            hostIsActiveOnLinux=false

        }
        commLogsPath= "$logsPath/HOST_$hostName/$dateStr/comm_logs_all.txt"
        hostLogsFilePath= "$logsPath/HOST_$hostName/"
        try {
            if (!isLinux) {
                hostLogsFilePath = hostLogsFilePath.replace("/", "\\")
            }
            var newFolder: Path =
                Paths.get(hostLogsFilePath)
            if (!Files.exists(newFolder)) {
                Files.createDirectories(newFolder)
            }
            hostLogsFilePath = "$logsPath/HOST_$hostName/$dateStr/"
            if (!isLinux) {
                hostLogsFilePath = hostLogsFilePath.replace("/", "\\")
            }

            newFolder = Paths.get(hostLogsFilePath)
            if (!Files.exists(newFolder)) {
                Files.createDirectories(newFolder)
            }
        }catch (e:Exception){
            println("ERROR CREATING directory: $hostLogsFilePath ")
        }

        hostLogsFilePath= "$logsPath/HOST_$hostName/$dateStr/log"
        hostLogsWithoutDateFilePath="$logsPath/HOST_$hostName/@date/log"

        hostCertPath=dataPath+hostCertPath
        this.isLinux=isLinux


        if (!isLinux) {
            hostCertPath= hostCertPath.replace("/", "\\")
            commLogsPath = commLogsPath.replace("/", "\\")
            hostLogsFilePath = hostLogsFilePath.replace("/", "\\")
            hostLogsWithoutDateFilePath = hostLogsWithoutDateFilePath.replace("/", "\\")

        }



    }




}