package com.viergegroup.swmain.webwatcher

import com.google.gson.GsonBuilder
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.GsonConfig
 
import com.shared.commons.StringUtils
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.dtosgsonadapters.TransactionDtoGSONAdapter
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.LogLevel
import com.viergegroup.swmain.entities.catalogs.TransactionMessageType
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class WatcherOperationLog(val requestEntity: RequestEntity) {
    private  lateinit var  output: Writer
    // var  commLogOutPut: Writer?=null

      var  logsPath:String=""
      var  filePath:String=""

    private  var  filePathErrorRequest:String="" 

    private  var  filePathRequest:String=""
    private  var  filePathResponse:String=""
    private  var  filePathWebApiResponse:String=""
    private  var  filePathWebApiSentHostRequest:String=""
    private  var  filePathWebApiOriginalRequest:String=""
    private  var  filePathWebApiOriginalAndBaseRequest:String=""
    private  var  filePathWebApiModifiedRequest:String=""
    //var  commLogFilePath:String=""
    private   var  originalTerminalSerialNumber:String=""





    var lineCounter=0

    private var currentDateStr: String=""






    init {
        create(requestEntity)
    }




    fun close(){

        if(logsPath == "") return
        try {
            output.close()
        }catch (e:Exception){
            WatcherGlobalLog.error("ERROR CLOSING FILE "+logsPath+" "+e.message)
        }
    }



    fun readCommLog(hostType: HostType, transactionType: TransactionMessageType, creationDate: LocalDate):String{
        var content=""

        try {

            var path= AppSettings.getHostConfig(hostType).commLogsPath
            if(transactionType!=TransactionMessageType.Undefined){
                path= AppSettings.getHostConfig(hostType).convertHostFilePath(creationDate)+"_"+ transactionType.toString() .lowercase(
                    Locale.getDefault()) +".txt"
            }
            // content=path

            val file = File(path)
            if (file.exists()) {

                val lines: List<String> = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)

                for(line in lines){
                    content += line+"\n"

                }

            }
        } catch (e: Exception) {
            if(e.message!=null)
                return e.message!!+" "+creationDate.toString()
            return "EXCEPCION "+creationDate.toString()
        }
        return content
    }


    fun deleteCommLog(hostType:HostType,transactionType: TransactionMessageType,creationDate: LocalDate):String{
        var path= AppSettings.getHostConfig(hostType).commLogsPath
        if(transactionType!=TransactionMessageType.Undefined){
            path= AppSettings.getHostConfig(hostType).convertHostFilePath(creationDate)+"_"+ transactionType.toString()
                .lowercase(Locale.getDefault()) +".txt"
        }
        try {
            val writer = PrintWriter(path)
            writer.print("")
            writer.flush()
            writer.close()
            return "SUCCESS"
        } catch (e: Exception) {
            if(e.message!=null)
                return e.message!!
            return "EXCEPCION"
        }


    }
    fun webOriginalRequest(dto: TransactionTerminalDto){
 
        val file = File(filePathWebApiOriginalRequest)
        val writer = PrintWriter(filePathWebApiOriginalRequest)
        try {
          
            val json=GsonConfig.gsonLogs.toJson(dto)
            file.createNewFile()
            writer.print(
                json
            )
            writer.flush()
            writer.close()
        }catch (e:Exception){

            WatcherGlobalLog.error("webRequest EXCEPTION ${e.message}")
        }
    }
    fun webOriginalAndBaseRequest(dto: TransactionTerminalDto){
     //   val dto= StringUtils.removeSensitiveDataForPrint(dtoOrg)
        WatcherGlobalLog.debug("webOriginalAndBaseRequest $filePathWebApiOriginalAndBaseRequest")
        val file = File(filePathWebApiOriginalAndBaseRequest)
        val writer = PrintWriter(filePathWebApiOriginalAndBaseRequest)
        try {
         
            val json=GsonConfig.gsonLogs.toJson(dto)

            file.createNewFile()
            writer.print(
                json
            )
            writer.flush()
            writer.close()
        }catch (e:Exception){

            WatcherGlobalLog.error("webRequest EXCEPTION ${e.message}")
        }
    }


    fun webRequestSentHost(dto: TransactionDto){
        val file = File(filePathWebApiSentHostRequest)
        val writer = PrintWriter(filePathWebApiSentHostRequest)
        try {
           
            val json=GsonConfig.gsonLogs.toJson(dto)

            file.createNewFile()
            writer.print(
                json
            )
            writer.flush()
            writer.close()
        }catch (e:Exception){

            WatcherGlobalLog.error("webRequest EXCEPTION ${e.message}")
        }
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
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

    fun webRequest(dtoOrg: Any){
        if(filePathWebApiOriginalRequest=="") return
        val file = File(filePathWebApiOriginalRequest)
        val writer = PrintWriter(filePathWebApiOriginalRequest)
        WatcherGlobalLog.info(getHeader("OPERATION","INFO"),"webRequest saving  in ${filePathWebApiOriginalRequest}")

        try {
            WatcherGlobalLog.info(getHeader("OPERATION","INFO"),"webRequest dto  ${GsonConfig.gsonLogs.toJson(dtoOrg)}")

            /*val gson= GsonBuilder().registerTypeAdapter(
                TransactionDto::class.java, 
                TransactionDtoGSONAdapter()
            ).setPrettyPrinting().create()*/
            val json=GsonConfig.gsonLogs.toJson(dtoOrg)

            file.createNewFile()
            writer.print(
                "${dtoOrg.javaClass.`package`}${dtoOrg.javaClass.name}\n"+
                json
            )
            writer.flush()
            writer.close()
            info(getHeader("OPERATION","INFO"),"webRequest saving OK FINISH")

        }catch (e:Exception){
            WatcherGlobalLog.error("webRequest EXCEPTION ${e.message}")
        }
    }

    fun webResponse(dtoOrg: Any) {
        try {

            val json=GsonConfig.gsonLogs.toJson(dtoOrg)
            //  logsMetaDataDBService.saveLogMetaData(dto.transactionRequest!!)
            val file = File(filePathWebApiResponse)
            val writer = PrintWriter(filePathWebApiResponse)
            //var json=""
            try {
                file.createNewFile()
                writer.print(
                    "${dtoOrg.javaClass.`package`}${dtoOrg.javaClass.name}\n"+
                     json
                )
                writer.flush()
                writer.close()
            }catch (e:Exception){

                WatcherGlobalLog.error("EXCEPTION WEBRESPONSE_PRINT : ${e.message}")

            }
        }catch (e:Exception){

            WatcherGlobalLog.error("webResponse EXCEPTION [${e.message}] FILE: [$filePathWebApiResponse]")

        }
    }

 /*   fun webResponse(dtoOrg: TransactionResponse) {
        try {

            val dto= StringUtils.removeSensitiveDataForPrint(dtoOrg)


            val fdto= TransactionFullResponseDto(dto)
            val json=WatcherGlobalLog.gsonPrint.toJson(fdto)
            if(dto.transactionRequest==null|| dto.transactionRequest!=null&&dto.transactionRequest!!.errorMessage=="") {
                if(dto.transactionRequest==null)
                    dto.transactionRequest= TransactionDto()
                dto.transactionRequest!!.errorCode=dto.errorCode
                dto.transactionRequest!!.errorMessage=dto.errorMessage

            }
            //  logsMetaDataDBService.saveLogMetaData(dto.transactionRequest!!)
            val file = File(filePathWebApiResponse)
            val writer = PrintWriter(filePathWebApiResponse)

            //var json=""
            try {
                file.createNewFile()
                writer.print(
                    json
                )
                writer.flush()
                writer.close()
            }catch (e:Exception){

                    WatcherGlobalLog.error("EXCEPTION WEBRESPONSE_PRINT : ${e.message}")

            }
        }catch (e:Exception){

            WatcherGlobalLog.error("webResponse EXCEPTION ${e.message}")

        }
    }*/


    fun obj(header:String,data: Any) {
        val content: String= GsonConfig.gsonLogs.toJson(data)
        val logMessage = "$header\t$content\n"

        if(AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.ConsoleDebug))
               println(WatcherGlobalLog.CYAN + logMessage + WatcherGlobalLog.WHITE);

        if(!AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.FileDebug)) return
        if(logsPath == "" ) return
        try {
            output.append(logMessage)
            output.flush()
        }catch (e:Exception){
            try {
                output = BufferedWriter(FileWriter(filePath, true))
                output.append(logMessage)
                output.flush()
            }catch (e:Exception){

            }

        }
    }

    fun info(header:String,message: String) {
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.ConsoleInfo))
              println(WatcherGlobalLog.YELLOW  + logMessage +WatcherGlobalLog.WHITE )

        if(!AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.FileInfo)) return
        if(logsPath == "" ) return
        try {
            output.append(logMessage)
            output.flush()
        }catch (e:Exception){
            try {
                output = BufferedWriter(FileWriter(filePath, true))

                output.append(logMessage)
                output.flush()
            }catch (e:Exception){

            }

        }
    }




 
    fun debug(header:String,message: String) {
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.ConsoleDebug))
            println("${WatcherGlobalLog.CYAN}$logMessage${WatcherGlobalLog.WHITE}")

        if(!AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.FileDebug)) return
        if(logsPath == "" ) return
        try {
            output.append(logMessage)
            output.flush()
        }catch (e:Exception){
            try {
                output = BufferedWriter(FileWriter(filePath, true))

                output.append(logMessage)
                output.flush()
            }catch (e:Exception){

            }
        }
    }



    fun saveIsoRaw(header:String,meta: Iso8583MetaData){
        if(logsPath == "") return
        var isRequest=true
        if(meta.transactionData!=null){
            isRequest=meta.transactionData!!.transactionDirection== TransactionDirection.Request
        }
        val filePath: String
        if (isRequest) {
            filePath = filePathRequest
        } else {
            filePath = filePathResponse

        }
        val myObj = File(filePath)
        val writer = PrintWriter(filePath)
        //var json=""
        if (myObj.createNewFile()) {

            writer.print(
                meta.getDetailsInLine()
            )
            writer.close()
        } else {
            writer.print(
                meta.getDetailsInLine()
            )
            writer.close()
        }

    }




    fun error(header: String,ex:Exception,msg: String){
        if(ex.message!=null)
            error(header,ErrorCode.UnHandledException,"["+ex.message!!+"]  "+msg)
        else
            error(header,ErrorCode.UnHandledException,msg)

    }
    fun error(header: String,ex:Exception){
        if(ex.message!=null)
            error(header,ErrorCode.UnHandledException,ex.message!!)
        else
            error(header,ErrorCode.UnHandledException,"EXCEPTION WITHOUT DESCRIPTION")

    }

    fun error(header: String,errorCode: Int, msg: String) {
        error(header,ErrorCode.fromId(errorCode),msg)
    }

    fun error(header: String,errorCode: ErrorCode, msg: String) {

       // val dateStr: String = AppSettings.getHostConfig(requestEntity.hostType).getCurrentDateTime()
        val infoContent = "${header}\n [${if (errorCode == ErrorCode.UnHandledException) "EXCEPTON" else "ERROR"}(${"%x".format(errorCode.errorCodeInt)})]:  \n${msg}\n"
        val coloredContent = "${WatcherGlobalLog.RED}${header}\n$infoContent${WatcherGlobalLog.WHITE}"
        if(AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.ConsoleError))
             println(coloredContent)

        if(!AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.FileError)) return
        if(logsPath == "" ) return
        try {
            output.append(infoContent)
            output.flush()
        }catch (e:Exception){
            try {
                output = BufferedWriter(FileWriter(filePath, true))

                output.append(infoContent)
                output.flush()
            }catch (e:Exception){

            }
        }
    }


    fun error(header:String,message: String) {
        val logMessage = "$header\t$message\n"
        if(AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.ConsoleError))
            println("${WatcherGlobalLog.RED}$logMessage${WatcherGlobalLog.WHITE}")

        if(!AppSettings.getHostConfig(requestEntity.hostType).logsOperationIsActive(LogLevel.FileError)) return
        if(logsPath == "" ) return
        try {
            output.append(logMessage)
            output.flush()
        }catch (e:Exception){
            try {
                output = BufferedWriter(FileWriter(filePath, true))

                output.append(logMessage)
                output.flush()
            }catch (e:Exception){

            }
        }
    }







    fun create(requestEntity: RequestEntity){

        try {
            val dateStr: String = AppSettings.getHostConfig(requestEntity.hostType).getCurrentDateTimeLogFile() 
            val terminalSerialNumber=requestEntity.terminalSerialNumber.trim().uppercase(Locale.getDefault()).replace(" ","_")
            this.originalTerminalSerialNumber=requestEntity.terminalSerialNumber
            println("create log: dateStr $dateStr uuid; ${requestEntity.uuid} terminalSerialNumber $terminalSerialNumber ")
            if(terminalSerialNumber != "" && requestEntity.uuid != "") {
                //   val source: Path = Paths.get(LogUtil::class.java.getResource("/logs").path)
                logsPath=""
                filePath=""
                if (AppSettings.getInstance().isLinuxServer) {
                    logsPath = AppSettings.getAuthorizerConfig(requestEntity.hostType).logsPath + "/" + terminalSerialNumber + "/$dateStr/"+requestEntity.operationIdRequest.apiPath+"/"
                } else {
                    logsPath = AppSettings.getAuthorizerConfig(requestEntity.hostType).logsPath + "\\" + terminalSerialNumber + "\\$dateStr\\"+requestEntity.operationIdRequest.apiPath.replace("/","\\")+"\\"
                }

                filePath = logsPath + requestEntity.uuid  +".txt"
                filePathRequest=logsPath + requestEntity.uuid  +"_ISO_REQUEST.txt"
                filePathResponse=logsPath + requestEntity.uuid  +"_ISO_RESPONSE.txt"
                filePathErrorRequest=logsPath + requestEntity.uuid  +"_JSON_ERROR.txt"
                filePathWebApiResponse=logsPath + requestEntity.uuid  +"_JSON_RESPONSE.txt"
                filePathWebApiModifiedRequest=logsPath + requestEntity.uuid  +"_JSON_MODIFIED_REQUEST.txt"
                filePathWebApiOriginalRequest=logsPath + requestEntity.uuid  +"_JSON_REQUEST.txt"
                filePathWebApiSentHostRequest=logsPath + requestEntity.uuid  +"_JSON_HOSTSENT_REQUEST.txt"
                filePathWebApiOriginalAndBaseRequest=logsPath + requestEntity.uuid  +"_JSON_ORIGINAL_AND_BASE_REQUEST.txt"
 
                //  println("LOGS_PATH: " + logsPath)
                //  println("FILE_PATH: " + filePath)
                val newFolder: Path =
                    Paths.get(logsPath )

                // println("newFolder: " + newFolder.pathString)

                if (!Files.exists(newFolder)) {

                    Files.createDirectories(newFolder)
                }
                println("CREATING LOG FILE IN $filePath")
                output = BufferedWriter(FileWriter(filePath,true))


            }
        } catch (e: Exception) {
            println("error in LOGGER: ${e.message} filePath $filePath ")
        }
    }






}