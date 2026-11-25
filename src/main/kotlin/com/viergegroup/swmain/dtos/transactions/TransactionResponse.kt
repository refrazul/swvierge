
package com.viergegroup.swmain.dtos.transactions

import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.TransactionStatus
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.transactions.TransactionData

import org.springframework.beans.BeanUtils

class TransactionResponse : ResponseEntity{

    
    var transactionRequest: TransactionDto?=null 
    var transactionResponse: TransactionDto?=null 
    var isoResponse:Iso8583MetaData?= null
    //var transactionDataResponse:TransactionResponseDto= TransactionResponseDto()
    /*var tag8A:String="" 
    var tag91:String=""
    var field4: BigDecimal = BigDecimal(0)
    var field12:Date= Date() 
    var field13:Date=Date()
    var field37:String=""
    var field38:String=""
    var field39:String=""
    var field44:String=""
    var field54:Double=0.0
    var field55:String=""
    var field63:String="" 
    var field64:String=""*/
    fun getIsoRequestASCII():String{
       if(transactionRequest!=null){
           return transactionRequest!!.isoASCII
       }
        return ""
  
    }
    fun getIsoResponseASCII():String{
        if(transactionResponse!=null){
            return transactionResponse!!.isoASCII
        }
        return ""

    }

    fun setIsoResponseASCII(iso:String){
        if(transactionResponse!=null){ 
             transactionResponse!!.isoASCII=iso
        }
        if(transactionRequest!=null){ 
            transactionRequest!!.isoASCII=iso
       }
 
    } 

 
    fun getResponseCode():String{
        if(transactionResponse!=null){
            return transactionResponse!!.responseCode
        } 
        return ""
    }
    fun isOk():Boolean{

        if(transactionResponse!=null){
            return transactionResponse!!.responseCode.equals("00")
        } 
        return false
    }
    constructor(transactionDataRequest: TransactionDto){
        var errorCodeEnum: ErrorCode
        if(transactionDataRequest.responseCode != "") {
             errorCodeEnum = ErrorCode.fromResponseCode(transactionDataRequest.responseCode)
            if(transactionDataRequest.errorMessage=="")
                this.errorMessage = errorCodeEnum.errorCodeMessage 
            else
                this.errorMessage = transactionDataRequest.errorMessage

            this.errorCode = errorCodeEnum.errorCodeInt

 
        }else{
            errorCodeEnum = ErrorCode.fromId(transactionDataRequest.errorCode)

            if(transactionDataRequest.errorMessage=="")
                this.errorMessage = errorCodeEnum.errorCodeMessage
            else
                this.errorMessage = transactionDataRequest.errorMessage

            this.errorCode = transactionDataRequest.errorCode
            transactionDataRequest.responseCode=errorCodeEnum.responseCode
        }

       /* if(AppSettings.getInstance().forceReverses&&transactionDataRequest.transactionType!=TransactionType.Reverse){
             errorCodeEnum=ErrorCode.HostTimeOut
            this.errorMessage = errorCodeEnum.errorCodeMessage
            this.errorCode = errorCodeEnum.errorCodeInt
        }*/


        this. transactionRequest=transactionDataRequest

        transactionRequest!!.transactionStatus= TransactionStatus.ReceivedInWebService

    }
    constructor()

    constructor(transactionDataRequest: TransactionData?){
        fillTransData(transactionDataRequest,null,"")

    }
    constructor(iso8583MetaData: Iso8583MetaData,iso8583MetaDataResponse: Iso8583MetaData?){
        if(iso8583MetaDataResponse!=null) {
            this.isoResponse=iso8583MetaDataResponse
            this.errorMessage = iso8583MetaDataResponse.errorMessage
            this.errorCode = iso8583MetaDataResponse.errorCode
            var errorEnum=ErrorCode.fromId( this.errorCode)
            if(errorEnum==ErrorCode.Approved) {
                if(!iso8583MetaData.isReverse())
                     errorEnum=ErrorCode.fromResponseCode( iso8583MetaDataResponse.getResponseCode())
            } 

            
            if(errorEnum!=ErrorCode.Approved){
                val requestTransData=iso8583MetaData.transactionData!!
                var transData=iso8583MetaDataResponse.transactionData
                if(transData==null){transData=TransactionData()}
                transData.cardType=requestTransData.cardType
                transData.transactionAmount=requestTransData.transactionAmount
                transData.transactionType=requestTransData.transactionType
                transData.merchantType=requestTransData.merchantType
                transData.hostType=requestTransData.hostType
                transData.pointOfServiceEntryMode=requestTransData.pointOfServiceEntryMode

            }
            iso8583MetaData.setResponseCode(errorEnum)
            iso8583MetaDataResponse.setResponseCode(errorEnum)

        }else{
            this.errorMessage = iso8583MetaData.errorMessage
            this.errorCode = iso8583MetaData.errorCode
        }


        if(iso8583MetaData.getResponseCode() != "00"&&iso8583MetaDataResponse==null){
            this.errorMessage = iso8583MetaData.errorMessage
            this.errorCode = iso8583MetaData.errorCode
            iso8583MetaData.setResponseCode(ErrorCode.fromId( this.errorCode))
        }


        if(iso8583MetaDataResponse!=null) {
           if (iso8583MetaDataResponse != null) { 
                val txData = iso8583MetaDataResponse.transactionData
                if (txData != null && txData.responseCode.isNullOrEmpty()) {
                    txData.responseCode = iso8583MetaDataResponse.getResponseCode()
                }
            }

            fillTransData(iso8583MetaData.transactionData, iso8583MetaDataResponse.transactionData,iso8583MetaDataResponse.getResponseCode())
        }else{

            fillTransData(iso8583MetaData.transactionData, null,"")

        }
        if(AppSettings.getInstance().forceReverses&&!iso8583MetaData.isReverse()&&iso8583MetaDataResponse!=null){
            val errorCodeEnum=ErrorCode.ClientTimeOut
            this.errorMessage = errorCodeEnum.errorCodeMessage
            this.errorCode = errorCodeEnum.errorCodeInt
            iso8583MetaDataResponse.setResponseCode(errorCodeEnum)
            iso8583MetaDataResponse.setAuthorizationId("") 
        }
    }


