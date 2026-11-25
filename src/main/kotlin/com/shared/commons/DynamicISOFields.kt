package com.shared.commons

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.viergegroup.isoprocessor.builder.Iso8583Builder
import com.viergegroup.isoprocessor.commons.IsoCommons
import com.viergegroup.isoprocessor.models.descriptors.PaddingType
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.swmain.dbmodels.TransactionDBData

import com.viergegroup.swmain.dtos.comm.CommHostRequestDto
import com.viergegroup.swmain.dtos.transactions.OriginalTransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.viergegroup.swmain.dtosgsonadapters.LocalDateTimeGSONAdapter
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


object DynamicISOFields {

    private var fieldsAreBeingSaving=false
    private var fieldsAreBeingReading=false
    fun formatDate(hostType: HostType, pattern: String, date:LocalDateTime): String {

        return  AppSettings.getHostConfig(hostType).getCurrentDateTime(date,pattern)

    }

    fun formatDate(hostType: HostType, pattern: String, date:Date): String {

        return  AppSettings.getHostConfig(hostType).getCurrentDateTime(date,pattern)

    }

    fun formatDate(hostType: HostType,pattern: String): String {

        return  AppSettings.getHostConfig(hostType).getCurrentDateTime(pattern)
    }
        // Constantes para CVM
        val CVM_01 = byteArrayOf(0x1E,0x00,0x00)
        val CVM_02 = byteArrayOf(0x1F,0x00,0x00)
        val CVM_03 = byteArrayOf(0x3F,0x00,0x00)
    
    fun defaultTag9F34(ctq9F6C: ByteArray): ByteArray {
        val tempDataOut = byteArrayOf(0, 0, 0)
        
        if (ctq9F6C == null) {
            System.arraycopy(CVM_03, 0, tempDataOut, 0, 3)
            return tempDataOut
        }
        
        val tem = (ctq9F6C[0].toInt() and 0b01000000)

        if (tem == 0b01000000) {
            System.arraycopy(CVM_01, 0, tempDataOut, 0, 3)
        } else {
            System.arraycopy(CVM_02, 0, tempDataOut, 0, 3)
        }
        return tempDataOut
    }



