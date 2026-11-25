package com.viergegroup.swmain.websecurity.bodyencryptor

import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.OperationId

class BodyEncryptResponse {
    var uuid=""

    var operationIdRequest:OperationId= OperationId.Undefined
    var isEncrypted=false
    var rawRequest=""
    var responseEntity=ResponseEntity()


}