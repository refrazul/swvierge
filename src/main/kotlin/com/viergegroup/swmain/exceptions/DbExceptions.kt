package com.viergegroup.swmain.exceptions

import com.shared.commons.AppSettings
import com.viergegroup.swmain.entities.catalogs.HostType

class DBKeyCRCFailed(e:String):Exception(e) {
    var  rrn:String=""
    var hostType= HostType.Undefined
    var  terminalSerialNumber:String=""
    var serverName:String=AppSettings.getInstance().serverName
}
class DBKeyDoesntExists(e:String):Exception(e) {
    var  rrn:String=""
    var hostType= HostType.Undefined
    var  terminalSerialNumber:String=""
    var serverName:String=AppSettings.getInstance().serverName
}
class ErrorReadingDBKey(val errorCode:Int=0,val errorMessage:String):Exception(errorMessage) {
    var  rrn:String=""
    var hostType= HostType.Undefined
    var  terminalSerialNumber:String=""
    var serverName:String=AppSettings.getInstance().serverName

}