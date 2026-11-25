package com.viergegroup.isoprocessor.models.iso

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.shared.commons.WebServiceException
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause

class Iso8583MetaData { 
companion object{   
    var authId=0 
    
    fun generateAuthId(hostType: HostType):String{
        WatcherGlobalLog.debug("generateAuthId: ${hostType}")
  
            if(hostType.authorizer== Authorizer.BANORTE){
                val dateFormat = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
                val fechaActual = Date()
                val fechaFormateada = dateFormat.format(fechaActual) // Obtiene la fecha actual en formato yymmddhhmmss
                WatcherGlobalLog.debug("fechaFormateada: ${fechaFormateada}")
 
                return fechaFormateada.padStart(12,'0')
            }else{
                val random = Random() 
                val authId = String.format("%06d", random.nextInt(999999))
                return authId

            }

        return ""
    }

}
    var guid: UUID
    var errorCode:Int=0
    var errorMessage=""
    var isoHex="" 
    var isoAscii=""
    var transactionData: TransactionData?=null
    var transactionType:TransactionType=TransactionType.Undefined
    var receivedAtMs: Long = 0   // P3: epoch ms al encolarse (para expirar huerfanos)
    private   var isoBytesArray=ByteArray(0)
    constructor(){
        guid=UUID.randomUUID();
    }
    constructor(guid:UUID){
        this.guid=guid

    } 

    fun setUUID(uuid:String){
        if(transactionData!=null){
            transactionData!!.uuid=uuid
        }

    }
    fun getRrn():String{

        if(transactionData!=null){
            return transactionData!!.rrn
        }
        return ""
    }
    fun getTerminalSerialNumber():String{

        if(transactionData!=null){
            return transactionData!!.terminalSerialNumber
        }
        return ""
    }
    fun isReverse():Boolean{
 
        if(transactionData!=null){
            return transactionData!!.transactionType== TransactionType.Reverse
        }
        return false
    }
    
    fun getIsoTransactionType():TransactionType{
   
        if(transactionData!=null){
            return transactionData!!.transactionType
        }
        return TransactionType.Undefined
    }

