package com.viergegroup.swmain.dtos.comm


import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionMessageType
import java.time.LocalDate
import com.viergegroup.swmain.entities.catalogs. KeyDirection

class CommHostRequestDto: RequestEntity() {
    var keyDirection= KeyDirection.Undefined 
    //var hostName=""
    var transactionMessageType= TransactionMessageType.Undefined
    var creationDate=""
    
    
    var posIdentification: String = "TAPONPAOLA01"
    var branchIdentification: String = "1099421"
    var companyIdentification: String = "TAPONPHONE"
    var systemIdentification: String = "upay_socket_5g_TAP01"

    fun creationDate2LocalDate():LocalDate{
        return try {
            LocalDate.parse(creationDate)
        }catch (e:Exception){
            LocalDate.now()
        }
    } 

}