package com.viergegroup.swmain.controllers



import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.gateway.*
import com.viergegroup.swmain.dtos.transactions.SearchTransactionResponse
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse

import com.viergegroup.swmain.managers.WebAPIManager


import com.viergegroup.prodhelper.GatewayResponseWrapperPassTrue
import com.viergegroup.prodhelper.SwViergeProdApi
import com.viergegroup.prodhelper.TransactionFullResponseDtoPassTrue

import  com.shared.commons.GsonConfig

import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.viergegroup.swmain.entities.catalogs.TransactionType
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import com.viergegroup.swmain.entities.catalogs.Authorizer


@RestController
class GatewayRootAliasController(
    private val gatewayController: GatewayController
) {

    @PostMapping(
        value = [ "/transaction", "/",""]
    ) 
    fun transactionRoot(
       // @RequestBody dto: GatewayTransactionDto,
        request: HttpServletRequest
    ): String {
        return "Ok"
    } 
}