    fun checkEmvData(dto: TransactionDto): TransactionDto {
        val watcher= WatcherContext.getInstance()
        watcher.debug("\n\n PUT PADDINGS IN CHECKEMVDATA  INI")


        val isoDescriptor=   Iso8583Parser.getIsoDescriptorData(dto.hostType,watcher)
       watcher.debug("isoDescriptor.mustParseEmvData ${isoDescriptor.mustParseEmvData}")
       watcher.debug("checkEmvData_NEO")
       val containsAsteriscs=dto.emvData.contains("*")
       val maskPCI=  AppSettings.getInstance().maskPCI

        if(!maskPCI)
            watcher.debug("dto_emvData ${dto.emvData} containsAsteriscs ${containsAsteriscs}")
   
        if(dto.emvData!=""&&!containsAsteriscs){  
            try{
                val field63Descriptor=   isoDescriptor.field63Descriptor
                val emvDescriptor=Iso8583Parser.getEmvDescriptor()
                dto.emvTagsMap=emvDescriptor.parseEmvData(dto.emvData,watcher )
                val emvMap=  dto.emvTagsMap
                val hasChip=emvMap.containsKey("9F26")
               watcher.debug("hasChip $hasChip")
                if(emvMap.containsKey("57")&&emvMap["57"]!=""&& dto.track2 ==""){
                    dto.track2 = emvMap["57"]!!
                    dto.track2 = emvMap["57"]!!
                   watcher.debug(" TRACK2 WAS SETTED !! ")

                } else if(dto.track2 !=""){
                    emvMap["57"]=dto.track2
                    watcher.debug(" TAG57  WAS SETTED !! ")

                } 


                watcher.debug(" BEFORE dto_track2Length  [${dto.track2Length}] ")

                if(emvMap.containsKey("57")){
                    watcher.debug(" AFTER emvMap[57]  [${ StringUtils.maskString(emvMap["57"]!!)}] ")
                    dto.track2Length = emvMap["57"]!!.length.toString().padStart(2,'0')
                    watcher.debug("  dto_track2Length 01  [${dto.track2Length}] ")
                    dto.track2Length = emvMap["57"]!!.replace("F","").length.toString().padStart(2,'0')
                   watcher.debug("  dto_track2Length 02 [${dto.track2Length}] ")
                }  else if(dto.track2 !=""){
                    watcher.debug(" AFTER dto_track2  [${ StringUtils.maskString(dto.track2)}] ")
                    watcher.debug(" AFTER to_track2_length  [${ dto.track2.length}] ")
                    dto.track2Length = dto.track2.length.toString().padStart(2,'0')
                    watcher.debug("  dto_track2Length 01  [${dto.track2Length}] ")
                    dto.track2Length = dto.track2.replace("F","").length.toString().padStart(2,'0')
                   watcher.debug("  dto_track2Length 02 [${dto.track2Length}] ")
                }


                watcher.debug(" AFTER dto_track2Length  [${dto.track2Length}] ")


                if(emvMap.containsKey("9F02")&&emvMap["9F02"]!=""&&(dto.transactionAmount==""
                            ||dto.transactionAmount=="000000000000"
                            )
                ){
                   watcher.debug("emvMap emvMap.containsKey(\"9F02\")")
                    dto.transactionAmount=emvMap["9F02"]!!.trim().padStart(12,'0')

                }


                

                  if(dto.transactionType==TransactionType.ClosePosTip
                  ||dto.transactionType==TransactionType.Cancellation
                   ||dto.transactionType==TransactionType.SalePosTip 
                  ||dto.transactionType==TransactionType.Refund ) //#LACPI
                {
                   watcher.debug("LACPI dto.transactionAmount ${dto.transactionAmount}")
                   watcher.debug("LACPI dto.transactionType ${dto.transactionType}")
                  emvMap["9F02"]=  dto.transactionAmount
                   watcher.debug("LACPI 9f02 ${emvMap["9F02"]}")
    
                }  
   
                watcher.debug(" checkEmvData_cardType  ${dto.cardType } 9F6C is present ${emvMap["9F6C"]!=null}")
                
                if (dto.cardType == CardType.VISA||emvMap["9F6C"]!=null) {
                    try{
                        watcher.debug("9F6C  ${emvMap["9F6C"]!!}") 
                        val str9f6c =emvMap["9F6C"]!!
                        watcher.debug("9F6C $str9f6c")
                        val bytes9f6c =StringUtils.hexStringToByteArrayNeo(str9f6c)
                        val bytes9f34 = defaultTag9F34(bytes9f6c)
                      
                        var str9f34 = StringUtils.encodeHexString(bytes9f34)
                        str9f34 = str9f34.uppercase(Locale.US)
                        emvMap["9F34"]=str9f34
                        watcher.debug("str9f34 $str9f34")
                    }catch(ex:Exception){
                        watcher.error(ex.message.toString())
                    }
                   
                }



                if(emvMap.containsKey("5A")&&emvMap["5A"]!=""&& dto.cardNumber ==""){
                    if(emvMap["5A"]!!.length>=15) {
                       watcher.debug("emvMap emvMa)p.containsKey(\"5A\" ${emvMap["5A"]!!}")
                        dto.cardNumber = emvMap["5A"]!!
                    }
                }
                if(emvMap.containsKey("5F24")&&emvMap["5F24"]!=""&&  dto.cardExpirationDate==""){
                   watcher.debug("emvMap emvMap.containsKey(\"5F24\") ${emvMap["5F24"]!!}")
                    dto.cardExpirationDate=emvMap["5F24"]!!

                }

                // ---------------- UNPREDICTABLE NUMBER (9F37) ----------------
                emvMap["9F37"]?.takeIf { it.isNotEmpty() }?.let { un ->
                    if (dto.unpredictableNumber.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F37'] (Unpredictable Number) = $un")
                        dto.unpredictableNumber = un
                        watcher.debug("dto.unpredictableNumber = ${dto.unpredictableNumber}")
                    }
                }

                // ---------------- AIP (82) ----------------
                emvMap["82"]?.takeIf { it.isNotEmpty() }?.let { aip ->
                    if (dto.aip.isNullOrEmpty()) {
                        watcher.debug("emvMap['82'] (AIP) = $aip")
                        dto.aip = aip
                        watcher.debug("dto.aip = ${dto.aip}")
                    }
                }

                // ---------------- ARQC (9F26) ----------------
                emvMap["9F26"]?.takeIf { it.isNotEmpty() }?.let { arqc ->
                    if (dto.arqc.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F26'] (ARQC) = $arqc")
                        dto.arqc = arqc
                        watcher.debug("dto.arqc = ${dto.arqc}")
                    }
                }

                  emvMap["9F35"]?.takeIf { it.isNotEmpty() }?.let { emvTerminalType ->
                    if (dto.emvTerminalType.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F35'] (emvTerminalType) = $emvTerminalType")
                        dto.emvTerminalType = emvTerminalType
                        watcher.debug("dto.dfName = ${dto.emvTerminalType}")
                    }
                }

            emvMap["9F34"]?.takeIf { it.isNotEmpty() }?.let { cvmResults ->
                    if (dto.cvmResults.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F34'] (cvmResults) = $cvmResults")
                        dto.cvmResults = cvmResults
                        watcher.debug("dto.cvmResults = ${dto.cvmResults}")
                    } 
                }


            emvMap["9F36"]?.takeIf { it.isNotEmpty() }?.let { atc ->
                    if (dto.atc.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F36'] (atc) = $atc")
                        dto.atc = atc
                        watcher.debug("dto.atc = ${dto.atc}")
                    } 
                }
 
                 emvMap["9F6E"]?.takeIf { it.isNotEmpty() }?.let { formFactrInd ->
                    if (dto.formFactrInd.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F6E'] (formFactrInd) = $formFactrInd")
                        dto.formFactrInd = formFactrInd
                        watcher.debug("dto.formFactrInd = ${dto.formFactrInd}")
                    } 
                } 
                emvMap["9F33"]?.takeIf { it.isNotEmpty() }?.let { emvTerminalCapabilities ->
                    if (dto.emvTerminalCapabilities.isNullOrEmpty()) {
                        watcher.debug("emvMap['9F33'] (emvTerminalCapabilities) = $emvTerminalCapabilities")
                        dto.emvTerminalCapabilities = emvTerminalCapabilities
                        watcher.debug("dto.emvTerminalCapabilities = ${dto.emvTerminalCapabilities}")
                    } 
                }  
 
            emvMap["95"]?.takeIf { it.isNotEmpty() }?.let { tvr ->
                    if (dto.tvr.isNullOrEmpty()) {
                        watcher.debug("emvMap['95'] (tvr) = $tvr")
                        dto.tvr = tvr
                        watcher.debug("dto.tvr = ${dto.tvr}")
                    }
                } 


             emvMap["84"]?.takeIf { it.isNotEmpty() }?.let { dfName ->
                    if (dto.dfName.isNullOrEmpty()) {
                        watcher.debug("emvMap['84'] (dfName) = $dfName")
                        dto.dfName = dfName
                        watcher.debug("dto.dfName = ${dto.dfName}")
                    }
                }

                if(dto.dfName==""){ 
                      emvMap["9F06"]?.takeIf { it.isNotEmpty() }?.let { dfName ->
                        if (dto.dfName.isNullOrEmpty()) {
                            watcher.debug("emvMap['dfName'] (dfName) = $dfName")
                            dto.dfName = dfName
                            watcher.debug("dto.dfName = ${dto.dfName}")
                        }
                    }  
                }
                watcher.debug("EVO_emvMap_9F06 ${emvMap["9F06"]}")

                 watcher.debug("EVO_emvMap_84 ${emvMap["84"]}")
                 watcher.debug("EVO_emvMap_dfName ${dto.dfName}")
 

                 
                if(emvMap.containsKey("9F36")&&emvMap["9F36"]!=""&&  dto.atc==""){
                   watcher.debug("emvMap emvMap.containsKey(\"9F36\") ${emvMap["9F36"]!!}")
                    dto.atc= emvMap["9F36"]!!
                   watcher.debug(" dto.atc ${dto.atc}")

                }
               if(emvMap.containsKey("9F39")&&emvMap["9F39"]!=""&&  dto.pointOfServiceEntryMode== PointOfServiceEntryMode.Undefined){
                   watcher.debug("emvMap emvMap.containsKey(\"9F39\") ${emvMap["9F39"]!!}")
                    dto.pointOfServiceEntryMode= PointOfServiceEntryMode.fromEmvTagValue(emvMap["9F39"]!!)
                   watcher.debug(" dto.pointOfServiceEntryMode ${dto.pointOfServiceEntryMode}")

                }
                if(!dto.hasChip)
                dto.hasChip=hasChip

                for(tokenDescriptor in  field63Descriptor.tokenDescriptors){
                    for(tokenFieldDescriptor in tokenDescriptor.tokenFieldDescriptors){
                        if(tokenFieldDescriptor.emvTag!=""){
                            try{
                                if(emvMap.containsKey(tokenFieldDescriptor.emvTag)) {
                                  //  logger.debug("emvMap containsKey ${tokenFieldDescriptor.emvTag}")

                                    var data=emvMap[tokenFieldDescriptor.emvTag]
                                    if(data!=null&&data!=""){
                                       watcher.debug("SET_EMV_VALUE  IN -> ${tokenFieldDescriptor.fieldVarName} , \"${data}\"")
                                        if(tokenFieldDescriptor.emvTag == "¿'  "){
                                           watcher.debug("CHANGE 5F2A  IN -> ${data} ")
                                            if(data.length>=4){
                                                data=data.substring(1,4)
                                            }
                                           watcher.debug("CHANGE 5F2A  IN -> ${data} ")

                                        }

                                        if(tokenFieldDescriptor.emvTag == "9F1A"){
                                           watcher.debug("CHANGE 9F1A  IN -> ${data} ")
                                            if(data.length>=4){
                                                data=data.substring(1,4)
                                            }
                                           watcher.debug("CHANGE 9F1A  IN -> ${data} ")

                                        }
                                         if(tokenFieldDescriptor.emvTag == "9F1E"){
                                           watcher.debug("CHANGE 9F1E  IN -> ${data} ")
                                            
                                                data=StringUtils.hexToAscii(data)
                                            
                                           watcher.debug("CHANGE 9F1E  IN -> ${data} ")

                                        }
                                        dto.setField(tokenFieldDescriptor.fieldVarName,data)
                                    }
                                }else{
                                   watcher.debug("emvMap !containsKey ${tokenFieldDescriptor.emvTag}")

                                }
                            }catch (ex:Exception){
                               watcher.debug("SET_EMV_VALUE EXCEPTION-> ${ex.message}")

                            }
                        }
                    } 
                }

            }catch (e:WebServiceException){
                dto.errorCode=e.errorCode
                dto.errorMessage=e.errorMessage
               watcher.debug("EXCEPTION 2 ${dto.errorMessage}")

                return dto
            }catch (e:Exception){
                dto.errorCode= ErrorCode.EMVDataFormatError.errorCodeInt
                dto.errorMessage=ErrorCode.EMVDataFormatError.errorCodeMessage+" "+e.message
               watcher.debug("EXCEPTION 3 ${dto.errorMessage}")

                return dto 
            }
            if(dto.issApplData!=""&&dto.hostType.authorizer==Authorizer.PROSA){

               watcher.debug("CHANGE dto.issApplData  IN -> ${dto.issApplData},${dto.issApplData.length} ")
                dto.issApplDataLength= (dto.issApplData.length/2).toString().padStart(4,'0')
                dto.issApplData=dto.issApplData.padEnd(64,'0')
               watcher.debug("CHANGE dto.issApplData  IN -> ${dto.issApplData} ")
               watcher.debug("CHANGE dto.issApplDataLength  IN -> ${dto.issApplDataLength} ")
 
            }     
            if(dto.issApplData!=""&&dto.hostType.authorizer!=Authorizer.PROSA){
 
               watcher.debug("CHANGE dto.issApplData  IN -> ${dto.issApplData},${dto.issApplData.length} ")
                dto.issApplDataLength= (dto.issApplData.length ).toString().padStart(4,'0')
                dto.issApplData=dto.issApplData.padEnd(64,'F')     
               watcher.debug("CHANGE dto.issApplData  IN -> ${dto.issApplData} ")
               watcher.debug("CHANGE dto.issApplDataLength  IN -> ${dto.issApplDataLength} ")

            } 

            if(dto.dfName!=""&&dto.hostType.authorizer==Authorizer.PROSA){
   
               watcher.debug("CHANGE dto.dfName  IN -> ${dto.dfName},${dto.dfName.length} ")
               // dto.dfNameLength= (dto.dfName.length/2).toString().padStart(4,'0')
                dto.dfNameLength= (dto.dfName.length).toString().padStart(4,'0')
 
               watcher.debug("CHANGE dto.dfNameLength  IN -> ${dto.dfNameLength} ")
 
            }else{  
                  watcher.debug("CHANGE dto.dfName  IN -> ${dto.dfName},${dto.dfName.length} ")
               // dto.dfNameLength= (dto.dfName.length/2).toString().padStart(4,'0')
                dto.dfNameLength= (dto.dfName.length).toString().padStart(4,'0') 
                // dto.dfName= (dto.dfName).padStart(32,'F') 

               watcher.debug("CHANGE dto.dfNameLength  IN -> ${dto.dfNameLength} ")
            }
            /*f(dto.emvLocalTransactionDate!=""){
                dto.emvLocalTransactionDate=dto.localTransactionDate
            }*/
        } 

        if (dto.transactionCurrencyCode.isNotEmpty() && dto.transactionCurrencyCode.length > 3) {
            try {
                val currencyCodeInt = dto.transactionCurrencyCode.toInt()
                dto.transactionCurrencyCode = String.format("%03d", currencyCodeInt)
                watcher.debug("CHANGE dto.transactionCurrencyCode IN -> ${dto.transactionCurrencyCode}")
            } catch (e: NumberFormatException) {
                watcher.error("Invalid transactionCurrencyCode: ${dto.transactionCurrencyCode} ${e.message} ")
            }
        }
        
        
        if (dto.dfName.isNotEmpty()) {
              dto.dfNameLength = (dto.dfName.length * 0.5).toInt()
               .toString()
                .padStart(4, '0')
         
           // dto.dfNameLength= (dto.dfName.length).toString().padStart(4,'0')

        }  
         


       watcher.debug("\n\n PUT PADDINGS IN CHECKEMVDATA  END")  

        return putPaddings(dto)
    }



