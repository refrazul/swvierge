package com.viergegroup.swmain.dbrepositories

import com.viergegroup.swmain.dbmodels.SystemStatsData
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.math.BigDecimal 



@Repository
interface SystemStatsRepository : CrudRepository<SystemStatsData, Long> {

    @Procedure("insert_system_stats")   
    fun insertSystemStats(
        @Param("p_ram_total") ramTotal: Long,
        @Param("p_ram_used") ramUsed: Long,
        @Param("p_ram_usage_percentage") ramUsagePercentage: BigDecimal,
        @Param("p_storage_total") storageTotal: Long,
        @Param("p_storage_used") storageUsed: Long,
        @Param("p_storage_usage_percentage") storageUsagePercentage: BigDecimal,
        @Param("p_network_usage_percentage") networkUsagePercentage: BigDecimal,
        @Param("p_cpu_usage_percentage") cpuUsagePercentage: BigDecimal
    ): Long?
  

    @Transactional(readOnly = true)
    @Procedure(name = "get_system_stats_min_max")
    fun getStatsMinMax(
        @Param("p_creation_date_start") startDate: LocalDateTime,
        @Param("p_creation_date_end") endDate: LocalDateTime
    ): List<Array<Any>> 

     
    @Transactional(readOnly = true)
    @Procedure("get_system_stats_paginated")
    fun getStatsPaginated(@Param("offset") offset: Int, @Param("limit") limit: Int): List<SystemStatsData>

    
    @Transactional(readOnly = true)
    @Procedure("get_system_stats_total_pages")
    fun getTotalPages(@Param("limit") limit: Int): Int
}
