package com.viergegroup.swmain.controllers

import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbservices.LogsMetaDataDBService
import com.viergegroup.swmain.dtos.logs.*
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.webwatcher.WatcherOperationLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.* 
import kotlin.io.path.exists
import java.nio.charset.StandardCharsets

 
@RestController

@RequestMapping("/db-logs")
class DBLogsController {

    @Autowired
    lateinit var logsMetaDataDBService: LogsMetaDataDBService


  

    @PostMapping("/list")
    fun getLogs(@RequestBody dto: LogsFilterDto): LogsDataListResponse {
        val dataList = logsMetaDataDBService.getLogs(dto)
        val response = LogsDataListResponse()
        val dtoList= mutableListOf<LogsMetaDataDBDto>()
       var logsPath=""
  
        val watcher=WatcherContext.getInstance() 
        for(data in dataList){
            val dbdto=LogsMetaDataDBDto() 
            //dbdto.setData(data)

 
            val uriFileLogs= mutableListOf<String>()
            StringUtils.copyNonEmptyProperties(data,dbdto)
            dbdto. uuid= StringUtils.encodeHexString(data.uuid).toLowerCase()
            WatcherGlobalLog.debug("LogsMetaData dbdto. uuid ${dbdto.uuid}")

            dbdto. creationDateTime= StringUtils.datetime2String(data.creationDateTime)
            dbdto. lastModificationDateTime= StringUtils.datetime2String(data.lastModificationDateTime)

            val dateStr=StringUtils.getDateTimeLog(data.creationDateTime)


            val uriBase="/${dbdto.hostType.vaultName}/logs/" + dbdto.terminalSerialNumber + "/$dateStr"+dbdto.operationId.apiPath+"/"
            logsPath = AppSettings.getInstance().getBasePath()+uriBase
            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            val isMicrosoftOS = osName.contains("windows") || osName.contains("microsoft")
            if (isMicrosoftOS) {
                logsPath=logsPath.replace("/","\\")
            }
     
            val deviceLog = "${AppSettings.getInstance().getBasePath()}/devicelogs/${dbdto.hostType.vaultName}/${dbdto.terminalSerialNumber}/$dateStr/${dbdto.rrn}_DEVICE_LOG.txt"
            WatcherGlobalLog.debug("deviceLog $deviceLog")
            if (File(deviceLog).exists()) {
                WatcherGlobalLog.debug("adding deviceLog $deviceLog")
                uriFileLogs.add(deviceLog)
            }
            dbdto.filePaths.add(deviceLog)


            val filePath = logsPath + dbdto.uuid + ".txt"
            WatcherGlobalLog.debug("filePath $filePath")

            if (File(filePath).exists()) {
                val uriPathRequest = uriBase + dbdto.uuid + ".txt"
                WatcherGlobalLog.debug("adding uriPathRequest $uriPathRequest")
                uriFileLogs.add(uriPathRequest)
            }

            dbdto.filePaths.add(filePath)

            val filePathRequest = logsPath + dbdto.uuid + "_ISO_REQUEST.txt"
            WatcherGlobalLog.debug("filePathRequest $filePathRequest")
            if (File(filePathRequest).exists()) {
                val uriPathRequest =uriBase+ dbdto.uuid + "_ISO_REQUEST.txt"
                WatcherGlobalLog.debug("adding uriPathRequest $uriPathRequest")
                uriFileLogs.add(uriPathRequest)
            }
            dbdto.filePaths.add(filePathRequest)

            val filePathResponse = logsPath + dbdto.uuid + "_ISO_RESPONSE.txt"
            WatcherGlobalLog.debug("filePathResponse $filePathResponse")

            if (File(filePathResponse).exists()) {
                val uriPathResponse =uriBase + dbdto.uuid + "_ISO_RESPONSE.txt"
                WatcherGlobalLog.debug("adding uriPathResponse $uriPathResponse")
                uriFileLogs.add(uriPathResponse)
            }
            dbdto.filePaths.add(filePathResponse)

            val filePathErrorRequest = logsPath + dbdto.uuid + "_JSON_ERROR.txt"
            WatcherGlobalLog.debug("filePathErrorRequest $filePathErrorRequest")

            if (File(filePathErrorRequest).exists()) {
                val uriPathErrorRequest = uriBase + dbdto.uuid + "_JSON_ERROR.txt"
                WatcherGlobalLog.debug("adding uriPathErrorRequest $uriPathErrorRequest")
                uriFileLogs.add(uriPathErrorRequest)
            }
            dbdto.filePaths.add(filePathErrorRequest)

            val filePathWebApiResponse = logsPath + dbdto.uuid + "_JSON_RESPONSE.txt"
            WatcherGlobalLog.debug("filePathWebApiResponse $filePathWebApiResponse")

            if (File(filePathWebApiResponse).exists()) {
                val uriPathWebApiResponse =uriBase+ dbdto.uuid + "_JSON_RESPONSE.txt"
                watcher.debug("adding uriPathWebApiResponse $uriPathWebApiResponse")
                uriFileLogs.add(uriPathWebApiResponse)
            }
            dbdto.filePaths.add(filePathWebApiResponse)


            val filePathWebApiModifiedRequest = logsPath + dbdto.uuid + "_JSON_MODIFIED_REQUEST.txt"
            WatcherGlobalLog.debug("filePathWebApiModifiedRequest $filePathWebApiModifiedRequest")

            if (File(filePathWebApiModifiedRequest).exists()) {
                val uriPathWebApiModifiedRequest =uriBase + dbdto.uuid + "_JSON_MODIFIED_REQUEST.txt"
                WatcherGlobalLog.debug("adding uriPathWebApiModifiedRequest $uriPathWebApiModifiedRequest")
                uriFileLogs.add(uriPathWebApiModifiedRequest)
            }
            dbdto.filePaths.add(filePathWebApiModifiedRequest)

            val filePathWebApiOriginalRequest = logsPath + dbdto.uuid + "_JSON_REQUEST.txt"
            WatcherGlobalLog.debug("filePathWebApiOriginalRequest $filePathWebApiOriginalRequest")

            if (File(filePathWebApiOriginalRequest).exists()) {
                val uriPathWebApiOriginalRequest = uriBase + dbdto.uuid + "_JSON_REQUEST.txt"
                WatcherGlobalLog.debug("adding uriPathWebApiOriginalRequest $uriPathWebApiOriginalRequest")
                uriFileLogs.add(uriPathWebApiOriginalRequest)
            }
            dbdto.filePaths.add(filePathWebApiOriginalRequest)

            val filePathWebApiSentHostRequest = logsPath + dbdto.uuid + "_JSON_HOSTSENT_REQUEST.txt"
            WatcherGlobalLog.debug("filePathWebApiSentHostRequest $filePathWebApiSentHostRequest")

            if (File(filePathWebApiSentHostRequest).exists()) {
                val uriPathWebApiSentHostRequest = uriBase + dbdto.uuid + "_JSON_HOSTSENT_REQUEST.txt"
                WatcherGlobalLog.debug("adding uriPathWebApiSentHostRequest $uriPathWebApiSentHostRequest")
                uriFileLogs.add(uriPathWebApiSentHostRequest)
            }
            dbdto.filePaths.add(filePathWebApiSentHostRequest)

            val filePathWebApiOriginalAndBaseRequest = logsPath + dbdto.uuid + "_JSON_ORIGINAL_AND_BASE_REQUEST.txt"
            WatcherGlobalLog.debug("filePathWebApiOriginalAndBaseRequest $filePathWebApiOriginalAndBaseRequest")





            if (File(filePathWebApiOriginalAndBaseRequest).exists()) {
                val uriPathWebApiOriginalAndBaseRequest = uriBase+ dbdto.uuid + "_JSON_ORIGINAL_AND_BASE_REQUEST.txt"
                WatcherGlobalLog.debug("adding uriPathWebApiOriginalAndBaseRequest $uriPathWebApiOriginalAndBaseRequest")
                uriFileLogs.add(uriPathWebApiOriginalAndBaseRequest)
            }
            dbdto.filePaths.add(filePathWebApiOriginalAndBaseRequest)

            dbdto.uriFileLogs = uriFileLogs // Assuming LogsMetaDataDBDto has a property uriFileLogs to hold the list

            dtoList.add(dbdto)
        }
        response.data = dtoList
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }



