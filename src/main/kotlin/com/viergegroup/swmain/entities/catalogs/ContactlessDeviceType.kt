package com.viergegroup.swmain.entities.catalogs

enum class ContactlessDeviceType (var contactlessDeviceTypeId:Int,var contactlessDeviceTypeName:String,var contactlessDeviceTypeProsa:String) {
    Card(1,"Card","Card Inserted"),
    Mobile(2,"Mobile","Card Swiped");
    // CheckIn(51,"CheckIn");


    companion object{
        private val map = ContactlessDeviceType.values().associateBy(
            ContactlessDeviceType::contactlessDeviceTypeName)
        fun fromContactlessDeviceTypeName(contactlessDeviceTypeName: String) = map[contactlessDeviceTypeName]
    }

} 