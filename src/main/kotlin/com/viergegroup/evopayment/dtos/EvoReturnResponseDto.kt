package com.viergegroup.evopayment.dtos


// ======================= RESPONSE =======================

class EvoReturnResponseDto {
    var ReturnResponse: EvoReturnResponse = EvoReturnResponse()
}

class EvoReturnResponse {
    var TransactionFacilityPayments: Int = 3
    var TransactionFacilityType: String = "3"

    var Plan: EvoPlanDto = EvoPlanDto()
    var PlanDescription: String = "VISA"

    var PaymentMethodId: Int = 2
    var PaymentMethodDescription: String = "VISA "
    var CardDescription: String = "VISA "
    var CardNumber: String = "451368******8504"
    var CardHashing: String =
        "37bf9053207ef362fcbc93d4ddb191615b6b36524bba9923247403c3d327af47cc4ea60c"

    var CardReadMode: String = "C"
    var CardReadModeDescription: String = "EMV CHIP"
    var CardTypeDescription: String = "CREDITO"

    var MerchantID: String = "1000000"
    var TerminalID: String = "30110001"
    var TerminalTrace: Int = 1644
    var SettlementBatchNumber: Int = 1

    var MerchantCategory: EvoMerchantCategoryDto = EvoMerchantCategoryDto()
    var CardCategory: EvoCardCategoryDto = EvoCardCategoryDto()

    var TransactionDescription: String = "Devol. de Compra"
    var OrigAuthTicket: Int = 158
    var OrigAuthDateTime: String = "2023-04-28 06:22:24"
    var OrigTerminalTrace: Int = 1643

    var CurrencySymbol: String = "$ "
    var CurrencyDescription: String = "CLP"
    var HostID: Int = 99
    var TransactionResolutionMode: String = "ONLINE"

    var AuthHostMsgType: String = "0210"
    var HostMsgType: String = "0200"

    var ValueFacilityPayments: Double = 0.0
    var TransmitionDateTime: String = "2023-04-28 11:24:48"
    var AuthResultCode: String = "00"
    var AuthHostProcessCode: String = "200000"
    var AuthCode: String = "001644"
    var AuthRRN: String = "164330110001"
    var AuthTicket: Int = 158
    var AuthDateTime: String = "2023-04-28 11:24:48"

    var ResponseActions: List<String> = listOf("Approve", "Tickets")
    var ResponseMessage: String = "APROBADA (00)"
    var ResponseCode: Int = -1
    var TransactionRequestAmount: Double = 0.0
    var TransactionAuthenticationType: String = "S"
    var OrigAuthCode: String = "001643"

    var Sequence: String =
        "31cd8bf0-e5e1-11ed-9b3e-e569ca523c50_stand_alone_5g_2.9.9.5_3000001_1100001_3110001"

    var Configuration: EvoConfigurationDto = EvoConfigurationDto()

    var AnswerKey: String =
        "31fb52b0-e5e1-11ed-9b3e-e569ca523c50_stand_alone_5g_2.9.9.5_3000001_1100001_3110001"

    var PublicAnswerKey: String =
        "3214a710-e5e1-11ed-9b3e-e569ca523c50_stand_alone_5g_2.9.9.5_3000001_1100001_3110001"

    var Tickets: List<Any> = listOf()

    var CompanyIdentification: String = "3000001"
    var SystemIdentification: String = "stand_alone_5g_2.9.9.5"
    var BranchIdentification: String = "1100001"
    var POSIdentification: String = "3110001"

    var CurrencyCode: Int = 152
    var Amount: String = "28500.00"
    var TipAmount: String = "0"
    var Reference: String = "230428112547"
}
