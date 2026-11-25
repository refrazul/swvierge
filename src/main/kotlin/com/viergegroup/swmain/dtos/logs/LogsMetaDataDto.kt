package com.viergegroup.swmain.dtos.logs

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.ModuleId
import com.viergegroup.swmain.entities.catalogs.OperationId
import java.time.LocalDateTime

class LogsMetaDataDto : RequestEntity() {
    var errorCode: String = ""
    var errorMessage: String = ""
    var creationDateTime: LocalDateTime = LocalDateTime.now()
    var lastModificationDateTime: LocalDateTime = LocalDateTime.now()
    var moduleId: ModuleId = ModuleId.Undefined
    var operationId: OperationId = OperationId.Undefined
}