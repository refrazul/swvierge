package com.viergegroup.swmain.dbmanagers

import com.viergegroup.swmain.dbservices.ClientActivityDBService
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.webwatcher.Watcher

object ClientActivityDBManager  {
    private lateinit var clientActivityDBService: ClientActivityDBService
    fun init( clientActivityDBService: ClientActivityDBService){
        this.clientActivityDBService=clientActivityDBService

    }


}