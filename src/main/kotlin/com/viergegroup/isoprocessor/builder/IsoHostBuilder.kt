//@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.viergegroup.isoprocessor.builder

import com.shared.commons.*

import org.springframework.beans.BeanUtils

import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData

import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.catalogs.MerchantType
import com.viergegroup.swmain.entities.catalogs.PointOfServiceEntryMode
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.WatcherContext
import java.lang.Exception
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

object IsoHostBuilder { 
    //@Autowired
   // lateinit   var transactionsDBService:TransactionsDBService


 fun buildIso(transactionType: TransactionType, transactionDto: TransactionDto): Iso8583MetaData {
     val watcher= WatcherContext.getInstance()

     watcher.debug("\n\n=============IsoHostBuilder buildIso INIT============= \n\n")
        val rrn=transactionDto.rrn
        val terminalSerialNumber=transactionDto.terminalSerialNumber
        val transactionType=transactionDto.transactionType
        val uuid=transactionDto.uuid

        val response=Iso8583MetaData()
       val transactionData= TransactionData()

        watcher.debug("PROCESSING transactionType ["+transactionType+"] ")

        BeanUtils.copyProperties(transactionDto,transactionData)
        if(transactionDto.transactionType==TransactionType.Undefined) {
            transactionData.transactionType = transactionType
        }else{
            transactionData.transactionType = transactionDto.transactionType


        }
          if(transactionData.merchantType!= MerchantType.Undefined) {
             transactionData.transactionTypeCode = transactionData.merchantType.merchantTypeCode
         }

     if(transactionDto.pointOfServiceEntryMode== PointOfServiceEntryMode.Undefined){
         if(transactionDto.pointOfServiceEntryModeCode == ""){
             if(transactionDto.pointOfServiceEntryMode!= PointOfServiceEntryMode.Undefined) {
                 transactionData.pointOfServiceEntryMode = transactionDto.pointOfServiceEntryMode
                 transactionData.pointOfServiceEntryModeCode = transactionDto.pointOfServiceEntryMode.pointOfServiceEntryModeId

             } 
         }else{ 
             val pem = PointOfServiceEntryMode.fromPointServiceEntryModeId(  transactionData.pointOfServiceEntryModeCode)
             transactionData.pointOfServiceEntryMode=pem

         }
     }
     //   println("JSON REQUEST: "+transactionData.describeJSON())
    watcher.debug("ISO_REQUEST_INITIAL :" + response.getDetails())
    watcher.debug("IsoHostBuilder.buildIso transactionData.hostType :" +transactionData.hostType)
      var byteArray= byteArrayOf()
     response.transactionData=transactionData

    watcher.debug("JSON_REQUEST_DATA: \n ${transactionData.describeJSON()}")
     if(transactionData.processingCode == "") {
         transactionData.processingCode = transactionType.processingCode
        watcher.debug("PROCESSING CODE ADDED : \n ${transactionData.processingCode}")

     }

     try {
        watcher.debug("START PROCESSING Iso8583Builder.buildIso8583 ")
          byteArray= Iso8583Builder.buildIso8583(transactionData)
         response.setIsoByteArray(byteArray)


        watcher.saveIsoRaw(response)
        watcher.debug("AFTER PROCESSING IsoHostBuilder.buildIso transactionData.hostType :" +transactionData.hostType)

        watcher.debug("ISO_REQUEST_AFTER_PROCESSING :" + response.getDetails())
     }catch (ex :WebServiceException){
        val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
            watcher.debug(  " updateIsoRequestByUuid EXCEPTION ${err}")
 
       AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR," rrn [${rrn}] uuid [${uuid}] terminalSerialNumber [${terminalSerialNumber}]   transactionType [${transactionType}] IsoHostBuilder WebServiceException EXCEPTION :" +err)
       WatcherGlobalLog.isoError(" rrn [${rrn}] uuid [${uuid}] terminalSerialNumber [${terminalSerialNumber}]   transactionType [${transactionType}] IsoHostBuilder WebServiceException EXCEPTION :" +err)

         response.setWebServiceException(ex) 
        watcher.debug("IsoHostBuilder buildiso EXCEPTION :" +ex.errorMessage)
 
     }catch (e:Exception){ 

          val err = "${e.javaClass.name}: ${e.localizedMessage ?: "sin mensaje"}\n${e.stackTraceToString()}"
            watcher.debug(  " buildiso EXCEPTION ${err}")
           AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR,"rrn [${rrn}] uuid [${uuid}] terminalSerialNumber [${terminalSerialNumber}]   transactionType [${transactionType}]  IsoHostBuilder WebServiceException EXCEPTION :" +err)
          WatcherGlobalLog.isoError("rrn [${rrn}] uuid [${uuid}] terminalSerialNumber [${terminalSerialNumber}]   transactionType [${transactionType}]  IsoHostBuilder WebServiceException EXCEPTION :" +err)

           response.setException(e)  
         watcher.debug("IsoHostBuilder EXCEPTION :" +e.message)
   
     }
     response.transactionData=transactionData 


    watcher.debug("\n\n============= IsoHostBuilder buildIso END============= \n\n")



        return response

    }







}