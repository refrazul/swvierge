
package com.viergegroup.isoprocessor.parser

import com.shared.commons.WebServiceException

import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.StringUtils
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.transactions.TransactionData
import java.lang.Exception
import java.nio.charset.StandardCharsets
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

object IsoHostParser {




    fun parseIso(isoAscii: String, hostType: HostType, watcher: Watcher): Iso8583MetaData {
        var response= Iso8583MetaData()
       watcher.debug("\n\n=============IsoHostParser parseIso from ascii INIT============= \n\n")

        try {

            val bytes = StringUtils.strictStringToBytes(isoAscii, StandardCharsets.US_ASCII)
            response= parseIso(bytes,hostType,watcher)

        }catch (ex:WebServiceException){
            val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
             watcher.debug(  " parseIso ${err}")
            AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR,"IsoHostParser isoAscii[${isoAscii}]  EXCEPTION :" +err)
             WatcherGlobalLog.isoError("IsoHostParser isoAscii[${isoAscii}]  EXCEPTION :" +err)

            response.setWebServiceException(ex)
        }catch (ex: Exception){
            
            val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
            watcher.debug(  " parseIso ${err}")
              AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR,"IsoHostParser isoAscii[${isoAscii}] EXCEPTION :" +err)
              WatcherGlobalLog.isoError("IsoHostParser isoAscii[${isoAscii}] EXCEPTION :" +err)

            response.setExceptionParsing(ex)
        }
       watcher.debug("\n\n=============IsoHostParser parseIso from ascii END============= \n\n")
 
        return response

    }
    fun parseIso(isoByteArrayOrig: ByteArray, hostType: HostType, watcher: Watcher): Iso8583MetaData {
       watcher.debug("\n\n=============IsoHostParser parseIso INIT============= \n\n")
        val response= Iso8583MetaData()
        val isoByteArray=StringUtils.cleanAsciiBytes(isoByteArrayOrig)
        response.setIsoByteArray(isoByteArray)
          val isoAscii=StringUtils.asciiFromByteArray(isoByteArray)
        var transactionData=TransactionData()  
        try{ 

             transactionData =
                Iso8583Parser. parseIso8583(isoByteArray,hostType, watcher)

        }catch (ex:WebServiceException){
           // response.setWebServiceException(ex)
            response.errorMessage="CHECK_THIS_ERROR WebServiceException!!!=>parseIso  EXCEPTION [${ex.message}]"
           watcher.error("parseIso  EXCEPTION [${ex.message}]")
               val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
             watcher.debug(  " parseIso ${err}")
 
            AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR,"IsoHostParser isoAscii[${isoAscii}] EXCEPTION :" +err)
              WatcherGlobalLog.isoError("IsoHostParser isoAscii[${isoAscii}] EXCEPTION :" +err)

        }catch (ex: Exception){

              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"
                watcher.debug(  " parseIso ${err}")
               AlarmEmailService.sendAlarmEmail(AlarmCause.ISO_ERROR,"IsoHostParser isoAscii[${isoAscii}]  EXCEPTION :" +err)
                             WatcherGlobalLog.isoError("IsoHostParser isoAscii[${isoAscii}] EXCEPTION :" +err)

           // response.setException(ex) 
            response.errorMessage="CHECK_THIS_ERROR Exception!!!=> parseIso  EXCEPTION [${ex.message}]"
            watcher.error("parseIso  EXCEPTION [${ex.message}]")
        }


        watcher.debug("\n\n=============IsoHostParser parseIso END============= \n\n")
        response.setResponseCode(transactionData.responseCode) 
        response.transactionData=transactionData
        watcher.debug("transactionData.responseCode  :" + transactionData.responseCode)
        watcher.saveIsoRaw(response)
        watcher.debug("ISO_RESPONSE :" + response.getDetails())

        return response
    }



}