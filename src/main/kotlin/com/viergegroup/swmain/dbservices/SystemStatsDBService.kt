package com.viergegroup.swmain.dbservices

import com.viergegroup.swmain.dbmodels.SystemStatsData
import com.viergegroup.swmain.dbrepositories.SystemStatsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import java.math.BigDecimal  
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
 
@Service
class SystemStatsDBService { 

    @Autowired 
    lateinit var systemStatsRepository: SystemStatsRepository
 
    @Transactional 
    fun insertSystemStats(stats: SystemStatsData): Long? {
        try {
            WatcherGlobalLog.debug("insertSystemStats - Entrada: ramTotal=${stats.ramTotal}, ramUsed=${stats.ramUsed}, ramUsagePercentage=${stats.ramUsagePercentage}, " +
                    "storageTotal=${stats.storageTotal}, storageUsed=${stats.storageUsed}, storageUsagePercentage=${stats.storageUsagePercentage}, " +
                    "networkUsagePercentage=${stats.networkUsagePercentage}, cpuUsagePercentage=${stats.cpuUsagePercentage}")
            
            val result = systemStatsRepository.insertSystemStats(
                stats.ramTotal,
                stats.ramUsed,     
                stats.ramUsagePercentage, 
                stats.storageTotal, 
                stats.storageUsed,
                stats.storageUsagePercentage, 
                stats.networkUsagePercentage,
                stats.cpuUsagePercentage
            )
            
            WatcherGlobalLog.debug("insertSystemStats - Salida: id=$result")
            return result
        } catch (e: Exception) {
            WatcherGlobalLog.error("insertSystemStats - Error: ${e.message}")
            return null
        }
    }  

    @Transactional
    fun getStatsMinMax(startDate: LocalDateTime, endDate: LocalDateTime): List<SystemStatsMinMaxDto> {
        try {
            WatcherGlobalLog.debug("getStatsMinMax - Entrada: startDate=$startDate, endDate=$endDate")
            
            val results = systemStatsRepository.getStatsMinMax(startDate, endDate).map { row ->
                SystemStatsMinMaxDto(
                    metricName = row[0] as String,
                    value = row[1] as BigDecimal, 
                    type = row[2] as String,
                    eventDate = row[3] as LocalDateTime,
                    totalTransactions = (row[4] as Number).toLong()
                )
            }
            
            WatcherGlobalLog.debug("getStatsMinMax - Salida: totalResultados=${results.size}")
            return results
        } catch (e: Exception) {
            WatcherGlobalLog.error("getStatsMinMax - Error: ${e.message}")
            return emptyList()
        }
    }
      
    @Transactional
    fun getStatsPaginated(offset: Int, limit: Int): List<SystemStatsData> {
        try {
            WatcherGlobalLog.debug("getStatsPaginated - Entrada: offset=$offset, limit=$limit")
            
            val results = systemStatsRepository.getStatsPaginated(offset, limit)
            
            WatcherGlobalLog.debug("getStatsPaginated - Salida: totalResultados=${results.size}")
            return results
        } catch (e: Exception) {
            WatcherGlobalLog.error("getStatsPaginated - Error: ${e.message}")
            return emptyList()
        }
    }
 
    @Transactional
    fun getTotalPages(limit: Int): Int {
        try {
            WatcherGlobalLog.debug("getTotalPages - Entrada: limit=$limit")
            val totalPages = systemStatsRepository.getTotalPages(limit)

            WatcherGlobalLog.debug("getTotalPages - Salida: totalPages=$totalPages")
            return totalPages
        } catch (e: Exception) {
            WatcherGlobalLog.error("getTotalPages - Error: ${e.message}")
            return 0
        }
    }
}
