package com.viergegroup.swmain.controllers

import com.viergegroup.swmain.dbservices.SystemStatsDBService
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import com.viergegroup.swmain.dtos.sys.SystemStatsRequestDto
import com.viergegroup.swmain.dtos.sys.SystemStatsResponseDto
import com.viergegroup.swmain.dtos.sys.SystemStatsInsertRequestDto


import org.springframework.web.bind.annotation.*
import com.shared.commons.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.BeanUtils
import com.viergegroup.swmain.dbmodels.SystemStatsData
import org.springframework.context.annotation.Profile

import com.viergegroup.swmain.dtos.sys.SystemStatsDataDto
@RestController

@RequestMapping("/sys-stats")  

class SystemStatsController { 
 
    @Autowired
    lateinit var systemStatsDBService: SystemStatsDBService 

    @PostMapping("/insert")
    fun insertSystemStats(@RequestBody requestDto: SystemStatsInsertRequestDto): SystemStatsResponseDto  {
        val data=SystemStatsData() 
        BeanUtils.copyProperties(requestDto,data)
        val response=SystemStatsResponseDto()
        response.id=systemStatsDBService.insertSystemStats(data)
        return response 
    } 
   
    @PostMapping("/min-max")  
    fun getStatsMinMax(@RequestBody requestDto: SystemStatsRequestDto): List<SystemStatsMinMaxDto> { 
        return systemStatsDBService.getStatsMinMax(requestDto.startDate, requestDto.endDate)
    }

    @PostMapping("/list")
    fun getStatsPaginated(@RequestBody requestDto: SystemStatsRequestDto): SystemStatsResponseDto {
        val stats = systemStatsDBService.getStatsPaginated(requestDto.startRow, requestDto.totalRows)
        val response=SystemStatsResponseDto() 
        for(datae in stats){ 
            val dto=SystemStatsDataDto()
            StringUtils.copyNonEmptyProperties(datae,dto)
            response.list.add(dto)
        }
        return response
    }
  
    @PostMapping("/pages")
    fun getTotalPages(@RequestBody requestDto: SystemStatsRequestDto): SystemStatsResponseDto {
        val totalPages = systemStatsDBService.getTotalPages(requestDto.totalRows)
        val response=SystemStatsResponseDto()
        response.totalPages=totalPages
        return response
    }
 

}



 