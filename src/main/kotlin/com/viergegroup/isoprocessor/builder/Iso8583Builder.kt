package com.viergegroup.isoprocessor.builder
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.google.gson.GsonBuilder
import com.shared.commons.*
import com.viergegroup.isoprocessor.models.descriptors.Iso8583DescriptorData
import com.viergegroup.isoprocessor.models.iso.Field63Data
import com.viergegroup.isoprocessor.models.iso.Iso8583Message
import com.viergegroup.isoprocessor.models.iso.Field57Data


import com.viergegroup.isoprocessor.models.iso.Iso8583MessageType
import com.viergegroup.isoprocessor.models.tokens.TokenId
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.swmain.entities.catalogs.*
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.entities.transactions.TransactionDirection

import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.File
import java.io.PrintWriter
import javax.script.ScriptEngineManager


object Iso8583Builder {

    private  var sem = ScriptEngineManager()
    private var se = sem.getEngineByName("JavaScript")
    private var expressionMapDefaultValue= mutableMapOf<String,Boolean>()
    private var expressionMapMandatoryIf= mutableMapOf<String,Boolean>()
    private var expressionMapRemoveIf= mutableMapOf<String,Boolean>()

    init {
        resetExpressionMap()
    }
    class MapWrapper{
        var expressionMapDefaultValue= mutableMapOf<String,Boolean>()
        var expressionMapMandatoryIf= mutableMapOf<String,Boolean>()
        var expressionMapRemoveIf= mutableMapOf<String,Boolean>()

    }
    fun resetExpressionMap(){

        WatcherGlobalLog.info("resetExpressionMap")
        expressionMapDefaultValue.clear()
        expressionMapMandatoryIf.clear()
        expressionMapRemoveIf.clear()
        for (hostType in HostType.values()){
            saveExpressionMap(hostType)
        }
    }

        fun resetExpressionMap(hostType:HostType){

            WatcherGlobalLog.info("resetExpressionMap")
             expressionMapDefaultValue.clear()
             expressionMapMandatoryIf.clear()
             expressionMapRemoveIf.clear()
            saveExpressionMap(hostType)
        }


        fun saveExpressionMap(hostType:HostType){
            val dataPath = AppSettings.getAuthorizerConfig(hostType).isoExpressionsMapPath
            //println("saveExpressionMap INI")

            try {
                val myObj = File(AppSettings.getAuthorizerConfig(hostType).isoExpressionsMapPath)
                if (!myObj.exists()) {
                    myObj.createNewFile()
                }
                val gson = GsonBuilder().setPrettyPrinting().create()
                val data=MapWrapper()
                data.expressionMapMandatoryIf= expressionMapMandatoryIf
                data.expressionMapRemoveIf= expressionMapRemoveIf
                data.expressionMapDefaultValue= expressionMapDefaultValue

                val jsonString = gson.toJson(data)
                val writer = PrintWriter(dataPath)
                writer.print(jsonString)
                writer.close()
            } catch (ex: Exception) {

                WatcherGlobalLog.error("saveExpressionMap EXCEPTION: " + ex.message+" dataPath ${dataPath} OF  ${hostType}")
            }
        }

