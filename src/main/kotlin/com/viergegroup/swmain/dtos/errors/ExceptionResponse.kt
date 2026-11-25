package com.viergegroup.swmain.dtos.errors

import com.shared.commons.AppSettings
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType

class ExceptionResponse : ResponseEntity() {

    var apiPath:String=""

  //  var  rrn:String=""
    var hostType= HostType.Undefined
    //var  commLogFilePath:String=""
    var  terminalSerialNumber:String=""
    var serverName:String= AppSettings.getInstance().serverName
}