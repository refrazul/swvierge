package com.viergegroup.swmain.webwatcher

import com.shared.commons.AppSettings
import com.shared.commons.GsonConfig

import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.LogLevel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause

internal object WatcherGlobalLog {
    private var logFile: File? = null
    private var logWriter: BufferedWriter? = null
    private var currentDate = ""
 
    private var logFileCommError: File? = null
    private var logWriterCommError: BufferedWriter? = null

    private var logFileReboot: File? = null
    private var logWriterReboot: BufferedWriter? = null

    private var logFileReverses: File? = null
    private var logWriterReverses: BufferedWriter? = null


    private var logFileCommDebug: File? = null
    private var logWriterCommDebug: BufferedWriter? = null





    private var logFileCommRaw: File? = null
    private var logWriterCommRaw: BufferedWriter? = null

     private var logFileDBError: File? = null
    private var logWriterDBError: BufferedWriter? = null


     private var logFileISOError: File? = null
    private var logWriterISOError: BufferedWriter? = null



    private var logWriterDiskWatcher: BufferedWriter? = null 
    private var logFileEncryptorUUID: File? = null

    private var logFileEncryptor: File? = null
    private var logWriterEncryptor :BufferedWriter? = null

    private var logFileDiskWatcher :File? = null
    private var logFileAlarms :File? = null

    private var logFileMetricsWatcher :File? = null
    private var logFileTxnsWatcher :File? = null
    private var logFilePath:String=""

    const val WHITE: String = "\u001B[0m" // Dark red

    const val DARK_RED: String = "\u001B[38;2;128;0;0m" // Dark red
    const val RED: String = "\u001B[38;2;255;0;0m" // Red
    const val LIGHT_RED: String = "\u001B[38;2;255;102;102m" // Light red
    const val PINK: String = "\u001B[38;2;255;192;203m" // Pink
    const val LIGHT_PINK: String = "\u001B[38;2;255;230;230m" // Light pink
    const val ROSE: String = "\u001B[38;2;255;228;181m" // Rose
    const val OFF_WHITE: String = "\u001B[38;2;255;255;240m" // Off-white
    const val DARK_BLUE: String = "\u001B[38;2;0;0;139m"
    const val LIGHT_BLUE_1: String = "\u001B[38;2;0;191;255m"
    const val LIGHT_BLUE_2: String = "\u001B[38;2;135;206;250m"
    const val BLUE: String = "\u001B[34m" // Standard Blue
    const val CYAN: String = "\u001B[36m" // Standard Cyan
    const val AQUA_BLUE: String = "\u001B[38;2;0;255;255m"
    const val DARK_CYAN: String = "\u001B[38;2;0;128;128m"
    const val LIGHT_CYAN: String = "\u001B[38;2;176;224;230m"
    const val YELLOW: String = "\u001B[33m" // Standard Yellow
    const val MUSTARD_YELLOW: String = "\u001B[38;2;139;69;19m"
    const val LIGHT_GOLDEN_YELLOW: String = "\u001B[38;2;255;255;153m"

    private val logChannelHst = Channel<Triple<HostType, String, String>>(capacity = Channel.UNLIMITED)
    private val logChannelOne= Channel<String>(capacity = Channel.UNLIMITED)

    private val logChannel = Channel<Pair<String, String>>(capacity = Channel.UNLIMITED)
    private val scopeDebug = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scopeInfo= CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scopeError= CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logLock = java.util.concurrent.locks.ReentrantLock()

