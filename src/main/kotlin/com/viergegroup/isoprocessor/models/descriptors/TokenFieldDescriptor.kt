package com.viergegroup.isoprocessor.models.descriptors

import com.shared.commons.AppSettings
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import com.viergegroup.swmain.webwatcher.Watcher

class TokenFieldDescriptor {
    var tokenId: String=""
    var forceTakeDefaultValue=false

    var fieldNumber = 0
    var fieldName: String = ""
    var fieldVarName: String=""
    var fieldDescription:String=""
    var fieldType: String=""
    var fieldLength = 0
    var lengthIsVariable = false
    var fieldLengthIndicator = -1
    var fieldDefaultValue:String?=null
    var defaultValues= mutableListOf<Iso8583DefaultValue>()
    var hasDefaultValue=false
    var defaultValueDescription=""
    var emvTag=""
    var paddingType=PaddingType.Undefined
    var paddingChar:String=""

    fun tryToGetDefaultValue(transactionData: TransactionData, currentValue:String, watcher: Watcher):String{
        var defaultValue =""
        if(transactionData.transactionDirection== TransactionDirection.Response) return ""
        for(defaultVal in defaultValues){
            defaultValue=defaultVal.tryGetDefaultValue(this,transactionData,watcher)
            if(!defaultValue.equals("")&&((currentValue.equals("")||!defaultValue.equals(currentValue)&&defaultVal.forceTakeDefaultValue) || AppSettings.getInstance().forceIsoTakeDefaultValues)){
                hasDefaultValue=true
                if(currentValue.equals("")) 
                    defaultValueDescription="Default value Description: "+defaultVal.description
                else
                    defaultValueDescription="Default value Description: value \"$currentValue\" was changed to \"$defaultValue\","+defaultVal.description
                return Iso8583DefaultValue.tryGetDate(defaultValue, watcher)
            } 
        }
        if(fieldDefaultValue!=null&&currentValue==""){
            return fieldDefaultValue!!
        }

        return Iso8583DefaultValue.tryGetDate(defaultValue, watcher)


    }





}