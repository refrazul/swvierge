package com.viergegroup.swmain.entities.catalogs

enum class TransactionStatus (var statusId:Int){
    Undefined(99),
    Approved(0),
    ReceivedInWebService(1),
    SentToHost(2), 
    WaitingForResponse(3),
    HostResponseReceived(4),
    Rejected(5), 
    Error(6),
    HostTimeOut(7),
    MustBeReversed(8), 
    Refunded(9), 
    Cancelled(10),
    Reversed(11) ,
    ReversedFailed(12)  ,
   ResponseReceivedLately(13)  

 
    ;


}