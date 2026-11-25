package com.viergegroup.swmain.dtos.transactions

import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import java.time.LocalDateTime
import java.math.BigDecimal


class TransactionDBDto{

    var transactionId: Long = 0
    var originalTransactionId:Long=-1



    var fiid:String=""
   var transactionIdMpp: String = ""
      
    var affiliation:String=""
    var merchantName:String=""
  // var merchantTypeCode:String=""
    var merchantAddress:String=""
    var retailerDataAddOns:String="            00010001"
    var bankName:String=""
    
    
    
    var clientIP:String=""
    var clientEmail:String=""
    var clientUserName:String=""
    var clientLocation:String=""
    var deviceModel:String=""

    var transactionStatus= TransactionStatus.ReceivedInWebService
    var transactionAmount: String = "" //FIELD 4

    val transactionAmountBd: BigDecimal= BigDecimal(0)

    var transactionCurrencyCode: String = ""
    var transactionCurrencyCodeNumber: Int = 484
    var transactionCurrencySymbol: String = ""
      

    var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""
    var responseCode:String=""
    var authorizationIdResponse:String=""
     var clientId:String="VIERGE" 


      var deviceType: DeviceType = DeviceType.POS 

    var hostType: HostType = HostType.Undefined
    var merchantType : MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var transactionMessageType= TransactionMessageType.Undefined
    var transactionDirection: TransactionDirection = TransactionDirection.Request
    var cardType: CardType = CardType.Undefined
    var paymentPlanType: PaymentPlanType = PaymentPlanType.Undefined
    var serverName:String=""

    var keySerialNumber:String=""

    var isoASCII:String =""
    var metaData:String =""
    var emvData:String =""
    var lastModificationDateTime= LocalDateTime.now()
    var creationDateTime: LocalDateTime = LocalDateTime.now()

    fun toData( data: TransactionDBData):TransactionDBData{

        data. transactionId=this.transactionId
        data. originalTransactionId=this.originalTransactionId
        data. transactionStatus=this.transactionStatus
        data. transactionAmount=this.transactionAmount
        data. rrn=this.rrn
        data. terminalSerialNumber=this.terminalSerialNumber
        data. responseCode=this.responseCode
        data. authorizationIdResponse=this.authorizationIdResponse
        data. hostType=this.hostType
        data. merchantType =this.merchantType
        data. transactionType =this.transactionType
        data. pointOfServiceEntryMode =this.pointOfServiceEntryMode
        data. transactionMessageType =this.transactionMessageType
        data. transactionDirection=this.transactionDirection
        data. cardType=this.cardType
        data. paymentPlanType=this.paymentPlanType
        data. keySerialNumber=this.keySerialNumber
        data. serverName=this.serverName
        data. clientIP=this.clientIP
        data. clientEmail=this.clientEmail
        data. clientUserName=this.clientUserName

        data. lastModificationDateTime=this.lastModificationDateTime
        data. creationDateTime=this.creationDateTime
        return data
    }

    fun toDBDataToEncrypt(): TransactionDBData {
        var data=TransactionDBData()
        data=toData(data)
        data.injectRequestISOASCIIStr(this.isoASCII)
         data.injectMetaDataStr(this.metaData)
        data.injectEmvDataStr(this.emvData)
        return data
    }


    fun toDBDataToDecrypt(): TransactionDBData {
        var data=TransactionDBData()
        data=toData(data)
        data.isoASCIIRequest=StringUtils.hexStringToByteArray(this.isoASCII)
        data.metaData =StringUtils.hexStringToByteArray(this.metaData)
        data.emvData= StringUtils.hexStringToByteArray(this.emvData)
        return data
    }
    fun setData(data:TransactionDBData){
        this. transactionId=data.transactionId
        this. originalTransactionId=data.originalTransactionId
        this. transactionStatus=data.transactionStatus
        this. transactionAmount=data.transactionAmount
        this. rrn=data.rrn
        this. terminalSerialNumber=data.terminalSerialNumber
        this. responseCode=data.responseCode
        this. authorizationIdResponse=data.authorizationIdResponse
        this. hostType=data.hostType
        this. merchantType =data.merchantType
        this. transactionType =data.transactionType
        this. pointOfServiceEntryMode =data.pointOfServiceEntryMode
        this. transactionMessageType =data.transactionMessageType
        this. transactionDirection=data.transactionDirection
        this. cardType=data.cardType
        this. serverName=data.serverName
        this. clientIP=data.clientIP
        this. clientEmail=data.clientEmail
        this. clientUserName=data.clientUserName


        this. paymentPlanType=data.paymentPlanType
        this. keySerialNumber=data.keySerialNumber
        this. lastModificationDateTime=data.lastModificationDateTime
        this. creationDateTime=data.creationDateTime
    }
    fun setDataToResponseEncrypt(data:TransactionDBData) {

      this.isoASCII=StringUtils.encodeHexString(data.isoASCIIRequest)
      this. metaData=StringUtils.encodeHexString(data.metaData)
      this.emvData=StringUtils.encodeHexString(data.emvData)

    }
    fun setDataToResponseDecrypt(data:TransactionDBData) {

        this.isoASCII=data.readISOASCIIStr()
        this. metaData=data.readMetaDataStr()
        this.emvData=data.readEmvDataStr()

    }



}