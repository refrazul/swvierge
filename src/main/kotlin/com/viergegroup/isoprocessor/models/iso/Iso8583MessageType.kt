package com.viergegroup.isoprocessor.models.iso

enum class Iso8583MessageType (var messageTypeId:String,var messageTypeDescription:String){
    Undefined("1111",""),

    Message0200("0200",""),
    Message9200("9200",""),

    Message0210("0210",""),

    Message0220("0220",""), 
    Message9220("9220",""), 
    Message0230("0230",""),
    Message0920("0920",""),

    Message0100("0100",""),
    Message0110("0110",""),
    Message0400("0400",""),  
    Message0410("0410",""),
    Message0420("0420",""),
    Message0430("0430",""), 
    Message9420("9420",""), 
 
    Message0800("0800",""),
    Message0810("0810","");
    companion object{
        private val map = Iso8583MessageType.values().associateBy(
            Iso8583MessageType::messageTypeId)
        fun fromMessageTypeString(messageTypeId: String):Iso8583MessageType{
            return map[messageTypeId] ?: return Undefined
        }
    }

}