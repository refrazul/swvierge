package com.viergegroup.swmain.entities.catalogs



enum class ModuleId (var moduleId:Int,var moduleName:String){
    Undefined(99,"Undefined"),
    All(0,"All"),
    Comm(1,"Comm"),
    Transactions(2,"Transactions"),
    Logs(3,"Logs"),
    Pandora(4,"Pandora"),
    Vault(5,"Vault"),
    MyHsm(6,"CarRental"),
    Iso(7,"Iso"),
    Emv(8,"Emv"),
    Config(9,"Config"),
    DB(10,"DB"),
    Gateway(11,"Gateway"),
    ;


    companion object{
        private val map = ModuleId.values().associateBy(
            ModuleId::moduleName)
        fun fromMerchantName(moduleName: String):ModuleId {
            val result= map[moduleName]
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