        fun cleanExpression(expression: String, transactionData: TransactionData, watcher: Watcher): String {


            return expression.trim().replace("\$tt", "'" + transactionData.transactionType.transactionTypeName + "'")
               .replace("\$kd", "'" + transactionData.keyDirection.keyDirectionName + "'") 
                .replace("\$ct", "'" + transactionData.cardType.cardTypeName + "'") 
                .replace("\$dt", "'" + transactionData.deviceType.name + "'") 
  
                .replace("\$pem", "'" + transactionData.pointOfServiceEntryMode.pointOfServiceEntryModeName + "'")
                .replace("\$mt", "'" + transactionData.merchantType.merchantTypeName + "'")
                .replace("\$ppt", "'" + transactionData.paymentPlanType.paymentPlanName + "'")
                .replace("\$paymentPlanType", "'" + transactionData.paymentPlanType.paymentPlanName + "'")
                .replace("\$tmt","'" + transactionData.transactionType.transactionMessageType.transactionMessageTypeName + "'" )
                .replace("\$transactionType", "'" + transactionData.transactionType.transactionTypeName + "'")
                .replace("\$tt", "'" + transactionData.transactionType.transactionTypeName + "'") 
                .replace("\$cardType", "'" + transactionData.cardType.cardTypeName + "'")
                .replace("\$posEntryMode", "'" + transactionData.pointOfServiceEntryMode.pointOfServiceEntryModeName + "'")
                .replace("\$merchantType", "'" + transactionData.merchantType.merchantTypeName + "'")

               // .replace("\$a", "" + StringUtils.formatAmountToCompare(transactionData.transactionAmount) + "")
                .replace("\$transactionAmount", "" + StringUtils.formatAmountToCompare(transactionData.transactionAmount,watcher) + "")
                .replace("\$amount", "" + StringUtils.formatAmountToCompare(transactionData.transactionAmount,watcher) + "")


                .replace("'AnyMt'", "'" + transactionData.merchantType.merchantTypeName + "'")
                .replace("'AnyPem'", "'" + transactionData.pointOfServiceEntryMode.pointOfServiceEntryModeName + "'")
                .replace("'AnyCt'", "'" + transactionData.cardType.cardTypeName + "'")
                .replace("'AnyTmt'","'" + transactionData.transactionType.transactionMessageType.transactionMessageTypeName + "'" )
                .replace("'AnyPpt'", "'" + transactionData.paymentPlanType.paymentPlanName + "'")
                .replace("'AnyTt'" ,"'" + transactionData.transactionType.transactionTypeName + "'")
                .replace("\$transactionMessageType", "'" + transactionData.transactionType.transactionMessageType + "'")
        }
        fun evaluateRemovefExpression(expression: String, transactionData: TransactionData, watcher: Watcher):Boolean{
            if(expression == "") return false
            val newExpression =cleanExpression(expression,transactionData,watcher)

            if(expressionMapRemoveIf.containsKey(newExpression)){
                return expressionMapRemoveIf[newExpression]!!
            }

         //   val validExp= validateExpression(newExpression)


            var evalResult=false
            try {
                evalResult = evalExpression(newExpression)
            }catch (e:Exception){
               watcher.error(e,"EXCEPTION EVALUATING EXPRESION $newExpression ")
              //  throw  WebServiceException(ErrorCode.InvalidScriptExpression,newExpression,logger)
            }
            if(!expressionMapRemoveIf.containsKey(newExpression)){
                expressionMapRemoveIf[newExpression] = evalResult
                saveExpressionMap(transactionData.hostType)
            }

            return evalResult



        }
        fun evalExpression(expression: String):Boolean{
            if(expression=="'Any'"||expression=="'Always'"||expression=="Any"||expression=="Always") return true
           // sem = ScriptEngineManager()
           // se = sem.getEngineByName("JavaScript")
           // val result: Boolean = BooleanExpressionEvaluator.evaluate(expression)
            val rhino: Context = Context.enter()

            val scope: Scriptable = rhino.initStandardObjects()
            val result: Any = rhino.evaluateString(scope, expression, "<cmd>", 1, null)
            return Context.toBoolean(result)
         //   return result// se.eval(expression).toString().toBoolean()
        }
        fun evaluateMandatoryIfExpression(expression: String, transactionData: TransactionData, watcher: Watcher):Boolean{
            if(expression == "") return false
            val newExpression =cleanExpression(expression,transactionData, watcher)

            if(expressionMapMandatoryIf.containsKey(newExpression)){
                return expressionMapMandatoryIf[newExpression]!!
            }

            //val validExp= validateExpression(newExpression)


            var evalResult=false
            try {
                evalResult = evalExpression(newExpression)
            }catch (e:Exception){
               watcher.error("EXCEPTION EVALUATING EXPRESION $newExpression ${e.message}")
            //    throw  WebServiceException(ErrorCode.InvalidScriptExpression,newExpression,logger)
            }
            if(!expressionMapMandatoryIf.containsKey(expression)){
                expressionMapMandatoryIf[expression] = evalResult
                saveExpressionMap(transactionData.hostType)
            }

            return evalResult



        }


