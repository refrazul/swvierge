package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método getTransactionsList
class TransactionsListResponse : ResponseEntity() {
    var data: List<TransactionDBDataDto> = emptyList()
}  