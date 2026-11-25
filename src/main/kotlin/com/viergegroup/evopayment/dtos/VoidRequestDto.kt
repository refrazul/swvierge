package com.viergegroup.evopayment.dtos

// ======================= REQUEST =======================

class EvoVoidRequestDto {
    var Void: EvoVoidDto = EvoVoidDto()
}

class EvoVoidDto {
    var Reference: String = "230428113226"
    var CompanyIdentification: String = "3000001"
    var SystemIdentification: String = "stand_alone_5g_2.9.9.5"
    var BranchIdentification: String = "1100001"
    var POSIdentification: String = "3110001"
    var OrigReference: String = "230428113115"
    var Security: List<EvoVoidSecurityDto> = listOf(EvoVoidSecurityDto())
 //   var Sequence: String = ""
}

class EvoVoidSecurityDto {
    var Type: String = "InputTokens"
    var Values: List<EvoVoidSecurityValueDto> = listOf(EvoVoidSecurityValueDto())
}

class EvoVoidSecurityValueDto {
    var Name: String = "Method"
    var Value: String = "DUKPT-eGlobal"
}

