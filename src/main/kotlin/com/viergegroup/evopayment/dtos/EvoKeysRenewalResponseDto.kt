
package com.viergegroup.evopayment.dtos

class EvoKeysRenewalResponseDto {
    var keysrenewalresponse: EvoKeysRenewalResponseDetailDto = EvoKeysRenewalResponseDetailDto()
}

class EvoKeysRenewalResponseDetailDto {
    var transactionfacilitypayments: Int = 1
    var transactionfacilitytype: String = "0"
    var plan: EvoPlanDto = EvoPlanDto()
    var plandescription: String = "MASTERCARD"
    var paymentmethodid: Int = 2
    var paymentmethoddescription: String = "VISA "
    var carddescription: String = "VISA "
    var cardnumber: String = "451368******7553"
    var cardhashing: String = "705b94de87c9ba19572e95142a2935f82e2df56d148c62f20dc1d8b1fd9f819eb8d28517"
    var cardreadmode: String = "M"
    var cardreadmodedescription: String = "MANUAL"
    var cardtypedescription: String = "CREDITO"
    var merchantid: String = "1000000"
    var terminalid: String = "30366111"
    var terminaltrace: Int = 4
    var settlementbatchnumber: Int = 1
    var merchantcategory: EvoMerchantCategoryDto = EvoMerchantCategoryDto()
    var cardcategory: EvoCardCategoryDto = EvoCardCategoryDto()
    var transactiondescription: String = "Compra"
    var currencysymbol: String = "$ "
    var currencydescription: String = "CLP"
    var hostid: Int = 1
    var transactionresolutionmode: String = "ONLINE"
    var authhostmsgtype: String = "0210"
    var hostmsgtype: String = "0200"
    var transmitiondatetime: String = "2023-04-28 08:23:47"
    var authresultcode: String = "00"
    var authhostprocesscode: String = "000000"
    var authcode: String = "000004"
    var authrrn: String = "000430366111"
    var authticket: Int = 1
    var authdatetime: String = "2023-04-28 08:23:47"
    var responseactions: List<String> = listOf("Approve", "Tickets")
    var responsemessage: String = "APROBADA (00)"
    var responsecode: Int = -1
    var transactionauthenticationtype: String = "S"
    var cardcryptogramresponse: String = "112233445566778888776655443322111234567890123456789012345600552BFF5C"
    var sequence: String = "e7f55120-e5c7-11ed-9b3e-e569ca523c50_OSS_V1.0_1000129_1000129_90522657"
    var translatetransactiontype: String = "KeysRenewal"
    var configuration: EvoConfigurationDto = EvoConfigurationDto()
    var answerkey: String = "e83ae5a0-e5c7-11ed-9b3e-e569ca523c50_OSS_V1.0_1000129_1000129_90522657"
    var publicanswerkey: String = "e840d910-e5c7-11ed-9b3e-e569ca523c50_OSS_V1.0_1000129_1000129_90522657"
    var tickets: List<String> = listOf()
    var companyidentification: String = "1000129"
    var systemidentification: String = "OSS_V1.0"
    var branchidentification: String = "1000129"
    var posidentification: String = "90522657"
    var currencycode: Int = 152
    var amount: Int = 0
}
class EvoPlanDto {
    var id: String = "1000129_121_*_2_CITIBANAMEX MASTERCARD aq_1"
    var foreignidentifier: String = "1"
    var host: EvoHostDto = EvoHostDto()
    var description: String = "MASTERCARD"
    var facilitypayments: Int = 1
    var facilitytype: String = "0"
    var currency: Int = 152
    var rate: Int = 0
    var isdynamic: Boolean = false
    var posordeviceactions: List<String> = listOf("RequestPIN")
    var category: Int = 0
    var cashback: EvoCashbackDto = EvoCashbackDto()
    var deferral: EvoDeferralDto = EvoDeferralDto()
}

class EvoHostDto { 
    var id: Int = 1
}

class EvoCashbackDto {
    var isallowed: String = "N"
}

class EvoDeferralDto {
    var isallowed: String = "N"
}

class EvoMerchantCategoryDto {
    var code: Int = 5541
}

class EvoCardCategoryDto {
    var name: String = "No especificada"
    var foreignidentifier: String = "Clave_NoEspecificada"
}

class EvoConfigurationDto {
    var company: EvoCompanyDto = EvoCompanyDto()
    var branch: EvoBranchDto = EvoBranchDto()
}

class EvoCompanyDto {
    var id: String = "1000129"
    var name: String = "CompanyTest"
    var description: String = "CompanyTest"
    var taxcode: String? = "" 
    var address: String = "Contadero S/N, CP: 00001"
    var city: String? = null
    var state: String? = null
    var regioncode: String? = null
    var zipcode: String? = null
    var countrycode: String? = null
    var category: Int? = null
    var additionalconfig: String? = null
    var label: String = "CompanyTest"
}

class EvoBranchDto {
    var id: String = "1000129"
    var name: String = "Branch 1 Test"
    var foreignidentifier: String = "1200001"
    var description: String = "Branch 1 Test"
    var address: String = "Contadero 2"
    var city: String = "CDMX"
    var state: String = "CDMX"
    var regioncode: String = "CL"
    var zipcode: String = "000002"
    var countrycode: String = "CL"
    var category: Int = 5541
}
