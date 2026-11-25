package com.viergegroup.swmain.controllers

import com.shared.commons.StringUtils
import com.viergegroup.swmain.dbmodels.ClientActivityDBData
import com.viergegroup.swmain.dbservices.ClientActivityDBService
import com.viergegroup.swmain.dtos.clientactivity.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController

@RequestMapping("/db-client-activity")
class DBClientActivityController {

    @Autowired
    lateinit var clientActivityDataService: ClientActivityDBService

    @PostMapping("/list")
    fun getClientActivityDataList(@RequestBody dto: ClientActivityFilterDto): ClientActivityDataListResponse {
        val dataList = clientActivityDataService.getClientActivityDataList(dto)
        val response = ClientActivityDataListResponse()
        val dtoList= mutableListOf<ClientActivityDBDataDto>()
        for(data in dataList){
            val dto=ClientActivityDBDataDto()
            StringUtils.copyNonEmptyProperties(data,dto)
            dto. lastModificationDateTime= StringUtils.datetime2String(data.lastModificationDateTime)
            dto. creationDateTime= StringUtils.datetime2String(data.creationDateTime)

            dtoList.add(dto)
        }
        response.data = dtoList
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }

    @PostMapping("/pages")
    fun getTotalClientActivityPages(@RequestBody dto: ClientActivityFilterDto): TotalClientActivityPagesResponse {
        val totalPages = clientActivityDataService.getTotalClientActivityPages(dto)
        val response = TotalClientActivityPagesResponse()
        response.totalPages = totalPages
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }

    @PostMapping("/upsert")
    fun upsertClientActivityData(@RequestBody data: ClientActivityDBData): UpsertClientActivityDataResponse {
        val id = clientActivityDataService.upsertClientActivityData(data)
        val response = UpsertClientActivityDataResponse()
        response.id = id
        response.errorCode = 0
        response.errorMessage = "Success"
        return response
    }
}
