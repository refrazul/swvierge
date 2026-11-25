package com.viergegroup.isoprocessor.commons

import com.viergegroup.isoprocessor.models.descriptors.Iso8583FieldDescriptor
import com.viergegroup.isoprocessor.models.descriptors.PaddingType
import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor

object IsoCommons {
    fun isNumeric(inputString:String):Boolean{
        val pattern = "^[0-9 ]*\$".toRegex()

        return pattern.containsMatchIn(inputString)
    }
    fun isAlphaNumeric(inputString:String):Boolean{
        val pattern = "^[a-zA-Z0-9!_. •=/\\-\\*\\+&]*$".toRegex()

        return pattern.containsMatchIn(inputString.replace("+",""))
    }

    fun setPaddingToValue(value:String,descriptor: TokenFieldDescriptor):String{
        if(descriptor.paddingType==PaddingType.Right&&descriptor.paddingChar.isNotEmpty()){
            return value.padEnd(descriptor.fieldLength,descriptor.paddingChar.toCharArray()[0])
        }
        if(descriptor.paddingType==PaddingType.Left&&descriptor.paddingChar.isNotEmpty()){
            return value.padStart(descriptor.fieldLength,descriptor.paddingChar.toCharArray()[0])
        }

        return value

    }
    fun setPaddingToValue(value:String,descriptor: Iso8583FieldDescriptor):String{
        if(descriptor.paddingType==PaddingType.Right&&descriptor.paddingChar.isNotEmpty()){
            return value.padEnd(descriptor.fieldLength,descriptor.paddingChar.toCharArray()[0])
        }
        if(descriptor.paddingType==PaddingType.Left&&descriptor.paddingChar.isNotEmpty()){
            return value.padStart(descriptor.fieldLength,descriptor.paddingChar.toCharArray()[0])
        }

        return value

    }
}