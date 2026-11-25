package com.viergegroup.swmain.entities

import com.viergegroup.swmain.entities.catalogs.*
import javax.persistence.EnumType
import javax.persistence.Enumerated

open class RequestEntity { 
    var uuid:String=""  
    var serverNameRequest:String=""
    var hostType: HostType = HostType.Undefined
    var bank: Bank = Bank.Undefined
    var moduleIdRequest: ModuleId = ModuleId.Undefined
    var operationIdRequest: OperationId = OperationId.Undefined
    var merchantType : MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var clientIP:String=""
    var clientEmail:String=""
    var clientUserName:String="" 
    var clientGeolocation:String=""
    var deviceModel:String=""
    var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""

}