package com.viergegroup.swmain.dtos.logs

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import com.viergegroup.swmain.entities.catalogs.CardType
import java.math.BigDecimal

class LogsMetaDataDBDto {

    var uuid:String=""
 
    var errorCode: ErrorCode = ErrorCode.Undefined
    var errorCodeNumber: Int = -1
    var errorMessage:String=""

    var timeZone:String=""
  

    var serverName:String=""
    var hostType: HostType = HostType.Undefined
    var bank: Bank = Bank.Undefined
    var moduleId: ModuleId = ModuleId.Undefined
    var operationId: OperationId = OperationId.Undefined
    var merchantType : MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var cardType:CardType=CardType.Undefined
    var cardBin:String=""
    var cardNumber:String=""

    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String=""
    var merchantAddress:String=""
    var bankName:String=""
    



    var ramTotal: BigDecimal=BigDecimal(0)
    var ramUsed: BigDecimal=BigDecimal(0)
    var ramUsagePercentage: BigDecimal=BigDecimal(0)
    var storageTotal: BigDecimal=BigDecimal(0)
    var storageUsed: BigDecimal=BigDecimal(0)
    var storageUsagePercentage: BigDecimal=BigDecimal(0)
    var networkUsagePercentage: BigDecimal=BigDecimal(0)
    var cpuUsagePercentage: BigDecimal=BigDecimal(0)


    
    var clientIP:String=""
    var clientEmail:String=""
    var clientUserName:String=""
    var clientGeolocation:String=""
    var deviceModel:String=""
    var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""

    var responseCode:String=""
    var transactionAmount: String = "" //FIELD 4

    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var operationDuration:Float=0F
    var lastModificationDateTime:String=""
    var creationDateTime: String = ""
    var uriFileLogs= listOf<String>()
    var filePaths= mutableListOf<String>()

}