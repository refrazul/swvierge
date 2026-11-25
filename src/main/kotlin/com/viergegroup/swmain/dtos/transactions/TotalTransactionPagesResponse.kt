package com.viergegroup.swmain.dtos.transactions

import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método getTotalTransactionPages
class TotalTransactionPagesResponse : ResponseEntity() {
    var totalPages: Int = 0
}
