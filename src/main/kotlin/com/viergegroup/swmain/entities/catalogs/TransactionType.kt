package com.viergegroup.swmain.entities.catalogs

import com.viergegroup.swmain.entities.transactions.TransactionData
 

enum class TransactionType (var transactionTypeId:Int,var transactionTypeName:String,var isoHeader:String,var isoHeaderResponse:String,var messageTypeRequest:String,var messageTypeResponse:String,var processingCode:String="000000",var transactionMessageType:TransactionMessageType=TransactionMessageType.Financial,var mainMerchantTypesAllowed:MutableList<MerchantType>){
    Undefined(99,"Undefined","ISO026000050","ISO026000045","","","",TransactionMessageType.Undefined,mutableListOf(MerchantType.Undefined)),
    Sale(0,"Sale","ISO026000050","ISO026000045","0200","0210","090000",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail,MerchantType.Restaurant)),
    DeferredPayment(35,"DeferredPayment","ISO026000050","ISO026000045","0200","0210","090000",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail,MerchantType.Restaurant)),

    SaleWithTip(1,"SaleWithTip","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.Financial,mutableListOf(MerchantType.Restaurant)),
    SalePosTip(2,"SalePosTip","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.Financial,mutableListOf(MerchantType.Restaurant)),
    Cancellation(5,"Cancellation","ISO026000050","ISO026000045","0220","0230","020000",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
   
    Refund(6,"Refund","ISO026000050","ISO026000045","0200","0210","200000",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail)),
    ClosePosTip(7,"ClosePosTip","ISO026000050","ISO026000045","0220","0230","000000",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail)),
  
   
 
    CheckIn(8,"CheckIn","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    CheckOut(9,"CheckOut","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail)),
   
   

  //  CheckIn(8,"CheckIn","ISO026000050","ISO026000045","0200","0210","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
   // CheckOut(9,"CheckOut","ISO026000050","ISO026000045","0220","0230","000001",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail)),
   
      

    SaleNotShow(3,"SaleNotShow","ISO026000050","ISO026000045","0200","0210","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Hotel,MerchantType.CarRental)),
    SaleForced(4,"SaleForced","ISO026000050","ISO026000045","0220","0230","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail,MerchantType.Restaurant)),
   
    RentalIn(10,"RentalIn","ISO026000050","ISO026000045","0200","0210","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    CarReturn(11,"CarReturn","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
 

    Balance(12,"Balance","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    Deposit(13,"Deposit","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    Consult(14,"Consult","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    CommunicationTest(15,"CommunicationTest","ISO026000045","ISO026000045","0800","0810","000001",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail)),
  //  Reversal(16,"Reversal","ISO026000050","ISO026000045","0400","0410","000001"),
   
    CheckInReaut(18,"CheckInReaut","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail)),
   
    RentalInReaut(19,"RentalInReaut","ISO026000050","ISO026000045","0100","0110","000001",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail)),
    
    KeyLoad(20,"KeyLoad","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.KeyLoad,mutableListOf(MerchantType.Retail)),
    Reverse(21,"Reverse","ISO026000050","ISO026000045","0420","0430","000000",TransactionMessageType.Reverse,mutableListOf(MerchantType.Retail)),


    LogOn(22,"LogOn","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail)),
    EchoTest(23,"EchoTest","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail)),
 //   RefundAMEX(24,"RefundAMEX","ISO006000050","ISO026000045","0220","0230","",TransactionMessageType.Advice,mutableListOf(MerchantType.Retail));
    KeyLoadConfirm(24,"KeyLoadConfirm","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.KeyLoad,mutableListOf(MerchantType.Retail)),
    
    KeyLoadPin(27,"KeyLoadPin","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.KeyLoad,mutableListOf(MerchantType.Retail)),
    KeyLoadPinConfirm(28,"KeyLoadPinConfirm","ISO026000050","ISO026000045","0200","0210","000000",TransactionMessageType.KeyLoad,mutableListOf(MerchantType.Retail)),
   
    
    LogOff(22,"LogOff","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail)),
    CashBack(25,"CashBack","ISO026000050","ISO026000045","0200","0210","090000",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail,MerchantType.Restaurant)),
    ReadCard(26,"ReadCard","ISO026000050","ISO026000045","0200","0210","090000",TransactionMessageType.Financial,mutableListOf(MerchantType.Retail,MerchantType.Restaurant)),

  
     KeyMACNew(27,"KeyMACNew","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
     KeyPINNew(28,"KeyPINNew","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),

     KeyMACChange(29,"KeyMACChange","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
     KeyPINChange(30,"KeyPINChange","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),


     KeyMACRepeat(31,"KeyMACRepeat","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
     KeyPINRepeat(32,"KeyPINRepeat","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),

    KeyMACVerify(33,"KeyMACVerify","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
    KeyPINVerify(34,"KeyPINVerify","ISO006000050","ISO026000045","0800","0810","",TransactionMessageType.Comm,mutableListOf(MerchantType.Retail,MerchantType.Restaurant,MerchantType.Hotel,MerchantType.CarRental)),
 
    ReverseFailed(35,"ReverseFailed","ISO026000050","ISO026000045","0420","9420","000000",TransactionMessageType.Reverse,mutableListOf(MerchantType.Retail))


    ;
    // CheckIn(51,"CheckIn");


    companion object{
        private val map = TransactionType.values().associateBy(
            TransactionType::transactionTypeName)
        fun fromTransactionTypeName(transactionTypeName: String) = map[transactionTypeName]


        /* private val map2 = TransactionType.values().associateBy(
             TransactionType::messageTypeRequest,TransactionType::processingCode)
         fun fromTransactionFromRequest(messageTypeRequest: String,processingCode:String) = map2[messageTypeRequest,processingCode]
 */

        fun fromTransactionFromRequest(transactionData: TransactionData): TransactionType {
            val enumClz = TransactionType.values()
            var result:TransactionType?
            result=enumClz.firstOrNull {it.messageTypeRequest==transactionData.getMessageTypeString() && it.processingCode==transactionData.processingCode}

            if(transactionData.networkManagmentInformationCode.equals("001")) return LogOn
            if(transactionData.networkManagmentInformationCode.equals("301")) return EchoTest

            if(result==null)
                result=enumClz.firstOrNull {it.messageTypeResponse==transactionData.getMessageTypeString() && it.processingCode==transactionData.processingCode}


            if(result==null) return Undefined
            return result
        }
        var types= mutableListOf<String>()

        fun getNames(): MutableList<String>{
            if(types.size==0) {
                TransactionType.values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }

    }

}