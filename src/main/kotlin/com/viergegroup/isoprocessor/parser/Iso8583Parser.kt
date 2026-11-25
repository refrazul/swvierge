package com.viergegroup.isoprocessor.parser

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.builder.Iso8583Builder
import com.viergegroup.isoprocessor.models.descriptors.Iso8583DescriptorData
import com.viergegroup.isoprocessor.models.descriptors.Iso8583EmvDescriptor
import com.viergegroup.isoprocessor.models.iso.Iso8583Message
import com.viergegroup.isoprocessor.models.iso.Iso8583MessageType
import com.viergegroup.swmain.entities.catalogs.*

import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import org.springframework.beans.BeanUtils
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class Iso8583Parser(var hostType: HostType,var watcher: Watcher) {
    companion object {
        private var iso8583DescriptorData: Iso8583DescriptorData?=null
        private var lastModificationTime=0L
        private fun getIso8583DescriptorData(hostType: HostType, watcher: Watcher): Iso8583DescriptorData {
            //     println("getIso8583DescriptorData")
            return read8583DescriptorData(hostType,watcher)
        }
        fun getIsoDescriptorData(hostType: HostType, watcher: Watcher): Iso8583DescriptorData {
            if(iso8583DescriptorData ==null||descriptorsWasUpdated(hostType)){
                Iso8583Builder.resetExpressionMap()
                iso8583DescriptorData = getIso8583DescriptorData(hostType,watcher)
            } 
            val data= Iso8583DescriptorData()
            BeanUtils.copyProperties(iso8583DescriptorData!!,data)
            return data
        }
        fun getEmvDescriptor( ): Iso8583EmvDescriptor {

            var data= Iso8583EmvDescriptor()
            try{
                val emvTagDescriptorsFilePath=AppSettings.getInstance().emvTagDescriptorsFilePath

                val gson = Gson()

                val file = File(emvTagDescriptorsFilePath)
                var content=""
                if (file.exists()) {
                    val lines = Files.readAllLines(Paths.get(emvTagDescriptorsFilePath), StandardCharsets.UTF_8)
                    for (line in lines) {
                        content += line

                    }
                    data=  gson.fromJson(
                        content,
                        Iso8583EmvDescriptor::class.java
                    )
                }

            }catch (e:Exception){

            }

            return data
        }
        fun descriptorsWasUpdated( hostType: HostType):Boolean{

            val myObj = File(AppSettings.getAuthorizerConfig(hostType).isoDescriptorsPath)

            if (myObj.exists()) {
                return  myObj.lastModified()!= lastModificationTime
            }
            return false

        }


        fun readFileWithFallback(filePath: String, watcher: Watcher, primaryCharset: Charset = Charset.forName("UTF-8"), fallbackCharset: Charset = Charset.forName("ISO-8859-1")): String {
            var content = StringBuilder()
            try {
                Files.lines(Paths.get(filePath), primaryCharset).use { lines ->
                    lines.forEach { line ->
                        content.append(line).append("\n")
                    }
                }
            } catch (e: Exception) {
               watcher.error("MalformedInputException with $primaryCharset encoding, trying $fallbackCharset")
                content = StringBuilder()
                try {
                    Files.lines(Paths.get(filePath), fallbackCharset).use { lines ->
                        lines.forEach { line ->
                            content.append(line).append("\n")
                        }
                    }
                } catch (innerEx: Exception) {
                   watcher.error("Failed to read file with $fallbackCharset encoding: ${innerEx.message}")
                   // throw innerEx
                }
            } catch (e: IOException) {
               watcher.error("IOException while reading file: ${e.message}")
               // throw e
            }
            return content.toString()
        }
        fun read8583DescriptorData(hostType: HostType, watcher: Watcher): Iso8583DescriptorData {
            val filePath = AppSettings.getAuthorizerConfig(hostType).isoDescriptorsPath
            var iso8583DescriptorData = Iso8583DescriptorData()

           watcher.debug("Iso8583Parser read8583DescriptorData $filePath")
            val file = File(filePath)
           watcher.debug("Iso8583Parser read8583DescriptorData EXISTS ${file.exists()}")

            if (file.exists()) {
                lastModificationTime = file.lastModified()
                try {
                    val content = readFileWithFallback(filePath, watcher)
                   watcher.debug("Content readed")
                    try {
                        val gson = Gson()
                        iso8583DescriptorData = gson.fromJson(content, Iso8583DescriptorData::class.java)
                        iso8583DescriptorData.emvTagDescriptors = getEmvDescriptor()
                    } catch (e: JsonSyntaxException) {
                       watcher.error("JSON SYNTAX ERROR: ${e.message}")
                    }
                } catch (e: Exception) {
                   watcher.error("ERROR READING FILE ${filePath} EXCEPTION ${e.message}")
                }
            } else {
               watcher.error("File does not exist: $filePath")
            }

            return iso8583DescriptorData
        }
 
        fun parseIso8583(buffer:ByteArray, hostType: HostType, watcher: Watcher): TransactionData {
            var transactionData= TransactionData()

           watcher.debug("\n\n============= Iso8583Parser  parseIso8583 INIT ============= \n\n")

            //  println("parseIso8583")
            val bufferS=String(buffer)
           watcher.debug("Iso8583Parser")

            val isoMessage = Iso8583Parser(hostType,watcher)
                .setIso8583Buffer(bufferS)
                .parse()
            transactionData.injectIso8583(isoMessage)
            for(field in isoMessage.isoFields){
                try{
                    transactionData.setField(field.fieldVarName,field.fieldValueString)
                    if(field.fieldVarName=="emvData"){
                       watcher.debug("parseEmvData field.fieldVarName ${field.fieldVarName}")
                        val emvDescriptor=Iso8583Parser.getEmvDescriptor()
                        transactionData.emvTagsMap=emvDescriptor.parseEmvData(field.fieldValueString,watcher)
                        isoMessage.injectEmvTagsMap(transactionData.emvTagsMap)

                    }
                }catch (e: java.lang.Exception){
                  //  throw WebServiceException(ErrorCode.InvalidIso8583,"field ${field.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA",watcher)
                    watcher.error(ErrorCode.InvalidIso8583,"field ${field.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA")

                }
            }



            //SOLO PROSA
            if(isoMessage.hasField(63)&&hostType.authorizer== Authorizer.PROSA) {
               watcher.debug("INIT PARSE FIELD 63 ONLY PROSA")

                for (token in isoMessage.field63Data.tokens) {
                    for(tokenField in token.tokenFields){
                        try {
                            transactionData.setField(tokenField.fieldVarName, tokenField.fieldValueString)

                        }catch (e: java.lang.Exception){
                          // throw WebServiceException(ErrorCode.InvalidIso8583,"field ${tokenField.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA",watcher)
                            watcher.error(ErrorCode.InvalidIso8583,"field ${tokenField.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA")

                        }
                    } 
                }
               watcher.debug("END PARSE FIELD 63 ONLY PROSA") 

            }

            if(isoMessage.hasField(57)&&hostType.authorizer== Authorizer.PROSA) {
               watcher.debug("INIT PARSE FIELD 57 ONLY PROSA")

                for (token in isoMessage.field57Data.tokens) {
                    for(tokenField in token.tokenFields){
                        try {
                            transactionData.setField(tokenField.fieldVarName, tokenField.fieldValueString)

                        }catch (e: java.lang.Exception){
                          // throw WebServiceException(ErrorCode.InvalidIso8583,"field ${tokenField.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA",watcher)
                            watcher.error(ErrorCode.InvalidIso8583,"field ${tokenField.fieldVarName} DOES NOT EXIST IN TRANSACTIONDATA")

                        }
                    } 
                }
               watcher.debug("END PARSE FIELD 63 ONLY PROSA") 

            }


            transactionData.pointOfServiceEntryMode= PointOfServiceEntryMode.fromPointServiceEntryModeId(transactionData.pointOfServiceEntryModeCode)
            if(transactionData.pointOfServiceEntryMode==PointOfServiceEntryMode.Undefined){

                transactionData.pointOfServiceEntryMode= PointOfServiceEntryMode.fromEmvTagValue(transactionData.getValueTagEmv("9F39"))

            }
            transactionData= CardType.trySetCardType(transactionData,watcher)
            transactionData.hostType=hostType
            transactionData.merchantType= MerchantType.fromMerchantCode(transactionData.merchantTypeCode)
            transactionData.transactionType= TransactionType.fromTransactionFromRequest(transactionData)
           watcher.debug(" transactionData.getMessageTypeString() ${transactionData.getMessageTypeString()}")
            transactionData.transactionDirection= TransactionDirection.fromMessageType(transactionData.getMessageTypeString())
            if( transactionData.transactionType==TransactionType.LogOn|| transactionData.transactionType==TransactionType.EchoTest)
                transactionData.merchantType=MerchantType.Comm
            if(    transactionData.pointOfServiceEntryMode== PointOfServiceEntryMode.KeyLoad)
                transactionData.transactionType= TransactionType.KeyLoad

           watcher.debug("\n\n=============  parseIso8583 END ============= \n\n")





            return transactionData

        }







    }




    private var iso8583Message: Iso8583Message = Iso8583Message(hostType,TransactionType.Undefined,watcher)
        get() {
            return field
        }
        set(value) {
            field = value
        }

    fun setIso8583Buffer(buffer:String): Iso8583Parser {
        // println("setIso8583Buffer")

        iso8583Message.isoMessageBuffer=buffer.trim()

        return this

    }








    fun parse( ): Iso8583Message {
        //   println("Iso8583Parser.build")
       watcher.debug("\n\n=============  Iso8583Parser parse INIT ============= \n\n")
        if(iso8583Message.isoMessageBuffer.equals("")){
           watcher.debug("buildByListFields")
            buildByListFields()
        }else{
           watcher.debug("parseByIsoBuffer")
            parseByIsoBuffer()
        }
       watcher.debug("\n\n=============  parse END ============= \n\n")

        return iso8583Message
    }

    private fun buildByListFields(){
        //   println("buildByListFields")



    }
    private   fun parseByIsoBuffer( ){
       watcher.debug("\n\n=============  Iso8583Parser parseByIsoBuffer INIT ============= \n\n")


        var isoMessageBuffer=iso8583Message.isoMessageBuffer
        // println("buildByIsoBuffer \"${isoMessageBuffer}\"")
       watcher.debug("buildByIsoBuffer \"${isoMessageBuffer}\"")
        val descriptor=getIsoDescriptorData(hostType,watcher)
       watcher.debug("iso8583DescriptorData.isoHeader: ${descriptor.getIsoHeader()}, len: ${descriptor.getHeaderLen()}")

        //isoMessageBuffer=isoMessageBuffer.replace(iso8583DescriptorData.isoHeader,"")
        iso8583Message.isoHeader=isoMessageBuffer.substring(0,descriptor.getHeaderLen())

        isoMessageBuffer=isoMessageBuffer.substring(descriptor.getHeaderLen(),isoMessageBuffer.length)

        //   println("iso8583DescriptorData.isoHeader ${getIsoDescriptorData().isoHeader}")

        // println("buildByIsoBuffer \"${isoMessageBuffer}\"")
       watcher.debug("buildByIsoBuffer \"${isoMessageBuffer}\"")

        val messageType=isoMessageBuffer.substring(0,4)

        //iso8583Message.isoHeader=iso8583DescriptorData.isoHeader

        //  println("messageType $messageType")
       watcher.debug("messageType $messageType")

        isoMessageBuffer=isoMessageBuffer.substring(4,isoMessageBuffer.length)

        // println("isoMessageBuffer $isoMessageBuffer")
       watcher.debug("isoMessageBuffer $isoMessageBuffer")

        val messageTypeEnum= Iso8583MessageType.fromMessageTypeString(messageType)
        iso8583Message.isoMessageType = messageTypeEnum
       watcher.debug(" iso8583Message.isoMessageType ${ iso8583Message.isoMessageType}")

        val primaryBitMap=    isoMessageBuffer.substring(0,16)
      //  println("primaryBitMap $primaryBitMap")
       watcher.debug("primaryBitMap $primaryBitMap")

        isoMessageBuffer= isoMessageBuffer.substring(16,isoMessageBuffer.length)

        iso8583Message.injectPrimaryBitMap(primaryBitMap)

        if(iso8583Message.hasField(1)){
            val secondaryBitmap=    isoMessageBuffer.substring(0,16)
           // println("secondaryBitmap $secondaryBitmap")
           watcher.debug("secondaryBitmap $secondaryBitmap")

            iso8583Message.injectSecondaryBitMap(secondaryBitmap)

            isoMessageBuffer= isoMessageBuffer.substring(16,isoMessageBuffer.length)

            //  println("isoMessageBuffer $isoMessageBuffer")

        }


        val isoMessageContentFields=isoMessageBuffer
        iso8583Message.isoMessageContentFields=isoMessageContentFields

       // println("isoMessageContentFields $isoMessageContentFields")
       // println("getPresentFields ${iso8583Message.getPresentFields()}")
       watcher.debug("isoMessageContentFields $isoMessageContentFields")
       watcher.debug("getPresentFields ${iso8583Message.getPresentFields()}")

        var bufferPos=0
        for(fieldNumber in 0..128 ){

            if(iso8583Message.hasField(fieldNumber)){
                val fieldDescriptor = getIsoDescriptorData(hostType,watcher).fieldDescriptors.firstOrNull{ it.fieldNumber == fieldNumber }
 
                if(fieldDescriptor==null&&fieldNumber!=1){
                  //  throw WebServiceException(ErrorCode.InvalidIso8583,"WE DONT HAVE A GUIDE FOR FIELD $fieldNumber",watcher)
                    watcher.error("WE DONT HAVE A GUIDE FOR FIELD $fieldNumber")

                }
                if(fieldDescriptor!=null) {
                    // val fieldDescriptor: FieldDescriptor = matchingObject.orElse(null)


                    // println("///////////// processing Field START  ${fieldDescriptor.fieldNumber}\n\n")
                   watcher.debug("///////////// processing Field START  ${fieldDescriptor.fieldNumber}")

                    // println("Field Info:   ${fieldDescriptor.getFieldInfo()}")
                   watcher.debug("Field Info:   ${fieldDescriptor.getFieldInfo()}")

                    //  println("bufferPos:   ${bufferPos}")
                   watcher.debug("bufferPos:   ${bufferPos}")

                    var fieldLength: Int
                    var fieldValue: String

                    if(fieldDescriptor.lengthIsVariable||fieldDescriptor.fieldLengthIndicator>0){
                        val bufferPosEnd= bufferPos+fieldDescriptor.fieldLengthIndicator
                        //  println("bufferPosEnd 1:   ${bufferPosEnd}")
                       watcher.debug("bufferPosEnd 1:   ${bufferPosEnd}")

                        if(bufferPosEnd>=isoMessageBuffer.length){
                          //  throw WebServiceException(ErrorCode.InvalidIso8583,"Insufficent Data In ",fieldDescriptor,watcher)
                            watcher.error(ErrorCode.InvalidIso8583,"Insufficent Data In ",fieldDescriptor)
   
                        }  

                        val fieldLengthString=isoMessageContentFields.substring(bufferPos,bufferPosEnd)
                        // println("fieldLengthString Variable:   ${fieldLengthString}")
                       watcher.debug("fieldLengthString VARIABLE OR INDICATOR >0:   ${fieldLengthString}")
 
                        fieldLength=fieldLengthString.toInt()
                    //    if(!fieldDescriptor.lengthIsVariable&&fieldLength)

                        //   bufferPos+=fieldDescriptor.fieldLengthIndicator
                    }else{
                        fieldLength=fieldDescriptor.fieldLength

                    }

                    //  println("fieldLength:   ${fieldLength}")
                   watcher.debug("fieldLength:   ${fieldLength}")

                    if(bufferPos>=isoMessageBuffer.length){
                        bufferPos=isoMessageBuffer.length-1
                       watcher.debug("bufferPos_2:   ${bufferPos}")

                    }
                    if(fieldDescriptor.lengthIsVariable||fieldDescriptor.fieldLengthIndicator>0) {
                        var bufferPosEnd= bufferPos + fieldLength+fieldDescriptor.fieldLengthIndicator

                        // IF()
                        if(bufferPosEnd>isoMessageBuffer.length){
                            bufferPosEnd=isoMessageBuffer.length
                            //throw WebServiceException(ErrorCode.InvalidIso8583,"Insufficent Data In ",fieldDescriptor)
                        }

                       watcher.debug("bufferPosEnd 2:   ${bufferPosEnd}  LENGTH ${isoMessageContentFields.length}")
                        //bufferPos+=fieldDescriptor.fieldLengthIndicator
                        fieldValue = isoMessageContentFields.substring(bufferPos,bufferPosEnd)
                        fieldValue=fieldValue.substring(fieldDescriptor.fieldLengthIndicator,fieldValue.length)
                        if(!fieldDescriptor.lengthIsVariable&&fieldLength>(fieldValue.length)){
 
                          //  throw WebServiceException(ErrorCode.InvalidIso8583,"EXPECTED LEN IS ${fieldValue.length} CURRENT LEN IS ${fieldLength}",fieldDescriptor,watcher)
                            watcher.error(ErrorCode.InvalidIso8583,"EXPECTED LEN IS ${fieldValue.length} CURRENT LEN IS ${fieldLength}",fieldDescriptor)

                        }
                       watcher.debug(" after remove len fieldValue :   ${fieldValue}  INDICATOR: ${fieldDescriptor.fieldLengthIndicator}  LENGTH ${fieldValue.length}")

                    }else{
                        val bufferPosEnd= bufferPos + fieldLength
                       watcher.debug("bufferPosEnd 3 :   ${bufferPosEnd} LENGTH ${isoMessageContentFields.length}")

                        if(bufferPosEnd>isoMessageBuffer.length){
                         //   throw WebServiceException(ErrorCode.InvalidIso8583,"Insufficent Data In",fieldDescriptor,watcher)
                            watcher.error(ErrorCode.InvalidIso8583,"Insufficent Data In",fieldDescriptor)

                        } 
 
                        fieldValue = isoMessageContentFields.substring(bufferPos,bufferPosEnd)

                    }


                   watcher.debug("fieldValue:   [\"${fieldValue}\"]  ${fieldDescriptor.getFieldInfo()} \n")

                    iso8583Message.setField(fieldNumber,fieldValue,fieldDescriptor)

                    if(fieldDescriptor.lengthIsVariable||fieldDescriptor.fieldLengthIndicator>0) {
                        bufferPos += fieldLength+fieldDescriptor.fieldLengthIndicator
                    }else{
                        bufferPos += fieldLength

                    }

                   watcher.debug(" ///////////// Iso8583Parser processing Field END ${fieldDescriptor.getFieldInfo()} NUMBER:[ ${fieldDescriptor.fieldNumber}]\n\n")



                }

            }

        }
       if(iso8583Message.hasField(63)){
            watcher.debug("iso8583Message.hasField(63)) [${iso8583Message.hasField(63)}]")
            iso8583Message.field63Data= Field63Parser(hostType,watcher)
                .init(iso8583Message.getFieldValue(63), getIsoDescriptorData(hostType,watcher).field63Descriptor)
                .build()

        }  
        if(iso8583Message.hasField(57)){
            watcher.debug("iso8583Message.hasField(57)) [${iso8583Message.hasField(57)}]")
            iso8583Message.field57Data= Field57Parser(hostType,watcher)
                .init(iso8583Message.getFieldValue(57), getIsoDescriptorData(hostType,watcher).field57Descriptor)
                .build()

        }  
       watcher.debug("\n\n============= Iso8583Parser buildByIsoBuffer END ============= \n\n")


        /* val matchingObject = iso8583DescriptorData.fieldDescriptors.stream()
             .filter { p: FieldDescriptor -> p.fieldNumber == fieldNumber }.findFirst()*/




    }

}