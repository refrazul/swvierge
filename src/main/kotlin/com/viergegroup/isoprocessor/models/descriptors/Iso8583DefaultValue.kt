package com.viergegroup.isoprocessor.models.descriptors

import com.shared.commons.*
import com.viergegroup.isoprocessor.builder.Iso8583Builder
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection

import com.viergegroup.swmain.webwatcher.Watcher
import java.text.SimpleDateFormat
import java.util.*

class Iso8583DefaultValue {

    var forceTakeDefaultValue=false
    var defaultValue:String=""
    var putIf:String=""
    var description:String="WE REPLACE THE ORIGINAL VALUE BY THIS AUTOMATICALLY"


    companion object {

        fun tryGetDate(defaultValue:String, watcher: Watcher):String{
           watcher.debug("tryGetDate defaultValue $defaultValue")

            if(defaultValue!=""&&( 
                                  defaultValue.lowercase().contains("yy")
                                ||defaultValue.lowercase().contains("dd")
                                ||defaultValue.lowercase().contains("mm")
                                ||defaultValue.lowercase().contains("ss")
                                ||defaultValue.lowercase().contains("hh")
 
                        )){
                        watcher.debug("tryGetDate defaultValue $defaultValue")
                               try {
                                val formatter = SimpleDateFormat(defaultValue)
                                val date=formatter.format(Date())
                               watcher.debug("tryGetDate date $date")

                                return  formatter.format(Date())
                            } 
                            catch(ex:Exception){
                               watcher.debug("tryGetDate ex ${ex.message}")

                                return defaultValue
                            }
            }
            return defaultValue


        }
    }

    fun tryGetDefaultValue(fieldDescriptor: TokenFieldDescriptor, transactionData: TransactionData, watcher: Watcher):String{

        if(transactionData.transactionDirection== TransactionDirection.Response) return ""

        if(!putIf.equals("")) {
            try {
                val putBool =Iso8583Builder.  evaluateDefaultValueExpression(putIf,transactionData,watcher)

                if(putBool){
                    description=description.replace("\$this",defaultValue)
                   watcher.debug("DEFAULT_VALUE_SETTED \"${fieldDescriptor.fieldVarName}e\" EXPRESSION= $putIf description $description  putBool $putBool")

                    if(!fieldDescriptor.lengthIsVariable){
                        if(defaultValue.length!=fieldDescriptor.fieldLength){
                            throw  WebServiceException(ErrorCode.InvalidScriptExpression,"tryGetDefaultValue TOKEN: ${fieldDescriptor.tokenId} DEFAULT VALUE : "+defaultValue+" Expected Len is : "+fieldDescriptor.fieldLength+" VALUE LEN: "+defaultValue.length+" fieldVarName: "+fieldDescriptor.fieldVarName+" expression:\""+putIf,watcher)

                        }
                    }
                    return tryGetDate( defaultValue,watcher)
                }
            }catch (e:Exception){
                throw  WebServiceException(ErrorCode.InvalidScriptExpression," tryGetDefaultValue TOKEN: ${fieldDescriptor.tokenId}  fieldVarName: "+fieldDescriptor.fieldVarName+" expression:\""+putIf+"\" fieldName: "+fieldDescriptor.fieldName+" "+e.message,watcher)
            }
        }


        return ""


    }



    fun tryGetDefaultValue(fieldDescriptor: Iso8583FieldDescriptor, transactionData: TransactionData, watcher: Watcher):String{

        if(transactionData.transactionDirection==TransactionDirection.Response) return ""
        if(!putIf.equals("")) {

              val putBool = Iso8583Builder. evaluateDefaultValueExpression(putIf,transactionData,watcher)


                if(putBool){
                    description=description.replace("\$this",defaultValue)

                   watcher.debug("DEFAULT_VALUE_SETTED \"${fieldDescriptor.fieldVarName}e\" EXPRESSION= $putIf description $description  putBool $putBool")

                    if(!fieldDescriptor.lengthIsVariable){
                        if(defaultValue.length!=fieldDescriptor.fieldLength&&fieldDescriptor.fieldLengthIndicator<=0){
                            throw  WebServiceException(ErrorCode.InvalidScriptExpression,"tryGetDefaultValue DEFAULT VALUE : "+defaultValue+" Expected Len is : "+fieldDescriptor.fieldLength+" VALUE LEN: "+defaultValue.length+" fieldVarName: "+fieldDescriptor.fieldVarName+" expression:\""+putIf,watcher)

                        }
                    }


                    return tryGetDate( defaultValue,watcher)
                }

        }


        return ""


    }








}