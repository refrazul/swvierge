package com.viergegroup.swmain.entities.catalogs

enum class Authorizer(var authorizerId:Int,var authorizerName:String,var rsaKeyId:String,var computeInternalKeys:Boolean,var sendClearData:Boolean=false){
    Undefined(-1,"Undefined","",false),
    PROSA(1,"PROSA","A000VIE1",false),
    BANORTE(2,"BANORTE","",true,true),
    EGLOBAL(3,"EGLOBAL","",false), 
    OPENPAY(4,"OPENPAY","",false), 
    AMEX(5,"AMEX","A1111",false), 
    VELPAY(6,"VELPAY","VE0001",false),
    VIERGE(7,"VIERGE","V0001",true,true),
    YOLOPAGO(8,"YOLOPAGO","YO0001",true,false), 
    PAYNET(9,"PAYNET","NET0001",true,false),
    CLAROPAY(10,"CLAROPAY","CP0001",true,false), 
    CLAROPAYT2P(13,"CLAROPAYT2P","CP0002",true,false), 
 
    CREDIBANCO(11,"CREDIBANCO","CRED001",true),
    EVOPAYMENT(12,"EVOPAYMENT","EVO001",true) 

     
    ; 
    
    // CheckIn(51,"CheckIn");


    companion object{
        private val map = values().associateBy(
            Authorizer::authorizerName)
        fun fromMerchantName(authorizerName: String):Authorizer{
            return map[authorizerName] ?: Undefined
        }

        private var validauthorizersDesc=""
        fun getValidCardsDescription():String{
            if(!validauthorizersDesc.equals("")) return validauthorizersDesc
            for(card in getNames()){
                validauthorizersDesc +=card+","
            }
            return validauthorizersDesc
        }

        var types= mutableListOf<String>()
        fun getNames(): MutableList<String>{
            if(types.size==0) {
                Authorizer.values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }


    }

}