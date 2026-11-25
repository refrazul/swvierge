package com.viergegroup.swmain.entities.catalogs

enum class TransactionMessageType (var transactionMessageTypeId:Int,var transactionMessageTypeName:String){
    Undefined(-1,"Undefined"),
    Financial(1,"Financial"),
    Advice(2,"Advice"),
    Comm(3,"Comm"),
    KeyLoad(4,"KeyLoad"), 
    KeyLoadPin(7,"KeyLoadPin"), 
  
    Reverse(5,"Reverse"),
    Closure(6,"Closure");  
}