        fun evaluateDefaultValueExpression(expression: String, transactionData: TransactionData, watcher: Watcher):Boolean{
            if(expression == "") return false
           watcher.debug("evaluateDefaultValueExpression EVALUATING : $expression")
            val newExpression =cleanExpression(expression,transactionData,watcher)

            if(expressionMapDefaultValue.containsKey(newExpression)){
               watcher.debug("evaluateDefaultValueExpression MAP_CONTAINS")

                return expressionMapDefaultValue[newExpression]!!
            }

          //  val validExp= validateExpression(newExpression)




           watcher.debug("evaluateDefaultValueExpression EVALUATING  CLEAN : $newExpression")

            var evalResult=false
            try {
                evalResult =  evalExpression(newExpression)
            }catch (e:Exception){
               watcher.error("EXCEPTION EVALUATING EXPRESION $newExpression ${e.message}")
              //  throw  WebServiceException(ErrorCode.InvalidScriptExpression,newExpression,logger)
            }
            if(!expressionMapDefaultValue.containsKey(newExpression)){
                expressionMapDefaultValue[newExpression] = evalResult
            }
            saveExpressionMap(transactionData.hostType)

            return evalResult



        }

        fun validateExpression(expression:String):String{
            if(expression == ""){
                if(expression.contains("\$")
                          ){

                    return "EXPRESSION CONTAINS AM INVALID VARIABLE "
                }
                if(!(expression.contains("'Hotel'")
                            ||expression.contains("'Retail'")
                            ||expression.contains("'Restaurant'")
                            ||expression.contains("'CarRental'")
                            ||expression.contains("'Office'")
                            ||expression.contains("'Sale'")||
                            expression.contains("'SaleWithTip'")
                            ||expression.contains("'SalePosTip'")
                            ||expression.contains("'SaleForced'")
                            ||expression.contains("'SaleNotShow'")
                            ||expression.contains("'CheckIn'")
                            ||expression.contains("'CheckOut'")
                            ||expression.contains("'RentalIn'")
                            ||expression.contains("'CarReturn'")
                            ||expression.contains("'Cancellation'")
                            ||expression.contains("'Refund'")
                            ||expression.contains("'RefundAMEX'")

                            ||expression.contains("'EchoTest'")
                            ||expression.contains("'LogOn'")
                            ||expression.contains("'LogOn'")
                            ||expression.contains("'Reverse'")
                            ||expression.contains("'Balance'")
                            ||expression.contains("'Deposit'")
                            ||expression.contains("'Consult'")
                            ||expression.contains("'Financial'")
                            ||expression.contains("'Advice'")
                            ||expression.contains("'Inserted'")
                            ||expression.contains("'Swiped'")
                            ||expression.contains("'Contactless'")
                            ||expression.contains("'FallBack'")
                            ||expression.contains("'Any'")
                            ||expression.contains("'Manual'")
                            ||expression.contains("'SkipPayment'")
                            ||expression.contains("'WithInterests'")
                            ||expression.contains("'WithooutInterests'")
                            ||expression.contains("'AnyMt'")
                            ||expression.contains("'AnyTt'")
                            ||expression.contains("'AnyPem'")
                            ||expression.contains("'AnyCt'")


                            )){

                    return "EXPRESSION DOES NOT CONTAINS A VALID VALUE"
                }
            }


            return ""
        }




