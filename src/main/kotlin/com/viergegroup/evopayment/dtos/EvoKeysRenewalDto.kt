
package com.viergegroup.evopayment.dtos

class EvoKeysRenewalDto {
    var keysRenewal: EvoKeysRenewalDetailDto = EvoKeysRenewalDetailDto()
}

class EvoKeysRenewalDetailDto {
    var requestType: String = "KeysRenewal"
    var pOSIdentification: String = "90522657"
    var branchIdentification: String = "1000129"
    var companyIdentification: String = "1000129"
    var systemIdentification: String = "OSS_V1.0"
    var currencyCode: String = "152"
    var transactionType: String = "KeysRenewal"
    var inputTokens: EvoInputTokensDto = EvoInputTokensDto()
    var security: List<EvoSecurityDto> = listOf(EvoSecurityDto())
}
