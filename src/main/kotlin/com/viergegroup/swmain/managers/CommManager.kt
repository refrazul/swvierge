package com.viergegroup.swmain.managers

import com.shared.commons.*
import com.viergegroup.isoprocessor.models.iso.Iso8583TransactionMetaData
import com.viergegroup.swmain.dtos.comm.CommHostRequestDto
import com.viergegroup.swmain.dtos.comm.DisconnectHostResponseDto
import com.viergegroup.swmain.dtos.comm.HostStatusResponseDto
import com.viergegroup.swmain.dtos.comm.SendMessageHostRequestDto
import com.viergegroup.swmain.dtos.iso.Iso8583TransactionMetaDto
import com.viergegroup.swmain.dtos.transactions.TransactionResponse
import com.viergegroup.swmain.webwatcher.WatcherContext
import org.springframework.beans.BeanUtils
import com.viergegroup.swmain.entities.catalogs.Authorizer
import  com.viergegroup.evopayment.managers.EvoManager

internal class CommManager {
 
    private  val  watcher= WatcherContext.getInstance()  

    fun connectToHost(requestDto: CommHostRequestDto): Iso8583TransactionMetaData {
       // logger= Logger.getLogger(requestDto.hostType,requestDto.terminalSerialNumber,requestDto.rrn)
      //  watcher.debugObj(requestDto)
        val response= HostMasterManager.connectToHost(requestDto.hostType,watcher)
       // watcher.debugObj(response)
        closeLogs()
        return response
    }

    fun closeLogs(){
       watcher.close()
    }



    fun getHostsList():List<AppHostSettingsData> {
        return AppSettings.getHostsList()
    }

    fun disconnectHost(requestDto: CommHostRequestDto): DisconnectHostResponseDto {
        val response= HostMasterManager.disconnectHost(requestDto.hostType,watcher)
        closeLogs()
        return response
    }


    fun hostListStatus(requestDto: CommHostRequestDto): HostStatusResponseDto {
        val hostList= AppSettings.getHostsList()
        val response= HostMasterManager.hostStatus(requestDto.hostType,watcher)
        closeLogs()
        return response

    }

    fun hostStatus(requestDto: CommHostRequestDto): HostStatusResponseDto {
        val response= HostMasterManager.hostStatus(requestDto.hostType,watcher)
        closeLogs()

        return response

    }



    fun sendMessage(dto: SendMessageHostRequestDto): Iso8583TransactionMetaDto {
        val response= Iso8583TransactionMetaDto()
         
        val responsedata= HostMasterManager.sendIsoAndWaitForResponse(dto.hostType,dto.isoASCII,watcher)
       watcher.close()
        response.errorCode=responsedata.errorCode
        response.errorMessage=responsedata.errorMessage
        BeanUtils.copyProperties(responsedata.iso8583MetaDataRequest,response.iso8583MetaDataRequest)
        BeanUtils.copyProperties(responsedata.iso8583MetaDataResponse,response.iso8583MetaDataResponse)
        if(responsedata.iso8583MetaDataRequest.transactionData!=null){
            response.iso8583MetaDataRequest.isoDetails=responsedata.iso8583MetaDataRequest.transactionData!!.describe()
        }
        if(responsedata.iso8583MetaDataResponse.transactionData!=null){
            response.iso8583MetaDataResponse.isoDetails=responsedata.iso8583MetaDataResponse.transactionData!!.describe()
        }


        return response

    }

 

    fun communicationTest(requestDto: CommHostRequestDto): TransactionResponse {
        var response= TransactionResponse(null)

        if(requestDto.hostType.authorizer==Authorizer.EVOPAYMENT){
            val evoResponse= EvoManager().keepAlive(requestDto)
            
            return evoResponse  
        }
        try { 
            response= HostMasterManager.sendEchoAndWaitForResponse(requestDto.hostType,watcher)
        }catch ( webException: WebServiceException){
            response.errorCode=webException.errorCode
            response.errorMessage=webException.errorMessage
 
        }  
        closeLogs() 
 
        return response
    } 



    fun funKeys(requestDto: CommHostRequestDto): TransactionResponse {
        var response= TransactionResponse(null)
        try {
            response= HostMasterManager.sendFunKeysAndWaitForResponse(requestDto.transactionType,requestDto.keyDirection,requestDto.hostType,watcher)
        }catch ( webException: WebServiceException){
            response.errorCode=webException.errorCode 
            response.errorMessage=webException.errorMessage

        }
        closeLogs()

        return response
    } 

    


    fun readCommLog(requestDto: CommHostRequestDto): String {

        val content= "" //watcher.readCommLog(requestDto.hostType,requestDto.transactionMessageType,requestDto.creationDate2LocalDate())
       watcher.close()
        return content

    }


    fun deleteCommLog(requestDto: CommHostRequestDto): String {

        val content="" //watcher.deleteCommLog(requestDto.hostType,requestDto.transactionMessageType,requestDto.creationDate2LocalDate())
       watcher.close()
        return content

    }



}