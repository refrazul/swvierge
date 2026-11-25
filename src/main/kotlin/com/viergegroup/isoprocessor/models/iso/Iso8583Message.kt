
package com.viergegroup.isoprocessor.models.iso

import com.shared.commons.ErrorCode
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.parser.Iso8583Parser
import com.viergegroup.isoprocessor.commons.IsoCommons
import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor
import com.viergegroup.isoprocessor.models.tokens.TokenId
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.StringUtils
import com.viergegroup.isoprocessor.models.descriptors.Iso8583FieldDescriptor
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.entities.transactions.TransactionData

import java.util.*

class Iso8583Message(val hostType: HostType,val transactionType: TransactionType, val watcher: Watcher) {

    internal var isoHeader:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }



    internal var isoMessageType: Iso8583MessageType = Iso8583MessageType.Message0200
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal var bitMap:BooleanArray= BooleanArray(128)
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal var primaryBitMapString:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal var secondaryBitMapString:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }

    internal var field57Data:Field57Data=Field57Data(hostType,watcher)
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal var field63Data:Field63Data=Field63Data(hostType,watcher)
        get() {
            return field
        }
        set(value) {
            field = value
        }
   // internal var iso8583EmvData:Iso8583EmvData=Iso8583EmvData(hostType,logger)

    internal var emvTagsMap= mutableMapOf<String,String>()


    internal var isoMessageBuffer:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal var isoMessageContentFields:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }
    internal  var isoFields= mutableListOf<Iso8583Field>()
        get() {
            return field
        }
        set(value) {
            field = value
        }

   fun hasField57():Boolean{
        return field57Data.hasTokens()
    } 

    

    fun hasField63():Boolean{
        return field63Data.hasTokens()
    }

    fun injectEmvTagsMap(emvTagsMap:MutableMap<String,String>){
        this.emvTagsMap=emvTagsMap
    }
    fun injectPrimaryBitMap(bitMap:String){
        primaryBitMapString=bitMap
        val byteArray= StringUtils.hexStringToByteArray(bitMap) //Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)
       var bitArrayS="" //Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)

        for(i in byteArray){
            bitArrayS+=i.toUByte().toString(2).replace("-1","1").padStart(8,'0')+""
        }
        var i=0
          for(bit in bitArrayS){
              if(bit.toString().equals("1")){
                  this.bitMap[i]=true

              }
              i++
          }
      //  	WatcherGlobalLog.info(" bitArrayS "+bitArrayS)
    }


    fun getBitMapHexString():String{

        if(hasField(1)){
            return StringUtils.encodeHexString(this.bitMap).uppercase(Locale.getDefault())
        }
        val primaryBitMap=BooleanArray(64)
        for (i in 0..63){
            primaryBitMap[i]=bitMap[i]
        }
        return StringUtils.encodeHexString(primaryBitMap).uppercase(Locale.getDefault())
    }

    fun getPrimaryBitMapHexString():String{

        /*if(hasField(1)){
            return StringUtils.encodeHexString(this.bitMap).uppercase(Locale.getDefault())
        }*/
        val primaryBitMap=BooleanArray(64)
        for (i in 0..63){
            primaryBitMap[i]=bitMap[i]
        }
        return StringUtils.encodeHexString(primaryBitMap).uppercase(Locale.getDefault())
    }
    fun getSecondaryBitMapHexString():String{

        val secondaryBitMap=BooleanArray(64)
        var counter=0
        if(bitMap.size>63) {
            for (i in 64 until bitMap.size) {
                secondaryBitMap[counter] = bitMap[i]
                counter++
            }
        }
        return StringUtils.encodeHexString(secondaryBitMap).uppercase(Locale.getDefault())
    }
    fun buildIsoBuffer():String{
        isoMessageBuffer=""
        isoMessageBuffer+=isoHeader+isoMessageType.messageTypeId+getBitMapHexString()

         isoFields = isoFields.sortedBy { it.fieldNumber } .toMutableList()
        val isoDescriptors=Iso8583Parser.getIsoDescriptorData(hostType,watcher).fieldDescriptors

 
        for (field in isoFields){

             if(field.fieldNumber==57){
                isoMessageBuffer+=field57Data.buildField()
                continue
            }


            if(field.fieldNumber==63){
                isoMessageBuffer+=field63Data.buildField()

                continue
            }
            val fieldDescriptor =
                isoDescriptors.firstOrNull { it.fieldNumber == field.fieldNumber }
            if(fieldDescriptor!=null){
                if(fieldDescriptor.lengthIsVariable||fieldDescriptor.fieldLengthIndicator>0){
                    val fieldLen=field.fieldValueString.length.toString().padStart(fieldDescriptor.fieldLengthIndicator,'0')
                    isoMessageBuffer+=fieldLen+field.fieldValueString

                }else{
                    isoMessageBuffer+=field.fieldValueString

                }
            }
        }


        return isoMessageBuffer


    }

    fun injectSecondaryBitMap(bitMap:String){
        secondaryBitMapString=bitMap
        val byteArray= StringUtils.hexStringToByteArray(bitMap)//Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)
        var bitArrayS="" //Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)
        for(i in byteArray){
            bitArrayS+=i.toUByte().toString(2).replace("-1","1").padStart(8,'0')+""
        }
        var i=0
        for(bit in bitArrayS){
            if(bit.toString().equals("1")){
                this.bitMap[i+64]=true

            }
            i++
        }

    }

    fun getPresentFields():String{
        var fields="Present Fields: ["
        var comma=","
        for (i in bitMap.indices) {
            if(i>=bitMap.size-1){
                comma=""
            }
            if(this.bitMap[i]){
                fields+=(i+1).toString() +comma
            }
        }
        fields+="]"
        return fields
    }

    /*fun turOnBitmap(fieldNumber: Int){
        if(fieldNumber<=128&&fieldNumber>0){
            bitMap[fieldNumber-1]=true
        }
    }*/
    fun setTokenValue(tokenId: TokenId, fieldNumber:Int, tokenFieldValue:String,transactionData: TransactionData) {

        field63Data.setTokenValue(tokenId,fieldNumber,tokenFieldValue,transactionData)

    }
    fun setTokenValue(tokenId:TokenId,transactionData:TransactionData,fieldNumber:Int,tokenFieldValue:String,tokenFieldDescriptor: TokenFieldDescriptor) {
        field63Data.setTokenValue(tokenId,fieldNumber,tokenFieldValue,tokenFieldDescriptor,transactionData)
    } 


    fun setToken57Value(tokenId: TokenId, fieldNumber:Int, tokenFieldValue:String,transactionData: TransactionData) {

        field57Data.setTokenValue(tokenId,fieldNumber,tokenFieldValue,transactionData)

    }
    fun setToken57Value(tokenId:TokenId,transactionData:TransactionData,fieldNumber:Int,tokenFieldValue:String,tokenFieldDescriptor: TokenFieldDescriptor) {
        field57Data.setTokenValue(tokenId,fieldNumber,tokenFieldValue,tokenFieldDescriptor,transactionData)
    }


    fun setField(fieldNumber:Int,fieldValue:String,fieldDescriptor: Iso8583FieldDescriptor){
        if(fieldNumber<=128&&fieldNumber>0){
            bitMap[fieldNumber-1]=true
        }
        if(fieldNumber>=64){
            bitMap[0]=true

        }


        val iso8583Field= Iso8583Field()
        iso8583Field.fieldNumber=fieldNumber
        iso8583Field.fieldValueString=fieldValue
        iso8583Field.fieldName=fieldDescriptor.fieldName
        iso8583Field.fieldDescription=fieldDescriptor.fieldDescription
        iso8583Field.fieldLength=fieldDescriptor.fieldLength
        iso8583Field.fieldVarName=fieldDescriptor.fieldVarName
        iso8583Field.hasDefaultValue=fieldDescriptor.hasDefaultValue
        iso8583Field.isMandatory=fieldDescriptor.isMandatory

        if(fieldDescriptor.lengthIsVariable||fieldDescriptor.fieldLengthIndicator>0){
           val fieldValueFinal=fieldValue.length.toString().padStart(fieldDescriptor.fieldLengthIndicator,'0')+fieldValue
            val fieldLengthString=fieldValueFinal.substring(0,fieldDescriptor.fieldLengthIndicator)

 
            if(!fieldDescriptor.lengthIsVariable&&fieldValueFinal.length>fieldDescriptor.fieldLength){
              //  throw WebServiceException(ErrorCode.InvalidFormatField,"Invalid  Length The Max IS ${fieldDescriptor.fieldLength} AND The Length of \"${fieldValueFinal}\" IS ${fieldValueFinal.length}  ",fieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"Invalid  Length The Max IS ${fieldDescriptor.fieldLength} AND The Length of \"${fieldValueFinal}\" IS ${fieldValueFinal.length}  ",fieldDescriptor)

            } 
            var expectedLen:Int=0
            try{
                expectedLen=fieldLengthString.toInt()
                if(expectedLen>fieldDescriptor.fieldLength){
                  //  throw WebServiceException(ErrorCode.InvalidFormatField,"Invalid  Length The Max IS ${fieldDescriptor.fieldLength} AND The length Specified Is $expectedLen  ",fieldDescriptor,watcher)
                    watcher.error(ErrorCode.InvalidFormatField,"Invalid  Length The Max IS ${fieldDescriptor.fieldLength} AND The length Specified Is $expectedLen  ",fieldDescriptor)

                }
            }catch (e:Exception){
              //  throw WebServiceException(ErrorCode.InvalidFormatField,"Invalid format Length \"${fieldLengthString}\" EXCEIPTION: \""+e.message+"\" FOR",fieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"Invalid format Length \"${fieldLengthString}\" EXCEIPTION: \""+e.message+"\" FOR",fieldDescriptor)

            }
            val fieldLenReal=fieldValueFinal.length-fieldDescriptor.fieldLengthIndicator
           watcher.debug("fieldLengthIndicator: ${fieldDescriptor.fieldLengthIndicator}, fieldLenReal $fieldLenReal  expectedLen $expectedLen")
            if(fieldLenReal<expectedLen){

                watcher.error(ErrorCode.InvalidFormatField,"Insuficient Data , Length Expected Is $fieldLenReal BUT Data Length IS ${expectedLen} IN -> ",fieldDescriptor)

            }
            //iso8583Field.fieldValueString=fieldValueFinal



        }else{
            if(fieldValue.length!=fieldDescriptor.fieldLength){
              //  throw WebServiceException(ErrorCode.InvalidFormatField,"Length Must be ${fieldDescriptor.fieldLength} NOT  ${fieldValue.length} VALUE: \"${fieldValue}\"  FOR  ",fieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField,"Length Must be ${fieldDescriptor.fieldLength} NOT  ${fieldValue.length} VALUE: \"${fieldValue}\"  FOR  ",fieldDescriptor)

            }
        }
        if(fieldDescriptor.fieldType.equals("ANS")){
            if(!IsoCommons.isAlphaNumeric(fieldValue)){
              //  throw WebServiceException(ErrorCode.InvalidFormatField," Field MUST BE ALPHANUMERIC-> ",fieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField," Field MUST BE ALPHANUMERIC-> ",fieldDescriptor)

            }
        }
        if(fieldDescriptor.fieldType.equals("N")){
            if(!IsoCommons.isNumeric(fieldValue)){
             //   throw WebServiceException(ErrorCode.InvalidFormatField," Field MUST BE NUMERIC-> ",fieldDescriptor,watcher)
                watcher.error(ErrorCode.InvalidFormatField," Field MUST BE NUMERIC-> ",fieldDescriptor)

            }

        }


        val fieldExists=isoFields.firstOrNull { it.fieldNumber==fieldNumber }
        if(fieldExists!=null){
            isoFields.remove(fieldExists)
        }

        isoFields.add(iso8583Field)

    }


    fun setField(fieldNumber:Int,fieldValue:String){
        if(fieldValue.trim().equals("")) return


        val fieldDescriptor = Iso8583Parser.getIsoDescriptorData(hostType,watcher).fieldDescriptors.firstOrNull{ it.fieldNumber == fieldNumber }

        if(fieldDescriptor!=null){
            setField(fieldNumber,fieldValue,fieldDescriptor)
        }else{
          //  throw WebServiceException(ErrorCode.InvalidFormatField," WE DONT HAVE GUIDE FOR fieldNumber $fieldNumber",watcher)
            watcher.error(ErrorCode.InvalidFormatField," WE DONT HAVE GUIDE FOR fieldNumber $fieldNumber")

        }


    }

    fun hasField(fieldNumber: Int):Boolean{
        if(fieldNumber>128) return false
        if(fieldNumber<=0) return false

        return bitMap[fieldNumber-1]
    }

    fun getField(fieldNumber: Int): Iso8583Field? {
        if(!hasField(fieldNumber)) return null
        val isoField =
            isoFields.firstOrNull { it.fieldNumber == fieldNumber }

        return isoField
    }


    fun getFieldValue(fieldNumber: Int): String{
      //  if(!hasField(fieldNumber)) ""
        val isoField =
            isoFields.firstOrNull { it.fieldNumber == fieldNumber }

        if(isoField!=null)
        return isoField.fieldValueString

        return ""
    }





    fun describe():String{
        var description = "Host:[$hostType]\n TransactionType: [${transactionType}] \nAuthorizer: [${hostType.authorizer}] \nHeader: ["+isoHeader+"]\nmessageType: [${isoMessageType.messageTypeId}]\n ${getPresentFields()} \nPrimary Bit Map: [${getPrimaryBitMapHexString()}] \nSecondary Bit Map: [${getSecondaryBitMapHexString()}]  "
        val iso8583DescriptorData = Iso8583Parser.getIsoDescriptorData(hostType,watcher)
        description+="\n   ============================================ \n\n\n"

        for(fieldNumber in 0..128 ) {

            if (this.hasField(fieldNumber)) {
                val fieldDescriptor =
                    iso8583DescriptorData.fieldDescriptors.firstOrNull { it.fieldNumber == fieldNumber }
                val isoField =
                    getField(fieldNumber)
                    if(fieldNumber==1||(fieldDescriptor==null || isoField==null)){
                        if(fieldNumber!=1)
                        description += "Field $fieldNumber Dont Have Descriptor \n"
                        continue

                    }
                    var isNVar=""
                    var hasDefaultValueStr=""
                    var isMandatoryStr=""

                    var length=fieldDescriptor.fieldLength
                    val type=fieldDescriptor.fieldType
                    if(fieldDescriptor.fieldLengthIndicator!=-1) {
                        isNVar = "x"
                        length=isoField.fieldValueString.length
                    }
                    if(isoField.hasDefaultValue){
                        hasDefaultValueStr="D"
                    }
                    if(isoField.isMandatory){
                        isMandatoryStr="M"
                    }

                    description += "Field $fieldNumber (${fieldDescriptor.fieldName} $hasDefaultValueStr$isMandatoryStr($isNVar${type}($length)) ): \"${isoField.fieldValueString}\" \n ${fieldDescriptor.defaultValueDescription}"
                    if(fieldDescriptor.fieldVarName=="emvData"){
                        val emvDescriptor=Iso8583Parser.getEmvDescriptor()
                        description+=emvDescriptor.describeEmvTagsMap(emvTagsMap)
                    }
                     if(fieldNumber==57&&hostType.authorizer== Authorizer.PROSA){
                        description+=field57Data.describe()
                    } 
                    if(fieldNumber==63&&hostType.authorizer== Authorizer.PROSA){
                        description+=field63Data.describe()
                    }
                  
                    description+="\n   --------------------------------------- \n"


            }
        }
        return description
    }
}