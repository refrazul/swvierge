package com.viergegroup.swmain.entities.catalogs

enum class PaymentPlanType (var paymentPlanId:Int,var paymentPlanName:String,var paymentPlanPROSACode:String="",var paymentPlanBANORTECode:String=""){
    Undefined(0,"Undefined","00","00"),
    SkipPayment(1,"SkipPayment","03","03"),
    WithInterests(2,"WithInterests","05","05"),
    WithoutInterests(3,"WithoutInterests","07","07");
    // CheckIn(51,"CheckIn");

    companion object{
        private val map = values().associateBy(
            PaymentPlanType::paymentPlanName)
        fun fromPaymentPlanName(paymentPlanName: String):PaymentPlanType{
            return  map[paymentPlanName] ?: Undefined
        }

        private var validpaymentPlansDesc=""
        fun getValidCardsDescription():String{
            if(!validpaymentPlansDesc.equals("")) return validpaymentPlansDesc
            for(card in getNames()){
                validpaymentPlansDesc +=card+","
            }
            return validpaymentPlansDesc
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