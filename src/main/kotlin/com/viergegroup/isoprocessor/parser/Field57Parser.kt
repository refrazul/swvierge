package com.viergegroup.isoprocessor.parser

import com.shared.commons.ErrorCode
import com.shared.commons.WebServiceException
import com.viergegroup.isoprocessor.models.iso.Field57Data
import com.viergegroup.isoprocessor.models.descriptors.Field57Descriptor
import com.viergegroup.isoprocessor.models.tokens.TokenId
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.Watcher

class Field57Parser(var hostType: HostType, var watcher: Watcher) {

    var field57Buffer:String=""  
 
    lateinit var field57Descriptor: Field57Descriptor

    var elementsCounter=0
    var tokenContentLength=0

    class TokenPartialData(var tokenId:TokenId,var tokenValues:String=""){

    }
 
   fun init(field57Buffer:String,field57Descriptor: Field57Descriptor): Field57Parser {

       //  println(" ///////////// INIT  FIELD 57 field57Buffer ")
       watcher.debug("///////////// INIT  FIELD 57 field57Buffer \n\n")
       watcher.debug("field57Buffer [${field57Buffer.length}] $field57Buffer")
        if(field57Buffer.length<=0){
            this.field57Buffer=field57Buffer
            return this
        }
       //this.field57Buffer=field57Buffer .substring(Field57Data.field57LengthIndictator+1,field57Buffer.length)
       this.field57Buffer=field57Buffer.substring(1,field57Buffer.length)

       //   println("field57Buffer $field57Buffer")
      watcher.debug("field57Buffer $field57Buffer")

       this.field57Descriptor=field57Descriptor
      val elementsCounterS= this.field57Buffer.substring(0,6)
       //   println("elementsCounterS $elementsCounterS")
      watcher.debug("elementsCounterS $elementsCounterS")

       val tokenContentLengthS= this.field57Buffer.substring(6,11)
       //   println("tokenContentLengthS $tokenContentLengthS")
      watcher.debug("tokenContentLengthS $tokenContentLengthS")

       elementsCounter=elementsCounterS.trim().toInt()
       tokenContentLength=tokenContentLengthS.trim().toInt()

       this.field57Buffer=this.field57Buffer.substring(13,this.field57Buffer.length)

       //   println("field57Buffer \"${this.field57Buffer}\" \n elementsCounterS  \n $elementsCounterS \n tokenContentLengthS $tokenContentLengthS \n ")
      watcher.debug("field57Buffer \"${this.field57Buffer}\" \n elementsCounterS  \n $elementsCounterS \n tokenContentLengthS $tokenContentLengthS \n\n")


       return this

   }
 
