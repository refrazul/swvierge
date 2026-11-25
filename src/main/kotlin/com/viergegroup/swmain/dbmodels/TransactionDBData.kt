package com.viergegroup.swmain.dbmodels

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.transactions.TransactionDBDataDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.entities.transactions.TransactionData
import org.springframework.beans.BeanUtils

import java.time.LocalDateTime
import javax.persistence.* 
import com.viergegroup.swmain.dtos.transactions.ReverseIsoDto
 
import com.shared.commons.DynamicISOFields
import com.viergegroup.swmain.webwatcher.Watcher
import com.google.gson.JsonSyntaxException
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import com.viergegroup.isoprocessor.builder.IsoHostBuilder
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.math.BigDecimal

@Entity
@Table(name="transaction_data")
class TransactionDBData { 
  
    constructor()  
 
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  //  @GeneratedValue(strategy = GenerationType.AUTO) // Cambiar a AUTO para SQLite
    @Column(unique = true, nullable = false)
     var transactionId: Long = 0

    @Column(nullable=false, columnDefinition = "bytea")
    var uuid:ByteArray = byteArrayOf()
    @Enumerated(EnumType.STRING)
    var operationId:OperationId= OperationId.Undefined
 
    var originalTransactionId:Long=-1
    @Enumerated(EnumType.STRING)
    var transactionStatus= TransactionStatus.ReceivedInWebService
    var transactionAmount: String = "" //FIELD 4
 
 
   
   // @Column(nullable = false, precision = 12, scale = 2)
    val transactionAmountBd: BigDecimal= BigDecimal(0)
    var transactionCurrencyCode: String = ""
    var transactionCurrencyCodeNumber: Int = 484
    var transactionCurrencySymbol: String = ""
    var systemTraceAuditNumber:String=""

    var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""
    var responseCode:String=""
    var authorizationIdResponse:String=""
    var timeZone:String=""
    @Transient
    var originalRrn: String = ""



    @Enumerated(EnumType.STRING)
    var errorCode: ErrorCode = ErrorCode.Undefined
    var errorCodeNumber: Int = -1
    @Column(length = 1000)
    var errorMessage:String="" 
 

    var clientIP:String=""
    var clientEmail:String=""
    var clientUserName:String="" 
    var clientLocation:String=""
    var deviceModel:String=""

    @Enumerated(EnumType.STRING)
    var hostType: HostType = HostType.Undefined
    @Enumerated(EnumType.STRING)
    var bank:Bank=Bank.Undefined


    var fiid:String=""
     var transactionIdMpp: String = ""

    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String=""
    var merchantAddress:String=""
    var bankName:String=""
    
     var clientId:String="VIERGE"

     
    @Enumerated(EnumType.STRING)
    var deviceType: DeviceType = DeviceType.POS 
 
 
    @Enumerated(EnumType.STRING)
    var merchantType :MerchantType = MerchantType.Undefined
    @Enumerated(EnumType.STRING)
    var transactionType: TransactionType = TransactionType.Undefined
    @Enumerated(EnumType.STRING)
    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    @Enumerated(EnumType.STRING)
    var transactionMessageType=TransactionMessageType.Undefined
    @Enumerated(EnumType.STRING)
    var transactionDirection: TransactionDirection = TransactionDirection.Request
    @Enumerated(EnumType.STRING)
    var cardType: CardType = CardType.Undefined
    @Enumerated(EnumType.STRING)
    var paymentPlanType: PaymentPlanType =PaymentPlanType.Undefined



    var cardBin: String = "" // FIELD 4
    var cardNumber: String = "" // FIELD 4
 


    @Column(nullable=false, columnDefinition = "text")
    var keySerialNumber:String=""



    @Column(nullable=false, columnDefinition = "bytea")
    var isoASCIIRequest:ByteArray = byteArrayOf()

    @Column(nullable=false, columnDefinition = "bytea")
    var isoASCIIResponse:ByteArray = byteArrayOf() 

    @Column(nullable=false, columnDefinition = "bytea")
    var metaData:ByteArray = byteArrayOf()

    @Column(nullable=false, columnDefinition = "bytea")
    var emvData: ByteArray = byteArrayOf() 

    @Column(nullable=false, columnDefinition = "text")
    var dbKcv:String=""

    @Column(nullable=false, columnDefinition = "text")
    var serverName:String=""

    @Transient
    var transactionDto: TransactionDto?=null
    @Transient
    var isInitial: Boolean =false 


    var lastModificationDateTime= LocalDateTime.now()
    var creationDateTime: LocalDateTime = LocalDateTime.now()

 


    var ramTotal: BigDecimal=BigDecimal(0)
    var ramUsed: BigDecimal=BigDecimal(0)
    var ramUsagePercentage: BigDecimal=BigDecimal(0)
    var storageTotal: BigDecimal=BigDecimal(0)
    var storageUsed: BigDecimal=BigDecimal(0)
    var storageUsagePercentage: BigDecimal=BigDecimal(0)
    var networkUsagePercentage: BigDecimal=BigDecimal(0)
    var cpuUsagePercentage: BigDecimal=BigDecimal(0)
    var operationDuration:Float=0F
 
 