   /* constructor(transactionDataRequest:TransactionData?,transactionDataResponseData:TransactionData?){
        fillTransData(transactionDataRequest,transactionDataResponseData)

    }*/

    fun fillTransData(transactionDataRequest:TransactionData?,transactionDataResponseData:TransactionData?,responseCodeAuthorizer:String){



       if(transactionDataResponseData!=null) {
           transactionResponse= TransactionDto()
      
           if(!responseCodeAuthorizer.contains("F")&&responseCodeAuthorizer.contains("H")
           &&responseCodeAuthorizer.contains("D")
           &&responseCodeAuthorizer.contains("E") &&responseCodeAuthorizer.contains("X"))
               transactionResponse!!.responseCodeAuthorizer=responseCodeAuthorizer

           BeanUtils.copyProperties(transactionDataResponseData, transactionResponse!!)
           transactionResponse!!.isoASCII=transactionDataResponseData.getIsoASCII()
            if(transactionResponse!!.responseCode==""&&transactionDataResponseData!=null|| transactionDataRequest!=null&&transactionDataRequest!!.affiliation=="0000000")
              transactionResponse!!.responseCode=transactionDataResponseData!!.responseCode
           
       }
        if(transactionDataRequest!=null) {
            transactionRequest= TransactionDto()
            BeanUtils.copyProperties(transactionDataRequest, transactionRequest!!)
            transactionRequest!!.isoASCII=transactionDataRequest.getIsoASCII()
            
            transactionRequest!!.messageType=   transactionRequest!!.transactionType.messageTypeRequest
           /* if(AppSettings.getInstance().forceReverses&&transactionDataRequest.transactionType!=TransactionType.Reverse){
                val errorCodeEnum=ErrorCode.HostTimeOut
                this.errorMessage = errorCodeEnum.errorCodeMessage
                this.errorCode = errorCodeEnum.errorCodeInt
                transactionDataRequest.responseCode=errorCodeEnum.responseCode
            }*/

        }

        if(  transactionResponse!=null){
            transactionResponse!!.messageType=   transactionResponse!!.transactionType.messageTypeResponse

            if(transactionResponse!!.responseCode.equals("00"))
                transactionRequest!!.transactionStatus=TransactionStatus.Approved

                
            if(!transactionResponse!!.responseCode.equals("00")) {
                transactionRequest!!.transactionStatus = TransactionStatus.Rejected
            } 
            if(AppSettings.getInstance().forceReverses&& 
            transactionResponse!!.transactionType!= TransactionType.Reverse
            ){
                val errorCodeEnum=ErrorCode.ClientTimeOut 
                this.errorMessage = errorCodeEnum.errorCodeMessage
                this.errorCode = errorCodeEnum.errorCodeInt
                 if(transactionResponse!!.responseCode=="")
                    transactionResponse!!.responseCode=errorCodeEnum.responseCode
                transactionResponse!!.authorizationIdResponse=""
            }  
            val errorCodeEnum =ErrorCode.fromResponseCode(transactionResponse!!.responseCode)
            if(errorCodeEnum.mustSendAReverse){ 
                transactionRequest!!.transactionStatus = TransactionStatus.MustBeReversed
            }
  
            transactionRequest!!.responseCode=    transactionResponse!!.responseCode
            transactionRequest!!.authorizationIdResponse=    transactionResponse!!.authorizationIdResponse
            transactionRequest!!.acquiringInstitutionIdentificationCode=    transactionResponse!!.acquiringInstitutionIdentificationCode


        }


        if(transactionDataResponseData==null&&transactionDataRequest!=null) {

             var errorCodeEnum = ErrorCode.fromResponseCode(transactionDataRequest.responseCode)
            if(errorCodeEnum==ErrorCode.Undefined||this.errorCode!=0){
                errorCodeEnum = ErrorCode.fromId(this.errorCode)
            }

            if(errorCodeEnum.mustSendAReverse){ 

                transactionRequest!!.transactionStatus = TransactionStatus.MustBeReversed

            } 
            if(transactionRequest!!.responseCode=="")
                transactionRequest!!.responseCode=errorCodeEnum.responseCode




         }



    }

}