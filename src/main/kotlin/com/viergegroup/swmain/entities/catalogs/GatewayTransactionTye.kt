package com.viergegroup.swmain.entities.catalogs

enum class GatewayTransactionTye (var gatewayTransactionTypeId:Int,var transactionType: TransactionType){
    undefined(-1,TransactionType.Undefined),
    purchase(1,TransactionType.Sale),
    text(3,TransactionType.ReadCard),
    refund(2,TransactionType.Refund);
   


    companion object{
        private val map = TransactionType.values().associateBy(
            TransactionType::transactionTypeName)
        fun fromTransactionTypeName(transactionTypeName: String) = map[transactionTypeName]



    }

}