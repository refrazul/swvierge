package com.viergegroup.swmain.entities.catalogs
  

enum class PointOfServiceEntryMode (var pemId:Int,var emvTagValue:String,var pointOfServiceEntryModeId:String,var pointOfServiceEntryModeName:String,var PointOfServiceEntryModeDescription:String){
    Undefined(99,"","0","Undefined","Undefined"),

    Inserted(1,"05","051","Inserted","Card Inserted"),
    Swiped(2,"02","021","Swiped","Card Swiped"),
    Fallback(3,"08","800","Fallback","Fallback"),
    ReCheckIn(4,"01","011","ReCheckIn","ReCheckIn"),
    Contactless(5,"07","071","Contactless","Contactless"),
    Manual(6,"00","011","Contactless","Contactless"), 
    Reversal(7,"91","911","Reversal","Reversal"),
    KeyLoad(8,"01","010","Keyload","KeyLoad");// CheckIn(51,"CheckIn");


    companion object{
        private val map = PointOfServiceEntryMode.values().associateBy(
            PointOfServiceEntryMode::pointOfServiceEntryModeId)
        fun fromPointServiceEntryModeId(pointOfServiceEntryModeId: String): PointOfServiceEntryMode {

            return map[pointOfServiceEntryModeId] ?: return Undefined
        }
        private val map2 = PointOfServiceEntryMode.values().associateBy(
            PointOfServiceEntryMode::emvTagValue)

        fun fromEmvTagValue(emvTagValue: String): PointOfServiceEntryMode {
            return map2[emvTagValue] ?: return Undefined
        }


        var types= mutableListOf<String>()

        fun getNames(): MutableList<String>{
            if(PointOfServiceEntryMode.types.size==0) {
                PointOfServiceEntryMode.values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }
    }

}