     fun buildIso8583(transactionData: TransactionData):ByteArray{
        //val transactionData= TransactionData()
         val watcher= WatcherContext.getInstance()


       val rrn=transactionData.rrn
       val terminalSerialNumber=transactionData.terminalSerialNumber
       val transactionType=transactionData.transactionType
       // BeanUtils.copyProperties( transactionData,transactionDto);
         val buffer: String

        watcher.debug("\n================== Iso8583Builder START BUILDING ISO8583 ==================\n")
        try {
            if(transactionData.pointOfServiceEntryMode== PointOfServiceEntryMode.Undefined) {
                val posEntryMode =
                    PointOfServiceEntryMode.fromPointServiceEntryModeId(transactionData.pointOfServiceEntryModeCode)
                transactionData.pointOfServiceEntryMode= posEntryMode
            }else{
                transactionData.pointOfServiceEntryModeCode=transactionData.pointOfServiceEntryMode.pointOfServiceEntryModeId
            }


        }catch (e:Exception){
            throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Sorry But The Value of [pointOfServiceEntryMode] (\"${transactionData.pointOfServiceEntryMode}\") IS NOT VALID ",watcher)

        }

         if(transactionData.cardType== CardType.Undefined&&transactionData.transactionType.transactionMessageType== TransactionMessageType.Financial) {

                 throw WebServiceException(ErrorCode.InvalidFormatField,"  rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Sorry But You Need to Specify \"cardType\" or a valid \"cadNumber\" or \"cardBin\" Using Only This Values: ${CardType.getValidCardsDescription()} ",watcher)

         }







        if(transactionData.merchantType== MerchantType.Undefined){
            throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Merchant Type Must Be DEFINED IN Iso8583ProcessorManager.buildIso8583",watcher)

        }
        if(transactionData.transactionType== TransactionType.Undefined){
            throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Transaction Type Must Be DEFINED IN Iso8583ProcessorManager.buildIso8583",watcher)

        }

        val isoDescriptor=Iso8583Parser.getIsoDescriptorData(transactionData.hostType,watcher)

        var isoMessage=Iso8583Message(transactionData.hostType,transactionData.transactionType,watcher)

        val isoHeader=isoDescriptor.getIsoHeader(transactionData)

        watcher.debug("isoHeader $isoHeader")
         isoMessage.injectEmvTagsMap(transactionData.emvTagsMap) 
        isoMessage.isoHeader=isoHeader
        if(transactionData.transactionDirection== TransactionDirection.Request){
            val messageType=Iso8583MessageType.fromMessageTypeString(transactionData.transactionType.messageTypeRequest)

            isoMessage.isoMessageType=messageType
        }
        if(transactionData.transactionDirection==TransactionDirection.Response){
            val messageType=Iso8583MessageType.fromMessageTypeString(transactionData.transactionType.messageTypeResponse)
            isoMessage.isoMessageType=messageType
        }

        watcher.debug("//////////Iso8583Builder START BUILDING FIELDS")


        for (isoFieldDescriptor in isoDescriptor.fieldDescriptors){
            val isMandatory= isoFieldDescriptor.isMandatory(transactionData,watcher)
            val hastToBeRemoved= isoFieldDescriptor.removeIn(transactionData,watcher)


            var fieldValue=""
            try{
                if(isoFieldDescriptor.fieldVarName != "")
                    fieldValue=transactionData.getField(isoFieldDescriptor.fieldVarName)

            }catch(e:Exception){
                throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  UNDEFINED [${isoFieldDescriptor.fieldVarName}] Attribute IN TRANSACTIONDATA FIELD [${isoFieldDescriptor.fieldNumber}]",watcher)

            }
            if(isMandatory&&!hastToBeRemoved&& fieldValue == ""){
                throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Field  value: [$fieldValue] (${isoFieldDescriptor.fieldNumber})  var: [${isoFieldDescriptor.fieldVarName}] DESCRIPTION: [${isoFieldDescriptor.fieldDescription}]   Is Mandatory IN    -> ",transactionData,watcher)


            }

             val defaultValue = isoFieldDescriptor.tryToGetDefaultValue(transactionData, fieldValue, watcher)

            if(isMandatory&&!hastToBeRemoved|| defaultValue != "" || fieldValue != ""){
                if (defaultValue != "") {

                    fieldValue=defaultValue
                    transactionData.setField(isoFieldDescriptor.fieldVarName,defaultValue)
                   watcher.debug(" FIELD SET_DEFAULT NUMBER: ${isoFieldDescriptor.fieldNumber} VALUE: ${isoFieldDescriptor.fieldVarName} value:$defaultValue ")

                }
                if(!hastToBeRemoved) {
                    isoMessage.setField(isoFieldDescriptor.fieldNumber, fieldValue, isoFieldDescriptor)
                   watcher.debug("FIELD: ${isoFieldDescriptor.fieldNumber} ${isoFieldDescriptor.fieldVarName} : fieldValue: [$fieldValue] defaultValue: [${defaultValue}] fieldLen: ${fieldValue.length} expectedLen ${isoFieldDescriptor.fieldLength}  lengthIsVariable ${isoFieldDescriptor.lengthIsVariable}  isMandatory $isMandatory hastToBeRemoved $hastToBeRemoved ")
                }else{
                   watcher.debug("FIELD_REMOVED: ${isoFieldDescriptor.fieldNumber} ${isoFieldDescriptor.fieldVarName} : fieldValue: [$fieldValue] defaultValue: [${defaultValue}] fieldLen: ${fieldValue.length} expectedLen ${isoFieldDescriptor.fieldLength}  lengthIsVariable ${isoFieldDescriptor.lengthIsVariable}  isMandatory $isMandatory hastToBeRemoved $hastToBeRemoved ")

                }
            }




        }
        watcher.debug("//////////Iso8583Builder END BUILDING FIELDS\n\n")

          watcher.debug("FIELD_63_MUST_BE_SENDED : ${isoDescriptor.field63Descriptor.mustBeSended}\n\n")
          watcher.debug("FIELD_57_MUST_BE_SENDED : ${isoDescriptor.field57Descriptor.mustBeSended}\n\n")
        if( isoDescriptor.field57Descriptor.mustBeSended){
          isoMessage= buildField57Prosa(isoDescriptor,isoMessage,transactionData)
 
        }
        if( isoDescriptor.field63Descriptor.mustBeSended){
          isoMessage= buildField63Prosa(isoDescriptor,isoMessage,transactionData)

        }
      

        
         buffer=isoMessage.buildIsoBuffer()
        watcher.debug("\n Iso8583Builder ISO BUILD FINISH: [$buffer] \n")

         transactionData.injectIso8583(isoMessage)

        return  buffer.toByteArray( Charsets.US_ASCII)

    }


