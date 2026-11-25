package com.viergegroup.swmain.dtos.gateway

import com.shared.commons.GsonConfig
import com.shared.commons.StringUtils

import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.entities.catalogs.GatewayTransactionTye
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

/*
*
* ,"instanceId":"764568cb-4c02-4501-a89e-f8ffafbf6a1b"
,"merchantId":"1fd50a7b-4eed-4bf4-ba9a-13a2ac353987"
,"retrievalReferenceNumber":"123456789001"
,"emvRequestPayload":"4f07a0000000031010500b564953412043524544495457114761739001010010d221220111438444895f201656495341204352454449542f43415244484f4c4445525f2a0205545f340101820220008407a0000000031010950500000000009a032302289c01009f02060000000201009f0607a00000000310109f100706010a03a000009f1a0209869f21031703519f2608f2644a0afa9f45359f2701809f360205b19f37043bedcaaa9f3901079f660426c04000c00108c10103"

,"pin":{"pinBlock":"2E79CC4A8CE6C5507598FEB61847FF96",
"keyIdentifier":"84848F194856160100000000",
"keyType":"DUKPT-AES"},"meta":""
,"gatewayId":"180a67fb-dcc3-4f45-afbe-f2c636333a70"
*
* */
class GatewayRequestDto{
    var instanceRequest= GatewayTransactionInstanceDto()
    var transactionType= GatewayTransactionTye.undefined
 

}
class GatewayTransactionInstanceDto{

    var id:String=""
    var instanceId:String="" 
    var merchantId:String="" 
    var emvRequestPayload:String="" 
    var hostType: HostType = HostType.TestingDUKPTHost
    var pin: GatewayPinDto?=null
    var retrievalReferenceNumber:String=""
    var gatewayId=""
    var meta=""
    var metaData: TransactionTerminalDto?=null
}

open class GatewayTransactionDto {
 
   /* val GatewayId: String = "" 
    val InstanceId: String = "" 
    val MerchantId: String = ""  
    val Oid: String = ""
    val Request: GatewayRequestDto = GatewayRequestDto()
    val RequestId: String = ""
    val Status: String = ""
    val TransactionId: String = ""*/
    var instanceRequest= GatewayTransactionInstanceDto()
    var transactionType= GatewayTransactionTye.undefined

  

    fun getTransactionTerminalDto(): TransactionTerminalDto {
        var dto= TransactionTerminalDto()
        val instanceRequest=instanceRequest

        try{
            if(instanceRequest.meta!="") {
                val res = StringUtils.decodeBase64<TransactionTerminalDto>(instanceRequest.meta)
                if(res!=null){
                    dto=res
                }
            }

        }catch (e:Exception){
            val msg="ERROR DECODING META  ${e.message}"
            WatcherGlobalLog.error(msg)
            throw Exception(msg)

        }
        dto.rrn=instanceRequest.retrievalReferenceNumber
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
     
        dto.emvData=instanceRequest.emvRequestPayload
        if(transactionType==GatewayTransactionTye.purchase&&dto.transactionType==TransactionType.Undefined)
             dto.transactionType= TransactionType.Sale
        WatcherGlobalLog.debug("RRN :  ${dto.rrn}")
        dto.emvData=instanceRequest.emvRequestPayload
        WatcherGlobalLog.debug("META DECODED:  ${GsonConfig.gsonLogs.toJson(dto)}")
        instanceRequest.metaData=dto
        return dto
    }
}