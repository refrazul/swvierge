package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método getTransactionData
class TransactionDataListResponse : ResponseEntity() {
    var data: List<TransactionDBData> = emptyList()
}