    fun build():Field57Data{
        watcher.debug("///////////// build FIELD 57 [${field57Buffer.length}]\n\n")

        val field57Data=Field57Data(hostType,watcher)
        if(field57Buffer.length<=0){
            watcher.debug("///////////// END build FIELD 57\n\n")
            return field57Data 
        }
        //  println(" ///////////// build FIELD 57")

        val field57Buffer=this.field57Buffer
        var tokenId: TokenId=TokenId.Q1 
        
        val tokensSplit=field57Buffer.split("! ")
        val partialTokens= mutableListOf<TokenPartialData>()

        for(tokenPart in tokensSplit){
            val tokenType= tokenPart.substring(0,2)
          val   tokenLength= tokenPart.substring(2,7).toInt()
            val tokenValuesSplits= tokenPart.split(" ")
            var tokenValues: String=""
            if(tokenValuesSplits.size>1){
                tokenValues=tokenPart.substring(8,tokenPart.length)

            }else{  
              //  throw WebServiceException(ErrorCode.InvalidTokenFormant,"Invalid format in Token Near \"$tokenPart\"",watcher)
                watcher.error(ErrorCode.InvalidTokenFormant,"Invalid format in Token Near \"$tokenPart\"")

            }
           watcher.debug("/******************* START tokenType $tokenType \n ")

       //     println("/******************* START tokenType $tokenType \n ")
        //    println("tokenPart $tokenPart \n ")
           watcher.debug("tokenPart $tokenPart \n ")

          //  println("tokenLength $tokenLength \n ")
           watcher.debug("tokenLength $tokenLength \n ")

            //println("tokenValues \"$tokenValues\" \n")
           watcher.debug("tokenValues \"$tokenValues\" \n")

            if(tokenLength<tokenValues.length){
              //  throw WebServiceException(ErrorCode.InvalidTokenFormant," Invalid Length in Token $tokenType the length expected is $tokenLength but Data Length is ${tokenValues.length} ",watcher)
                watcher.error(ErrorCode.InvalidTokenFormant," Invalid Length in Token $tokenType the length expected is $tokenLength but Data Length is ${tokenValues.length} ")

            }

            try{
                tokenId=TokenId.fromTokenIdString(tokenType)
            }catch (e:Exception){
               // throw WebServiceException(ErrorCode.InvalidTokenFormant," We Dont have  an ENUM DEFINED For Token $tokenType",watcher)
                watcher.error(ErrorCode.InvalidTokenFormant," We Dont have  an ENUM DEFINED For Token $tokenType")

            }

            partialTokens.add(TokenPartialData(tokenId,tokenValues))



            //  println("/******************* END tokenType $tokenType \n \n ")
           watcher.debug("/******************* END tokenType $tokenType \n \n ")


        }



        
        for(tokenDescriptor in field57Descriptor.tokenDescriptors){
            var bufferPos=0
            val tokenIdEnum=TokenId.fromTokenIdString(tokenDescriptor.tokenId)
            val partialToken=partialTokens.firstOrNull{it.tokenId==tokenIdEnum}
            var tokenB6Len=32
             var tokenB5Len=32

            if(partialToken!=null) {
                val tokenBuffer=partialToken.tokenValues
                //  println("\n\nTOKEN $tokenIdEnum tokenBuffer  \"$tokenBuffer\" \n\n")
               watcher.debug("\n\nTOKEN $tokenIdEnum tokenBuffer  \"$tokenBuffer\" \n\n")

                for (tokenFieldDescriptor in tokenDescriptor.tokenFieldDescriptors) {
                    
                    if(tokenDescriptor.tokenId=="B5"&&tokenFieldDescriptor.fieldNumber==2){
                       // tokenFieldDescriptor.fieldLength=tokenB5Len
                          watcher.debug("\n\nNEW_TOKEN_B5_tokenFieldDescriptor_fieldLength \"${tokenFieldDescriptor.fieldLength}\" \n\n")

                    }
                
                    if(tokenDescriptor.tokenId=="B6"&&tokenFieldDescriptor.fieldNumber==2){
                        tokenFieldDescriptor.fieldLength=tokenB6Len
                          
                          watcher.debug("\n\nNEW_TOKEN_B6_tokenFieldDescriptor_fieldLength  \"${tokenFieldDescriptor.fieldLength}\" \n\n")

                    } 
   
                   
                    var fieldValue: String
                    val fieldLen = tokenFieldDescriptor.fieldLength
                    val endPos=bufferPos+tokenFieldDescriptor.fieldLength 

             
                    if(tokenFieldDescriptor.lengthIsVariable||tokenFieldDescriptor.fieldLengthIndicator>0) {
                        var bufferPosEnd= bufferPos+tokenFieldDescriptor.fieldLengthIndicator

                        // IF()
                        if(bufferPosEnd>tokenBuffer.length){
                            bufferPosEnd=tokenBuffer.length
                            //throw WebServiceException(ErrorCode.InvalidIso8583,"Insufficent Data In ",fieldDescriptor)
                        }
                       watcher.debug("bufferPosEnd 2:   ${bufferPosEnd}  LENGTH ${tokenBuffer.length}")
                        //bufferPos+=fieldDescriptor.fieldLengthIndicator
                        fieldValue = tokenBuffer.substring(bufferPos,bufferPosEnd)
                        fieldValue=fieldValue.substring(tokenFieldDescriptor.fieldLengthIndicator,fieldValue.length)

                       watcher.debug(" after remove len fieldValue :   ${fieldValue} INDICATOR: ${tokenFieldDescriptor.fieldLengthIndicator} LENGTH ${fieldValue.length}")

                    }else{
                        val endPos2=bufferPos+tokenFieldDescriptor.fieldLength

                        if(tokenBuffer.length<endPos){
                          //  throw WebServiceException(ErrorCode.InvalidTokenFormant," INSUFFICIENT DATA FOR ",tokenFieldDescriptor,watcher)
                            watcher.error(ErrorCode.InvalidTokenFormant," INSUFFICIENT DATA FOR ",tokenFieldDescriptor)

                        }
                        fieldValue=tokenBuffer.substring(bufferPos,endPos2) 
                    }




                    //  println("TOKEN $tokenIdEnum fieldLen  $fieldLen  endPos $endPos  \n fieldValue  \"$fieldValue\" fieldNumber ${tokenFieldDescriptor.fieldNumber} fieldName \"${tokenFieldDescriptor.fieldName}\" \n")
                   watcher.debug("TOKEN $tokenIdEnum fieldLen  $fieldLen  endPos $endPos  \n fieldValue  \"$fieldValue\" fieldNumber ${tokenFieldDescriptor.fieldNumber} fieldName \"${tokenFieldDescriptor.fieldName}\" \n")


                    if(tokenDescriptor.tokenId=="B5"&&tokenFieldDescriptor.fieldNumber==1){
                            try{
                                tokenB5Len =fieldValue.toInt()*2
                            }catch(ex:Exception){
 

                            }
                          watcher.debug("\n\nTOKEN_B5_tokenB5Len  \"$tokenB5Len\" \n\n")

                    }

                 if(tokenDescriptor.tokenId=="B6"&&tokenFieldDescriptor.fieldNumber==1){
                            try{
                                    tokenB6Len=fieldValue.toInt()*2
                            }catch(ex:Exception){ 


                            }
                          watcher.debug("\n\nTOKEN_B6_tokenB6Len  \"$tokenB6Len\" \n\n")

                    }

                    field57Data.setTokenValue(
                        tokenIdEnum,
                        tokenFieldDescriptor.fieldNumber,
                        fieldValue,
                        tokenFieldDescriptor,
                        null
                    )

                    bufferPos += fieldLen

                }
            }

        }



        return field57Data


    }





}