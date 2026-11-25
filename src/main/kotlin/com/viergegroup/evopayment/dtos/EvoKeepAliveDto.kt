
package com.viergegroup.evopayment.dtos

class EvoKeepAliveDto {
    var KeepAlive: EvoKeepAliveRequestDto = EvoKeepAliveRequestDto()
}
 
class EvoKeepAliveRequestDto { 
    var RequestType: String = "KeepAlive"
    var POSIdentification: String = "TAPONPAOLA01"  
    var BranchIdentification: String = "1099421"
    var CompanyIdentification: String = "TAPONPHONE"
    var SystemIdentification: String = "upay_socket_5g_TAP01" 
}
