package com.viergegroup.isoprocessor.models.descriptors

import com.shared.commons.ErrorCode
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.builder.Iso8583Builder
import com.viergegroup.swmain.entities.transactions.TransactionData
import java.util.ArrayList

class TokenDescriptor {

    var tokenId:String=""
    var tokenFieldDescriptors: ArrayList<TokenFieldDescriptor> = arrayListOf()
    var mandatoryIf=""
    var removeIf=""
    var isMandatory=false



    fun removeIn(transactionData: TransactionData, watcher: Watcher):Boolean{
        var remove=false

        if(!removeIf.equals("")) {
            try {
                remove = Iso8583Builder.evaluateRemovefExpression(removeIf,transactionData,watcher)
                if(remove)
                 watcher.debug("REMOVE_IF EXPRESSION= $removeIf isMandatory $remove IN $tokenId")

            }catch (e:Exception){
                throw  WebServiceException(ErrorCode.InvalidScriptExpression,"REMOVE_IF tokenId: "+tokenId+" expression:\""+removeIf+"\" : "+e.message,watcher)
            }
        }



        return remove


    }
    fun isMandatory(transactionData: TransactionData, watcher: Watcher):Boolean{

        if(!mandatoryIf.equals("")) {
            try {

                isMandatory = Iso8583Builder.evaluateMandatoryIfExpression(mandatoryIf,transactionData,watcher)

                if(isMandatory)
                    watcher.debug("IS_MANDATORY EXPRESSION= $mandatoryIf isMandatory $isMandatory IN $tokenId")

            }catch (e:Exception){
                throw  WebServiceException(ErrorCode.InvalidScriptExpression,"IS_MANDATORY tokenId: "+tokenId+" expression:\""+mandatoryIf+"\" : "+e.message,watcher)
            }
        }


        return isMandatory


    }
}