package com.viergegroup.isoprocessor.models.tokens

import com.shared.commons.ErrorCode
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.isoprocessor.commons.IsoCommons
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.isoprocessor.models.descriptors.TokenDescriptor
import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.transactions.TransactionData

class TokenData(var hostType: HostType, var watcher: Watcher) {

    var tokenId=TokenId.Token00
    var tokenLength:Int=0
 

    var tokenFields= mutableListOf<TokenField>()

    var tokenDescriptor: TokenDescriptor?=null
    var emvData=""

    fun setField(fieldNumber:Int, transactionData: TransactionData?, fieldValue:String, tokenFieldDescriptor: TokenFieldDescriptor){

        val tokenField= TokenField()
        var defaultValue=fieldValue
        watcher.debug("SET_FIELD_fieldValue: $fieldValue ")

        if(transactionData!=null) {
            val defVal = tokenFieldDescriptor.tryToGetDefaultValue(transactionData,fieldValue, watcher)
            if ( !defVal.equals("")&&fieldValue=="" ) { 
               watcher.debug("SETTING DEFAULT VALUE: $defVal ")
                defaultValue = defVal
            }
        }
        tokenField.fieldNumber=fieldNumber
        tokenField.fieldValueString=defaultValue
        tokenField.fieldName=tokenFieldDescriptor.fieldName
        tokenField.fieldDescription=tokenFieldDescriptor.fieldDescription
        tokenField.fieldLength=tokenFieldDescriptor.fieldLength
        tokenField.fieldVarName=tokenFieldDescriptor.fieldVarName
        tokenField.hasDefaultValue=tokenFieldDescriptor.hasDefaultValue

        if(tokenFieldDescriptor.lengthIsVariable||tokenFieldDescriptor.fieldLengthIndicator>0){
            val fieldValueFinal=defaultValue.length.toString().padStart(tokenFieldDescriptor.fieldLengthIndicator,'0')+defaultValue

            val fieldLengthString=fieldValueFinal.substring(0,tokenFieldDescriptor.fieldLengthIndicator)
            if(!tokenFieldDescriptor.lengthIsVariable&&fieldValueFinal.length>tokenFieldDescriptor.fieldLength){
            //    throw WebServiceException(ErrorCode.InvalidFormatField," Invalid  Length The Max IS ${tokenFieldDescriptor.fieldLength} AND The Length of \"${fieldValueFinal}\" IS ${fieldValueFinal.length}  ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField," Invalid  Length The Max IS ${tokenFieldDescriptor.fieldLength} AND The Length of \"${fieldValueFinal}\" IS ${fieldValueFinal.length}  ",tokenFieldDescriptor)
            }
            var fieldLen: Int=0
            try{
                fieldLen=fieldLengthString.toInt()
            }catch (e:Exception){
               // throw WebServiceException(ErrorCode.InvalidFormatField,"TOKEN Invalid format Length \"${fieldLengthString}\" FOR ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"TOKEN Invalid format Length \"${fieldLengthString}\" FOR ",tokenFieldDescriptor)

            }
            val expectedLen=defaultValue.length-tokenFieldDescriptor.fieldLengthIndicator
            if(defaultValue.length<expectedLen){

               // throw WebServiceException(ErrorCode.InvalidFormatField,"Insuficient Data , Length Expected Is $fieldLen BUT Data Length IS ${expectedLen} IN -> ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"Insuficient Data , Length Expected Is $fieldLen BUT Data Length IS ${expectedLen} IN -> ",tokenFieldDescriptor)

            }



        }else{
            if(defaultValue.length<tokenFieldDescriptor.fieldLength){
             //   throw WebServiceException(ErrorCode.InvalidFormatField,"Length Must be ${tokenFieldDescriptor.fieldLength} NOT  ${defaultValue.length}  VALUE: \"${defaultValue}\"  FOR ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"Length Must be ${tokenFieldDescriptor.fieldLength} NOT  ${defaultValue.length}  VALUE: \"${defaultValue}\"  FOR ",tokenFieldDescriptor)
            }
        }
        if(tokenFieldDescriptor.fieldType.equals("ANS")){
            if(!IsoCommons.isAlphaNumeric(defaultValue)){
             //   throw WebServiceException(ErrorCode.InvalidFormatField," Field MUST BE ALPHANUMERIC ,VALUE: \"${defaultValue}\"  ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField," Field MUST BE ALPHANUMERIC ,VALUE: \"${defaultValue}\"  ",tokenFieldDescriptor)

            }
        }
        if(tokenFieldDescriptor.fieldType.equals("N")){
            if(!IsoCommons.isNumeric(defaultValue)){
              //  throw WebServiceException(ErrorCode.InvalidFormatField," Field MUST BE NUMERIC ,VALUE: \"${defaultValue}\"  ",tokenFieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField," Field MUST BE NUMERIC ,VALUE: \"${defaultValue}\"  ",tokenFieldDescriptor)

            } 

        }

        val tokenExists=tokenFields.firstOrNull { it.fieldNumber==fieldNumber }
        if(tokenExists!=null){
            tokenFields.remove(tokenExists)
        }
        tokenFields.add(tokenField)

    }

