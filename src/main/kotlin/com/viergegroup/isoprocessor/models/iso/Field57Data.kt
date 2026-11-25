package com.viergegroup.isoprocessor.models.iso

import com.shared.commons.ErrorCode
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.isoprocessor.models.tokens.TokenData
import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor
import com.viergegroup.isoprocessor.models.tokens.TokenId
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher

class Field57Data(var hostType: HostType, var watcher: Watcher) {

    var tokens= mutableListOf<TokenData>()
    companion object{
        val field57LengthIndictator=3
    }
    fun hasTokens():Boolean{
        return tokens.size>0
    }
    fun buildField():String{
        var buffer=""


        for(token in tokens){
            var tokenHeader=token.tokenId.tokenIdString
            var tokenBody=""
            val tokenDescriptorData=Iso8583Parser.getIsoDescriptorData(hostType,watcher).field57Descriptor.tokenDescriptors.firstOrNull{it.tokenId==token.tokenId.tokenIdString}
            if(tokenDescriptorData==null){ 
                throw WebServiceException(ErrorCode.InvalidTokenFormant,"TOKEN GUIDE \"${token.tokenId}\" NOT FOUND ",watcher)

            }
 
            for(tokenFieldDescriptor in tokenDescriptorData.tokenFieldDescriptors){
                val tokenFieldData=token.tokenFields.firstOrNull { it.fieldNumber==tokenFieldDescriptor.fieldNumber }
                watcher.debug("  fieldVarName  ${tokenFieldDescriptor.fieldVarName}  ")
                watcher.debug(" fieldName  ${tokenFieldDescriptor.fieldName} fieldNumber  ${tokenFieldDescriptor.fieldNumber} fieldLength  ${tokenFieldDescriptor.fieldLength}  ")
                watcher.debug("  lengthIsVariable  ${tokenFieldDescriptor.lengthIsVariable}  ")
                watcher.debug("  fieldDefaultValue  ${tokenFieldDescriptor.fieldDefaultValue}  ")

 


                if(tokenFieldData==null&&tokenFieldDescriptor.fieldDefaultValue==null){
                    throw WebServiceException(ErrorCode.InvalidTokenFormant,"Field ${tokenFieldDescriptor.fieldNumber} WAS NOT SET IN ",tokenFieldDescriptor,watcher)


                }


                var fieldValue=""
                if(tokenFieldData==null&&tokenFieldDescriptor.fieldDefaultValue!=null) {
                    fieldValue= tokenFieldDescriptor.fieldDefaultValue!!
                    watcher.debug(" CONCAT fieldDefaultValue (${tokenFieldDescriptor.fieldDefaultValue!!.length})  [${tokenFieldDescriptor.fieldDefaultValue}]  ")

                }
                if(tokenFieldData!=null){
                    fieldValue=tokenFieldData.fieldValueString
                    watcher.debug(" CONCAT fieldValueString (${tokenFieldData.fieldValueString.length})  [${tokenFieldData.fieldValueString}]  ")

                }
                if(tokenFieldDescriptor.lengthIsVariable ||tokenFieldDescriptor.fieldLengthIndicator>0){
                    val fieldLen=fieldValue.length.toString().padStart(tokenFieldDescriptor.fieldLengthIndicator,'0')
                    tokenBody+=fieldLen+fieldValue
                    watcher.debug(" CONCAT fieldValue (${fieldValue.length})  [${fieldValue}]  ")
                    watcher.debug(" CONCAT fieldDefaultValue (${tokenFieldDescriptor.fieldDefaultValue!!.length})  [${tokenFieldDescriptor.fieldDefaultValue}]  ")

                }else{
                    watcher.debug(" CONCAT fieldValue (${fieldValue.length})  [${fieldValue}]  ")
                    tokenBody+=fieldValue

                }



            }


           // 	WatcherGlobalLog.debug("TOKEN_ID ${token.tokenId} TOKENBODY $tokenBody ")

           watcher.debug(" tokenHeader ${tokenHeader} TOKEN_ID ${token.tokenId} TOKENBODY $tokenBody ")
            val tokenBuild="! "+tokenHeader+tokenBody.length.toString().padStart(5,'0')+" "+tokenBody

            buffer+=tokenBuild
         ///   	WatcherGlobalLog.debug("TOKEN_ID ${token.tokenId} tokenBuild $tokenBuild \n\n")
           watcher.debug("TOKEN_ID ${token.tokenId} tokenBuild $tokenBuild \n\n")

        }
        val finalLen=(buffer.length+12)
       // 	WatcherGlobalLog.debug(" FIELD 57 finalLen : $finalLen  buffer.length ${buffer.length}")

       watcher.debug(" FIELD 57 finalLen : $finalLen  buffer.length ${buffer.length}")
        buffer=finalLen.toString().padStart(field57LengthIndictator,'0')+"& "+(tokens.size+1).toString().padStart(5,'0')+finalLen.toString().padStart(5,'0') +buffer


      //  	WatcherGlobalLog.debug("FINAL FIELD 57: \"$buffer\" ")

       watcher.debug("FINAL FIELD 57: \"$buffer\" ")

        return buffer

    }


    fun setTokenValue(tokenId:TokenId,fieldNumber:Int,tokenFieldValue:String,transactionData: TransactionData){
        var tokenData=tokens.firstOrNull(){
            it.tokenId==tokenId
        } 

       if(tokenData==null) {
           tokenData= TokenData(hostType,watcher)
           tokenData.tokenId=tokenId
           tokens.add(tokenData)
       }

        tokenData.setField(fieldNumber,tokenFieldValue,transactionData)

    }


    fun setTokenValue(tokenId:TokenId, fieldNumber:Int, tokenFieldValue:String, tokenFieldDescriptor: TokenFieldDescriptor, transactionData: TransactionData?){
        var tokenData=tokens.firstOrNull(){
            it.tokenId==tokenId
        }
 
        if(tokenData==null) {
            tokenData= TokenData(hostType,watcher)
            tokenData.tokenId=tokenId
            tokens.add(tokenData)
        }

        tokenData.setField(fieldNumber,transactionData,tokenFieldValue,tokenFieldDescriptor)

    }




    fun setToken(tokenData:TokenData){
        val tokenExists=tokens.firstOrNull(){
            it.tokenId==tokenData.tokenId
        }
        if(tokenExists!=null){
            tokens.remove(tokenExists)
        }

        tokens.add(tokenData)

    }

    fun describe():String{
        var description=""
        var emvData=""
        for(token in tokens){
            description+=token.describe()+"\n"
            emvData+=token.emvData
        }

        description+="EMVDATA:\n $emvData \n"

        return description
    }


}