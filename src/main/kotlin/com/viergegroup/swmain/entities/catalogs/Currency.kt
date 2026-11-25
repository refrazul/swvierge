package com.viergegroup.swmain.entities.catalogs

enum class Currency(var currencyId:Int,var currencyName:String="",var currencyCode:String) {
    Undefined(-1,"Undefined",""),
    MXN(1,"MXN","484"),
    USD(2,"USD","080");


    companion object{
        var currencyList= mutableListOf<Currency>()
        var currencyValues=
                Currency.values()
        fun getEnglishLevelsList(): MutableList<Currency> {
            if(currencyList.size==0) {
                currencyValues = Currency.values()
                currencyValues.forEach {
                    currencyList.add(it)
                }
            }
            return currencyList

        }
        private val mapCurrencyId = Currency.values().associateBy(
                Currency::currencyId)
        fun fromCurrencyId(type: Int) = mapCurrencyId[type]

        private val mapCurrencyCode = Currency.values().associateBy(
                Currency::currencyCode)
        fun fromCurrencyCode(type: String) = mapCurrencyCode[type]


    }
}