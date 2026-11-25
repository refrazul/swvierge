package com.viergegroup.swmain.entities.transactions

import com.viergegroup.swmain.entities.catalogs.TransactionType


enum class  TransactionDirection(var transactionDirectionId:Int,var transactionDirectionName:String){
    Undefined(99,"Undefined"),
    Request(1,"Request"),
    Response(2,"Response");


    companion object{
        private val map = values().associateBy(
            TransactionDirection::transactionDirectionName)
        fun fromTransactionDirectionName(transactionDirectionName: String) = map[transactionDirectionName]

        fun fromMessageType(messageTypeRequest: String): TransactionDirection {
            val enumClz = TransactionType.values()
            var result=enumClz.firstOrNull {it.messageTypeRequest==messageTypeRequest}

            if(result==null){
                result=enumClz.firstOrNull {it.messageTypeResponse==messageTypeRequest }
                if(result!=null)
                    return Response
            }else{
                return Request
            }

            return Undefined
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