    fun buildField57Prosa(isoDescriptor: Iso8583DescriptorData, isoMessage:Iso8583Message, transactionData:TransactionData):Iso8583Message{
        val watcher= WatcherContext.getInstance()

        if(transactionData.hostType.authorizer!=Authorizer.PROSA) return isoMessage
       watcher.debug("\n buildField57Prosa ")
       watcher.debug("//////////Iso8583Builder START BUILD TOKENS") 
        var reasonNotBuildToken=""
        for( token in isoDescriptor.field57Descriptor.tokenDescriptors){
            val isMandatory= token.isMandatory(transactionData,watcher)
            val hastToBeRemoved= token.removeIn(transactionData,watcher)
            var tokenMustBeBuilded=true
            if(hastToBeRemoved){
                reasonNotBuildToken="REMOVE WHEN: [${token.removeIf}]"

            }
            for(tokenDescriptorTEMP in token.tokenFieldDescriptors){
                var fieldValue: String
                reasonNotBuildToken=""
                try{
                    fieldValue=transactionData.getField(tokenDescriptorTEMP.fieldVarName)

                }catch(e:Exception){
                    throw WebServiceException(ErrorCode.InvalidFormatField,"UNDEFINED FIELDVARNAME number:[${tokenDescriptorTEMP.fieldNumber}] name:[${tokenDescriptorTEMP.fieldName}] var:[${tokenDescriptorTEMP.fieldVarName}] Attribute IN TRANSACTIONDATA TOKEN [${tokenDescriptorTEMP.tokenId}]",watcher)

                }
                var defaultValue=""
                try {
                    defaultValue = tokenDescriptorTEMP.tryToGetDefaultValue(transactionData,fieldValue, watcher)
                }catch (w:WebServiceException){
                   watcher.debug("EXCEPTION  ID: [${token.tokenId}  var: [${tokenDescriptorTEMP.fieldVarName} IN tryToGetDefaultValue "+w.errorMessage)

                }catch (e:Exception){

                   watcher.debug("EXCEPTION ID: [${token.tokenId}  var: [${tokenDescriptorTEMP.fieldVarName}  IN tryToGetDefaultValue "+e.message)

                }
                if(fieldValue == "" && defaultValue == ""){
                    reasonNotBuildToken+=" || var: [${tokenDescriptorTEMP.fieldVarName}]  fieldValue or defaultValue [${defaultValue}] ||"
                    tokenMustBeBuilded=false
                    break
                }
            }
            if(!tokenMustBeBuilded||hastToBeRemoved) {

               watcher.debug("TOKEN_NOT_BUILDED_INFO ID: [${token.tokenId}] tokenMustBeBuilded ${tokenMustBeBuilded} hastToBeRemoved ${hastToBeRemoved} \n CHECK_DEFAULT_VALUE [$reasonNotBuildToken] ")
            }
            if(isMandatory){
               watcher.debug("TOKEN_MUST_BE_BUILDED_INFO BECAUSE: [${token.mandatoryIf}] ")

            }
            if(hastToBeRemoved||!isMandatory&&!tokenMustBeBuilded) continue

            for(tokenField in token.tokenFieldDescriptors){

                var fieldValue=""
                try{
                    if(tokenField.fieldVarName != "")
                        fieldValue=transactionData.getField(tokenField.fieldVarName)
                        watcher.debug("TOKEN_${token.tokenId} tokenId [${token.tokenId}] fieldValue [${fieldValue}] tokenField.fieldVarName ${tokenField.fieldVarName} ")

                }catch(e:Exception){
                    throw WebServiceException(ErrorCode.InvalidFormatField,"UNDEFINED VAR: [${tokenField.fieldVarName}] Attribute IN TransactionData for  TOKEN [${tokenField.tokenId}]",watcher)

                }
                val defaultValue=tokenField.tryToGetDefaultValue(transactionData,fieldValue,watcher)
                if(defaultValue != ""){
                    watcher.debug("field value tooks defaultValue: [${defaultValue}] ")
                    fieldValue=defaultValue
                }

                if(fieldValue == "" && defaultValue == "" &&tokenField.fieldDefaultValue==null&&isMandatory&&!hastToBeRemoved){
                    throw WebServiceException(ErrorCode.FieldIsMandatory," Field (${tokenField.fieldNumber}) Value: [$fieldValue]  TOKEN [${tokenField.tokenId}] ",transactionData,watcher)

                }
                if(fieldValue == "" &&tokenField.fieldDefaultValue!=null){
                    fieldValue=tokenField.fieldDefaultValue!!
                }

                if (defaultValue != "") {

                    fieldValue=defaultValue
                   watcher.debug(" TOKEN_SET_DEFAULT ${token.tokenId} VALUE: [${tokenField.fieldVarName}] value:[$defaultValue] ")

                    transactionData.setField(tokenField.fieldVarName,defaultValue)

                }

                val tokenId=TokenId.fromTokenIdString(token.tokenId)

                if(tokenId==TokenId.Undefined){
                    throw WebServiceException(ErrorCode.InvalidFormatField,"tokenId [${token.tokenId}] UNDEFINED -> ",transactionData,watcher)

                }
                if((isMandatory||tokenMustBeBuilded) && !hastToBeRemoved) {
                   watcher.debug("TOKEN_INFO: ID ${token.tokenId} tokenField.fieldNumber [${tokenField.fieldNumber}]  VALUE: [$fieldValue]  tokenMustBeBuilded [$tokenMustBeBuilded]  hastToBeRemoved [$hastToBeRemoved]  isMandatory [$isMandatory]")

                    isoMessage.setToken57Value(tokenId, transactionData, tokenField.fieldNumber, fieldValue, tokenField)
                }
            }

        }

       watcher.debug("\n ISO BUILD ALMOST FINISH \n")

       if(transactionData.transactionType.transactionMessageType!= TransactionMessageType.Comm) {
             watcher.debug("isoMessage.hasField57() ->  [${isoMessage.hasField57()}")
            if(isoMessage.hasField57()) {
                val resultLen = "1&".padStart(Field57Data.field57LengthIndictator + 1, '0')
            watcher.debug("field 57 IS PRESENT  LEN->  [${resultLen} \n")
                isoMessage.setField(57, resultLen) //fiedl57 DESCRIPTOR
            }
        }

      
       watcher.debug("END  buildField57Prosa ")

        return isoMessage
    }
    fun buildField63Prosa(isoDescriptor: Iso8583DescriptorData, isoMessage:Iso8583Message, transactionData:TransactionData):Iso8583Message{
        val watcher= WatcherContext.getInstance()

        if(transactionData.hostType.authorizer!=Authorizer.PROSA) return isoMessage
       watcher.debug("\n buildField63Prosa ")

       val rrn=transactionData.rrn
       val terminalSerialNumber=transactionData.terminalSerialNumber
       val transactionType=transactionData.transactionType

       watcher.debug("//////////Iso8583Builder START BUILD TOKENS")
        var reasonNotBuildToken=""
        for( token in isoDescriptor.field63Descriptor.tokenDescriptors){
            val isMandatory= token.isMandatory(transactionData,watcher)
            val hastToBeRemoved= token.removeIn(transactionData,watcher)
            var tokenMustBeBuilded=true
            if(hastToBeRemoved){
                reasonNotBuildToken="REMOVE WHEN: [${token.removeIf}]"

            }
            for(tokenDescriptorTEMP in token.tokenFieldDescriptors){
                var fieldValue: String
                reasonNotBuildToken=""
                try{
                    fieldValue=transactionData.getField(tokenDescriptorTEMP.fieldVarName)

                }catch(e:Exception){
                    throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}] UNDEFINED  FIELDVARNAME number:[${tokenDescriptorTEMP.fieldNumber}] name:[${tokenDescriptorTEMP.fieldName}] var:[${tokenDescriptorTEMP.fieldVarName}] Attribute IN TRANSACTIONDATA TOKEN [${tokenDescriptorTEMP.tokenId}]",watcher)

                }
                var defaultValue="" 
                try {
                    defaultValue = tokenDescriptorTEMP.tryToGetDefaultValue(transactionData,fieldValue, watcher)
                }catch (w:WebServiceException){
                   watcher.debug("EXCEPTION  ID:rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}] [${token.tokenId}  var: [${tokenDescriptorTEMP.fieldVarName} IN tryToGetDefaultValue "+w.errorMessage)

                }catch (e:Exception){

                   watcher.debug("EXCEPTION ID: rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}] [${token.tokenId}  var: [${tokenDescriptorTEMP.fieldVarName}  IN tryToGetDefaultValue "+e.message)

                }
                if(fieldValue == "" && defaultValue == ""){
                    reasonNotBuildToken+=" || var: [${tokenDescriptorTEMP.fieldVarName}]  fieldValue or defaultValue [${defaultValue}] ||"
                    tokenMustBeBuilded=false
                    break
                }
            }
            if(!tokenMustBeBuilded||hastToBeRemoved) {

               watcher.debug("TOKEN_NOT_BUILDED_INFO ID: [${token.tokenId}] tokenMustBeBuilded ${tokenMustBeBuilded} hastToBeRemoved ${hastToBeRemoved} \n CHECK_DEFAULT_VALUE [$reasonNotBuildToken] ")
            }
            if(isMandatory){
               watcher.debug("TOKEN_MUST_BE_BUILDED_INFO BECAUSE: [${token.mandatoryIf}] ")

            }
            if(hastToBeRemoved||!isMandatory&&!tokenMustBeBuilded) continue

