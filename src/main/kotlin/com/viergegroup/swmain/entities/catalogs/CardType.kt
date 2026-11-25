package com.viergegroup.swmain.entities.catalogs

import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

enum class CardType(var cardTypeId:Int,var cardTypeName:String){
    Undefined(-1,"Undefined"), 
    VISA(1,"VISA"),
    MASTERCARD(2,"MASTERCARD"),
    AMEX(3,"AMEX"), 
    DISCOVER(4,"DISCOVER"),
    JCB(5,"JCB"), 
    CARNET(6,"CARNET"), 
    UNIONPAY(7,"UNIONPAY");
    ;
    // CheckIn(51,"CheckIn");
 

    companion object{
        private val map = CardType.values().associateBy(
            CardType::cardTypeName)
        fun fromMerchantName(cardTypeName: String) = map[cardTypeName]
        fun getRandomCardType(): CardType {
            // Obtenemos todos los valores del enum
            val cardTypes = CardType.values()

            // Generamos un índice aleatorio dentro del rango de los valores del enum
            val randomIndex = kotlin.random.Random.nextInt(cardTypes.size)

            // Devolvemos el valor del enum en el índice aleatorio
            return cardTypes[randomIndex]
        }
       private var validCardTypesDesc=""
        fun trySetCardType(dto: TransactionDto, watcher: Watcher): TransactionDto {

            try{
            val result=CardBin.getCardType(dto,watcher)
            if(dto.cardType!=Undefined&&result==Undefined){
                return dto
            }
            dto.cardType=result
           }catch (ex:Exception){
               watcher.debug("trySetCardType ex: ${ex.message}")
          }
            return dto
        }

        fun trySetCardType(dto: TransactionDto): TransactionDto {

            try{
                val result=CardBin.getCardType(dto)
                if(dto.cardType!=Undefined&&result==Undefined){
                    return dto
                }
                dto.cardType=result
            }catch (ex:Exception){
                WatcherGlobalLog.debug("trySetCardType ex: ${ex.message}")
            }
            return dto
        }

        fun trySetCardType(dto: TransactionData, watcher: Watcher):TransactionData{
            val result=CardBin.getCardType(dto,watcher)
            if(dto.cardType!=Undefined&&result==Undefined){
                return dto
            }
            dto.cardType=result
            return dto
        }


        fun getValidCardsDescription():String{
            if(!validCardTypesDesc.equals("")) return validCardTypesDesc
            for(card in getNames()){
                if(card!="Undefined")
                 validCardTypesDesc += "$card,"
            }
            return validCardTypesDesc
        }

        var types= mutableListOf<String>()
        fun getNames(): MutableList<String>{
            if(types.size==0) {
                values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }


    }

}