package com.viergegroup.evopayment.dtos


class EvoVoidResponseDto {
    var VoidResponse: EvoVoidResponse = EvoVoidResponse()
}

class EvoVoidResponse { 
    var TransactionFacilityPayments: Int = 1
    var TransactionFacilityType: String = "0"
    var Plan: EvoPlanDto = EvoPlanDto()
    var PlanDescription: String = "VISA"
    var PaymentMethodId: Int = 2
    var PaymentMethodDescription: String = "VISA"
    var CardDescription: String = "VISA"
    var CardNumber: String = "451368******8504"
    var CardHashing: String = ""
    var CardReadMode: String = "M"
    var CardReadModeDescription: String = "MANUAL" 
    var CardTypeDescription: String = "CREDITO"
    var MerchantID: String = "1000000"
    var TerminalID: String = "30110001"
    var TerminalTrace: Int = 1646
    var SettlementBatchNumber: Int = 1
    var MerchantCategory: EvoMerchantCategoryDto = EvoMerchantCategoryDto()
    var CardCategory: EvoCardCategoryDto = EvoCardCategoryDto()
    var TransactionDescription: String = "Anul. de Compra"
    var OrigAuthTicket: Int = 158
    var OrigAuthDateTime: String = "2023-04-28 06:30:17"
    var OrigTerminalTrace: Int = 1645
    var CurrencySymbol: String = "$"
    var CurrencyDescription: String = "CLP"
    var HostID: Int = 99
    var TransactionResolutionMode: String = "ONLINE"
    var AuthHostMsgType: String = "0420"
    var ValueFacilityPayments: Double = 0.0
    var TransmitionDateTime: String = "2023-04-28 11:31:27"
    var AuthResultCode: String = "00"
    var AuthHostProcessCode: String = "000000"
    var AuthCode: String = "001645"
    var AuthRRN: String = "164530110001"
    var AuthTicket: Int = 158
    var AuthDateTime: String = "2023-04-28 11:31:27"
    var ResponseActions: List<String> = listOf("Approve", "Tickets")
    var ResponseMessage: String = "APROBADA (00)"
    var ResponseCode: Int = -1
    var TransactionRequestAmount: Double = 0.0
    var TransactionAuthenticationType: String = "S"
    var OrigAuthCode: String = "001645"
    var OrigAuthRRN: String = "164530110001"
    var Sequence: String = ""
    var Configuration: EvoConfigurationDto = EvoConfigurationDto()
    var AnswerKey: String = ""
    var PublicAnswerKey: String = ""
    var Tickets: List<Any> = listOf()
    var CompanyIdentification: String = "3000001"
    var SystemIdentification: String = "stand_alone_5g_2.9.9.5"
    var BranchIdentification: String = "1100001"
    var POSIdentification: String = "3110001" 
    var CurrencyCode: Int = 152
    var Amount: String = "21450.00"
    var TipAmount: String = "0"
    var Reference: String = "230428113226"
}

// ======================= SUB-OBJECTS =======================
