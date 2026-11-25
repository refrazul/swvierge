package com.viergegroup.swmain.controllers

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.builder.IsoHostBuilder
import com.viergegroup.isoprocessor.parser.IsoHostParser
import com.viergegroup.swmain.dtos.iso.DescribeISOAsciiResponse
import com.viergegroup.swmain.dtos.iso.DescribeISORequestDto
import com.viergegroup.swmain.dtos.iso.Iso8583MetaDto
import com.viergegroup.swmain.dtos.iso.ToIsoAsciiResponseDto
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse
import com.viergegroup.swmain.dtos.transactions.TransactionRequestMinDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets


@RestController
@RequestMapping("/iso")
class IsoController {
    private lateinit var  watcher: Watcher


    @PostMapping("to-transactiondata")
    fun toTransactionData(@RequestBody requestDto: DescribeISORequestDto): SendTransactionResponse {
        val response= Iso8583MetaDto()
        watcher=WatcherContext.getInstance()
        try {
            val bytes= StringUtils.strictStringToBytes(requestDto.isoASCII, StandardCharsets.US_ASCII)
            val metaData= IsoHostParser.parseIso(bytes,requestDto.hostType,watcher)
            BeanUtils.copyProperties(metaData,response)
            if(metaData.transactionData!=null){
                response.isoDetails=metaData.transactionData!!.describe()
            }
            if(metaData.errorCode!=0){
                //  response.logDetails=watcher.log.logContent
            }
        }catch (we: WebServiceException){
            response.errorCode=we.errorCode
            response.errorMessage=we.errorMessage
        }catch (e:Exception){
            if(e.message!=null)
                response.errorMessage=e.message!!
            response.errorCode=-1
        }
        val transactionDto= TransactionDto()
        if(response.transactionData!=null)
            BeanUtils.copyProperties(response.transactionData!!,transactionDto)
        transactionDto.errorCode=response.errorCode
        transactionDto.errorMessage=response.errorMessage



        val responseFinal=SendTransactionResponse()
        StringUtils.copyNonEmptyProperties(transactionDto,responseFinal)
        StringUtils.copyNonEmptyProperties(transactionDto,responseFinal.transactionResponse)
        return responseFinal

    }


    @PostMapping("describe-iso-ascii")
    fun describeIsoAscii(@RequestBody requestDto: DescribeISORequestDto): DescribeISOAsciiResponse {
        var response= Iso8583MetaDto()
        val finalRes=DescribeISOAsciiResponse()
        val watcher=WatcherContext.getInstance()
        try {
            val bytes= StringUtils.strictStringToBytes(requestDto.isoASCII, StandardCharsets.US_ASCII)
            val metaData= IsoHostParser.parseIso(bytes,requestDto.hostType,watcher)
            BeanUtils.copyProperties(metaData,response)
            if(metaData.transactionData!=null){
                response.isoDetails=metaData.transactionData!!.describe()
            }
            if(metaData.errorCode!=0){
                //response.logDetails=logger.logContent
            }
        }catch (we:WebServiceException){
            response.errorCode=we.errorCode
            response.errorMessage=we.errorMessage
        }catch (e:Exception){
            if(e.message!=null)
                response.errorMessage=e.message!!
            response.errorCode=-1
        }
        var message=""
        if(response.errorMessage != ""){
            message+=response.errorMessage
        }
        if(response.transactionData!=null){
            message+=response.transactionData!!.describe()
        }
        finalRes.errorMessage=response.errorMessage
        finalRes.errorCode=response.errorCode
        finalRes.data=message

        return finalRes
    }

    @PostMapping("to-iso-ascii")
    fun toIsoASCII(@RequestBody dtoOrg: TransactionRequestMinDto): ToIsoAsciiResponseDto {
        val response= Iso8583MetaDto()
        val dto= TransactionDto()
        try {
            StringUtils.copyNonEmptyProperties(dtoOrg,dto)
            val  metaData= IsoHostBuilder.buildIso(dto.transactionType,dto)
            BeanUtils.copyProperties(metaData,response)
            if(metaData.transactionData!=null){
                response.isoDetails=metaData.transactionData!!.describe()
            }
            if(metaData.errorCode!=0){
                //     response.logDetails=watcher.log.logContent
            }
        }catch (we:WebServiceException){
            response.errorCode=we.errorCode
            response.errorMessage=we.errorMessage


        }catch (e:Exception){
            if(e.message!=null)
                response.errorMessage=e.message!!
            response.errorCode=ErrorCode.UnHandledException.errorCodeInt

        }

        val realResponse= ToIsoAsciiResponseDto()
        BeanUtils.copyProperties(response,realResponse)

        return realResponse
    }




}