package com.viergegroup.swmain.dtos.pandora

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType


class DbFilter : RequestEntity(){

    var transactionId:Long=0L

    var authorizationIdResponse:String=""


}