package com.viergegroup.swmain.controllers

import com.shared.commons.StringUtils
import com.viergegroup.swmain.managers.WebAPIManager
import com.viergegroup.swmain.dtos.transactions.SearchTransactionResponse
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.shared.commons.ErrorCode

//mvnw.cmd spring-boot:run

//.\mvnw.cmd spring-boot:run
 
 
@RestController
@RequestMapping("transactions")
class TransactionsController {

  /* @PostMapping("/search-txn") //
   fun searchTxn(@RequestBody dto: TransactionTerminalDto): SearchTransactionResponse {

       val response=SearchTransactionResponse()
       val response2=WebAPIManager().searchTxn(dto)
       StringUtils.copyNonEmptyProperties(response2,response)
       if(response2.transactionDto!=null)
           BeanUtils.copyProperties(response2.transactionDto!!,response.transactionDto)
       return response
   }*/
  
    @PostMapping("/send-transaction")
    fun sendTransaction(@RequestBody dto: TransactionTerminalDto): SendTransactionResponse {
        val response=SendTransactionResponse()
        val response2=WebAPIManager().sendTransaction(dto)
        StringUtils.copyNonEmptyProperties(response2,response)
        StringUtils.copyNonEmptyProperties(response2.transactionResponse,response.transactionResponse)
        if(response.transactionResponse.responseCode==""){
            response.errorCodeEnum=ErrorCode.fromId(response.errorCode)
            response.transactionResponse.responseCode=  response.errorCodeEnum.responseCode
        }
        val responseCodeAuthorizer=response.transactionResponse.responseCode

        if(!responseCodeAuthorizer.contains("F")&&!responseCodeAuthorizer.contains("H")
            &&!responseCodeAuthorizer.contains("D")
            &&!responseCodeAuthorizer.contains("E") &&!responseCodeAuthorizer.contains("X")&&response.transactionResponse.responseCodeAuthorizer=="")
            response.transactionResponse.responseCodeAuthorizer=responseCodeAuthorizer


        return response
    }

}