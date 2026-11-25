package com.viergegroup.swmain.dtos.sys
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsMetaDataDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal 
import com.viergegroup.swmain.entities.RequestEntity

 class SystemStatsInsertRequestDto:RequestEntity() {

    val ramTotal: Long=0L

    val ramUsed: Long=0L

    val ramUsagePercentage: BigDecimal=BigDecimal(0)

    val storageTotal: Long=0L

    val storageUsed: Long=0L

    val storageUsagePercentage: BigDecimal=BigDecimal(0)

    val networkUsagePercentage: BigDecimal=BigDecimal(0)

    val cpuUsagePercentage: BigDecimal=BigDecimal(0)

 
}
