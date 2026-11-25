package com.viergegroup.swmain.websecurity.bodyencryptor

import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.shared.commons.AppSettings

import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.shared.commons.StringUtils

object CryptoDebugLogger {

    private val debugDataList = mutableListOf<EncryptionDebugData>()
    private val csvHeaders = listOf(
        "encryptDecrypt",
        "timestamp",
        "uuid", "uuidBytesHex",
        "originalJson", "originalJsonByteArray",
        "encryptedMetaData",
        "aesKeyClearHex", "ivHex",
        "hmacHex","receivedHmacHex", "calculatedHmacHex",
        "serialKey", "error"
    )
    fun logErrorData(data: EncryptionDebugData) {
        synchronized(this) {
            debugDataList.add(data)

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
            val logsPath = AppSettings.getInstance().data.logsPath
            WatcherGlobalLog.encryptor("logsPath: $logsPath")

            val file = File("$logsPath/csv_error_encryptor_$timestamp.csv")
            WatcherGlobalLog.encryptor("$logsPath/csv_error_encryptor_$timestamp.csv")
 
            val isNewFile = !file.exists()

            try {
                FileWriter(file, true).use { writer ->
                    if (isNewFile) {
                        writer.append(csvHeaders.joinToString(",")).appendLine()
                    }
                    writer.append(buildCsvLine(data)).appendLine()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                WatcherGlobalLog.encryptor("Error writing debug CSV: ${e.message}")
            }
        }
    }
    fun logDebugData(data: EncryptionDebugData) {
        synchronized(this) {
            debugDataList.add(data)

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
            val logsPath = AppSettings.getInstance().data.logsPath
            WatcherGlobalLog.encryptor("logsPath: $logsPath")

            val file = File("$logsPath/csv_encryptor_$timestamp.csv")
            WatcherGlobalLog.encryptor("$logsPath/csv_encryptor_$timestamp.csv")
 
            val isNewFile = !file.exists()

            try {
                FileWriter(file, true).use { writer ->
                    if (isNewFile) {
                        writer.append(csvHeaders.joinToString(",")).appendLine()
                    }
                    writer.append(buildCsvLine(data)).appendLine()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                WatcherGlobalLog.encryptor("Error writing debug CSV: ${e.message}")
            }
        }
    }

    private fun buildCsvLine(data: EncryptionDebugData): String {
        try{
            return listOf(
                data.encryptOrDecrypt.name,
                LocalDateTime.now().toString(),
                data.uuid,
                data.uuidBytesHex,
                escape(data.originalJson.replace("\n","")),
                StringUtils.encodeHexString(data.originalJsonByteArray),
                data.encryptedMetaData,
                data.aesKeyClearHex,
                data.ivHex,
                data.hmacHex,
                data.receivedHmacHex,
                data.calculatedHmacHex,
                data.serialKey,
                escape(data.error ?: "")
            
            ).joinToString(",")
        }catch(ex:Exception){
            return ""
        }
    }

    private fun escape(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}
