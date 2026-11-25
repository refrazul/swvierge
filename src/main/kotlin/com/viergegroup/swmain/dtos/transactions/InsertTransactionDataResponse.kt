package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método insertTransactionData
class InsertTransactionDataResponse : ResponseEntity() {
    var id: Long? = null
}