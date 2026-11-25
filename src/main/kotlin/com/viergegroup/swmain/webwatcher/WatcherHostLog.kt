package com.viergegroup.swmain.webwatcher

import com.shared.commons.AppSettings
import com.shared.commons.GsonConfig
import com.shared.commons.StringUtils
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.LogLevel

import java.io.*

import java.text.SimpleDateFormat
import java.util.*

internal class WatcherHostLog(val hostType: HostType) {
    private var logFile: File? = null 
    private var logWriter: BufferedWriter? = null
    private var currentDate = ""
    private var lineCounter=0
    var commLogIsBusy=false
    // Map to store colors for each system trace number
    private val systemTraceColorMap = mutableMapOf<String, String>()
    private  lateinit var  output: Writer

    private   var  path: String=""
    private  var dir: File?=null

    init {
        updateLogFile()
    }
    private fun updateLogFile() {
        val dateStr = getCurrentDateStr()

        if (currentDate != dateStr|| logWriter ==null|| (dir!=null&&!dir!!.exists()) ||logFile!=null&&!logFile!!.exists()) {
            currentDate = dateStr
             path = getLogsPath() + File.separator+hostType.vaultName+File.separator+"consolelogs"
             dir = File(path)
            if(logFile!=null){
                logWriter?.close()
            }
            if (dir!=null&&!dir!!.exists()) {
                println("${WatcherGlobalLog.LIGHT_CYAN}CREATING CONSOLE LOGS=> [${path}]${WatcherGlobalLog.WHITE}" );
                dir!!.mkdirs()
            }
             logFile = File(dir, "$dateStr.txt")
            logWriter?.close()
           logWriter = BufferedWriter(FileWriter(logFile, true))
        }
    }

    private fun getLogsPath(): String {
        return AppSettings.getInstance().getBasePath() + File.separator
    }


    // List of available colors
    private val colors = listOf(
        "\u001B[92m",  // Green bright
        "\u001B[93m",  // Yellow bright
        "\u001B[94m",  // Blue bright
        "\u001B[95m",  // Magenta bright
        "\u001B[96m",  // Cyan bright
        "\u001B[97m"   // White bright
    )


    private fun getCurrentDateStr(): String {
        return SimpleDateFormat("yyyy_MM_dd").format(Date())
    }
    private fun getNetworkPath(): String {
        return AppSettings.getInstance().getBasePath() + File.separator +hostType.vaultName
    }


    fun getColorForSystemTrace(systemTraceNumber: String): String {
        return systemTraceColorMap.getOrPut(systemTraceNumber) {
            colors.random()
        }
    }



 



    fun debug( debugHeader:String,isoRaw: String, isoMetadata: Iso8583MetaData?, isRequest: Boolean) {
        updateLogFile()
        val ANSI_RESET = "\u001B[0m"
        val ANSI_HEADER = hostType.color
        val header: String
        var messageTypeStr = ""
        var systemTraceNumberStr = ""
        var rrnStr = ""
        var errorMessage = "OK"
        var errorCode = "00"
        var requestStr: String
        var addInfo = "NA"
        requestStr = "Sent/App/"
        if (!isRequest) requestStr = "Received/Host/"
        val dateStr: String =  AppSettings.getHostConfig(hostType).getCurrentDateTime()
        if (isoMetadata != null) {
            if (isoMetadata.transactionData != null) {
                messageTypeStr = isoMetadata.transactionData!!.getMessageTypeString()
                systemTraceNumberStr = isoMetadata.transactionData!!.systemTraceAuditNumber
                rrnStr = isoMetadata.transactionData!!.rrn
                if (isoMetadata.transactionData!!.getIsoResponseCode() != "00") {
                    errorMessage = isoMetadata.transactionData!!.getIsoErrorDescriptionResponseCode()
                    errorCode = isoMetadata.transactionData!!.getIsoResponseCode()
                }
            } else {
                errorMessage = isoMetadata.errorMessage
                errorCode = isoMetadata.errorCode.toString()
            }
        } else {
            errorMessage = "METADATA IS NULL"
            errorCode = "-1"
        }

        val separator = " | "
        var tlsVersion = "TCP/IP"
        if (isoMetadata?.transactionData != null) {
            addInfo = requestStr + isoMetadata.transactionData!!.merchantType.toString() + "/" +
                    isoMetadata.transactionData!!.transactionType.toString() + "/" +
                    isoMetadata.transactionData!!.pointOfServiceEntryMode.toString() + "/" +
                    isoMetadata.transactionData!!.transactionDirection
            addInfo = addInfo.replace("Undefined", "-")
        }
        val settings = AppSettings.getHostConfig(hostType)
        if (settings.enableTls) {
            tlsVersion = settings.tlsProtocol
        }

        header = dateStr + separator + hostType + separator + addInfo.padEnd(40, ' ') + separator +
                messageTypeStr.padStart(4, '0') + separator + systemTraceNumberStr.padStart(6, '0') + separator +
                rrnStr.padStart(12, '0') + separator + tlsVersion + separator + errorCode + separator +
                errorMessage.replace("\n", " ").replace("\r", "").replace("|", "")

        val splited = isoRaw.split("ISO")
        var isoRawS = isoRaw
        if (splited.size > 1) {
            isoRawS = "ISO" + splited[1]
        }

        // Assign or retrieve color for the message based on system trace number
        val messageColor = getColorForSystemTrace(systemTraceNumberStr)

        val msg = """
            ====================       ISO MESSAGE INI            ========================
            
            $debugHeader
            $ANSI_HEADER$header$ANSI_RESET
            $messageColor${StringUtils.cleanTextContent(isoRawS)}$ANSI_RESET
            
            ====================       ISO MESSAGE END            ========================
            
        """.trimIndent()

        if(AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.ConsoleInfo))
            println("\n" + msg + "\n")


