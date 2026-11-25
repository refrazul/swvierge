package com.viergegroup.swmain.webwatcher

import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.dbmanagers.ClientActivityDBManager
import com.viergegroup.swmain.dbmanagers.LogsDBManager
import com.viergegroup.swmain.dbmanagers.TransactionDBManager
import com.viergegroup.swmain.dbmanagers.SystemStatsDBManager
import com.viergegroup.swmain.dbmanagers.TransactionsMetaDataDBManager
import com.viergegroup.isoprocessor.models.descriptors.Iso8583FieldDescriptor


import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor

import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dbservices.*
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.managers.OAuth2Manager 

import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.ModuleId
import com.viergegroup.swmain.entities.catalogs.OperationId
import java.text.SimpleDateFormat
import java.util.*
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto
import com.shared.commons.GsonConfig
import com.shared.commons.StringUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause


class Watcher {
    private    var operationLog:WatcherOperationLog?=null
    private    var hostLog: WatcherHostLog?=null
    private lateinit var requestEntity: RequestEntity

    private  var authToken="" 

    private val logChannel = Channel<Pair<String, String>>(capacity = Channel.UNLIMITED)
    private val scopeDebug = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scopeInfo= CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val scopeError= CoroutineScope(Dispatchers.IO + SupervisorJob())


    
     val WHITE: String = "\u001B[0m" // Dark red

     val DARK_RED: String = "\u001B[38;2;128;0;0m" // Dark red
     val RED: String = "\u001B[38;2;255;0;0m" // Red
     val LIGHT_RED: String = "\u001B[38;2;255;102;102m" // Light red
     val PINK: String = "\u001B[38;2;255;192;203m" // Pink
     val LIGHT_PINK: String = "\u001B[38;2;255;230;230m" // Light pink
     val ROSE: String = "\u001B[38;2;255;228;181m" // Rose
     val OFF_WHITE: String = "\u001B[38;2;255;255;240m" // Off-white
     val DARK_BLUE: String = "\u001B[38;2;0;0;139m"
     val LIGHT_BLUE_1: String = "\u001B[38;2;0;191;255m"
     val LIGHT_BLUE_2: String = "\u001B[38;2;135;206;250m"
     val BLUE: String = "\u001B[34m" // Standard Blue
     val CYAN: String = "\u001B[36m" // Standard Cyan
     val AQUA_BLUE: String = "\u001B[38;2;0;255;255m"
     val DARK_CYAN: String = "\u001B[38;2;0;128;128m"
     val LIGHT_CYAN: String = "\u001B[38;2;176;224;230m"
     val YELLOW: String = "\u001B[33m" // Standard Yellow
     val MUSTARD_YELLOW: String = "\u001B[38;2;139;69;19m"
     val LIGHT_GOLDEN_YELLOW: String = "\u001B[38;2;255;255;153m"

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

    }
     

    fun setAuthToken(authToken:String){
        if(authToken!=""){

            this.authToken=authToken
            debug("Watcher setAuthToken=> ${ StringUtils.maskString( this.authToken)}")
        }else{
            debug("authToken WAS EMPTY")
        } 
     

    } 

   /* fun getAuthToken():String{
        if(  this.authToken==""){

            val response=OAuth2Manager.generateToken(this)
            if(response.token!=null)
             this.authToken=response.token!!
        }
       return  this.authToken
    }*/

