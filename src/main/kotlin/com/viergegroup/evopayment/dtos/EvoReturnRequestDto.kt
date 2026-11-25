package com.viergegroup.evopayment.dtos

// ======================= REQUEST =======================

class EvoReturnRequestDto {
    var Return: EvoReturnDto = EvoReturnDto()
}

class EvoReturnDto {
    var Reference: String = "230428112547"
    var OrigReference: String = "230428112322"

    var ReadingDeviceType: String = "Default"
    var CompanyIdentification: String = "3000001" 
    var SystemIdentification: String = "stand_alone_5g_2.9.9.5"
    var BranchIdentification: String = "1100001"
    var POSIdentification: String = "3110001"

    var FacilityPayments: String = "1"
    var FacilityType: String = "0"

    var CardReadMode: String = "C"
    var Amount: Double = 28500.0 
    var CardNumber: String = "4513680641758504"
    // En el JSON viene como string:
    var CurrencyCode: String = "152"

   // var OrigAnswerKey: String = 
     //    "1ed617f0-ec32-11ec-8e2f6fe4801914c5_Test1.0.0_1000001_1100001_1110001"

    var InputTokens: EvoInputTokensDto = EvoInputTokensDto()
    var Security: List<EvoSecurityDto> = listOf(EvoSecurityDto())
   // var Sequence: String = "" 
}
