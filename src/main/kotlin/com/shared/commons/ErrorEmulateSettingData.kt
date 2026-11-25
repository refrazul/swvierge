package com.shared.commons

import com.google.gson.GsonBuilder
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.LogLevel
import com.viergegroup.swmain.entities.catalogs.TransactionType

class ErrorEmulateSettingData {
    var transactionType:TransactionType=TransactionType.Sale
    var responseCodes= listOf<String>() 
    var isActive=false

} 