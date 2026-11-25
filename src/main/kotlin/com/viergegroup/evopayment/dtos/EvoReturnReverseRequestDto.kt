package com.viergegroup.evopayment.dtos

class EvoReturnReverseRequestDto {
    var Return: EvoReturnReverseDto = EvoReturnReverseDto()
}

class EvoReturnReverseDto { 
   // var Amount: Double = 1000.0
    //var TipAmount: Double = 1000.0
 
  //  var CardNumber: String = "4513680641758504"
 //   var CardReadMode: String = "C" 
 
    var SystemIdentification: String = ""
    var CompanyIdentification: String = ""
    var BranchIdentification: String = ""

    var POSIdentification: String = "" 
   
   // var FacilityPayments: String = "1"
   // var FacilityType: String = "0"
    var IsReverse: Int = 1 
    //var OrigReference: String = "230428112322"

    var Reference: String = "230428112322" 
    /*var Reference: String = "230428112322" 
    var CurrencyCode: String = "484"
    var InputTokens: EvoInputTokensDto = EvoInputTokensDto()
    var Security: List<EvoSecurityDto> = listOf(EvoSecurityDto())*/

}
