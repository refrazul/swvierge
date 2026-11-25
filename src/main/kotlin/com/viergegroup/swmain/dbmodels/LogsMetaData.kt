package com.viergegroup.swmain.dbmodels
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsMetaDataDto
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.time.LocalDateTime
import javax.persistence.*
import java.math.BigDecimal

@Entity
@Table(name="log_meta_data")
class LogsMetaData {
    constructor()  
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  //@GeneratedValue(strategy = GenerationType.AUTO) // Cambiar a AUTO para SQLite
    @Column(unique = true, nullable = false)
    var logId: Long = 0
    @Column(nullable=false, columnDefinition = "bytea")

    var uuid:ByteArray = byteArrayOf()

    @Enumerated(EnumType.STRING)
    var cardType: CardType = CardType.Undefined


    @Enumerated(EnumType.STRING)
    var errorCode:ErrorCode=ErrorCode.Undefined 
    var errorCodeNumber: Int = -1
    @Column(length = 1000)
    var errorMessage:String=""

    var cardBin:String=""
    var cardNumber:String=""


    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String=""
    var merchantAddress:String=""
    var bankName:String=""
    




    var serverName:String=""
    @Enumerated(EnumType.STRING)
    var hostType: HostType = HostType.Undefined
    @Enumerated(EnumType.STRING)
    var bank:Bank=Bank.Undefined
    @Enumerated(EnumType.STRING)
    var moduleId:ModuleId= ModuleId.Undefined
    @Enumerated(EnumType.STRING)
    var operationId:OperationId= OperationId.Undefined
    @Enumerated(EnumType.STRING)
    var merchantType :MerchantType = MerchantType.Undefined
    @Enumerated(EnumType.STRING)
    var transactionType: TransactionType = TransactionType.Undefined

 
    


    var ramTotal: BigDecimal=BigDecimal(0)

    var ramUsed: BigDecimal=BigDecimal(0)

    var ramUsagePercentage: BigDecimal=BigDecimal(0)


    var storageTotal: BigDecimal=BigDecimal(0)

    var storageUsed: BigDecimal=BigDecimal(0)
    var storageUsagePercentage: BigDecimal=BigDecimal(0)
    var networkUsagePercentage: BigDecimal=BigDecimal(0)
    var cpuUsagePercentage: BigDecimal=BigDecimal(0)


    var clientIP:String="" 
    var clientEmail:String=""
    var clientUserName:String=""
    var clientGeolocation:String=""
    var deviceModel:String=""
    var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""

    var responseCode:String=""
    var transactionAmount: String = "" //FIELD 4

    @Enumerated(EnumType.STRING)
    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var operationDuration:Float=0F

    var lastModificationDateTime= LocalDateTime.now()
    var creationDateTime: LocalDateTime = LocalDateTime.now()
    var timeZone: String = "" //FIELD 4


    fun  injectDto(dto:LogsMetaDataDto){
        StringUtils.copyNonEmptyProperties(dto,this)
        WatcherGlobalLog.info("LogsMetaData dto.uuid ${dto.uuid}")
        uuid=StringUtils.hexStringToByteArray(dto.uuid)
    }

}