package com.viergegroup.swmain.entities.catalogs

import com.shared.commons.AppSettings
import com.viergegroup.crypto.security.PasswordUtil

enum class Admin (var userId:Int,var userName:String,var password:String,var grants:MutableList<com.viergegroup.swmain.entities.catalogs.OperationId>){
    Yiva(0,"yiva","TFg3NzAxWT1sUlNoRlNaV05UZkpBRVNFRFdWSkw2N0lydThMWTc3SllEY2VlYjhWSz1EWDc3MDE=", mutableListOf<com.viergegroup.swmain.entities.catalogs.OperationId>(
        com.viergegroup.swmain.entities.catalogs.OperationId.All
    )),
    ;

}