    fun checkRequest(dto: TransactionTerminalDto, originalTransaction: TransactionDBData?): TransactionDto {
        val watcher= WatcherContext.getInstance()
        watcher.debug("checkRequest WITH_ORIGINAL_TXN [${originalTransaction!=null}]")

        var newRequestDto= TransactionDto()

        newRequestDto.serverName=AppSettings.getInstance().serverName
        dto.serverName=AppSettings.getInstance().serverName
        val requestTransactionAmount=dto.transactionAmount  
        val requestTransactionType=dto.transactionType
        watcher.debug(" requestTransactionAmount: [${requestTransactionAmount}]")
        watcher.debug(" requestTransactionType: [${ requestTransactionType}]")
       
       
        watcher.debug("newRequestDto.serverName ${newRequestDto.serverName}")
        watcher.debug("dto.hostType ${dto.hostType}")
 
        newRequestDto.errorCode =0
        newRequestDto.errorMessage =""

        if(requestTransactionType!= TransactionType.Cancellation){
            dto.processingCode = ""
        }

        if(dto.transactionType.transactionMessageType!= TransactionMessageType.Advice
        &&dto.transactionType.transactionMessageType!= TransactionMessageType.Reverse
        ) { 
            watcher.debug("NOT ADVICE and not REVERSE ")
            watcher.debug("HOST_TYPE: ${dto.hostType}")
            if(newRequestDto!=null)
               watcher.debug("newRequestDto_originalRrn  "+newRequestDto.originalRrn )

            watcher.debug("original_dto "+GsonConfig.gsonLogs.toJson(dto))
            watcher.debug("newRequest_dto  "+GsonConfig.gsonLogs.toJson(newRequestDto))
             

            newRequestDto = StringUtils.copyNonEmptyProperties(dto, newRequestDto) as TransactionDto
            watcher.debug("(dto, newRequestDto)=> FULL COPIED dto to newRequestDto "+GsonConfig.gsonLogs.toJson(newRequestDto))

            newRequestDto.uuid=dto.uuid
            watcher.debug("  newRequestDto.uuid ${newRequestDto.uuid}")

         //   val jsonRequest = gson.toJson(newRequestDto)
           //rintln("newRequestDto 1 $jsonRequest")
            if (dto.atc != "") {
                newRequestDto.atcCZ = dto.atc
            }
        }
        val isReverse= dto.transactionType.transactionMessageType==TransactionMessageType.Reverse


     


        if(dto.transactionType.transactionMessageType==TransactionMessageType.Advice //RDM_REVERSE
       || dto.transactionType.transactionMessageType==TransactionMessageType.Reverse
        ){ 
            newRequestDto = StringUtils.copyNonEmptyProperties(dto, newRequestDto) as TransactionDto

            watcher.debug("IS_ADVICE OR IS_REVERSE")
            watcher.debug("transaction_type: [${dto.transactionType.transactionMessageType}]")
            if(isReverse){

                watcher.reverseDebug("transaction_type: [${dto.transactionType.transactionMessageType}]")
                watcher.reverseDebug("IS_REVERSE")

            } 

        /*    var originalTransaction=transactionsDBServiceOLD.getOriginalTransactionDto(newRequestDto.terminalSerialNumber,newRequestDto.authorizationIdResponse,watcher)
            if(originalTransaction==null){

                originalTransaction=transactionsDBServiceOLD.findTxn(newRequestDto.rrn,newRequestDto.terminalSerialNumber,newRequestDto.hostType,watcher)

            }*/
            watcher.debug("originalTransaction_NULL [${originalTransaction==null}]")
            
            if(originalTransaction==null||!isReverse&&originalTransaction.transactionStatus!= TransactionStatus.Approved){
                newRequestDto.errorCode = ErrorCode.OriginalTransactionDoesntExist.errorCodeInt
                newRequestDto.errorMessage = ErrorCode.OriginalTransactionDoesntExist.errorCodeMessage

                if(originalTransaction!=null) {
                    watcher.debug("originalTransaction_transactionStatus [${originalTransaction.transactionStatus}]")

                    if(originalTransaction.transactionStatus==TransactionStatus.Cancelled){
                        newRequestDto.errorCode = ErrorCode.OriginalTransactionWasCancelled.errorCodeInt
                        newRequestDto.errorMessage = ErrorCode.OriginalTransactionWasCancelled.errorCodeMessage

                    }
                    if(originalTransaction.transactionStatus==TransactionStatus.Refunded){
                        newRequestDto.errorCode = ErrorCode.OriginalTransactionWasRefunded.errorCodeInt
                        newRequestDto.errorMessage = ErrorCode.OriginalTransactionWasRefunded.errorCodeMessage

                    }
                }
                if (newRequestDto.rrn == "")
                    newRequestDto.rrn = getNewRRN(dto,dto.hostType)
                return newRequestDto

            }else{ 
                     watcher.debug("  _LACPI2 originalTransaction_transactionType ${originalTransaction.transactionType}")

                        if(originalTransaction.transactionType==TransactionType.Refund){
                                  watcher.debug(" _LACPI2 originalTransaction_IS_REFUND")
                           val  originalWasRefund=true
                              watcher.debug(" _LACPI2 originalWasRefund ${originalWasRefund}")
                          if(isReverse){
                                    newRequestDto.processingCode="200000"
                                     watcher.debug("_LACPI2_isReverse newRequestDto.processingCode [${  newRequestDto.processingCode}]")

                                }
                        }

                if(originalTransaction.transactionType.transactionMessageType==TransactionMessageType.Advice
                &&!isReverse) {
                    watcher.debug("originalTransaction IS_ADVICE")
                   
                    newRequestDto.errorCode=ErrorCode.AdviceTransactionCantGenerateAdviceTransaction.errorCodeInt
                    newRequestDto.errorMessage=ErrorCode.AdviceTransactionCantGenerateAdviceTransaction.errorCodeMessage
                    if(newRequestDto.rrn=="")
                       newRequestDto.rrn = getNewRRN(dto,dto.hostType)

                    return newRequestDto

                }else{
                    watcher.reverseDebug("newRequestDto_originalRrn: [${newRequestDto.originalRrn}]")

                    if(isReverse){ //RDM_REVERSE
                        watcher.reverseDebug("original_transactionType: [${originalTransaction.transactionType.transactionMessageType}]")
                    }
                    watcher.debug("originalTransaction OK")
                    watcher.debug("originalTransaction_not_null [${originalTransaction.transactionDto!=null}] ")

                    watcher.debug("originalTransaction_transactionDto_not_null [${originalTransaction.transactionDto!=null}] ")

                    if(originalTransaction.transactionDto!=null){
                        var originalTxn=OriginalTransactionDto()
                         originalTxn= StringUtils.copyNonEmptyProperties(originalTransaction.transactionDto!!, originalTxn) as OriginalTransactionDto
                        watcher.debug("originalTxn=>")
                        watcher.debugObj(originalTxn)

                        watcher.debug("originalTxn=> checkRequest_systemTraceAuditNumber: [${originalTransaction.transactionDto!!.systemTraceAuditNumber}]")
                        watcher.debug(" originalTxn=>checkRequest_localTransactionTime: [${originalTransaction.transactionDto!!.localTransactionTime}]")
                        watcher.debug(" originalTxn=>checkRequest_localTransactionDate: [${originalTransaction.transactionDto!!.localTransactionDate}]")
                        watcher.debug(" originalTxn=>checkRequest_settlementDate: [${originalTransaction.transactionDto!!.settlementDate}]")
                        watcher.debug(" originalTxn=>checkRequest_captureDate: [${originalTransaction.transactionDto!!.captureDate}]")
                  
                        newRequestDto= StringUtils.copyNonEmptyProperties(originalTxn, newRequestDto) as TransactionDto

                        if(requestTransactionType== TransactionType.Cancellation){
                            if(dto.processingCode != ""){
                                newRequestDto.processingCode=dto.processingCode
                            }else{
                                newRequestDto.processingCode=""
                            }
                        }

                        if(requestTransactionType==TransactionType.Refund){
                            newRequestDto.transactionAmount=requestTransactionAmount
                            watcher.debug("  newRequestDto_transactionAmount: [${ newRequestDto.transactionAmount}]")
                        }
                      

                    }else{
                        val requestTransactionAmount=newRequestDto.transactionAmount 
                        newRequestDto= StringUtils.copyNonEmptyProperties(originalTransaction, newRequestDto) as TransactionDto
                       
                        if(requestTransactionType==TransactionType.Refund){
                            newRequestDto.transactionAmount=requestTransactionAmount
                             watcher.debug("  newRequestDto_transactionAmount: [${ newRequestDto.transactionAmount}]")
                        }
                    }

                    newRequestDto.transactionType=dto.transactionType
                    watcher.debug("BEFORE=>.toJson(newRequestDto) ")
                    val jsonRequest=  GsonConfig.gsonRaw.toJson(newRequestDto)
                    watcher.debug("newRequestDto=> checkRequest_systemTraceAuditNumber: [${newRequestDto.systemTraceAuditNumber}]")
                    watcher.debug(" newRequestDto=>checkRequest_localTransactionTime: [${newRequestDto.localTransactionTime}]")
                    watcher.debug(" newRequestDto=>checkRequest_localTransactionDate: [${newRequestDto.localTransactionDate}]")
                    watcher.debug(" newRequestDto=>checkRequest_settlementDate: [${newRequestDto.settlementDate}]")
                    watcher.debug(" newRequestDto=>checkRequest_settlementDate: [${newRequestDto.captureDate}]")
                    watcher.debug("newRequestDto=>.toJson(newRequestDto) $jsonRequest")

                    watcher.debugObj(newRequestDto)


                newRequestDto.localTransactionTime = getLocalTransactionTime(dto.hostType)
                    newRequestDto.localTransactionDate = getLocalDate(dto.hostType)
                         newRequestDto.transmisionDateAndTime = getTransmissionDateAndTime(dto.hostType)
                       newRequestDto.captureDate = getCaptureDate(dto.hostType)
                                    //  newRequestDto. emvLocalTransactionDate=getEmvLocalDate(dto.hostType,watcher)
                       newRequestDto.systemTraceAuditNumber= getNewSystemTranceNumber(dto.hostType) // _LACPI2

                      watcher.debug("  newRequestDto.transmisionDateAndTime_LACPI ${ newRequestDto.transmisionDateAndTime}")
                       watcher.debug("  newRequestDto.systemTraceAuditNumber_LACPI2 ${  newRequestDto.systemTraceAuditNumber}")

                        watcher.debug("  newRequestDto.localTransactionTime_LACPI ${ newRequestDto.localTransactionTime}")


                     watcher.debug(" dto.otherAmount_LACPI ${ dto.otherAmount}")


                        if(originalTransaction!=null&&originalTransaction.transactionDto!=null) //#LACPI
                            watcher.debug(" originalTransaction.otherAmount_LACPI ${ originalTransaction.transactionDto!!.otherAmount}")
   
                            newRequestDto.otherAmount=dto.otherAmount
                        watcher.debug(" newRequestDto2.otherAmount_LACPI ${ newRequestDto.otherAmount}")

                        if(dto.transactionType==TransactionType.ClosePosTip){
                            var amountdouble=StringUtils.parseAmountToDouble( originalTransaction.transactionAmount,watcher)
                            val otheramnbtdouble=StringUtils.parseAmountToDouble( dto.otherAmount,watcher)
       
                             watcher.debug("  amountdouble_LACPI: [${amountdouble}]")
                            watcher.debug("  otheramnbtdouble_LACPI: [${otheramnbtdouble}]")
                            amountdouble+=otheramnbtdouble
                             watcher.debug("  TOTAL_amountdouble_LACPI: [${amountdouble}]")
                            newRequestDto.transactionAmount=StringUtils.formatDoubleToAmountIso(amountdouble,watcher)
                             watcher.debug("   newRequestDto.transactionAmount_LACPI: [${ newRequestDto.transactionAmount}]")
   

                        }



                    return newRequestDto

                } 
            }
        }
        newRequestDto=putPaddings(newRequestDto)
        newRequestDto.operationIdRequest=dto.operationIdRequest
          if(originalTransaction!=null&&originalTransaction!!.transactionType==TransactionType.Refund){
                                  watcher.debug(" _LACPI2 originalTransaction_IS_REFUND")
                           val  originalWasRefund=true
                              watcher.debug(" _LACPI2 originalWasRefund ${originalWasRefund}")
                          if(isReverse){
                                    newRequestDto.processingCode="200000"
                                     watcher.debug("_LACPI2_isReverse newRequestDto.processingCode [${  newRequestDto.processingCode}]")

                                }
                        }


                val newRequestDto2= checkRequest(newRequestDto)

                         if(originalTransaction!=null&&originalTransaction!!.transactionType==TransactionType.Refund){
                                  watcher.debug(" _LACPI2 originalTransaction_IS_REFUND")
                           val  originalWasRefund=true
                              watcher.debug(" _LACPI2 originalWasRefund ${originalWasRefund}")
                          if(isReverse){
                                    newRequestDto2.processingCode="200000"
                                     watcher.debug("_LACPI2_isReverse newRequestDto2_processingCode [${  newRequestDto2.processingCode}]")

                                }
             }


        return newRequestDto2
    }



