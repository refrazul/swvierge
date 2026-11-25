package com.viergegroup.swmain.controllers

import com.shared.commons.*
import com.viergegroup.swmain.dtos.comm.*
import com.viergegroup.swmain.managers.CommManager
import com.viergegroup.swmain.dtos.transactions.SendTransactionResponse


import com.viergegroup.swmain.entities.catalogs.Authorizer 

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
//mvnw.cmd spring-boot:run
//.\mvnw.cmd spring-boot:run
import  com.shared.commons.RebootService
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCauseData
import  com.viergegroup.evopayment.managers.EvoManager
  
@RestController
@RequestMapping("/comm")
class CommController {
 
 
    @PostMapping("send-alarm")
    fun sendAlarm(@RequestBody requestDto: AlarmCauseData): ConnectToHostResponseDto{
        val response=ConnectToHostResponseDto()
        try{
            AlarmEmailService.sendAlarmEmail(requestDto.alarmCause,requestDto.errorDescription)
            response.errorMessage="success"
            response.errorCode=0
        }catch(ex:Exception){
            response.errorMessage=ex.message.toString()

            response.errorCode=-1

        }
       
        return  response
    }  

    @PostMapping("restart-host")
    fun restartHost(@RequestBody requestDto: CommHostRequestDto): ConnectToHostResponseDto {
        CommManager().disconnectHost(requestDto)
        val response=ConnectToHostResponseDto()
        val response2=  CommManager().connectToHost(requestDto)
        StringUtils.copyNonEmptyProperties(response2,response)
        response.isConnected=response2.errorCode==0
        response.hostIp=AppSettings.getHostConfig(requestDto.hostType).hostIp
        response.hostPort=AppSettings.getHostConfig(requestDto.hostType).hostPort
        response.timeOut=AppSettings.getHostConfig(requestDto.hostType).hostConnectionTimeOut
        return response 
    } 


    @PostMapping("connect-to-host")
    fun connectToHost(@RequestBody requestDto: CommHostRequestDto): ConnectToHostResponseDto {


        val response=ConnectToHostResponseDto()
        val response2=  CommManager().connectToHost(requestDto)
        StringUtils.copyNonEmptyProperties(response2,response)
        response.isConnected=response2.errorCode==0
        response.hostIp=AppSettings.getHostConfig(requestDto.hostType).hostIp
        response.hostPort=AppSettings.getHostConfig(requestDto.hostType).hostPort
        response.timeOut=AppSettings.getHostConfig(requestDto.hostType).hostConnectionTimeOut

        return response
    }

    @PostMapping("disconnect-host")
    fun disconnectHost(@RequestBody requestDto: CommHostRequestDto): DisconnectHostResponseDto {
        return CommManager().disconnectHost(requestDto)

    }



   /* @PostMapping("hosts-list")
    fun getHostsList():List<AppHostSettingsData> {

        return CommManager().getHostsList()
    }*/



 


  /*  @PostMapping("host-status-list")
    fun hostListStatus(@RequestBody requestDto: CommHostRequestDto): HostStatusResponseDto {
        return CommManager().hostListStatus(requestDto)


    }*/

    @PostMapping("host-status")
    fun hostStatus(@RequestBody requestDto: CommHostRequestDto): HostStatusResponseDto {

        return CommManager().hostStatus(requestDto)


    }


   /* @PostMapping("host-send-message")
    fun sendMessage(@RequestBody dto: SendMessageHostRequestDto): Iso8583TransactionMetaDto {

        return CommManager().sendMessage(dto)

    }

*/

    @PostMapping("communication-test")
    fun communicationTest(@RequestBody requestDto: CommHostRequestDto): SendTransactionResponse {
        val response=SendTransactionResponse()
  

          val response2= CommManager().communicationTest(requestDto)
        StringUtils.copyNonEmptyProperties(response2,response)
        if(response2.transactionResponse!=null)
         StringUtils.copyNonEmptyProperties(response2.transactionResponse!!,response.transactionResponse)
             
        return response
    }
   
 
    @PostMapping("fun-keys")
    fun funKeys(@RequestBody requestDto: CommHostRequestDto): SendTransactionResponse {
        val response=SendTransactionResponse()
        val response2= CommManager().funKeys(requestDto)
        StringUtils.copyNonEmptyProperties(response2,response)
        if(response2.transactionResponse!=null)
         StringUtils.copyNonEmptyProperties(response2.transactionResponse!!,response.transactionResponse)

        return response
    } 


 /*   @PostMapping("read-comm-log")
    fun readCommLog(@RequestBody requestDto: CommHostRequestDto): String {
        return CommManager().readCommLog(requestDto)


    }

    @PostMapping("delete-comm-log")
    fun deleteCommLog(@RequestBody requestDto: CommHostRequestDto): String {
        return CommManager().deleteCommLog(requestDto)

    }*/


}