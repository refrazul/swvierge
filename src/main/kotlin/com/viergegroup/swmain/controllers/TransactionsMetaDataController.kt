package com.viergegroup.swmain.controllers

import com.viergegroup.swmain.dbservices.SystemStatsDBService
import com.viergegroup.swmain.dtos.sys.SystemStatsMinMaxDto
import com.viergegroup.swmain.dtos.sys.SystemStatsRequestDto
import com.viergegroup.swmain.dtos.sys.SystemStatsResponseDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsMetaReqDto

import com.viergegroup.swmain.dtos.txnmeta.TransactionsMetaResDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsDBMetaDto
import com.viergegroup.swmain.dtos.txnmeta.TransactionsJDBMetaDto

import org.springframework.web.bind.annotation.*
import com.shared.commons.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.BeanUtils
import com.viergegroup.swmain.dbmodels.SystemStatsData
import com.viergegroup.swmain.dtos.sys.SystemStatsDataDto
import org.springframework.context.annotation.Profile
import com.viergegroup.swmain.dbservices.TransactionsMetaDataDBService

@RestController

@RequestMapping("/transactions-md")  
class TransactionsMetaDataController { 
 
    @Autowired  
    lateinit var transactionsMetaDataDBService: TransactionsMetaDataDBService 
   
/*  @PostMapping("/today-report")
    fun getStatsPaginated(@RequestBody requestDto: TransactionsMetaReqDto): TransactionsMetaResDto {
        val stats = transactionsMetaDataDBService.getTodayTransactionReports(requestDto.reportType)
        val response=TransactionsMetaResDto()  
        for(datae in stats){  
      
            response.list.add(datae)  
        } 
        return response  
    } */     
   @PostMapping("/today-report")
    fun getStatsPaginated(@RequestBody requestDto: TransactionsMetaReqDto): TransactionsMetaResDto {
        val stats = transactionsMetaDataDBService.getTodayTransactionReports(requestDto.reportType,requestDto.hostType)
        val response=TransactionsMetaResDto()  
        response.data=TransactionsJDBMetaDto.toTransactionsJDBMetaDto(stats)
        response.errorCode=0

        return response   
    } 
        
 

}



 