    fun setField(fieldNumber:Int,fieldValue:String,transactionData:TransactionData){
        if(fieldValue.trim().equals("")) return
        if(tokenId==TokenId.Token00){
        //    throw WebServiceException(ErrorCode.InvalidFormatField," Token DEFAULT IS NOT VALID",watcher)
            watcher.error(ErrorCode.InvalidFormatField," Token DEFAULT IS NOT VALID")

        }

         tokenDescriptor=Iso8583Parser.getIsoDescriptorData(hostType,watcher).field63Descriptor.tokenDescriptors.firstOrNull{ it.tokenId==tokenId.tokenIdString}

        if(tokenDescriptor==null){
          //  throw WebServiceException(ErrorCode.InvalidFormatField,"There is not GUIDE FOR TOKEN ${tokenId.tokenIdString}",watcher)
            watcher.error(ErrorCode.InvalidFormatField,"There is not GUIDE FOR TOKEN ${tokenId.tokenIdString}")

        }


        val fieldDescriptor = tokenDescriptor!!.tokenFieldDescriptors.firstOrNull{ it.fieldNumber == fieldNumber }

        if(fieldDescriptor!=null){
            setField(fieldNumber,transactionData,fieldValue,fieldDescriptor)
        }else{
           // throw WebServiceException(ErrorCode.InvalidTokenFormant," WE DONT HAVE GUIDE FOR fieldNumber $fieldNumber FOR TOKEN ${tokenId.tokenIdString}",watcher)
            watcher.error(ErrorCode.InvalidTokenFormant," WE DONT HAVE GUIDE FOR fieldNumber $fieldNumber FOR TOKEN ${tokenId.tokenIdString}")

        }
 

    }

    fun describe():String{
        emvData=""
        var description="\nTOKEN ${tokenId.tokenIdString} \n"
        tokenDescriptor=Iso8583Parser.getIsoDescriptorData(hostType,watcher).field63Descriptor.tokenDescriptors.firstOrNull{ it.tokenId==tokenId.tokenIdString}

        var fieldNumber=1
        if(tokenDescriptor==null) {
            description+="NO HAY DESCRIPTOR DE TOKEN ${tokenId}"
            return description

        }
        for(tokenField in tokenFields){
            var fieldInfo: String

            val fieldDescriptor =
                tokenDescriptor!!.tokenFieldDescriptors.firstOrNull { it.fieldNumber == fieldNumber }

            if(fieldDescriptor==null) {
                description+="NO HAY FIELD DESCRIPTOR DE TOKEN  CAMPO ${tokenField.fieldName} "
                continue

            }
 


                    var isNVar=""
                    var fieldLen = fieldDescriptor.fieldLength
                    val type=fieldDescriptor.fieldType
                    var isDefault=""
                    if(fieldDescriptor.fieldLengthIndicator!=-1) {
                        isNVar = "x"
                        fieldLen= tokenField.fieldValueString.length
                    }
                    if(fieldDescriptor.hasDefaultValue){
                        isDefault="D"
                    }
                    fieldInfo="$isDefault($isNVar${type}($fieldLen))"

                if(fieldDescriptor.emvTag!=""){
                    emvData+="${fieldDescriptor.emvTag} (${tokenField.fieldVarName}) ->  \"${tokenField.fieldValueString}\" \n"
                }

            description+="\t ${tokenField.fieldName} (EMV_TAG: ${fieldDescriptor.emvTag}) $fieldInfo: \"${tokenField.fieldValueString}\" \n ${fieldDescriptor.defaultValueDescription}\n"
            fieldNumber++

        }


        return description

    }


}