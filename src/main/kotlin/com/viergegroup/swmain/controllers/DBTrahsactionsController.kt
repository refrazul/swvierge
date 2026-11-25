package com.viergegroup.swmain.controllers

import com.shared.commons.AppSettings
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.dtos.shared.GenerateDBDataResponse
import com.viergegroup.swmain.dtos.transactions.*
import com.viergegroup.swmain.dtos.transactions.GenerateTxnsDto
import com.viergegroup.swmain.entities.catalogs.OperationId
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*



@RestController

@RequestMapping("/db-transactions")
class DBTransactionsController {
    @Autowired
    lateinit var transactionDataService: TransactionDataService


   /* @PostMapping("details")
    fun getTransactionData(@RequestBody dto: TransactionFilterDto): TransactionDetailsData {
        val data = transactionDataService.getTransactionData(dto,WatcherContext.getInstance())
        val response = TransactionDetailsData()
        response.data =data
        response.errorCode = 0
        response.errorMessage = "Success"
        return response 
    }*/


    @PostMapping("/list")
    fun getTransactionsList(@RequestBody dto: TransactionFilterDto): TransactionsListResponse {
        val dataList = transactionDataService.getTransactionsList(dto)
        val response = TransactionsListResponse()
        val dtoList= mutableListOf<TransactionDBDataDto>()
        var logsPath="" 
        var watcher=WatcherContext.getInstance();
 
        for( data in dataList){
            val dbdto= TransactionDBDataDto()
            StringUtils.copyNonEmptyProperties(data,dbdto)
            dbdto. rrn= data.rrn

            watcher.info("DBTransactionsController_dto_uuid ${data.uuid}")
            watcher.info("dbdto.operationId ${dbdto.operationId}")
            watcher.info("dbdto.operationId.apiPath ${dbdto.operationId.apiPath}")
            watcher.info("dbdto.rrn ${dbdto.rrn}  data.rrn ${data.rrn}")

            dbdto. creationDateTime= StringUtils.datetime2String(data.creationDateTime)
            dbdto. lastModificationDateTime= StringUtils.datetime2String(data.lastModificationDateTime)
            dbdto. uuid= StringUtils.encodeHexString(data.uuid).toLowerCase()
            try{ 
               dbdto. isoASCIIRequest= StringUtils.asciiFromByteArray(data.isoASCIIRequest)

            }catch(ex:Exception){
                watcher.info("ERROR CONVERTIGN  isoASCIIRequest ${ex.message}")

            }
            try{
                dbdto. isoASCIIResponse= StringUtils.asciiFromByteArray(data.isoASCIIResponse)
    
                }catch(ex:Exception){
                    watcher.info("ERROR CONVERTIGN  isoASCIIResponse ${ex.message}")
    
                }
       
 
            val uriFileLogs= mutableListOf<String>()
            val dateStr=StringUtils.getDateTimeLog(data.creationDateTime)
            val uriBase="/${dbdto.hostType.vaultName}/logs/" + dbdto.terminalSerialNumber + "/$dateStr"+ dbdto.operationId.apiPath+"/"
            logsPath = AppSettings.getInstance().getBasePath()+uriBase


            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            val isMicrosoftOS = osName.contains("windows") || osName.contains("microsoft")
            if (isMicrosoftOS) {
                logsPath=logsPath.replace("/","\\")
            }


            val filePath = logsPath + dbdto.uuid + ".txt"
            watcher.debug("filePath $filePath")

            if (File(filePath).exists()) {
                val uriPathRequest = uriBase + dbdto.uuid + ".txt"
                watcher.debug("adding uriPathRequest $uriPathRequest")
                uriFileLogs.add(uriPathRequest)
            }

            dbdto.filePaths.add(filePath)

            val filePathRequest = logsPath + dbdto.uuid + "_ISO_REQUEST.txt"
            watcher.debug("filePathRequest $filePathRequest")
            if (File(filePathRequest).exists()) {
                val uriPathRequest =uriBase+ dbdto.uuid + "_ISO_REQUEST.txt"
                watcher.debug("adding uriPathRequest $uriPathRequest")
                uriFileLogs.add(uriPathRequest)
            }
            dbdto.filePaths.add(filePathRequest)

            val filePathResponse = logsPath + dbdto.uuid + "_ISO_RESPONSE.txt"
            watcher.debug("filePathResponse $filePathResponse")

            if (File(filePathResponse).exists()) {
                val uriPathResponse =uriBase + dbdto.uuid + "_ISO_RESPONSE.txt"
                watcher.debug("adding uriPathResponse $uriPathResponse")
                uriFileLogs.add(uriPathResponse)
            }
            dbdto.filePaths.add(filePathResponse)

            val filePathErrorRequest = logsPath + dbdto.uuid + "_JSON_ERROR.txt"
            watcher.debug("filePathErrorRequest $filePathErrorRequest")

            if (File(filePathErrorRequest).exists()) {
                val uriPathErrorRequest = uriBase + dbdto.uuid + "_JSON_ERROR.txt"
                watcher.debug("adding uriPathErrorRequest $uriPathErrorRequest")
                uriFileLogs.add(uriPathErrorRequest)
            }
            dbdto.filePaths.add(filePathErrorRequest)

            val filePathWebApiResponse = logsPath + dbdto.uuid + "_JSON_RESPONSE.txt"
            watcher.debug("filePathWebApiResponse $filePathWebApiResponse")

            if (File(filePathWebApiResponse).exists()) {
                val uriPathWebApiResponse =uriBase+ dbdto.uuid + "_JSON_RESPONSE.txt"
                watcher.debug("adding uriPathWebApiResponse $uriPathWebApiResponse")
                uriFileLogs.add(uriPathWebApiResponse)
            }
            dbdto.filePaths.add(filePathWebApiResponse)


            val filePathWebApiModifiedRequest = logsPath + dbdto.uuid + "_JSON_MODIFIED_REQUEST.txt"
            watcher.debug("filePathWebApiModifiedRequest $filePathWebApiModifiedRequest")

            if (File(filePathWebApiModifiedRequest).exists()) {
                val uriPathWebApiModifiedRequest =uriBase + dbdto.uuid + "_JSON_MODIFIED_REQUEST.txt"
                watcher.debug("adding uriPathWebApiModifiedRequest $uriPathWebApiModifiedRequest")
                uriFileLogs.add(uriPathWebApiModifiedRequest)
            }
            dbdto.filePaths.add(filePathWebApiModifiedRequest)

            val filePathWebApiOriginalRequest = logsPath + dbdto.uuid + "_JSON_REQUEST.txt"
            watcher.debug("filePathWebApiOriginalRequest $filePathWebApiOriginalRequest")

            if (File(filePathWebApiOriginalRequest).exists()) {
                val uriPathWebApiOriginalRequest = uriBase + dbdto.uuid + "_JSON_REQUEST.txt"
                watcher.debug("adding uriPathWebApiOriginalRequest $uriPathWebApiOriginalRequest")
                uriFileLogs.add(uriPathWebApiOriginalRequest)
            }
            dbdto.filePaths.add(filePathWebApiOriginalRequest)

            val filePathWebApiSentHostRequest = logsPath + dbdto.uuid + "_JSON_HOSTSENT_REQUEST.txt"
            watcher.debug("filePathWebApiSentHostRequest $filePathWebApiSentHostRequest")

            if (File(filePathWebApiSentHostRequest).exists()) {
                val uriPathWebApiSentHostRequest = uriBase + dbdto.uuid + "_JSON_HOSTSENT_REQUEST.txt"
                watcher.debug("adding uriPathWebApiSentHostRequest $uriPathWebApiSentHostRequest")
                uriFileLogs.add(uriPathWebApiSentHostRequest)
            }
            dbdto.filePaths.add(filePathWebApiSentHostRequest)

            val filePathWebApiOriginalAndBaseRequest = logsPath + dbdto.uuid + "_JSON_ORIGINAL_AND_BASE_REQUEST.txt"
            watcher.debug("filePathWebApiOriginalAndBaseRequest $filePathWebApiOriginalAndBaseRequest")

            if (File(filePathWebApiOriginalAndBaseRequest).exists()) {
                val uriPathWebApiOriginalAndBaseRequest = uriBase+ dbdto.uuid + "_JSON_ORIGINAL_AND_BASE_REQUEST.txt"
                watcher.debug("adding uriPathWebApiOriginalAndBaseRequest $uriPathWebApiOriginalAndBaseRequest")
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
    @PostMapping("/pages")
    fun getTotalTransactionPages(@RequestBody dto: TransactionFilterDto): TotalTransactionPagesResponse {
        val totalPages = transactionDataService.getTotalTransactionPages(dto)
        val response = TotalTransactionPagesResponse()
        response.totalPages = totalPages
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }
    @PostMapping("insert")
    fun insertTransactionData(@RequestBody dto: TransactionDBDataDto): InsertTransactionDataResponse {
        val transactionData = TransactionDBData()
        transactionData.injectDto(dto)
        transactionData.cardNumber=StringUtils.maskCardNumber(transactionData.cardNumber) 
        WatcherGlobalLog.debug("insertTransactionData_cardNumber_maskCardNumber [${transactionData.cardNumber}]")
 
        val id = transactionDataService.insertTransactionData(transactionData,false,WatcherContext.getInstance())
        val response = InsertTransactionDataResponse()
        response.id = id
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }

    @PostMapping("/generate-txns")
    fun generateTxns( @RequestBody dto: GenerateTxnsDto): GenerateDBDataResponse {
        transactionDataService.generateTxns(dto.numberTxns)
        val response=GenerateDBDataResponse()
        response.errorMessage=""
        response.errorCode=0
        return response
    }
}
