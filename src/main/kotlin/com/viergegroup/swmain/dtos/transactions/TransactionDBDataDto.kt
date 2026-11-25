package com.viergegroup.swmain.dtos.transactions

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal

class TransactionDBDataDto : RequestEntity() {

    var errorCode: ErrorCode = ErrorCode.Undefined
    var errorCodeNumber: Int = -1
    var errorMessage:String=""
 
    var timeZone:String=""

    var operationId=OperationId.Undefined
  
    var originalTransactionId:Long=-1
    var clientLocation:String=""

    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined

    var transactionMessageType= TransactionMessageType.Undefined 

    var transactionDirection: TransactionDirection = TransactionDirection.Request

    var cardType: CardType = CardType.Undefined


    var cardBin:String=""
    var cardNumber:String=""

    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String=""
    var merchantAddress:String=""
    var bankName:String=""
    
    var deviceType: DeviceType = DeviceType.POS 
     var clientId:String="VIERGE"

    var paymentPlanType: PaymentPlanType = PaymentPlanType.Undefined
    var serverName:String=""
   
    var transactionStatus: TransactionStatus = TransactionStatus.ReceivedInWebService
    var transactionAmount: String = "" // FIELD 4
    val transactionAmountBd: BigDecimal= BigDecimal(0)
    var transactionCurrencyCode: String = ""
    var transactionCurrencyCodeNumber: Int = 484
    var transactionCurrencySymbol: String = ""
 

    var responseCode: String = ""
    var authorizationIdResponse: String = "" 
    var keySerialNumber: String = ""
    var isoASCIIRequest: String = ""
    var isoASCIIResponse: String = ""
    var systemTraceAuditNumber:String=""


    var ramTotal: BigDecimal=BigDecimal(0)
    var ramUsed: BigDecimal=BigDecimal(0)
    var ramUsagePercentage: BigDecimal=BigDecimal(0)
    var storageTotal: BigDecimal=BigDecimal(0)
    var storageUsed: BigDecimal=BigDecimal(0)
    var storageUsagePercentage: BigDecimal=BigDecimal(0)
    var networkUsagePercentage: BigDecimal=BigDecimal(0)
    var cpuUsagePercentage: BigDecimal=BigDecimal(0)

    var operationDuration:Float=0F


    var metaData: String = ""
    var emvData: String = ""
    var dbKcv: String = ""
    var lastModificationDateTime:String=""
    var creationDateTime: String=""
   var uriFileLogs= listOf<String>()
   var filePaths= mutableListOf<String>()

}
