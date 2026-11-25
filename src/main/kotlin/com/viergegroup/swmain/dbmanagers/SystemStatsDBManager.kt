package com.viergegroup.swmain.dbmanagers

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.decodeHex
import com.viergegroup.swmain.dbmodels.SystemStatsData
import com.viergegroup.swmain.dbservices.ClientActivityDBService 
import com.viergegroup.swmain.dbservices.SystemStatsDBService
import com.viergegroup.swmain.dbservices.TransactionDataService
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.dtos.transactions.MerchantDataDto
import java.time.LocalDateTime
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
 
internal object   SystemStatsDBManager { 
    private lateinit var systemStatsDBService: SystemStatsDBService
    fun init( systemStatsDBService: SystemStatsDBService){
        this.systemStatsDBService=systemStatsDBService
    }   

    
    fun insertSystemStats(stats: SystemStatsData): Long? {

        return systemStatsDBService.insertSystemStats(stats)
    }  

    fun getStatsMinMax(startDate: LocalDateTime, endDate: LocalDateTime): List<SystemStatsMinMaxDto> {
        return systemStatsDBService.getStatsMinMax(startDate, endDate)
    }
   
    fun getStatsPaginated(offset: Int, limit: Int): List<SystemStatsData> {
        return systemStatsDBService.getStatsPaginated(offset, limit)
    }

    fun getTotalPages(limit: Int): Int {
      
        return systemStatsDBService.getTotalPages(limit)
    }

}