fun getAuthToken():String{
    // si alguien inyectó un token explícito en este Watcher, respetarlo; si no, usar el cache compartido
    if(this.authToken != "") return this.authToken
    return OAuth2Manager.getValidToken(this)
}

    companion object{

        private lateinit var transactionDataService: TransactionDataService
        private lateinit var  logsMetaDataDBService: LogsMetaDataDBService
        private lateinit var  clientActivityDBService: ClientActivityDBService
        private lateinit var  systemStatsDBService: SystemStatsDBService
        private  lateinit var transactionsMetaDataDBService: TransactionsMetaDataDBService

      
 


        fun init(
           transactionDataService: TransactionDataService,
           logsMetaDataDBService: LogsMetaDataDBService,
            systemStatsDBService: SystemStatsDBService,

           clientActivityDBService: ClientActivityDBService,
           transactionsMetaDataDBService: TransactionsMetaDataDBService
       ){ 
            this.transactionDataService=transactionDataService
            this.logsMetaDataDBService=logsMetaDataDBService
            this.clientActivityDBService=clientActivityDBService
            this.systemStatsDBService=systemStatsDBService
            this.transactionsMetaDataDBService=transactionsMetaDataDBService
            SystemStatsDBManager.init(systemStatsDBService)  
            TransactionDBManager.init(transactionDataService)
            SystemStatsDBManager.init(systemStatsDBService) 
            ClientActivityDBManager.init(clientActivityDBService)
            LogsDBManager.init(logsMetaDataDBService) 
            TransactionsMetaDataDBManager.init(transactionsMetaDataDBService)
        } 
 


   
        internal fun existsRequest(uuid: String): Boolean {
          WatcherGlobalLog.  info("checkLogExists [${uuid}]")
            if(AppSettings.getInstance().databaseEnabled){
                val idLogs=LogsDBManager.checkLogExists(uuid)
                WatcherGlobalLog.   info("checkLogExists  idLogs [${idLogs}]")
                return idLogs>0
            }
            return false
        }



    }

    constructor(){
     
    }

    constructor( requestEntity: RequestEntity){ 
        this.requestEntity=requestEntity
       
        if(requestEntity.operationIdRequest!=OperationId.HostOperation){
            operationLog= WatcherOperationLog(requestEntity)
            hostLog=WatcherHostLog(requestEntity.hostType)
            val moduleId=requestEntity.moduleIdRequest
            val operationId=requestEntity.operationIdRequest

            info("buildDBManagers BEFORE upsertLogMetaData [${requestEntity.uuid}] operationId [$operationId]  moduleId [$moduleId] ")
            if(AppSettings.getInstance().databaseEnabled
                &&operationId!=OperationId.ReadLogFile
                &&operationId!=OperationId.GetLogsList
                &&operationId!=OperationId.GetTotalLogPages
                &&operationId!=OperationId.HostOperation
                ) {
                val result = LogsDBManager.upsertLogMetaData(requestEntity,this)
                info("buildDBManagers AFTER upsertLogMetaData result $result")
            }
        }else{
            hostLog=WatcherHostLog(requestEntity.hostType)

        }
    }

    /*********************LOGS*****************************************/

    /*********************COMMONS LOGS*****************************************/

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date())
    }

    fun getLogsOpPath(): String {
        return operationLog?.let { it.filePath } ?: "NO_PATH"
    }

    private fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            if (element.className != this::class.java.name && !element.className.startsWith("java.lang.Thread")) {
                return "${element.className}:${element.lineNumber} (${element.methodName})"
            }
        }
        return "ERROR GETTING INFO CLASS "
    }

    fun getHeader(logType:String,logLevel:String):String{
        return "\t${getCurrentTimestamp()}\t|\t${logType}\t|\t${logLevel}\t|\t${getCallerInfo()}|"
    }
    fun debug(isoRaw: String, isoMetadata: Iso8583MetaData?, isRequest: Boolean){
        if(hostLog!=null){
            hostLog!!.debug(getHeader("HOST(${hostLog!!.hostType})","DEBUG"),isoRaw,isoMetadata,isRequest)
            return
        }
        WatcherGlobalLog.debug(getHeader("GLOBAL","DEBUG"),"HOST LOG WAS NULL=>\n ["+isoRaw+"]")
    }




    /*********************COMMONS LOGS*****************************************/


    /*********************OPERATION LOGS*****************************************/

    fun saveIsoRaw(meta: Iso8583MetaData){
    
        if(operationLog!=null){
            CoroutineScope(Dispatchers.IO).async {
                try {
                    operationLog!!.saveIsoRaw(getHeader("OPERATION","DEBUG"),meta)
                } catch (e: Exception) {
                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
               
           
                }
            }else{
                WatcherGlobalLog.debug(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL =>saveIsoRaw ")
    
            } 


    }


    fun webResponse(dtoOrg: Any) {
        if(operationLog!=null){
        CoroutineScope(Dispatchers.IO).async {
       
            try {
                operationLog!!.webResponse(dtoOrg)
            } catch (e: Exception) {
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
           
       
            }
        }else{
            WatcherGlobalLog.debug(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL =>webResponse ${GsonConfig.gsonLogs.toJson(dtoOrg)}")

        }
    }

 
    fun webOriginalAndBaseRequest(dtoOrg: TransactionTerminalDto){

        
        if(operationLog!=null){
            CoroutineScope(Dispatchers.IO).async {
           
                try {
                    operationLog!!.webOriginalAndBaseRequest(dtoOrg)
                } catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.webOriginalAndBaseRequest(dtoOrg: TransactionTerminalDto) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
           
                }
            }else{
                WatcherGlobalLog.debug(getHeader("GLOBAL","DEBUG"),"webOriginalAndBaseRequest")
                WatcherGlobalLog.debug(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL =>webOriginalAndBaseRequest   ${GsonConfig.gsonLogs.toJson(dtoOrg)}")

            }



    }


    fun webRequest(dtoOrg: Any){
    
        if(operationLog!=null){
            CoroutineScope(Dispatchers.IO).async {
           
                try {
                    operationLog!!.webRequest(dtoOrg)
                } catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.webRequest ERROR => ${e.message}")
                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
           
                }
            }else{

                WatcherGlobalLog.debug(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL =>webRequest   ${GsonConfig.gsonLogs.toJson(dtoOrg)}")

            }


    }


    fun webRequestSentHost(dtoOrg: TransactionDto){
      

        if(operationLog!=null){
            CoroutineScope(Dispatchers.IO).async {
           
                try {
                    operationLog!!.webRequestSentHost(dtoOrg)
                } catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.webRequestSentHost ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
           
           
                }
            }else{
                WatcherGlobalLog.debug(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL =>webRequestSentHost   ${GsonConfig.gsonLogs.toJson(dtoOrg)} ")

            }
    }
    /*********************OPERATION LOGS*****************************************/


    fun debugObj(data:Any){
  
        var header=getHeader("GLOBAL","DEBUG-OBJ")
          if(operationLog!=null){ 
            header=getHeader("OPERATION","DEBUG-OBJ")

          }
          if(hostLog!=null){
                header=getHeader("HOST(${hostLog!!.hostType})","DEBUG-OBJ")

          }
        if(operationLog!=null||hostLog!=null){
            CoroutineScope(Dispatchers.IO).async {
           

                try {
                    if(operationLog!=null){ 
                        operationLog!!.obj(header,data)
                       
                    }
                    if(hostLog!=null){
                        hostLog!!.obj(header,data)
                       
                    }

                } catch (e: Exception) {
                    AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.fun debugObj(data:Any) ERROR => ${e.message}")

                    println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
                }
               


             
                       
           
                }
            }else{
                WatcherGlobalLog.obj(header,data)

            }


    }

    fun debug(msg:String){
      
        var header=getHeader("OPERATION","DEBUG")
        if (operationLog != null||hostLog!=null) {
          
            
            try {
                scopeDebug.launch {
                    logChannel.send(Pair(header, msg))
                }
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.  fun debug(msg:String) ERROR => ${e.message}")

                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
            

            
        }else{
            WatcherGlobalLog.debug(getHeader("GLOBAL","DEBUG"),msg)

       }

    }

    

    
    private fun dispatchLogError(header: String, msg: String) {
          
        try {
         
            // Aquí decides dónde mandar el log real
            if (operationLog != null) {
                operationLog!!.error(header, msg)
            } else if (hostLog != null) {
                hostLog!!.error(header, msg)
            } 
        } catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER. fun dispatchLogError(header: String, msg: String)  ERROR => ${e.message}")

            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        }

        



    }
    private fun dispatchLogInfo(header: String, msg: String) {

           
        try {
         
         
            // Aquí decides dónde mandar el log real
            if (operationLog != null) {
                operationLog!!.info(header, msg)
            } else  if(hostLog != null) {
                hostLog!!.info(header, msg)
            } 
        } catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.dispatchLogInfo(header: String, msg: String ERROR => ${e.message}")

            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        }

    }
    private fun dispatchLogDebug(header: String, msg: String) {
             
      
        try {
         
            // Aquí decides dónde mandar el log real
            if (operationLog != null) {
                operationLog!!.debug(header, msg)
            } else if (hostLog != null) {
                hostLog!!.debug(header, msg)
            } 
        } catch (e: Exception) {
            AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.un dispatchLogDebug(header: String, msg: String) ERROR => ${e.message}")

            println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
        }
       
    }

    
    fun reverseDebug(message: String) { 
      
      WatcherGlobalLog.reverseDebug(getHeader("GLOBAL","REVERSES"),message)
        debug(message)
    }   



    fun info(msg:String){
      
        var header=getHeader("OPERATION","INFO")

        if (operationLog != null||hostLog!=null) {
          
              
            try {
                scopeInfo.launch {
                    logChannel.send(Pair(header, msg))
                }
    
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.info(msg:String){un dispatchLogDebug(header: String, msg: String) ERROR => ${e.message}")

                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
           
        }else{

          WatcherGlobalLog.error(getHeader("GLOBAL","ERROR"),msg)

       }


    }  
 
    fun error(errorCode:ErrorCode, message:String?, fieldDescriptor: Iso8583FieldDescriptor) {
        val fieldMessage="LOG_ERROR  |||| FIELD (${fieldDescriptor.fieldNumber}): \"${fieldDescriptor.fieldName}\" VARIABLE: \"${fieldDescriptor.fieldVarName}\"  \n PLEASE CHECK THE DOCUMENTATION\n ${fieldDescriptor.fieldDescription} "
 
        var errorMessage=errorCode.errorCodeMessage +fieldMessage
        if(message!=null) {
            errorMessage = errorCode.errorCodeMessage + " " + message + " " + fieldMessage

        }
      
         error(errorCode,errorMessage)

    }
 
    
    fun error(errorCode:ErrorCode, message:String?, fieldDescriptor: TokenFieldDescriptor){
        var emvTagMsg="LOG_ERROR ||||"
        if(fieldDescriptor.emvTag!=""){
            emvTagMsg=",(EMV_TAG -> \"${fieldDescriptor.emvTag}\"),"
        }
        val fieldMessage="  TOKEN \"${fieldDescriptor.tokenId}\" $emvTagMsg FIELD (${fieldDescriptor.fieldNumber}): \"${fieldDescriptor.fieldName}\" VARIABLE \"${fieldDescriptor.fieldVarName}\" \n PLEASE CHECK THE DOCUMENTATION \n ${fieldDescriptor.fieldDescription} "

       var errorMessage=errorCode.errorCodeMessage +fieldMessage+message.toString()
       
       error(errorCode,errorMessage)   
    } 

    fun error(msg:String){
        if (operationLog != null||hostLog!=null) {
          
            var header=getHeader("OPERATION","INERRORFO")

                   
            try {
        
                scopeError.launch {
                    logChannel.send(Pair(header, msg))
                }
    
            } catch (e: Exception) {
                AlarmEmailService.sendAlarmEmail(AlarmCause.LOG_ERROR,"WATCHER.fun error(msg:String) ERROR => ${e.message}")
                println("${DARK_RED}Error writing log files: ${e.message}${WHITE}")
            }
           


       }else{

          WatcherGlobalLog.error(getHeader("GLOBAL","ERROR"),msg)

       }
    }


    fun error(ex:Exception){

        if(operationLog!=null){
            operationLog!!.error(getHeader("OPERATION","EXCEPTION"),ex)
            return
        }
        WatcherGlobalLog.error(getHeader("GLOBAL","EXCEPTION"),"operationLog WAS NULL =>error EXCEPTION [${ex.message}] ")

    }

    fun error(ex:ErrorCode,message:String){
        if(operationLog!=null){
            operationLog!!.error(getHeader("OPERATION","ERROR"),ex,message)
            return 
        }
        WatcherGlobalLog.error(getHeader("GLOBAL","ERROR"),"operationLog WAS NULL; ERROR=> [${ex}] message [${message}] ")

    } 
    fun error(ex:Exception,message:String){
        if(operationLog!=null){
            operationLog!!.error(getHeader("OPERATION","EXCEPTION"),ex,message)
            return
        }
        WatcherGlobalLog.error(getHeader("GLOBAL","EXCEPTION"),"operationLog WAS NULL =>error EXCEPTION [${ex.message}] message [${message}] ")

    }


    /*********************LOGS*****************************************/

 
    /*********************LOGS*****************************************/


    internal fun upsertLogMetaData(requestEntity: RequestEntity): Boolean {
        info("upsertLogMetaData requestEntity->[${requestEntity.operationIdRequest}] [${requestEntity.uuid}]")
        if(AppSettings.getInstance().databaseEnabled){
            val idLogs=LogsDBManager.upsertLogMetaData(requestEntity,this)
            info("upsertLogMetaData  idLogs [${idLogs}]")
            return idLogs!=null
        }
        return false
    }

    internal fun updateLogErrorData(responseEntity: ResponseEntity,merchantDto:MerchantDataDto): Boolean {
        info("updateLogErrorData  databaseEnabled [${AppSettings.getInstance().databaseEnabled}] [${responseEntity.errorCode}] [${responseEntity.errorMessage}]")
        if(AppSettings.getInstance().databaseEnabled){
            merchantDto.cardNumber=StringUtils.maskCardNumber( merchantDto.cardNumber)

            responseEntity.operationDuration = (System.currentTimeMillis() - responseEntity.startTime) / 1000.0f
            info("updateLogErrorData_responseEntity [${GsonConfig.gsonLogs.toJson(responseEntity)}]")
            info("updateLogErrorData_merchantDto [${GsonConfig.gsonLogs.toJson(merchantDto)}]")
 
            info("uresponseEntity.operationDuration  [${responseEntity.operationDuration}]")
            responseEntity.errorCodeEnum=ErrorCode.fromId(responseEntity.errorCode)
            val idLogs=LogsDBManager.updateLogErrorData(responseEntity,merchantDto)
            info("updateLogErrorData  idLogs [${idLogs}]")
            return idLogs!=null
        }
        return false 
    }
    /*********************LOGS*****************************************/




    /*********************TRANSACTIONS*****************************************/

    internal  fun saveTransaction(transactionResponse: TransactionResponse): TransactionDBData? {
        if(AppSettings.getInstance().databaseEnabled){
            return  TransactionDBManager.saveTransaction(this,transactionResponse)
        }
        return null 
    }

    internal fun findTransaction(rrn:String, terminalSerialNumber:String,hostType: HostType):TransactionDBData?{
        if(AppSettings.getInstance().databaseEnabled){
            return  TransactionDBManager.findTxn(rrn,terminalSerialNumber,hostType,this)
        } 
        return null
    }


    internal fun findOriginalTransaction(rrn:String, terminalSerialNumber:String,authorizationIdResponse:String,hostType: HostType):TransactionDBData?{
        if(AppSettings.getInstance().databaseEnabled){
            return  TransactionDBManager.findOriginalTxn(rrn,terminalSerialNumber,authorizationIdResponse,hostType,this)
        }
        return null 
    }
    /*********************TRANSACTIONS*****************************************/


/*
    fun close(){
        if(hostLog!=null){
            hostLog!!.close()
        }
        if(operationLog!=null){ 
            operationLog!!.close()
        }
    }*/
     fun close(){
        // Cancela las 3 corrutinas colectoras y el canal: sin esto quedan vivas esperando
        // en el canal y retienen todo el grafo del Watcher (writers, FDs) → leak por request.
        // Idempotente: cancelar/cerrar dos veces es no-op.
        try { logChannel.close() } catch (_: Exception) {}
        scopeDebug.cancel()
        scopeInfo.cancel()
        scopeError.cancel()
        if(hostLog!=null){
            hostLog!!.close()
        }
        if(operationLog!=null){ 
            operationLog!!.close()
        }
    }

    fun getTerminalSerialNumber():String{
        return requestEntity.terminalSerialNumber
    }
    fun getRRN():String{
        return requestEntity.rrn
    }
    fun getHostType():HostType{
        return requestEntity.hostType
    }
}