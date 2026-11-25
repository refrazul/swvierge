package com.viergegroup.evopayment.dtos

class EvoKeepAliveResponseDto {
    var KeepAliveResponse: EvoKeepAliveResponseDetailDto = EvoKeepAliveResponseDetailDto()
}

class EvoKeepAliveResponseDetailDto {  
    var ResponseCode: Int = -1
    var ResponseMessage: String = "Peticion aceptada"
   
}
 
class EvoKeepAliveConfigurationDto {
    var company: EvoKeepAliveCompanyDto = EvoKeepAliveCompanyDto()
    var branch: EvoKeepAliveBranchDto = EvoKeepAliveBranchDto()
}

class EvoKeepAliveCompanyDto {
    var id: String = "1000001"
    var name: String = "CompanyTest"
    var description: String = "CompanyTest"
    var address: String = "Contadero S/N, CP: 00001"
    var city: String = "Santiago"
    var state: String = "SANTIAGO DE CHILE"
    var regionCode: String = "CLP"
    var zipCode: String? = null
    var countryCode: String = "CL"
    var category: Int? = null
    var additionalConfig: String? = null
    var label: String = "CompanyTest"
}

class EvoKeepAliveBranchDto {
    var id: String = "1100001"
    var name: String = "Branch 1 Test"
    var foreignIdentifier: String = "1100001"
    var description: String = "Branch 1 Test"
    var address: String = "Contadero 1"
    var city: String = "Santiago"
    var state: String = "SANTIAGO DE CHILE"
    var regionCode: String = "CLP"
    var zipCode: String = "000001"
    var countryCode: String = "CL"
    var category: Int = 5541
}
