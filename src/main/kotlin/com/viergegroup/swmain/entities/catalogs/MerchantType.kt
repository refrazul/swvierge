package com.viergegroup.swmain.entities.catalogs

enum class MerchantType (var merchantTypeId:Int,var merchantTypeName:String,var merchantTypeCode:String=""){
    Undefined(99,"Undefined",""),
    Retail(1,"Retail","5422"),
    Restaurant(2,"Restaurant","5812"),
    Comm(7,"Comm","0000"),
    Hotel(3,"Hotel","5399"),
    CarRental(4,"CarRental","5390"),
    Office(5,"Office","5428");


    companion object{ 
        private val map = MerchantType.values().associateBy(
            MerchantType::merchantTypeName)
        fun fromMerchantName(merchantTypeName: String):MerchantType {
            val result= map[merchantTypeName]
            if(result==null) return Undefined
            return result
        }

        private val map2 = MerchantType.values().associateBy(
            MerchantType::merchantTypeCode)
        fun fromMerchantCode(code: String):MerchantType {
            val result= map2[code]

            if(result==null) return Undefined


            return result
        }

        var types= mutableListOf<String>()

        fun getNames(): MutableList<String>{
            if(types.size==0) {
                MerchantType.values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }
    }

}