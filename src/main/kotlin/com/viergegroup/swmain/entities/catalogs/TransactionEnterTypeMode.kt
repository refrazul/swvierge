package com.viergegroup.swmain.entities.catalogs

enum class TransactionEnterTypeMode (var transactionEnterTypeModeId:Int,var transactionEnterTypeModeName:String,var transactionEnterTypeModeProsa:String) {
    TerminalPos(1,"TerminalPos","Card Inserted"),
    ECommerce(2,"ECommerce","Card Swiped");

    // CheckIn(51,"CheckIn");


    companion object{
        private val map = TransactionEnterTypeMode.values().associateBy(
            TransactionEnterTypeMode::transactionEnterTypeModeName)
        fun fromTransactionEnterTypeModeName(transactionEnterTypeModeName: String) = map[transactionEnterTypeModeName]
    }

}