    init {
        scopeDebug.launch {
            for ((header, msg) in logChannel) {
                dispatchLogDebug(header, msg)
            }
        }
      
        scopeError.launch {
            for ((header, msg) in logChannel) {
                dispatchLogError(header, msg)
            }
        }
        scopeInfo.launch {
            for ((header, msg) in logChannel) {
                dispatchLogInfo(header, msg)
            }
        }
        updateLogFile()

    }
     fun writeLogSafely(level: LogLevel,levelConsole:LogLevel, color: String, logMessage: String) {
        if (AppSettings.getInstance().logGlobalIsActive(levelConsole)) {
            println("$color$logMessage$WHITE")
        }
    
        if (!AppSettings.getInstance().logGlobalIsActive(level)) return
    
        try {
            logLock.lock() //  Bloqueo entre hilos

            FileWriter(logFile, true).use { writer ->
                writer.write(logMessage)
                writer.flush()
            }
        } catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.writeLogSafely(level: LogLevel, color: String, logMessage: String) ERROR => ${e.message}")

            println("${DARK_RED}Error writing log file: ${e.message}$WHITE")
        } finally {
            logLock.unlock()
        }
    }
    
  

     fun dispatchLogError(header: String, message: String) {
        updateLogFile()
        val logMessage = "$header\t$message\n"
        writeLogSafely(LogLevel.FileError,LogLevel.ConsoleError, DARK_RED, logMessage)
    }
    
     fun dispatchLogInfo(header: String, message: String) {
        updateLogFile()
        val logMessage = "$header\t$message\n"
        writeLogSafely(LogLevel.FileInfo,LogLevel.ConsoleInfo, LIGHT_GOLDEN_YELLOW, logMessage)
    }
    
     fun dispatchLogDebug(header: String, message: String) {
        updateLogFile()
        val logMessage = "$header\t$message\n"
        writeLogSafely(LogLevel.FileDebug,LogLevel.ConsoleDebug, DARK_CYAN, logMessage)
    }
    

    
    fun debug(message: String) {
       var header=getHeader("GLOBAL","DEBUG")

        scopeDebug.launch {
            logChannel.send(Pair(header, message))
        }
    }

    fun debug(hostType: HostType,header:String,message: String) {
        scopeDebug.launch {
            logChannel.send(Pair(header+" ${hostType}", message))
        }
    }

    fun debug(header:String,message: String) {
        scopeDebug.launch {
            logChannel.send(Pair(header, message))
        }
    }




    fun info(message: String) {
          var header=getHeader("GLOBAL","INFO")

        scopeInfo.launch {
            logChannel.send(Pair(header, message))
        }
    }




    fun info(header:String,message: String) {
        scopeInfo.launch {
            logChannel.send(Pair(header, message))
        }
    }



    fun error(message: String) {
      var header=getHeader("GLOBAL","ERROR")

        scopeError.launch {
            logChannel.send(Pair(header, message))
        }
    }

    fun error(header:String,message: String) {
        scopeError.launch {
            logChannel.send(Pair(header, message))
        }
    }

  

    private fun closePreviousWriters() {
        try {
            logWriter?.close()
            logWriter = null
            logWriterCommError?.close()
            logWriterCommError = null
            logWriterReboot?.close()
            logWriterReboot = null
            logWriterEncryptor?.close()
            logWriterEncryptor = null
            logWriterReverses?.close()
            logWriterReverses = null

             logWriterCommDebug?.close()
            logWriterCommDebug = null


            logWriterDiskWatcher?.close()
            logWriterDiskWatcher = null

                
            logWriterCommRaw?.close()
            logWriterCommRaw = null


        } catch (e: IOException) {
            println("${DARK_RED}Error closing log writers: ${e.message}${WHITE}")
        }
    }

    private fun updateLogFile() {
        val dateStr = getCurrentDateStr()
        // Cerrar+reabrir los writers SOLO cuando cambia el dia (o la 1a vez).
        // Antes closePreviousWriters() corria SIEMPRE y ponia logWriter=null, dejando la
        // guarda siempre-verdadera -> se reabrian ~10 archivos en CADA linea de log.
        if (currentDate != dateStr || logWriter == null) {
            closePreviousWriters()
            currentDate = dateStr
            val path = getLogsPath() + File.separator
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
 
            logFileCommRaw = File(dir, "comm_raw_${dateStr}.txt")
                        logFileDBError = File(dir, "db_error_${getCurrentDateOnlyStr()}.txt")

             logFileISOError = File(dir, "iso_error_${getCurrentDateOnlyStr()}.txt")


              logFilePath=path+ "global_log_$dateStr.txt"

            logFileCommError = File(dir, "comm_error_$dateStr.txt")
            logFileReverses = File(dir, "reverses_${getCurrentDateOnlyStr()}.txt")

            logFileCommDebug = File(dir, "comm_debug_${getCurrentDateOnlyStr()}.txt")


             logFileDiskWatcher = File(dir, "disk_watcher_$dateStr.txt")
            logFileEncryptor = File(dir, "encryptor_$dateStr.txt")
            logFileReboot = File(dir, "reboot_$dateStr.txt")
            logFileAlarms = File(dir, "alarms_$dateStr.txt")
            logFileMetricsWatcher = File(dir, "os_metrics_$dateStr.txt")
            logFileTxnsWatcher = File(dir, "txns_metrics_$dateStr.txt")
   
            logFile = File(dir, "global_log_$dateStr.txt")
            logWriter?.close()
            logWriter = BufferedWriter(FileWriter(logFile, true))
            logWriterCommError?.close()

            logWriterCommError = BufferedWriter(FileWriter(logFileCommError, true))
            logWriterReboot?.close()

            logWriterReboot = BufferedWriter(FileWriter(logFileReboot, true))
            logWriterCommRaw?.close()

            logWriterCommRaw= BufferedWriter(FileWriter(logFileCommRaw, true))

            logWriterDBError?.close()

            logWriterDBError= BufferedWriter(FileWriter(logFileDBError, true))
            logWriterISOError?.close()

            logWriterISOError= BufferedWriter(FileWriter(logFileISOError, true))

            logFileEncryptor = File(dir, "encryptor_$dateStr.txt")
            logWriterEncryptor?.close()

            logWriterEncryptor = BufferedWriter(FileWriter(logFileEncryptor, true))
            logWriterReverses?.close()

            logWriterReverses = BufferedWriter(FileWriter(logFileReverses, true))
            


                logWriterCommDebug = BufferedWriter(FileWriter(logFileCommDebug, true))
            


            logWriterDiskWatcher = BufferedWriter(FileWriter(logFileDiskWatcher, true))

        }
    } 

    private fun getLogsPath(): String {
        return AppSettings.getInstance().getBasePath() + File.separator + "logs"
    }
  
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        sdf.timeZone = TimeZone.getTimeZone("America/Mexico_City")
        return sdf.format(Date())
    }
    private fun getCurrentDateOnlyStr(): String {
        val sdf = SimpleDateFormat("yyyy_MM_dd")
        sdf.timeZone = TimeZone.getTimeZone("America/Mexico_City")
        return sdf.format(Date()) 
    }

    private fun getCurrentDateStr(): String {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH")
        sdf.timeZone = TimeZone.getTimeZone("America/Mexico_City")
        return sdf.format(Date()) 
    }

    fun close(){

        if (logWriter!=null) {
            try{
                logWriter!!.close()
                logWriter=null
            }catch (ex:Exception){

            }
        }
    }

 

    private fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            if (element.className != this::class.java.name && !element.className.startsWith("java.lang.Thread")) {
                return "${element.fileName}:${element.lineNumber} (${element.methodName})"
            }
        }
        return "Unknown"
    }

    fun getHeader(logType:String,logLevel:String):String{
        return "\t${getCurrentTimestamp()}\t|\t${logType}\t|\t${logLevel}\t|\t${getCallerInfo()}|"
    }






    fun obj(header:String,data: Any) {

        CoroutineScope(Dispatchers.IO).async {
           
            try{

                val content: String= GsonConfig.gsonLogs.toJson(data)
                val logMessage = "$header\t$content\n"
                if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug))
                  println("${DARK_CYAN}$logMessage${WHITE}" )
        
                if(AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) {
    
                    logWriter?.let {
                        try {
                            it.write(logMessage)
                            it.flush()
                            //  it.close()
                        } catch (e: Exception) {
                            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. obj(header:String,data: Any) ERROR => ${e.message}")
    
                            print("${DARK_RED}$logMessage${WHITE}")
                            println("${DARK_RED}Error writing log files: ${e.message} ${WHITE}")
                        }
                    }
                }

            } catch (e: Exception) {
                            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. obj(header:String,data: Any) ERROR => ${e.message}")
                            println("${DARK_RED}Error writing log files: ${e.message} ${WHITE}")
               }
           
    
       
       }
       
    }

    fun obj(data: Any) {

        CoroutineScope(Dispatchers.IO).async {
           
            try{

        val content: String=GsonConfig.gsonLogs.toJson(data)
        val logMessage = "${getHeader("GLOBAL","DEBUG-OBJ")}\n$content\n"
        if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug))
            println("${DARK_CYAN}$logMessage${WHITE}")

        if(AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)){
            logWriter?.let {
                try {
                    it.write(logMessage)
                    it.flush()
                     // it.close()
                } catch (e: Exception) {
                    println("${DARK_RED} MSG=> [$logMessage] ${WHITE}")
                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
            }
        }
        }
        catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. obj(header:String,data: Any) ERROR => ${e.message}")

            println("${DARK_RED}Error writing log files: ${e.message} ${WHITE}")
    }
        
     }

            

    }
    fun encryptor(message: String) {

        CoroutineScope(Dispatchers.IO).async {
          
            try{
        updateLogFile()
        val logMessage = "${getHeader("GLOBAL","ENCRYPTOR")}\n$message\n"
        if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug))
            println("${DARK_CYAN}$logMessage${WHITE}")

       // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
       logWriterEncryptor?.let {
            try {
                it.write(logMessage)
                it.flush()
                //  it.close()
            } catch (e: IOException) {
                println("${DARK_RED}$logMessage${WHITE}")
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
        }
       
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.encryptor(message: String)  ERROR => ${e.message}")
                println("${DARK_RED}Error writing log files: ${e.message} ${WHITE}")
        }




            }

            
    }
    
    fun encryptor(uuid: String, message: String) {
        CoroutineScope(Dispatchers.IO).async {
          
            try {

        updateLogFile()
        if(uuid==""){
            encryptor(message)
            
        }else{
            val dateStr = getCurrentDateStr()
            val logMessage = "${getHeader("GLOBAL", "COMM_ERROR")}\n$message\n"
        
            // Mostrar en consola si está habilitado
            if (AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug)) {
                println("${DARK_CYAN}$logMessage${WHITE}")
            }
        
            // Ruta al archivo de log
            val logDir = File(getLogsPath(), "encryptor/$dateStr")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
        
            val logFile = File(logDir, "$uuid.txt")
            try {
                BufferedWriter(FileWriter(logFile, true)).use { writer ->
                    writer.write(logMessage)
                    writer.close()
                }
            } catch (e: IOException) {
                println("${DARK_RED}$logMessage${WHITE}")
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
        }
       

        }
        catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.encryptor(uuid: String, message: String)  ERROR => ${e.message}")

            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        }

       
            }

            
    }
    

    fun commError(message: String) {

        CoroutineScope(Dispatchers.IO).async {
            try {
            updateLogFile()
            val logMessage = "${getHeader("GLOBAL","COMM_ERROR")}\n$message\n"
            if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleCommError))
                println("${DARK_CYAN}$logMessage${WHITE}")
           // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
            logWriterCommError?.let {
                try {
                    it.write(logMessage)
                    it.flush()
                   // it.close()
                } catch (e: IOException) {
                    println("${DARK_RED}$logMessage${WHITE}")
                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
            }
            }
            catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.commError(message: String)  ERROR => ${e.message}")

                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
         }
  
    }   
    fun reverseDebugRrn(rrn: String, message: String) { 
        val header = getHeader("GLOBAL", "REVERSES")
        val logMessage = "$header\n$message\n"
        val safeRrn = rrn.replace(Regex("[^a-zA-Z0-9._-]"), "_") // evita problemas en nombre de archivo

        // Consola (como antes)
        println("${DARK_CYAN} REVERSE_RRN: [${rrn}] REVERSE_DEBUG $logMessage${WHITE}")

        CoroutineScope(Dispatchers.IO).async {
            try {
                updateLogFile()

                // 1) Archivo general de reversos (se mantiene, pero SIN cerrar el writer global)
                logWriterReverses?.let {
                    try {
                        it.write(logMessage)
                        it.flush()
                    } catch (e: IOException) {
                        println("${DARK_RED}$logMessage${WHITE}")
                        println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                    }
                }

                // 2) Archivo por RRN: logs/reverses/YYYY_MM_DD/reverse_<RRN>.txt
                val dateFolder = getCurrentDateOnlyStr() // "yyyy_MM_dd"
                val logDir = File(getLogsPath(), "reverses/$dateFolder")
                if (!logDir.exists()) {
                    logDir.mkdirs()
                }

                val rrnFile = File(logDir, "reverse_${safeRrn}.txt")
                BufferedWriter(FileWriter(rrnFile, true)).use { writer ->
                    writer.write(logMessage)
                    writer.flush()
                }
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(
                    AlarmCause.LOG_ERROR,
                    "WatcherGlobalLog.reverseDebugRrn(rrn, message) ERROR => ${e.message}"
                )
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
        }
    }



      fun commDebug(rrn: String, message: String) { 
        val header = getHeader("GLOBAL", "COMM_DEBUG")
        val logMessage = "$header\n$message\n"
        val safeRrn = rrn.replace(Regex("[^a-zA-Z0-9._-]"), "_") // evita problemas en nombre de archivo

        // Consola (como antes)
        println("${DARK_CYAN} COMM_DEBUG_RRN: [${rrn}] COMM_DEBUG $logMessage${WHITE}")

        CoroutineScope(Dispatchers.IO).async {
            try {
                updateLogFile()

                // 1) Archivo general de reversos (se mantiene, pero SIN cerrar el writer global)
                logWriterCommDebug?.let {
                    try {
                        it.write(logMessage)
                        it.flush()
                    } catch (e: IOException) {
                        println("${DARK_RED}$logMessage${WHITE}")
                        println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                    }
                }

                // 2) Archivo por RRN: logs/reverses/YYYY_MM_DD/reverse_<RRN>.txt
                val dateFolder = getCurrentDateOnlyStr() // "yyyy_MM_dd"
                val logDir = File(getLogsPath(), "commdebug/$dateFolder")
                if (!logDir.exists()) {
                    logDir.mkdirs()
                } 

                val rrnFile = File(logDir, "comm_debug_${safeRrn}.txt")
                BufferedWriter(FileWriter(rrnFile, true)).use { writer ->
                    writer.write(logMessage)
                    writer.flush()
                }
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(
                    AlarmCause.LOG_ERROR,
                    "WatcherGlobalLog.commDebug(rrn, message) ERROR => ${e.message}"
                )
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
        }
    }





    fun reverseDebug(message: String) {

        CoroutineScope(Dispatchers.IO).async {
           
                try {
                    updateLogFile()
                    val logMessage = "${getHeader("GLOBAL","REVERSES")}\n$message\n"
                   // if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleInfo))
                        println("${DARK_CYAN} REVERSE_DEBUG $logMessage${WHITE}")  
                
                // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
                    logWriterReverses?.let {
                        try {
                            it.write(logMessage)
                            it.flush()
                            it.close()
                        } catch (e: IOException) {
                            println("${DARK_RED}$logMessage${WHITE}")
                            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                        }
                    }
                }
                catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.n reverseDebug(message: String) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
    
            }

            
    } 

    

    fun dbError(message: String) {
        if(message.trim()=="") return 
           
         
        CoroutineScope(Dispatchers.IO).async {
                try {
                    updateLogFile()
                    val logMessage = "${getHeader("COMM","RAW")}\n$message\n"
                    if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleCommRaw))
                        println("${DARK_CYAN}$logMessage${WHITE}")  
                
                // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
                    logWriterDBError?.let { 
                        try {
                            it.write(logMessage)
                            it.flush()
                        } catch (e: IOException) {
                            println("${DARK_RED}$logMessage${WHITE}")
                            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                        }
                    }
                    

                }
                catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.commRaw(message: String) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }

         }

            
    } 



    fun isoError(message: String) {
        if(message.trim()=="") return 
           

        CoroutineScope(Dispatchers.IO).async {
                try {
                    updateLogFile()
                    val logMessage = "${getHeader("COMM","RAW")}\n$message\n"
                    if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleCommRaw))
                        println("${DARK_CYAN}$logMessage${WHITE}")  
                
                // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
                    logWriterISOError?.let { 
                        try {
                            it.write(logMessage)
                            it.flush()
                        } catch (e: IOException) {
                            println("${DARK_RED}$logMessage${WHITE}")
                            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                        }
                    }
                    

                }
                catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.commRaw(message: String) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }

         }

            
    } 
   
   
    fun commRaw(message: String) {
        if(message.trim()=="") return 
          
           
        CoroutineScope(Dispatchers.IO).async {
                try {
                    updateLogFile()
                    val logMessage = "${getHeader("COMM","RAW")}\n$message\n"
                    if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleCommRaw))
                        println("${DARK_CYAN}$logMessage${WHITE}")  
                
                // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
                    logWriterCommRaw?.let { 
                        try {
                            it.write(logMessage)   
                            it.flush()
                        } catch (e: IOException) {
                            println("${DARK_RED}$logMessage${WHITE}")
                            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                        }
                    }
                    

                }
                catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.commRaw(message: String) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }

         }

            
    }  
 

    fun reverseDebug(header:String,message: String) {

        CoroutineScope(Dispatchers.IO).async {
            try { 
                updateLogFile()
                val logMessage = "${header}\n$message\n"
                if(AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug))
                    println("${DARK_CYAN}$logMessage${WHITE}")
        
            // if(!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
                logWriterReverses?.let {
                    try {
                        it.write(logMessage)
                        it.flush()
                    } catch (e: IOException) {
                        println("${DARK_RED}$logMessage${WHITE}")
                        println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                    }
                }
        
                
            }
            catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.everseDebug(header:String,message: String)  ERROR => ${e.message}")

                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }


            }

            
    } 


    fun alarmsDebug(message: String) {

        CoroutineScope(Dispatchers.IO).async {
            
            updateLogFile()
            val header = "ALARMS_DEBUG"
            val logMessage= "${getHeader("GLOBAL","ALARMS_DEBUG")}\n$message\n"
        // if (AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug)) {
            //   println("${DARK_CYAN}$logMessage${WHITE}")
            //} 
        
            // Solo abrir el archivo si se debe escribir en el log
        // if (!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
        
            try {
                // Abrir y escribir en el archivo usando BufferedWriter con 'use'
                BufferedWriter(FileWriter(logFileAlarms, true)).use { writer ->
                    writer.write(logMessage) 
                    writer.flush() // Aunque no es estrictamente necesario, asegura que los datos se escriban.
                }
            } catch (e: IOException) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. alarmsDebug(message: String)  ERROR => ${e.message}")

                println("${DARK_RED}$logMessage${WHITE}")
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            } 
      
    
     }

            
    }



    fun diskWatcher(message: String) {
        updateLogFile()
        val header = "DISK_WATCHER"
        val logMessage= "${getHeader("GLOBAL","DISK_WATCHER")}\n$message\n"
     
       // if (AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug)) {
         //   println("${DARK_CYAN}$logMessage${WHITE}")
        //} 
      
        // Solo abrir el archivo si se debe escribir en el log
       // if (!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
    
        try {
            // Abrir y escribir en el archivo usando BufferedWriter con 'use'
            BufferedWriter(FileWriter(logFileDiskWatcher, true)).use { writer ->
                writer.write(logMessage) 
                writer.flush() // Aunque no es estrictamente necesario, asegura que los datos se escriban.
            }
        } catch (e: IOException) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. diskWatcher(message: String)   ERROR => ${e.message}")

            println("${DARK_RED}$logMessage${WHITE}")
            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        } 
    }
     

    fun metricsWatcher(message: String) {
        updateLogFile()
        val header = "METRICS_WATCHER"
        val logMessage= "${getHeader("GLOBAL","METRICS_WATCHER")}\n$message\n"
    
        try { 
            // Abrir y escribir en el archivo usando BufferedWriter con 'use'
            BufferedWriter(FileWriter(logFileMetricsWatcher, true)).use { writer ->
                writer.write(logMessage)  
                writer.flush() // Aunque no es estrictamente necesario, asegura que los datos se escriban.
            }
        } catch (e: IOException) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog. metricsWatcher(message: String)   ERROR => ${e.message}")

            println("${DARK_RED}$logMessage${WHITE}")
            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        } 
    }
    fun txnsWatcher(message: String) {
        updateLogFile()
        val logMessage= "${getHeader("GLOBAL","TXNS_WATCHER")}\n$message\n"
      
        try {  
            // Abrir y escribir en el archivo usando BufferedWriter con 'use'
            BufferedWriter(FileWriter(logFileTxnsWatcher, true)).use { writer ->
                writer.write(logMessage)  
                writer.flush() // Aunque no es estrictamente necesario, asegura que los datos se escriban.
            }
        } catch (e: IOException) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.txnsWatcher(message: String)  ERROR => ${e.message}")

            println("${DARK_RED}$logMessage${WHITE}")
            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        } 
    }



     
    fun rebootDebug(message: String) {
        updateLogFile()
        val header = "DISK_WATCHER" 
        val logMessage= "${getHeader("GLOBAL","REBOOT_DEBUG")}\n$message\n"
     
        if (AppSettings.getInstance().logGlobalIsActive(LogLevel.ConsoleDebug)) {
            println("${DARK_CYAN}$logMessage${WHITE}")
        } 
     
        // Solo abrir el archivo si se debe escribir en el log
       // if (!AppSettings.getInstance().logGlobalIsActive(LogLevel.FileDebug)) return
     
        try {
            // Abrir y escribir en el archivo usando BufferedWriter con 'use'
            BufferedWriter(FileWriter(logFileReboot, true)).use { writer ->
                writer.write(logMessage) 
                writer.flush() // Aunque no es estrictamente necesario, asegura que los datos se escriban.
            }
        } catch (e: IOException) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WatcherGlobalLog.rebootDebug(message: String)  ERROR => ${e.message}")

            println("${DARK_RED}$logMessage${WHITE}")
            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        } 
    }
    


}
