package com.viergegroup.swmain.dtos.iso

import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.transactions.TransactionData

class Iso8583MetaDto: ResponseEntity()  {
    var logDetails=""
    var isoDetails=""
    var isoHex=""
    var isoAscii=""
    var transactionData: TransactionData?=null
}