        if(!AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.FileDebug)) return
        logWriter?.let {
            try {
                it.write(msg)
                it.flush()
            } catch (e: IOException) {
                print("${WatcherGlobalLog.LIGHT_RED} MSG=> [$msg]${WatcherGlobalLog.WHITE}")
                println("${WatcherGlobalLog.LIGHT_RED}Error writing log files: ${e.message} ${WatcherGlobalLog.WHITE}")
            }
        }
    }

    fun obj(header:String,data: Any) {
        updateLogFile()
        val content: String=GsonConfig.gsonLogs.toJson(data)
        val logMessage = "$header\t$content\n"
        if(AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.ConsoleDebug))
              println("${WatcherGlobalLog.LIGHT_CYAN}$logMessage${WatcherGlobalLog.WHITE}" );


        if(!AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.FileDebug)) return
        logWriter?.let {
            try {
                it.write(logMessage)
                it.flush()
            } catch (e: IOException) {
                print("${WatcherGlobalLog.LIGHT_RED}[35m MSG=> [$logMessage]${WatcherGlobalLog.WHITE}")
                println("${WatcherGlobalLog.LIGHT_RED} Error writing log files: ${e.message}${WatcherGlobalLog.WHITE}")
            }
        }

    }


    fun debug(header:String,message: String) {
        updateLogFile()
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.ConsoleDebug))
             println("${WatcherGlobalLog.LIGHT_CYAN}$logMessage${WatcherGlobalLog.WHITE}")

        if(!AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.FileDebug)) return
        logWriter?.let {
            try {
                it.write(logMessage)
                it.flush()
            } catch (e: IOException) {
                print("${WatcherGlobalLog.LIGHT_RED} MSG=> [$logMessage] ${WatcherGlobalLog.WHITE}")
                println("${WatcherGlobalLog.LIGHT_RED}Error writing log files: ${e.message} ${WatcherGlobalLog.WHITE}")
            }
        }


    }

    fun info(header:String,message: String) {
        updateLogFile()
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.ConsoleInfo))
          println("${WatcherGlobalLog.LIGHT_GOLDEN_YELLOW}${logMessage}${WatcherGlobalLog.WHITE}")

        if(!AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.FileInfo)) return
        logWriter?.let {
            try {
                it.write(logMessage)
                it.flush()
            } catch (e: IOException) {
                print("${WatcherGlobalLog.LIGHT_RED} MSG=> [$logMessage] ${WatcherGlobalLog.WHITE}")
                println("${WatcherGlobalLog.LIGHT_RED}Error writing log files: ${e.message}${WatcherGlobalLog.WHITE}")
            }
        }

    }


    fun error(header:String,message: String){

        updateLogFile()
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.ConsoleError))
             println("${WatcherGlobalLog.LIGHT_RED}$logMessage${WatcherGlobalLog.WHITE}" )


        if(!AppSettings.getHostConfig(hostType).logHostIsActive(LogLevel.FileError)) return
        logWriter?.let {
            try {
                it.write(logMessage)
                it.flush()
            } catch (e: IOException) {
                print("${WatcherGlobalLog.LIGHT_RED} MSG=> [$logMessage] ${WatcherGlobalLog.WHITE}")
                println("${WatcherGlobalLog.LIGHT_RED}Error writing log files: ${e.message}${WatcherGlobalLog.WHITE}")
            }
        }
    }


    fun getHeader(hostType: HostType):String{
        var dateStr: String = AppSettings.getHostConfig(hostType).getCurrentDateTime()
        dateStr= "$lineCounter:$dateStr\n"
        lineCounter++
        return dateStr
    }
    fun close() {
        try {
            logWriter?.close()
            logWriter=null
        } catch (e: IOException) {
            println("${WatcherGlobalLog.LIGHT_RED}Error closing log files: ${e.message} ${WatcherGlobalLog.WHITE}")
        }
    }
}