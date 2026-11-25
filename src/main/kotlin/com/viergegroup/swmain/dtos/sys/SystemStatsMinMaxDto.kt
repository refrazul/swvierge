
package com.viergegroup.swmain.dtos.sys
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsMetaDataDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal  

data class SystemStatsMinMaxDto(
    var metricName: String,
    var value: BigDecimal,
    var type: String, // "MIN" o "MAX" 
    var eventDate: LocalDateTime,
    var totalTransactions: Long
)