    fun checkRequest(dto: TransactionDto): TransactionDto {
        val watcher= WatcherContext.getInstance()
        watcher.debug(" checkRequest systemTraceAuditNumber: [${dto.systemTraceAuditNumber}]")
        watcher.debug(" checkRequest localTransactionTime: [${dto.localTransactionTime}]")
        watcher.debug(" checkRequest localTransactionDate: [${dto.localTransactionDate}]")
        watcher.debug(" checkRequest settlementDate: [${dto.settlementDate}]")
        watcher.debug(" checkRequest settlementDate: [${dto.captureDate}]")

        dto.transactionAmount=dto.transactionAmount.replace(".","").padStart(12,'0')
        dto.otherAmount=dto.otherAmount.replace(".","").padStart(12,'0')
   
        if(dto.transactionType.transactionMessageType==TransactionMessageType.Financial){
            dto.responseCode=""
            dto.authorizationIdResponse=""

        }
        if (dto.transactionType == TransactionType.KeyLoad) {
            dto.pointOfServiceEntryMode=PointOfServiceEntryMode.KeyLoad
        }
        watcher.debug(" before dto.systemTraceAuditNumber  [${dto.systemTraceAuditNumber}]")
        if (dto.systemTraceAuditNumber == "") {
            dto.systemTraceAuditNumber = getNewSystemTranceNumber(dto.hostType)
        }
        watcher.debug("after dto.systemTraceAuditNumber  [${dto.systemTraceAuditNumber}]")

        if (dto.emvTermSerialNumber == ""&&dto.terminalSerialNumber.length>=8) {
            dto.emvTermSerialNumber =dto.terminalSerialNumber.takeLast(8)
            watcher.debug("dto.emvTermSerialNumber [${dto.emvTermSerialNumber}] dto.terminalSerialNumber [$dto.terminalSerialNumber]")

        }


        if (AppSettings.getInstance().sendDynamicValues) {
            if (dto.systemTraceAuditNumber != "") {
                dto.systemTraceAuditNumber = getNewSystemTranceNumber(dto.hostType)
            }
            if(dto.rrn==""){
                dto.rrn = getNewRRN(dto,dto.hostType)
            }

            //if(dto.transactionType!=TransactionType.LogOn&&){


           // }

            if (dto.settlementDate != "") {
                dto.settlementDate = getSettlementDate(dto.hostType)
            }
        }
        dto.localTransactionTime = getLocalTransactionTime(dto.hostType)
        dto.localTransactionDate = getLocalDate(dto.hostType)
        dto.transmisionDateAndTime = getTransmissionDateAndTime(dto.hostType)
        dto.captureDate = getCaptureDate(dto.hostType)
        dto. emvLocalTransactionDate=getEmvLocalDate(dto.hostType,watcher)
   
        //logger.debug("   dto.transmisionDateAndTime ${  dto.transmisionDateAndTime} dto.localTransactionTime ${ dto.localTransactionTime}  dto.localTransactionDate ${ dto.localTransactionDate}")
 
        if (dto.transactionType.transactionMessageType != TransactionMessageType.Advice
            && dto.transactionType.transactionMessageType != TransactionMessageType.Reverse
        ) {

            if (!AppSettings.getHostConfig(dto.hostType).useSystemTraceNumberHardcode) {
                dto.systemTraceAuditNumber = getNewSystemTranceNumber(dto.hostType)
            }else{
                if(dto.transactionType!=TransactionType.KeyLoad){
                    dto.systemTraceAuditNumber = "130683"

                }
                else{
                    dto.systemTraceAuditNumber = "762794"


                }

            }
            if(dto.rrn==""){
                dto.rrn = getNewRRN(dto,dto.hostType)
            }
        }



      watcher.debug("_LACPI2_processingCode [${  dto.processingCode}]")

        return dto
    }
    fun checkAcquirerData(dto: TransactionDto): TransactionDto {
        val watcher= WatcherContext.getInstance()

        WatcherGlobalLog.debug("checkAcquirerData INI")

       var newDto=CardType.trySetCardType(dto)

       if( newDto.track2=="6510000000000216D19122011000074900000F"){
            newDto.track2="6510000000000216D23122011000074900000F"
           newDto.track2="6510000000000216D23122011000074900000F"
           watcher.debug(" TRACK2 HARDCODE newDto_track2Length  ${newDto.track2}")
       }
       watcher.debug(" CHECK TELECARGA DATA")
       watcher.debug(" dto.merchantTypeCode ${dto.merchantTypeCode}")
       watcher.debug(" dto.affiliation ${dto.affiliation}")
       watcher.debug(" dto.retailerDataAddOns ${dto.retailerDataAddOns}")
       watcher.debug(" dto.merchantName ${dto.merchantName}")
       watcher.debug(" dto.bankName ${dto.bankName}")
       watcher.debug(" dto.merchantAddress ${dto.merchantAddress}")

       val maskPCI=  AppSettings.getInstance().maskPCI

       if(dto.bankName!=""){
            dto.bank=Bank.fromNameSuper(dto.bankName)
            watcher.debug(" dto.bank ${dto.bank}")

       } 
       if(dto.affiliation!=""){
         dto.retailerData=dto.affiliation+dto.retailerDataAddOns

       }else{
        dto.retailerData=""
       }

       watcher.debug(" dto.retailerData ${dto.retailerData}")

       // newDto.track2Length= newDto.track2.replace("F","").length.toString().padStart(2,'0')
       // watcher.debug("newDto_track2Length  ${newDto.track2Length}")
        watcher.debug(" newDto_track2Length  ${newDto.track2Length}")
        if(dto.track2.length>=38){
             watcher.debug("TRACK2_ROKD RECORTAR A 37")

            newDto.track2=dto.track2.substring(0,37)
        } 
          if(dto.paymentPlanType!=PaymentPlanType.Undefined)
            newDto.paymentPlanCode=dto.paymentPlanType.paymentPlanPROSACode

        if(dto.track2!=""&&dto.track2.length>=16&&dto.cardBin==""){
            if(AppSettings.getHostConfig(dto.hostType).printDebugData)
            if(!maskPCI){
                watcher.debug("TRACK2_ROKD ADD TRACK2: ${dto.track2}")
            }
              
  
 
            if(dto.hostType.authorizer==Authorizer.OPENPAY){
                newDto.cardBin=dto.track2.substring(0,8)
                dto.cardBin=newDto.cardBin

            }else{
                newDto.cardBin=dto.track2.substring(0,6)
                dto.cardBin=newDto.cardBin
            }

            var splited=dto.track2.split('D')
            if(splited.size<=1){
                splited=dto.track2.split('=')
            }
            watcher.debug("ADD splited.size: ${splited.size}")

            if(splited.size>=2) {

                newDto.cardNumber= splited[0]
                if(!maskPCI){
                watcher.debug("ADD   newDto.cardNumber: ${newDto.cardNumber}")
                }
                newDto.lastPan4Digits = splited[0].substring(splited[0].length-4, splited[0].length)
            }
        } else if(dto.cardNumber!=""&&dto.cardNumber.length>=16&&dto.cardBin==""){
            watcher.debug("ADD  dto.cardNumber: ${dto.cardNumber}")
            newDto.cardBin=dto.cardNumber.substring(0,16)
            newDto.lastPan4Digits=dto.cardNumber.substring(dto.cardNumber.length-4,dto.cardNumber.length)
            watcher.debug("ADD  newDto.cardBinr: ${newDto.cardBin}")
 
        }
        if(newDto.cardType==CardType.Undefined){
            newDto=CardType.trySetCardType(newDto)
            watcher.debug("CARDtype: ${newDto.cardType}")

        }
        newDto= putPaddings(newDto)
        if(newDto.emvTermSerialNumber==""&&newDto.terminalSerialNumber.length>=8){
           newDto.emvTermSerialNumber=newDto.terminalSerialNumber.substring(0,8)
        }

        if(newDto.emvTermSerialNumber.length>8){
            newDto.emvTermSerialNumber=newDto.emvTermSerialNumber.substring(0,8)
        }

        watcher.debug("  newDto.emvTermSerialNumber: ${newDto.emvTermSerialNumber}")

        if(newDto.hostType.authorizer==Authorizer.PROSA||newDto.hostType.authorizer==Authorizer.OPENPAY
            ||newDto.hostType.authorizer==Authorizer.VELPAY){
            return checkProsaData(newDto)
        }
        if(newDto.hostType.authorizer==Authorizer.BANORTE
            ||newDto.hostType.authorizer==Authorizer.VIERGE||newDto.hostType.authorizer==Authorizer.YOLOPAGO
            ||newDto.hostType.authorizer==Authorizer.CLAROPAY
            ||newDto.hostType.authorizer==Authorizer.PAYNET){
            return checkBanorteData(newDto)

        }

        watcher.debug("BVEFORE  emvLocalTransactionDate") 

        newDto.emvLocalTransactionDate=formatDate(dto.hostType,"yyMMdd")
        watcher.debug("AFTER  emvLocalTransactionDate")

        watcher.debug("dto.emvLocalTransactionDate ${dto.emvLocalTransactionDate}")
        watcher.debug("checkAcquirerData END")
        newDto= putPaddings(newDto)
        return newDto
    }
    fun checkProsaData(dto: TransactionDto): TransactionDto {
       val watcher= WatcherContext.getInstance()

        val newDto=dto
        val hostSettings=AppSettings.getHostConfig(dto.hostType)
        watcher.debug("checkProsaData INI")

       // logger.debug("dto.clearTrack2 [${dto.clearTrack2}], dto.track2 [${dto.track2}]")

        if(dto.track2==""&&dto.track2!=""&&dto.hostType.authorizer==Authorizer.PROSA){
            newDto.track2=dto.track2.replace("=","D").padEnd(48,'F')
            if(hostSettings.printDebugData){
                watcher.debug("TRACK2_ROKD newDto.clearTrack2 ${newDto.track2}")
            }
           // newDto.track2="" 
        }
        if(dto.track2==""&&dto.track2!=""&&dto.hostType.authorizer==Authorizer.OPENPAY){
            newDto.track2=dto.track2.replace("=","D")
            if(hostSettings.printDebugData){
                watcher.debug("TRACK2_ROKD newDto.clearTrack2 ${newDto.track2}")
            }
        }
        watcher.debug("checkProsaData END")

        return newDto
    }