    fun getMessageTypeResponse():String{

        if(transactionData!=null){
            return transactionData!!.transactionType.messageTypeResponse
        }
        return ""
    }

    
   fun getUUIDTxn():String{

        if(transactionData!=null){
            return transactionData!!.uuid
        }
        return ""
    }
    fun applyForReverse():Boolean{

        if(transactionData!=null){
            return transactionData!!.transactionType.transactionMessageType== TransactionMessageType.Financial
        }
        return false
    }
    fun isOk():Boolean{

        if(transactionData!=null){
            return transactionData!!.responseCode.equals("00")
        }
        return false
    }
    fun setResponseCode(errorCode: ErrorCode){
        this. errorCode=errorCode.errorCodeInt
        if( this. errorMessage=="")
            this. errorMessage=errorCode.errorCodeMessage
        else{
            this. errorMessage=errorCode.errorCodeMessage+ this. errorMessage

        }
        if(transactionData!=null){
            WatcherGlobalLog.debug("Iso8583MetaData.setResponseCode(ErrorCode) incoming mapped responseCode=[${errorCode.responseCode}] current responseCode=[${transactionData!!.responseCode}] responseCodeAuthorizer=[${transactionData!!.responseCodeAuthorizer}]")
            if(transactionData!!.responseCode == "" && errorCode.responseCode != ""){
                transactionData!!.responseCode = errorCode.responseCode
            }
            // Prefer not to overwrite authorizer raw code if present
            if(transactionData!!.responseCodeAuthorizer == ""){
                transactionData!!.responseCodeAuthorizer = errorCode.responseCode
            }
        }

    }
    fun setResponseCode(responseCode: String){
        val errorCode=ErrorCode.fromResponseCode(responseCode)
        this. errorCode=errorCode.errorCodeInt
        if( this. errorMessage=="")
            this. errorMessage=errorCode.errorCodeMessage
        else{
            this. errorMessage=errorCode.errorCodeMessage+"\n"+ this. errorMessage
        }
        WatcherGlobalLog.debug("Iso8583MetaData.setResponseCode(String) incoming=[${responseCode}] mapped=[${errorCode.responseCode}] current=[${transactionData?.responseCode}] authorizer=[${transactionData?.responseCodeAuthorizer}]")
        if(transactionData!=null){
            if(responseCode != ""){
                // raw authorizer code provided
                transactionData!!.responseCodeAuthorizer = responseCode
                if(transactionData!!.responseCode == "" && errorCode.responseCode != ""){
                    transactionData!!.responseCode = errorCode.responseCode
                }
            }else{
                // no raw code, use mapped value only if responseCode empty
                if(transactionData!!.responseCode == "" && errorCode.responseCode != ""){
                    transactionData!!.responseCode = errorCode.responseCode
                }
            }
        }
    }
    fun setAuthorizationId(authId: String){

        if(transactionData!=null){
            transactionData!!.authorizationIdResponse=authId
        }

    }
    fun getResponseCode():String{

        if(transactionData!=null){
            return transactionData!!.responseCode
        } 
        return ""
    }
    fun getDataTransactionType():TransactionType{

        if(transactionData!=null){
            return transactionData!!.transactionType
        }
        return TransactionType.Undefined
    }
    fun getTransactionMessageType():String{

        if(transactionData!=null){
            return transactionData!!.transactionType.transactionMessageType.toString().lowercase(Locale.getDefault())
        }
        return "unknown" 
    }
    fun getSystemTraceNumber():String{

        if(transactionData!=null){
            return transactionData!!.systemTraceAuditNumber
        }
        return ""
    }
    fun setTransactionStatus(transactionStatus: TransactionStatus){
        if(transactionData!=null){
            transactionData!!.transactionStatus=transactionStatus
        }
    }
    fun getHostType():HostType{

        if(transactionData!=null){
            return transactionData!!.hostType
        }
        return HostType.ProsaDUKPT
    }

    fun isRequestISO():Boolean{
        if(transactionData==null)
            return true

        return transactionData!!.transactionDirection== TransactionDirection.Request

    }
    fun getDetailsInLine():String{
        var json=""
        var iso=""
        if(transactionData!=null){
            json=transactionData!!.describeJSONInLine()
            iso=transactionData!!.describe()
        }

        return "${errorCode}\n${errorMessage}\n${isoHex}\n${isoAscii}\n${json}\n${iso}\n"

    }
    fun getDetails():String{
        var json=""
        if(transactionData!=null){ 
            json=transactionData!!.describeJSON() 
        }
        return "errorCode: ${errorCode} \n errorMessage: ${errorMessage} \n isoHex: ${isoHex} \n isoAscii : ${isoAscii} \n json: ${json} \n\n"

    }

    fun setExceptionParsing(ex:Exception){
        this.errorCode=111 
        if(ex.message!=null)
          this.errorMessage=ex.message!!
        else{
           this.errorMessage = (ex.message ?: "THERE WAS AN EXCEPTION WITH NULL MESSAGE") +
                       " | STACKTRACE: " + ex.stackTraceToString()
        }
    }
    fun setException(ex:Exception){
        this.errorCode=110 
        if(ex.message!=null)
          this.errorMessage=ex.message!!
        else{
           this.errorMessage = (ex.message ?: "THERE WAS AN EXCEPTION WITH NULL MESSAGE") +
                       " | STACKTRACE: " + ex.stackTraceToString()
        } 
    }
    fun setWebServiceException(ex:WebServiceException){
        this.errorCode=ex.errorCode
        if(this.errorCode==0)   this.errorCode=-1
        this.errorMessage=ex.errorMessage
    }
    fun getIsoByteArray():ByteArray{
        return isoBytesArray
    }
    fun setIsoByteArray(byteArray: ByteArray){
        this.isoBytesArray=byteArray
        isoHex= StringUtils.encodeHexString(byteArray)
        isoAscii=  StringUtils.hexToAscii(isoHex)
    }


}