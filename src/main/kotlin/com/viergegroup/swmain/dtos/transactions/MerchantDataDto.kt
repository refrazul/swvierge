
package com.viergegroup.swmain.dtos.transactions
import com.viergegroup.swmain.entities.catalogs.CardType

class MerchantDataDto {
    var cardType: CardType = CardType.Undefined

    var cardBin:String="" 
    var cardNumber:String=""
    var responseCode:String=""
 
    var affiliation:String=""
    var merchantName:String=""
    var merchantTypeCode:String=""
    var merchantAddress:String=""
    var bankName:String=""
    
    var rrn:String=""
    var authorizationIdResponse:String=""

}