    fun injectDto(dto: TransactionDBDataDto) {
        StringUtils.copyNonEmptyProperties(dto, this) 
        this.isoASCIIRequest = StringUtils.asciiToByteArray(dto.isoASCIIRequest)
        this.isoASCIIResponse = StringUtils.asciiToByteArray(dto.isoASCIIResponse)
        this.metaData = StringUtils.asciiToByteArray(dto.metaData)
        this.emvData = StringUtils.asciiToByteArray(dto.emvData)
    } 
    fun injectUUID(uuid: String) {
       this. uuid=StringUtils.hexStringToByteArray(uuid)
    }
 

    fun injectEmvDataStr(emvData:String){
        this.emvData=StringUtils.asciiToByteArray(emvData)
    }
    fun injectRequestISOASCIIStr(isoASCII:String){
        this.isoASCIIRequest=StringUtils.asciiToByteArray(isoASCII)
    }
    fun injectResponseISOASCIIStr(isoASCII:String){
        this.isoASCIIResponse=StringUtils.asciiToByteArray(isoASCII)
    }


    fun injectMetaDataStr(metaData:String){
        this.metaData=StringUtils.asciiToByteArray(metaData)
    }
  


    fun readMetaDataStr():String{
        return StringUtils.byteArrayToASCII(metaData)
    }
    fun readEmvDataStr():String{ 
        return StringUtils.byteArrayToASCII(emvData)
    }
    fun readISOASCIIStr():String{
        return StringUtils.byteArrayToASCII(isoASCIIRequest)
    }
   
      
     
