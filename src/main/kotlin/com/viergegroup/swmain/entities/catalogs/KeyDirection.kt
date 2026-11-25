package com.viergegroup.swmain.entities.catalogs

import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

enum class KeyDirection(var keyDirectionId:Int,var keyDirectionName:String){
    Undefined(-1,"Undefined"), 
    Outbound(1,"Outbound"),
    Inbound(2,"Inbound"),
    Both(3,"Both")
    ;

}