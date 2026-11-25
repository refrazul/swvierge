package com.viergegroup.swmain.dbmodels
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsMetaDataDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal 

@Entity
@Table(name = "system_stats")
 class SystemStatsData{
    constructor() 
    
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  //  @GeneratedValue(strategy = GenerationType.AUTO) // Cambiar a AUTO para SQLite
    @Column(unique = true, nullable = false)
    var systemStatsId: Long = 0
 
     var ramTotal: Long=0L 

    var ramUsed: Long=0L

    var ramUsagePercentage: BigDecimal=BigDecimal(0)

    var storageTotal: Long=0L

    var storageUsed: Long=0L

    var storageUsagePercentage: BigDecimal=BigDecimal(0)

    var networkUsagePercentage: BigDecimal=BigDecimal(0)

    var cpuUsagePercentage: BigDecimal=BigDecimal(0)

    var transactionsNumber: Long=0L

    var connectionCount: Int=0
    
    var timestamp: LocalDateTime = LocalDateTime.now()
}
