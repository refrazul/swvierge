package com.viergegroup.evopayment.dtos

class EvoSaleRequestDto {
    var Sale: EvoSaleDto = EvoSaleDto()
}

class EvoSaleDto { 
    var Amount: Double = 1000.0

    
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
//   var IsReverse: String = "0" 
 
    var Reference: String = "230428112322" 
    var CurrencyCode: String = "484"
    var InputTokens: EvoInputTokensDto = EvoInputTokensDto()
  //  var EmvTags: String = "9F34031F03025F3401019F0607A00000015230109F39010557136510000000000216D19122011000074900000F5F2A020484820218008407A0000001523010950580000088009A032303279C01009F02060000000010509F03060000000000009F090200019F10080106A000094000009F1A0204849F1E0837303138333733349F260895F9477A97EB0AF29F2701809F3303E008C89F34031F03029F3501229F360203049F3704C9C2CCE09F4104000000269F5301529F6E00"
    var Security: List<EvoSecurityDto> = listOf(EvoSecurityDto())
}

class EvoInputTokensDto {
    var TokenB2: String = "FFF90000800440008000B54C0070D135FC1F00000000500000000000000039000C874844842309080034E9E2EB00360110A50003020000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
    var TokenB3: String = "FF0080627772E0B0C8000000000000002200024103020014A0000000041010FFFFFFFFFFFFFFFFFF"
    var TokenB4: String = "05131001410302150500"
    var TokenES: String = "IGIP320D20 IGIPP32040788062777250000000000000000000"
    var TokenCZ: String = ""
    var TokenEZ: String = "00201000000F4100002300000340010527A000053DAF82B6BC34B689E417785EC56AB4B739C5403FD70C5112214B41A1D3"
}

class EvoSecurityDto {
    var Type: String = "InputTokens"
    var Values: List<EvoSecurityValueDto> = listOf(EvoSecurityValueDto())
}

class EvoSecurityValueDto {
    var Name: String = "Method"
    var Value: String = "DUKPT-eGlobal"
}
