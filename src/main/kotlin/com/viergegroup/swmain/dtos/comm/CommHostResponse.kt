package com.viergegroup.swmain.dtos.comm

import com.viergegroup.swmain.entities.ResponseEntity

class CommHostResponse : ResponseEntity(){

    var settlementDate:String=""
    var transmisionDateAndTime:String=""
    var responseCode:String=""
    var networkManagmentInformationCode:String=""
    var systemTraceAuditNumber:String=""


}