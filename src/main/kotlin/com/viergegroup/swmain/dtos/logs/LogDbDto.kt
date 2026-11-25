package com.viergegroup.swmain.dtos.logs

import com.shared.commons.ErrorCode
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection

import java.time.LocalDateTime

class LogDbDto : ResponseEntity() {

    var serverName:String=""
    var errorCodeNumber: Int = -1
    var transactionStatus= TransactionStatus.ReceivedInWebService
    var transactionAmount: String = "" //FIELD 4
    //var rrn: String = "" //FIELD 37
    var terminalSerialNumber: String = ""
    var responseCode:String=""
    var authorizationIdResponse:String=""
    var hostType: HostType = HostType.Undefined
    var merchantType : MerchantType = MerchantType.Undefined
    var transactionType: TransactionType = TransactionType.Undefined
    var pointOfServiceEntryMode: PointOfServiceEntryMode = PointOfServiceEntryMode.Undefined
    var transactionMessageType= TransactionMessageType.Undefined
    var transactionDirection: TransactionDirection = TransactionDirection.Request
    var cardType: CardType = CardType.Undefined
    var paymentPlanType: PaymentPlanType = PaymentPlanType.Undefined
    var keySerialNumber:String=""
    var lastModificationDateTime= LocalDateTime.now()
    var creationDateTime: LocalDateTime = LocalDateTime.now()
    val logsType= mutableListOf<String>()

}