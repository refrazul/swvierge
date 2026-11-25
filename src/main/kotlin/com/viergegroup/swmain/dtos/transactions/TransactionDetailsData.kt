package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.entities.ResponseEntity

class TransactionDetailsData : ResponseEntity() {
    var data: TransactionDBData?=null
}