    fun checkBanorteData(dto: TransactionDto): TransactionDto {
        val watcher= WatcherContext.getInstance()

        val newDto=dto
        watcher.debug("checkBanorteData INI")

        try {
            watcher.debug("dto.cardExpirationDate ${dto.cardExpirationDate}")

            if(dto.cardExpirationDate.length>=6){
                dto.cardExpirationDate=dto.cardExpirationDate.substring(0,4)
                watcher.debug("dto.cardExpirationDate SUBSTRING ${dto.cardExpirationDate}")

            }
            if(dto.track2!=""){
                dto.track2=dto.track2.padEnd(37,' ')
                dto.track2=dto.track2.padEnd(37,' ')
                watcher.debug("track2 padded:  ${ dto.track2.length}")

            }
            if((dto.deferral!=""||dto.numberOfPayments!="")&&dto.paymentPlanType!=PaymentPlanType.Undefined){
                if(dto.numberOfPayments==""){
                    dto.numberOfPayments="00"
                }
                if(dto.deferral==""){
                    dto.deferral="00"
                }




                watcher.debug(" dto.deferral   ${dto.deferral} dto.numberOfPayments ${dto.numberOfPayments} planType: ${dto.paymentPlanType}")

                dto.paymentQ6Data = StringUtils.asciiToHex(dto.deferral) + StringUtils.asciiToHex(dto.numberOfPayments) + StringUtils.asciiToHex(dto.paymentPlanType.paymentPlanBANORTECode)

            }
            watcher.debug("dto.paymentQ6Data   ${dto.paymentQ6Data}")
        }catch (e:Exception){
            watcher.debug("exception in   dto.paymentQ6Data ${e.message}")
        }

        try {
            val terSerHexLen=StringUtils.lenStringToHex(dto.terminalSerialNumber)
            val terminalSerialNumber=dto.terminalSerialNumber
            val appSerNHexLen=StringUtils.lenStringToHex(dto.applicationVersionNumber)
            val applicationVersionNumber=StringUtils.asciiToHex(dto.applicationVersionNumber)
            watcher.debug(" terSerHexLen:   $terSerHexLen \n terminalSerialNumber: $terminalSerialNumber \n appSerNHexLen. $appSerNHexLen \n applicationVersionNumber:$applicationVersionNumber")
            if(applicationVersionNumber!=""&&applicationVersionNumber!=""){
                dto.terminalData ="01"+terSerHexLen+ terminalSerialNumber +"02" +appSerNHexLen+ applicationVersionNumber
                watcher.debug("dto.terminalData   ${dto.terminalData}")
            }else{
                dto.terminalData=""
            }


        }catch (e:Exception){
            watcher.debug("exception in   dto.terminalData ${e.message}")
        }

        watcher.debug("checkBanorteData END")
        return newDto
    }
    fun putPaddings(dto: TransactionDto): TransactionDto {
        val watcher= WatcherContext.getInstance()

        val newDto=dto
        val isoDescriptor=   Iso8583Parser.getIsoDescriptorData(dto.hostType,watcher)
       WatcherGlobalLog.debug("putPaddings INI")
        try {
            for (fieldDescriptor in isoDescriptor.fieldDescriptors) {
                if (fieldDescriptor.paddingType != PaddingType.Undefined) {
                    try {
                        val field = newDto.getField(fieldDescriptor.fieldVarName)
                        WatcherGlobalLog.debug("PADDING_SETTED TO : ${fieldDescriptor.fieldVarName}")
                         val newValue=  IsoCommons.setPaddingToValue(field, fieldDescriptor)
                        newDto.setField(
                            fieldDescriptor.fieldVarName,
                            newValue
                        )
                        WatcherGlobalLog.debug(" PADDING_SETTED TO : ${fieldDescriptor.fieldVarName} ,oldValue : [$field], newValue: [$newValue]  ")

                    }catch (e:Exception){
                        WatcherGlobalLog.error(" PADDING_SETTED  EXCEPTION:  ${e.message} CHECK THIS FIELD   ${fieldDescriptor.fieldVarName}")

                    }
                }
            }
            for (tokenDescriptor in isoDescriptor.field63Descriptor.tokenDescriptors) {
                for (fieldDescriptor in tokenDescriptor.tokenFieldDescriptors) {
                    if (fieldDescriptor.paddingType != PaddingType.Undefined) {
                        try {
                            WatcherGlobalLog.debug("TOKEN PADDING_SETTED TO : ${fieldDescriptor.fieldVarName} 2")
                            val field = newDto.getField(fieldDescriptor.fieldVarName)
                            val newValue=  IsoCommons.setPaddingToValue(field, fieldDescriptor)
                            newDto.setField(
                                fieldDescriptor.fieldVarName,
                                newValue
                            )

                            WatcherGlobalLog.debug("TOKEN PADDING_SETTED TO : ${fieldDescriptor.fieldVarName} ,oldValue : [$field], newValue: [$newValue]  ")
                        }catch (e:Exception){
                            WatcherGlobalLog.error("TOKEN PADDING_SETTED  EXCEPTION:  ${e.message} CHECK THIS FIELD -TOKEN ${tokenDescriptor.tokenId}: ${fieldDescriptor.fieldVarName}")

                        }
                    }
                }
            }

        }catch (e:Exception){
            WatcherGlobalLog.error("putPaddings EXCEPTION: ${e.message}")

        }

        WatcherGlobalLog.debug("putPaddings END")

        return newDto
    }


