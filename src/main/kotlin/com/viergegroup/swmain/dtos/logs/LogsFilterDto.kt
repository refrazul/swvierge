package com.viergegroup.swmain.dtos.logs
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.ModuleId
import com.viergegroup.swmain.entities.catalogs.OperationId
import java.time.LocalDate
import java.time.LocalDateTime

class LogsFilterDto: RequestEntity(){
    var moduleId: ModuleId = ModuleId.Undefined
    var operationId: OperationId = OperationId.Undefined
  //  val hostTypeFilter=HostType.Undefined
    var terminalSerialNumberFilter=""
    var errorCode: String = ""   
    var serverName:String=""
     var startDate: LocalDateTime = LocalDateTime.of(1993, 11, 1, 0, 0)
     var endDate: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59)
    var startRow: Int = 0
    var totalRows: Int = 15  
     var rrnFilter=""
     var operatioId:String=""
     var clientPass:String=""
     var requestHash:String=""

 }
