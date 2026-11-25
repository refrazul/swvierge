
package com.viergegroup.swmain.dtos.transactions
import com.viergegroup.swmain.entities.transactions.TransactionData

import com.fasterxml.jackson.annotation.JsonInclude
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.dbmodels.TransactionDBData

import java.lang.reflect.Field
import java.time.LocalDateTime
import com.viergegroup.isoprocessor.models.iso.Iso8583MetaData

class ReverseIsoDto{
  var errorCode:Int=0   
    var errorMessage=""
    var reverseDbData:TransactionDBData=TransactionDBData()
    var transactionDto:TransactionDto=TransactionDto() 
    var transactionData:TransactionData=TransactionData()
  
    var iso8583MetaData:Iso8583MetaData=Iso8583MetaData()

}