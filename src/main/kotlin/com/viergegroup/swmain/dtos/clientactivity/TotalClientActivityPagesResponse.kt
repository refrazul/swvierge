package com.viergegroup.swmain.dtos.clientactivity

import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método getTotalClientActivityPages
class TotalClientActivityPagesResponse : ResponseEntity() {
    var totalPages: Int = 0
}