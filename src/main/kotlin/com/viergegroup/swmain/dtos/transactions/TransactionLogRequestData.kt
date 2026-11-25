package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.catalogs.HostType

class TransactionLogRequestData {
    var hostType= HostType.Undefined
    var terminalSerialNumber:String=""
    var rrn:String=""
    var creationDate:String=""

}