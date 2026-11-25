package com.viergegroup.isoprocessor.models.descriptors


import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.builder.Iso8583Builder
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection


class Iso8583FieldDescriptor {

    var fieldNumber = 0
    var fieldName: String=""
    var fieldVarName: String=""

    var fieldType: String=""
    var fieldLength = 0
    var lengthIsVariable = false
    var fieldLengthIndicator = -1
    var metaDescriptorClassName: String=""
    var fieldDescription: String=""
    var defaultValues= mutableListOf<Iso8583DefaultValue>()
    var mandatoryIf=""
    var removeIf=""
    var isMandatory=false
    var hasDefaultValue=false
    var defaultValueDescription=""
    var paddingType=PaddingType.Undefined
    var paddingChar:String=""

 


    fun tryToGetDefaultValue(transactionData: TransactionData, currentValue:String, watcher: Watcher):String{
        var defaultValue =""
        if(transactionData.transactionDirection== TransactionDirection.Response) return ""

        for(defaultVal in defaultValues){
            defaultValue=defaultVal.tryGetDefaultValue(this,transactionData,watcher)
            if(!defaultValue.equals("")&&((currentValue.equals("")||!defaultValue.equals(currentValue) &&defaultVal.forceTakeDefaultValue) || AppSettings.getInstance().forceIsoTakeDefaultValues)){
                hasDefaultValue=true
                if(currentValue.equals(""))
                defaultValueDescription="Default value Description: "+defaultVal.description
                else
                    defaultValueDescription="Default value Description: value \"$currentValue\" was changed to \"$defaultValue\","+defaultVal.description

                return defaultValue
            }
        }


        return defaultValue


    }




    fun isMandatory(transactionData: TransactionData, watcher: Watcher):Boolean{
        var expression =""

        if(!mandatoryIf.equals("")) {
            try {
                isMandatory = Iso8583Builder.evaluateMandatoryIfExpression(mandatoryIf,transactionData,watcher)

                if(isMandatory)
                   watcher.debug("IS_MANDATORY EXPRESSION= " + mandatoryIf+" isMandatory "+isMandatory)

            }catch (e:Exception){
                throw  WebServiceException(ErrorCode.InvalidScriptExpression,"isMandatory fieldVarName: "+fieldVarName+" expression:\""+expression+"\" fieldName: "+fieldName+" "+e.message,watcher)
            }
        }


        return isMandatory


    }


    fun removeIn(transactionData: TransactionData, watcher: Watcher):Boolean{
        var remove=false



        if(!removeIf.equals("")) {
            try {
                remove = Iso8583Builder.evaluateRemovefExpression(removeIf,transactionData,watcher)
                if(remove)
                   watcher.debug("REMOVE_IF EXPRESSION= " + removeIf+" isMandatory "+remove)

            }catch (e:Exception){
                throw  WebServiceException(ErrorCode.InvalidScriptExpression,"REMOVE_IF fieldVarName: "+fieldVarName+" expression:\""+removeIf+"\" fieldName: "+fieldName+" "+e.message,watcher)
            }
        }


        return remove


    }





    fun getFieldInfo(): String? {
        var info = " var [$fieldVarName] | fieldNumber: [$fieldNumber] "
        info += "fieldName: [$fieldName] "
        info += "fieldVarName: [$fieldVarName] "
        info += "fieldLength:  [$fieldLength] "
        info += "lengthIsVariable: [$lengthIsVariable]  "
        info += "fieldLengthIndicator: [$fieldLengthIndicator]   "
        return info
    }
}