    fun saveDynamicISOFieldsData(hostType:HostType,data:DynamicISOFieldsData){

        while(fieldsAreBeingSaving)
            fieldsAreBeingSaving=true
        val path = AppSettings.getAuthorizerConfig(hostType).dynamicParametersPath
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(data)
        try {
            val myObj = File(path)
            if (myObj.createNewFile()) {
                val writer = PrintWriter(path)

                writer.print(
                    jsonString
                )

                writer.close()
                WatcherGlobalLog.debug("File created: " + myObj.name)
            } else {
                //var reader = BufferedReader(FileReader(path))
                val writer = PrintWriter(path)
                writer.print(
                    jsonString
                )
                writer.close()


                //  var splitted=line.split("\n")
                //  var firstLine=splitted[0]
            }
        } catch (e: IOException) {
            WatcherGlobalLog.error("EROR SAVING FIELDS: ${e.message}")
        }
        fieldsAreBeingSaving=false
    }
    fun getDynamicISOFieldsData(hostType:HostType):DynamicISOFieldsData{
        var data=DynamicISOFieldsData()
        while(fieldsAreBeingReading)
            fieldsAreBeingReading=true
        try {
            val initialFile = File(AppSettings.getAuthorizerConfig(hostType).dynamicParametersPath)
            val xmlInputStream: InputStream = FileInputStream(initialFile)


            val bufferedReader = BufferedReader(InputStreamReader(xmlInputStream))

            var line: String?
            var json = ""
            //    val buffer = StringBuffer(2048)
            while (bufferedReader.readLine().also { line = it } != null) {
                json += line
            }
            //println("json: $json")
            data = Gson().fromJson(json, DynamicISOFieldsData::class.java)
        }catch (e:Exception){
            WatcherGlobalLog.error("getDynamicISOFieldsData EXCEPTION ${e.message}")

        }
        fieldsAreBeingReading=false
        return data
    }
    fun getNewRRN( hostType:HostType): String {
        val dateFormat =   AppSettings.getHostConfig(hostType).getCurrentDateTime("yyMMddHHmmss")
        return dateFormat.format(Date())
    }


