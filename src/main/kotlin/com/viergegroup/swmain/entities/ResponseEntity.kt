package com.viergegroup.swmain.entities

import com.shared.commons.ErrorCode
import com.viergegroup.swmain.entities.catalogs.OperationId

open class ResponseEntity {
    var uuid: String=""
    var errorCode=-1
    var errorCodeEnum=ErrorCode.Undefined
    var errorMessage=""
    var rrn="" 
    var operationIdRequest: OperationId = OperationId.Undefined
    var startTime = System.currentTimeMillis()
    var operationDuration:Float=0F  
  //  var requestElapsedTime:Float=0.0F
}    