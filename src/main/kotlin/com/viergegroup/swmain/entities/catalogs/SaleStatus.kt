package com.viergegroup.swmain.entities.catalogs

enum class SaleStatus(var saleStatusId:Int) {
    Active(1),
    Canceled(2),
    Refunded(3),
    Reversed(4);


    companion object{
        var saleStatusList= mutableListOf<SaleStatus>()
        var saleStatusValues=
                SaleStatus.values()
        fun getEnglishLevelsList(): MutableList<SaleStatus> {
            if(saleStatusList.size==0) {
                saleStatusValues = SaleStatus.values()
                saleStatusValues.forEach {
                    saleStatusList.add(it)
                }
            }
            return saleStatusList

        }
        private val mapSaleStatusId = SaleStatus.values().associateBy(
                SaleStatus::saleStatusId)
        fun fromSaleStatusId(type: Int) = mapSaleStatusId[type]




    }
}