    @PostMapping("/read-file-log")
    fun readFileLog(@RequestBody request: FileLogRequestDto): FileLogResponseDto {
        val response = FileLogResponseDto()
        var logsPath = AppSettings.getInstance().getBasePath() + request.uri
        if(request.uri.contains("devicelogs")){
            logsPath=request.uri
        }
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val isMicrosoftOS = osName.contains("windows") || osName.contains("microsoft")
        val formattedLogsPath = if (isMicrosoftOS) logsPath.replace("/", "\\") else logsPath
        
        WatcherGlobalLog.debug("readFileLog")
        val filePath = Paths.get(formattedLogsPath)
        WatcherGlobalLog.debug("filePath $filePath")
        
        if (!Files.exists(filePath)) {
            response.errorCode = ErrorCode.SecurityError.errorCodeInt
            response.errorMessage = "FILE DOES NOT EXIST: $formattedLogsPath"
            response.fileContent = "File path: $formattedLogsPath\nFile exists: ${Files.exists(filePath)}\n"
            WatcherGlobalLog.debug("File path: $formattedLogsPath\nFile exists: ${Files.exists(filePath)}\n")
            return response
        }
        
        val stringBuilder = StringBuilder()
        try {
            Files.newBufferedReader(filePath, StandardCharsets.UTF_8).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = reader.readLine()
                }
            }
            
            response.fileContent = "File path: $formattedLogsPath\nFile exists: ${Files.exists(filePath)}\n${stringBuilder.toString()}"
            WatcherGlobalLog.debug("File path: $formattedLogsPath\nFile exists: ${Files.exists(filePath)}")
            response.errorCode = 0
            response.errorMessage = "SUCCESS"
        } catch (e: IOException) {
            response.errorCode = ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage = "IO EXCEPTION: ${e.message}"
            response.fileContent = "File path: $formattedLogsPath\nFile exists: ${Files.exists(filePath)}\n ${e.message}"
            WatcherGlobalLog.error("IO EXCEPTION: ${e.message}")
        }
        
        return response
    }
    @PostMapping("/pages")
    fun getLogsPages(@RequestBody dto: LogsFilterDto): TotalLogsPagesResponse {
        val totalPages = logsMetaDataDBService.getLogsPages(dto)
        val response = TotalLogsPagesResponse()
        response.totalPages = totalPages
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }

    @PostMapping("/upsert")
    fun upsertLogMetaData(@RequestBody dto: LogsMetaDataDto): UpsertLogMetaDataResponse {
        val logData=LogsMetaData()
        logData.injectDto(dto)
        val watcher=WatcherContext.getInstance()
        val id = logsMetaDataDBService.upsertLogMetaData(logData,watcher)
        val response = UpsertLogMetaDataResponse()
        response.id = id
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }
}
