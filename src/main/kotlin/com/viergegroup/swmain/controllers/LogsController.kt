package com.viergegroup.swmain.controllers

import com.shared.commons.AppSettings
import com.viergegroup.swmain.dtos.logs.LogsDto
import com.viergegroup.swmain.dtos.logs.LogsResponseDto



import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileWriter
import java.io.IOException

@RestController


@RequestMapping("logs")
class LogsController {


  
    @PostMapping("/save")
    fun saveLog(@RequestBody transactionDto: LogsDto): LogsResponseDto {
        // Obtener la fecha actual en formato YYYY-MM-DD
        val dateStr: String = AppSettings.getHostConfig(transactionDto.hostType).getCurrentDateTimeLogFile() 
        println("saveLog_dateStr $dateStr")
        
        // Crear la ruta del directorio de logs con la fecha
        val logDirPath = "${AppSettings.getInstance().getBasePath()}/devicelogs/${transactionDto.hostType.vaultName}/${transactionDto.terminalSerialNumber}/$dateStr"
        val logDir = File(logDirPath)
         
        // Crear la carpeta si no existe
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                return LogsResponseDto().apply {
                    errorCode = -1
                    errorMessage = "Error creating directory for logs"
                }
            } 
        }
    
        val logFileName = "${transactionDto.rrn}_DEVICE_LOG.txt" 
        val logFile = File(logDirPath, logFileName)
        
        return try {
            FileWriter(logFile, true).use { writer ->
                writer.write(transactionDto.logData)
                writer.write(System.lineSeparator())
            }
            LogsResponseDto().apply {
                errorCode = 0
                errorMessage = "Log saved successfully $logDirPath "
            }
        } catch (e: IOException) {
            e.printStackTrace()
            LogsResponseDto().apply {
                errorCode = -1
                errorMessage = "Error saving log: ${e.message}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogsResponseDto().apply {
                errorCode = -1
                errorMessage = "Unexpected error: ${e.message}"
            }
        }
    }
    
    
}