    fun getNewRRN(terminalDto: TransactionTerminalDto, hostType:HostType):String{

        WatcherGlobalLog.debug("getNewRRN 1 ${terminalDto.rrn}")
        if(terminalDto.rrn.length>=12) return terminalDto.rrn
        return generateNewRRN(terminalDto.hostType)

    }
    fun getNewRRN(terminalDto: TransactionDto, hostType:HostType):String{
        WatcherGlobalLog.debug("getNewRRN 2 ${terminalDto.rrn}")

        if(terminalDto.rrn.length>=12) return terminalDto.rrn
        return generateNewRRN(terminalDto.hostType)

    }
    fun generateNewRRN(hostType: HostType):String{
        val rrn: String = AppSettings.getHostConfig(hostType).getCurrentDateTime("yyMMddHHmmss")
        return rrn.padEnd(12,'0')
    }
    fun generateRandomDate(): LocalDateTime {
        val year = kotlin.random.Random.nextInt(2022, 2025) // Genera un año aleatorio entre 2022 y 2024
        val month = kotlin.random.Random.nextInt(1, 13) // Genera un mes aleatorio entre 1 y 12
        val day = kotlin.random.Random.nextInt(1, 29) // Genera un día aleatorio entre 1 y 28 (para simplificar, no consideramos años bisiestos)
        val hour = kotlin.random.Random.nextInt(0, 24) // Genera una hora aleatoria entre 0 y 23
        val minute = kotlin.random.Random.nextInt(0, 60) // Genera un minuto aleatorio entre 0 y 59
        val second = kotlin.random.Random.nextInt(0, 60) // Genera un segundo aleatorio entre 0 y 59
        return LocalDateTime.of(year, month, day, hour, minute, second)
    }

    fun generateRandomRRN(): String {
        val randomDate = generateRandomDate()
        val rrn = randomDate.format(DateTimeFormatter.ofPattern("MMddHHmmss"))
        return rrn.padEnd(12, '0')
    }

     
    private val lock = Any()

    fun getNewSystemTranceNumber(authorizer: HostType): String {
        synchronized(lock) {
            val data = getDynamicISOFieldsData(authorizer) 
            data.systemTraceAuditNumber++ 
    
            if (data.systemTraceAuditNumber >= 1000000L) {
                data.systemTraceAuditNumber = 1
            }
    
            saveDynamicISOFieldsData(authorizer, data)
            return data.systemTraceAuditNumber.toString().padStart(6, '0')
        }
    }


    
    fun generateRandomKeySerialNumber(): String {
        val hexChars = "0123456789ABCDEF"
        val terminalSerialNumber = StringBuilder()
        repeat(32) {
            val randomIndex = kotlin.random.Random.nextInt(hexChars.length)
            terminalSerialNumber.append(hexChars[randomIndex])
        }
        return terminalSerialNumber.toString()
    }

    fun generateRandomDateTime(minMonthsAgo: Long, maxMonthsAgo: Long): LocalDateTime {
        val currentDateTime = LocalDateTime.now()
        val minDateTime = currentDateTime.minusMonths(maxMonthsAgo)
        val maxDateTime = currentDateTime.minusMonths(minMonthsAgo)
        val minutesBetween = ChronoUnit.MINUTES.between(minDateTime, maxDateTime)
        val randomMinutes = kotlin.random.Random.nextLong(0, minutesBetween + 1)
        return minDateTime.plusMinutes(randomMinutes)
    }

    fun generateRandomTerminalSerialNumber(): String {
        val serials = listOf("SMM32521871016024585","SMM32521871016024581","SMM32521871016024584","YMM32521871016024581","YYM32521871016024581")
        return serials[ kotlin.random.Random.nextInt(serials.size)]
    }
    fun generateRandomTransactionAmount(): String {
        val randomAmount = kotlin.random.Random.nextInt(1, 401) // Genera un valor aleatorio entre 1 y 400
        val amountString = randomAmount.toString().padStart(12, '0') // Rellena con ceros a la izquierda
        return amountString
    }
 
    fun getAuthorizationIdentificationResponse(authorizer:HostType):String{
        val data= getDynamicISOFieldsData(authorizer)
        data.authorizationIdResponse++
        if(data.authorizationIdResponse>=1000000L){
            data.authorizationIdResponse=1
        }
        saveDynamicISOFieldsData(authorizer,data)
        return data.authorizationIdResponse.toString().padStart(6,'0')

    }

    fun getLogOnRequestMetaData(hostType:HostType): Iso8583MetaData {
       val watcher= WatcherContext.getInstance()

        val response=Iso8583MetaData()
        val data=TransactionData()
        val byteArray: ByteArray
        data.pointOfServiceEntryMode=PointOfServiceEntryMode.Undefined
        
        data.systemTraceAuditNumber= getNewSystemTranceNumber(hostType)
        data.transactionDirection= TransactionDirection.Request
        //data.settlementDate= getSettlementDate()
        data.transmisionDateAndTime= getTransmissionDateAndTime(data.hostType)
        data.networkManagmentInformationCode="001"
        data.merchantType=MerchantType.Comm
        data.responseCode="00"
        data.transactionType= TransactionType.LogOn
        data.cardType=CardType.VISA
        response.transactionData=data
        try {
            byteArray = Iso8583Builder.buildIso8583(data)
            response.setIsoByteArray(byteArray)
        }catch (w:WebServiceException){
            response.setWebServiceException(w)
        }
       watcher.saveIsoRaw(response)
        return response
    }



