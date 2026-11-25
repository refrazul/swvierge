package com.viergegroup.swmain.dtos.sys
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsMetaDataDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal 


 class SystemStatsDataDto {
    var id: Long = 0 

    var ramTotal: Long= 0

    var ramUsed: Long= 0

    var ramUsagePercentage: BigDecimal=BigDecimal(0)

    var storageTotal: Long= 0

    var storageUsed: Long= 0

    var storageUsagePercentage: BigDecimal=BigDecimal(0)

    var networkUsagePercentage: BigDecimal=BigDecimal(0)

    var cpuUsagePercentage: BigDecimal=BigDecimal(0)

    var transactionsNumber: Long= 0

    var connectionCount: Int= 0

    var timestamp: LocalDateTime = LocalDateTime.now()
}
