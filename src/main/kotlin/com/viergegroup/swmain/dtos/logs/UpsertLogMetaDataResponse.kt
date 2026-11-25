package com.viergegroup.swmain.dtos.logs

import com.viergegroup.swmain.entities.ResponseEntity

// Clase de respuesta para el método upsertLogMetaData
class UpsertLogMetaDataResponse : ResponseEntity() {
    var id: Long? = null
}