    fun getLogOnResponseMetaData(hostType:HostType, systemTraceNumber:String): Iso8583MetaData {
        val response=Iso8583MetaData()
        val watcher= WatcherContext.getInstance()

        val data= TransactionData()
        if(systemTraceNumber != "") {
            data.systemTraceAuditNumber = systemTraceNumber
        }else{
            data.systemTraceAuditNumber= getNewSystemTranceNumber(hostType)

        }
        //data.settlementDate= getSettlementDate()
        data.transmisionDateAndTime= getTransmissionDateAndTime(data.hostType)
        data.networkManagmentInformationCode="001"
        data.merchantType=MerchantType.Comm
        data.pointOfServiceEntryMode=PointOfServiceEntryMode.Undefined

        data.responseCode="00"
        data.transactionType= TransactionType.LogOn
        data.cardType=CardType.VISA
        response.transactionData=data
        data.transactionDirection= TransactionDirection.Response

        try {
            val byteArray = Iso8583Builder.buildIso8583(data)
            response.setIsoByteArray(byteArray)
        }catch (w:WebServiceException){
           watcher.debug("  EXCEPTION getLogOnResponseMetaData "+w.message.toString())

            response.setWebServiceException(w)

        }
       watcher.saveIsoRaw(response)
        return response
    }


    fun getPosInvoiceData():String {

        return   ("").padEnd(
            20,
            ' '
        )
    }
    fun getEchoTestResponseMetaData(hostType:HostType, systemTraceNumber:String): Iso8583MetaData {
        val watcher= WatcherContext.getInstance()

        val response=Iso8583MetaData()
        val data=TransactionData()
        val byteArray:ByteArray
        if(systemTraceNumber != "") {
            data.systemTraceAuditNumber = systemTraceNumber
        }else{
            data.systemTraceAuditNumber= getNewSystemTranceNumber(hostType)

        }
        data.pointOfServiceEntryMode=PointOfServiceEntryMode.Undefined

        //data.settlementDate= getSettlementDate()
        data.transmisionDateAndTime= getTransmissionDateAndTime(data.hostType)
        data.networkManagmentInformationCode="301"
        data.merchantType=MerchantType.Comm
        data.responseCode="00"
        data.transactionType= TransactionType.LogOn
        data.transactionDirection= TransactionDirection.Response
        data.cardType=CardType.VISA
        response.transactionData=data
        try {
            byteArray = Iso8583Builder.buildIso8583(data)
            response.setIsoByteArray(byteArray)
        }catch (w:WebServiceException){
           watcher.debug("  EXCEPTION getEchoTestResponseMetaData "+w.message.toString())

            response.setWebServiceException(w)

        }
       watcher.saveIsoRaw(response)
        return response
    }


     
    fun getEchoRequestMetaData(hostType:HostType):Iso8583MetaData{
       val watcher= WatcherContext.getInstance()
        val response=Iso8583MetaData() 
        val data=TransactionData()
        data.pointOfServiceEntryMode=PointOfServiceEntryMode.Undefined
        data.systemTraceAuditNumber= getNewSystemTranceNumber(hostType)
       data.settlementDate= getSettlementDate(data.hostType)
        data.transmisionDateAndTime= getTransmissionDateAndTime(data.hostType)
        data.networkManagmentInformationCode="301"
        data.merchantType=MerchantType.Comm
       // data.responseCode="00"
        data.transactionType= TransactionType.EchoTest
        data.transactionDirection= TransactionDirection.Request

        data.cardType=CardType.VISA

        response.transactionData=data
        try {
            val byteArray = Iso8583Builder.buildIso8583(data)
            response.setIsoByteArray(byteArray)
        }catch (w:WebServiceException){
            response.setWebServiceException(w)

        }

        watcher.saveIsoRaw(response)

        return response
    }

    
    fun getFunKeysMetaData(transactionType:TransactionType,keyDirection:KeyDirection,hostType:HostType):Iso8583MetaData{
       val watcher= WatcherContext.getInstance()
        val response=Iso8583MetaData() 
        val data=TransactionData()
        data.pointOfServiceEntryMode=PointOfServiceEntryMode.Undefined
        data.systemTraceAuditNumber= getNewSystemTranceNumber(hostType)
       data.settlementDate= getSettlementDate(data.hostType)
        data.transmisionDateAndTime= getTransmissionDateAndTime(data.hostType)

            if ((transactionType == TransactionType.KeyMACNew ||
            transactionType == TransactionType.KeyMACChange ||
            transactionType == TransactionType.KeyMACRepeat ||
            transactionType == TransactionType.KeyMACVerify) &&
            keyDirection == KeyDirection.Outbound) {
            
            data.securityControlInfo = "0001"
 
        } else if ((transactionType == TransactionType.KeyMACNew ||
                    transactionType == TransactionType.KeyMACChange ||
                    transactionType == TransactionType.KeyMACRepeat ||
                    transactionType == TransactionType.KeyMACVerify) &&
                keyDirection == KeyDirection.Inbound) {
            
            data.securityControlInfo = "0002"

        } else if ((transactionType == TransactionType.KeyMACNew ||
                    transactionType == TransactionType.KeyMACChange ||
                    transactionType == TransactionType.KeyMACRepeat ||
                    transactionType == TransactionType.KeyMACVerify) &&
                keyDirection == KeyDirection.Both) {
            
            data.securityControlInfo = "0003"

        } else if ((transactionType == TransactionType.KeyPINNew ||
                    transactionType == TransactionType.KeyPINChange ||
                    transactionType == TransactionType.KeyPINRepeat ||
                    transactionType == TransactionType.KeyPINVerify) &&
                keyDirection == KeyDirection.Outbound) {

            data.securityControlInfo = "0101"

        } else if ((transactionType == TransactionType.KeyPINNew ||
                    transactionType == TransactionType.KeyPINChange ||
                    transactionType == TransactionType.KeyPINRepeat ||
                    transactionType == TransactionType.KeyPINVerify) &&
                keyDirection == KeyDirection.Inbound) {

            data.securityControlInfo = "0102"

        } else if ((transactionType == TransactionType.KeyPINNew ||
                    transactionType == TransactionType.KeyPINChange ||
                    transactionType == TransactionType.KeyPINRepeat ||
                    transactionType == TransactionType.KeyPINVerify) &&
                keyDirection == KeyDirection.Both) {

            data.securityControlInfo = "0103"
        }
        when (transactionType) {
            TransactionType.KeyMACChange, TransactionType.KeyPINChange -> {
                data.networkManagmentInformationCode = "161"
            }
            TransactionType.KeyMACNew, TransactionType.KeyPINNew -> {
                data.networkManagmentInformationCode = "162"
            }
            TransactionType.KeyMACRepeat, TransactionType.KeyPINRepeat -> {
                data.networkManagmentInformationCode = "163"
            }
            TransactionType.KeyMACVerify, TransactionType.KeyPINVerify -> {
                data.networkManagmentInformationCode = "164"
            }
        }





        data.merchantType=MerchantType.Comm
       // data.responseCode="00"
        data.transactionType= transactionType
         data.keyDirection= keyDirection

        data.transactionDirection= TransactionDirection.Request

        data.cardType=CardType.VISA

        response.transactionData=data
        try {
            val byteArray = Iso8583Builder.buildIso8583(data)
            response.setIsoByteArray(byteArray)
        }catch (w:WebServiceException){
            response.setWebServiceException(w)

        }

        watcher.saveIsoRaw(response)

        return response
    }



     fun getSettlementDate(hostType: HostType):String{


         return formatDate(hostType,"MMdd")
     }

   /* fun getEmvLocalTransactionDate():String{


        return formatDate("yyMMdd")
    }*/


    fun getCaptureDate(hostType: HostType):String{


        return formatDate(hostType,"MMdd")
    }

    fun getTransmissionDateAndTime(hostType: HostType):String{


        return formatDate(hostType,"MMddHHmmss")
    }

    fun getLocalTransactionTime(hostType: HostType):String{


        return formatDate(hostType,"HHmmss")
    }
    fun getLocalDate(hostType: HostType):String{


        return formatDate(hostType,"MMdd")
    }


    fun getEmvLocalDate(hostType: HostType,watcher:Watcher):String{

        watcher.debug("getEmvLocalDate= ${hostType} ")
        val emvDate= formatDate(hostType,"YYMMdd")
        watcher.debug("emvDate= ${emvDate} ")
        return emvDate
    }



}