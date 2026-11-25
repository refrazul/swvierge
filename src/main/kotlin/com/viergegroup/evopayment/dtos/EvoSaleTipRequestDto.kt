package com.viergegroup.evopayment.dtos

class EvoSaleTipRequestDto {
    var Sale: EvoSaleTipDto = EvoSaleTipDto()
}

class EvoSaleTipDto { 
    var Amount: Double = 1000.0
    var TipAmount: Double = 1000.0

    
    var CardNumber: String = "4513680641758504"
    var CardReadMode: String = "C"
 
    var SystemIdentification: String = ""
    var CompanyIdentification: String = ""
    var BranchIdentification: String = ""

    var POSIdentification: String = ""
    /*   "FacilityPayments": "1",
    "FacilityType": "0",
     */ 
    var FacilityPayments: String = "1"
    var FacilityType: String = "0"
    var IsReverse: String = "0"

    var Reference: String = "230428112322" 
    var CurrencyCode: String = "484"
    var InputTokens: EvoInputTokensDto = EvoInputTokensDto()
  //  var EmvTags: String = "9F34031F03025F3401019F0607A00000015230109F39010557136510000000000216D19122011000074900000F5F2A020484820218008407A0000001523010950580000088009A032303279C01009F02060000000010509F03060000000000009F090200019F10080106A000094000009F1A0204849F1E0837303138333733349F260895F9477A97EB0AF29F2701809F3303E008C89F34031F03029F3501229F360203049F3704C9C2CCE09F4104000000269F5301529F6E00"
    var Security: List<EvoSecurityDto> = listOf(EvoSecurityDto())
}