    fun buildIso8583Reverse(watcher: Watcher): ReverseIsoDto {
        var response=ReverseIsoDto()   
        var data = Iso8583MetaData()   
        watcher.debug("buildIso8583Reverse STARTED FOR rrn= $rrn transactionId=$transactionId ")
        WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse STARTED FOR rrn= $rrn transactionId=$transactionId ")
        val transReverseDto = TransactionDto() 
  
         
 
        try {
            if (this.transactionDto == null) {   
                /*  response = ReverseIsoDto().apply {
                    errorCode = ErrorCode.UnHandledException.errorCodeInt
                    errorMessage = "transactionDto IS_NULL"
                }  
                    StringUtils.copyNonEmptyProperties(this, response.reverseDbData)
                watcher.error("buildIso8583Reverse ERROR: transactionDto is NULL, returning error response")
                WatcherGlobalLog.reverseDebug("buildIso8583Reverse ERROR: transactionDto is NULL, returning error response")*/
                WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse ERROR: transactionDto is NULL, returning error response")
                this.transactionDto=StringUtils.dbData2Dto(this) 
              //  BeanUtils.copyProperties(this,  this.transactionDto)

                // return response 
            }  
        
            watcher.debug("Cloning transactionDto into transReverseDto...")
            WatcherGlobalLog.reverseDebugRrn(rrn,"Cloning transactionDto into transReverseDto...")
            val originalWasRefund=this.transactionDto!!.transactionType==TransactionType.Refund
              WatcherGlobalLog.reverseDebugRrn(rrn,"_LACPI originalWasRefund $originalWasRefund")

                WatcherGlobalLog.reverseDebugRrn(rrn,"Cloning transactionDto into transReverseDto...")

            StringUtils.copyNonEmptyProperties(this.transactionDto!!, transReverseDto)
    
            watcher.debug("TransactionDto cloned successfully")
            WatcherGlobalLog.reverseDebug("TransactionDto cloned successfully")
     
            var time = DynamicISOFields.formatDate(transReverseDto.hostType, "MMddHHmmssSS", transReverseDto.transactionDateTime)
            if (time.length >= 11) {
                time = time.substring(0, time.length - 1)
            } 
            transReverseDto.originalDataElements = (transReverseDto.transactionType.messageTypeRequest +
               transReverseDto.rrn + time + transReverseDto.captureDate).padEnd(42, '0')
             transReverseDto.transactionType=TransactionType.Reverse
            transReverseDto.transactionStatus=TransactionStatus.SentToHost
           // transReverseDto.authorizationIdResponse="";   

            transReverseDto.systemTraceAuditNumber= DynamicISOFields.getNewSystemTranceNumber(transReverseDto.hostType)
            data.transactionType=TransactionType.Reverse
            
             if(originalWasRefund){
                       transReverseDto.processingCode="200000"
                      watcher.debug("_LACPI2_isReverse newRequestDto2_processingCode [${ transReverseDto.processingCode}]")

              }
            watcher.debug("Generated originalDataElements: ${transReverseDto.originalDataElements}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"Generated originalDataElements: ${transReverseDto.originalDataElements}")
    
            if (transReverseDto.merchantType == MerchantType.Undefined) {
                transReverseDto.merchantType = MerchantType.Retail
                watcher.debug("MerchantType was Undefined, set to Retail")
                WatcherGlobalLog.reverseDebugRrn(rrn,"MerchantType was Undefined, set to Retail")
            }
    
            watcher.debug("Calling DynamicISOFields.checkRequest()")
            WatcherGlobalLog.reverseDebugRrn(rrn,"Calling DynamicISOFields.checkRequest()")
    
            val requestDtoNew = DynamicISOFields.checkRequest(transReverseDto)
    
            watcher.debug("RequestDto after checkRequest: $requestDtoNew")
            WatcherGlobalLog.reverseDebugRrn(rrn,"RequestDto after checkRequest: $requestDtoNew")
        
        
           
           // requestDtoNew.responseCode =responseCode
            watcher.debug(" responseCode: ${requestDtoNew.responseCode}")
            WatcherGlobalLog.reverseDebugRrn(rrn," responseCode: ${requestDtoNew.responseCode}")
               
            if(requestDtoNew.responseCode=="H9"||requestDtoNew.responseCode==""){
               requestDtoNew.responseCode = "00" // DynamicISOFields.getAuthorizationIdentificationResponse(transReverseDto.hostType)
                watcher.debug("Generated responseCode: ${requestDtoNew.responseCode}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"Generated responseCode: ${requestDtoNew.responseCode}")
        
    
            }
           if (requestDtoNew.authorizationIdResponse.isEmpty()) {
               requestDtoNew.authorizationIdResponse = "000000" // DynamicISOFields.getAuthorizationIdentificationResponse(transReverseDto.hostType)
                watcher.debug("Generated authorizationIdResponse: ${requestDtoNew.authorizationIdResponse}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"Generated authorizationIdResponse: ${requestDtoNew.authorizationIdResponse}")
            }  
            //  requestDtoNew.authorizationIdResponse=""
                  
            if (requestDtoNew.posInvoiceData.isEmpty()) {
                transReverseDto.posInvoiceData = DynamicISOFields.getPosInvoiceData()
                watcher.debug("Generated posInvoiceData: ${transReverseDto.posInvoiceData}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"Generated posInvoiceData: ${transReverseDto.posInvoiceData}")
            }
    
            watcher.debug("Updated TransactionDto after processing: ${transReverseDto.posInvoiceData}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"Updated TransactionDto after processing: ${transReverseDto.posInvoiceData}")
    
            watcher.debug("Calling IsoHostBuilder.buildIso()")
            WatcherGlobalLog.reverseDebugRrn(rrn,"Calling IsoHostBuilder.buildIso()")
    
            data = IsoHostBuilder.buildIso(transReverseDto.transactionType, requestDtoNew)
         /*   data.errorCode = 0 
            transReverseDto.errorCode=0
            transReverseDto.errorMessage=""*/ 

            if( data.errorCode !=0){
                 response.iso8583MetaData=data
                 response.transactionDto=transReverseDto
                  response.errorCode= data.errorCode
                  response.errorMessage= data.errorMessage
                 transReverseDto.errorCode=data.errorCode
               transReverseDto.errorMessage=data.errorMessage
                WatcherGlobalLog.reverseDebugRrn(rrn," data.errorCode  ${ data.errorCode } data.errorMessage [${data.errorMessage}]")
                return response

            }



            val transactionData=TransactionData()
            StringUtils.copyNonEmptyProperties( transReverseDto,transactionData)
     

            data.transactionData=transactionData
            data.errorMessage = ""
          
            if (data.transactionData != null) {
                watcher.error("buildIso8583Reverse RESPONSE_CODE: ${data.transactionData!!.responseCode}")
                WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse RESPONSE_CODE: ${data.transactionData!!.responseCode}")
            }
            StringUtils.copyNonEmptyProperties(transReverseDto, response.reverseDbData)
   
        } catch (e: JsonSyntaxException) { 
            data.errorCode = -1
            data.errorMessage = "JSON Parsing Error: ${e.message}"
            watcher.error("buildIso8583Reverse JSON Parsing Error: ${e.message}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse JSON Parsing Error: ${e.message}")
                         AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR,"EXCEPTION CONSTURYENDO EL REVERSO [${e.message}] ")

        } catch (e: Exception) {
             AlarmEmailService.sendAlarmEmail(AlarmCause.REVERSE_ERROR,"EXCEPTION CONSTURYENDO EL REVERSO [${e.message}] ")

            data.errorCode = -1
            data.errorMessage = "Exception Error: ${e.message}"
            watcher.error("buildIso8583Reverse Exception: ${e.message}")
            WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse Exception: ${e.message}")
        }
    
        watcher.debug("buildIso8583Reverse COMPLETED")
        WatcherGlobalLog.reverseDebugRrn(rrn,"buildIso8583Reverse COMPLETED")
        response.iso8583MetaData=data
        response.transactionDto=transReverseDto
        return response
    }
    
 


}