            for(tokenField in token.tokenFieldDescriptors){

                var fieldValue=""
                try{
                    if(tokenField.fieldVarName != "")
                        fieldValue=transactionData.getField(tokenField.fieldVarName)
                        watcher.debug("TOKEN_${token.tokenId} tokenId [${token.tokenId}] fieldValue [${fieldValue}] tokenField.fieldVarName ${tokenField.fieldVarName} ")

                }catch(e:Exception){
                    throw WebServiceException(ErrorCode.InvalidFormatField,"rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}] UNDEFINED VAR: [${tokenField.fieldVarName}] Attribute IN TransactionData for  TOKEN [${tokenField.tokenId}]",watcher)

                }
                val defaultValue=tokenField.tryToGetDefaultValue(transactionData,fieldValue,watcher)
                if(defaultValue != ""){
                    watcher.debug("field value tooks defaultValue: [${defaultValue}] ")
                    fieldValue=defaultValue
                }

                if(fieldValue == "" && defaultValue == "" &&tokenField.fieldDefaultValue==null&&isMandatory&&!hastToBeRemoved){
                    throw WebServiceException(ErrorCode.FieldIsMandatory,"rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}]  Field (${tokenField.fieldNumber}) Value: [$fieldValue]  TOKEN [${tokenField.tokenId}] ",transactionData,watcher)

                } 
                if(fieldValue == "" &&tokenField.fieldDefaultValue!=null){
                    fieldValue=tokenField.fieldDefaultValue!!
                }

                if (defaultValue != "") {

                    fieldValue=defaultValue
                   watcher.debug(" TOKEN_SET_DEFAULT ${token.tokenId} VALUE: [${tokenField.fieldVarName}] value:[$defaultValue] ")

                    transactionData.setField(tokenField.fieldVarName,defaultValue)

                }

                val tokenId=TokenId.fromTokenIdString(token.tokenId)

                if(tokenId==TokenId.Undefined){
                    throw WebServiceException(ErrorCode.InvalidFormatField," rrn [${rrn}] terminalSerialNumber [${terminalSerialNumber}]  transactionType [${transactionType}] tokenId [${token.tokenId}] UNDEFINED -> ",transactionData,watcher)

                }
                if((isMandatory||tokenMustBeBuilded) && !hastToBeRemoved) {
                   watcher.debug("TOKEN_INFO: ID ${token.tokenId} tokenField.fieldNumber [${tokenField.fieldNumber}]  VALUE: [$fieldValue]  tokenMustBeBuilded [$tokenMustBeBuilded]  hastToBeRemoved [$hastToBeRemoved]  isMandatory [$isMandatory]")

                    isoMessage.setTokenValue(tokenId, transactionData, tokenField.fieldNumber, fieldValue, tokenField)
                }
            }
 
        }

       watcher.debug("\n ISO BUILD ALMOST FINISH \n")

       if(transactionData.transactionType.transactionMessageType!= TransactionMessageType.Comm) {
            if(isoMessage.hasField63()) {
                val resultLen = "1&".padStart(Field63Data.field63LengthIndictator + 1, '0')
                  watcher.debug("field 63 IS PRESENT  LEN->  [${resultLen} \n")
                isoMessage.setField(63, resultLen) //fiedl63 DESCRIPTOR
            }
          
        }

      
       watcher.debug("END  buildField63Prosa ")

        return isoMessage
    }


}