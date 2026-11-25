package com.viergegroup.swmain.dtos.transactions

import com.shared.commons.ErrorCode
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.transactions.TransactionDirection
import org.springframework.beans.BeanUtils
import com.viergegroup.swmain.entities.catalogs.CardType
import com.viergegroup.swmain.entities.catalogs.CardBin

class TransactionFullResponseDto : ResponseEntity{
    var transactionResponse: TransactionDto = TransactionDto()
    constructor() 
    constructor(transactionResponseData: TransactionResponse){ 
 
        errorCode=transactionResponseData.errorCode
        errorMessage=transactionResponseData.errorMessage
     //   BeanUtils.copyProperties(transactionResponse,this) 
        if(transactionResponseData.transactionResponse!=null) {
         //   println("TransactionFullResponseDto transactionResponseData.transactionResponse NOT_NULL response_code : ${transactionResponseData.transactionResponse!!.responseCode}")
 
            var errorCodeEnum=ErrorCode.fromResponseCode(transactionResponseData.transactionResponse!!.responseCode)
 
             if(this.errorCode==-1) { 
                 errorCode = errorCodeEnum.errorCodeInt
                 errorMessage = errorCodeEnum.errorCodeMessage
             }else{
                 if(transactionResponse.responseCode==""||transactionResponse.responseCode=="00") {
                     errorCodeEnum = ErrorCode.fromId(errorCode)
                     transactionResponse.responseCode = errorCodeEnum.responseCode
                 }
             }

            BeanUtils.copyProperties(transactionResponseData.transactionResponse!!, transactionResponse)

        } 

        if(transactionResponseData.transactionRequest!=null) {
          //  println("TransactionFullResponseDto transactionResponseData.transactionRequest NOT_NULL")

            if(transactionResponseData.transactionResponse==null) {
            //    println("TransactionFullResponseDto transactionResponseData.transactionResponse NULL")

                var errorCodeEnum=ErrorCode.fromResponseCode(transactionResponseData.transactionRequest!!.responseCode)
                if(errorCodeEnum!=ErrorCode.Undefined&&this.errorCode==-1) {
                    errorCode = errorCodeEnum.errorCodeInt
                    errorMessage = errorCodeEnum.errorCodeMessage
                }else{
                    if(transactionResponse.responseCode==""||transactionResponse.responseCode=="00") {
                        errorCodeEnum = ErrorCode.fromId(errorCode)
                        transactionResponse.responseCode = errorCodeEnum.responseCode
                    }
                }
                transactionResponse.responseCode=errorCodeEnum.responseCode
                //println("TransactionFullResponseDto   transactionResponse.responseCode ${  transactionResponse.responseCode}")

            }
          //  transactionResponse.clearCipherDataEZ=transactionResponseData.transactionRequest!!.clearCipherDataEZ
            transactionResponse.captureDate=transactionResponseData.transactionRequest!!.captureDate
            transactionResponse.localTransactionDate=transactionResponseData.transactionRequest!!.localTransactionDate
            transactionResponse.localTransactionTime=transactionResponseData.transactionRequest!!.localTransactionTime
         //   transactionResponse.clearCipherBlocks=transactionResponseData.transactionRequest!!.clearCipherBlocks
            transactionResponse.keySerialNumber=transactionResponseData.transactionRequest!!.keySerialNumber
                        transactionResponse.transactionType=transactionResponseData.transactionRequest!!.transactionType
               transactionResponse.cardNumber=transactionResponseData.transactionRequest!!.cardNumber
               transactionResponse.cardBin=transactionResponseData.transactionRequest!!.cardBin
               transactionResponse.emvTagsMap=transactionResponseData.transactionRequest!!.emvTagsMap  
               transactionResponse.emvData=transactionResponseData.transactionRequest!!.emvData  
               if(   transactionResponse.emvTagsMap.containsKey("9F26")) {
                  transactionResponse.arqc= transactionResponse.emvTagsMap["9F26"]!! 
                 
               }
              if(   transactionResponse.emvTagsMap.containsKey("95")) {
                  transactionResponse.tvr= transactionResponse.emvTagsMap["95"]!!
                 
               }
               transactionResponse.lastPan4Digits=transactionResponseData.transactionRequest!!.lastPan4Digits  

               transactionResponse.terminalSerialNumber=transactionResponseData.transactionRequest!!.terminalSerialNumber  

            //   transactionResponse.aid=transactionResponseData.transactionRequest!!.aid   
                 transactionResponse.additionalData=""  
                 transactionResponse.bankName=transactionResponseData.transactionRequest!!.bankName  
               transactionResponse.cardType=transactionResponseData.transactionRequest!!.cardType  
               if(transactionResponse.cardType==CardType.Undefined){
                try{
            var cardBin=transactionResponse.cardBin

                                if(cardBin.length<6){
                                cardBin=cardBin.padEnd(6,'0')
                                }
                                transactionResponse.cardType=CardBin.getCardType(transactionResponse.cardBin.toLong())
                            }catch(ex:Exception){
                                transactionResponse.cardType=CardType.VISA
                            }
                  
               }
               
                               transactionResponse.transactionAmount=transactionResponseData.transactionRequest!!.transactionAmount  
                 transactionResponse.merchantType=transactionResponseData.transactionRequest!!.merchantType  

                transactionResponse.otherAmount=transactionResponseData.transactionRequest!!.otherAmount  

                 transactionResponse.additionalAmounts=transactionResponseData.transactionRequest!!.additionalAmounts   

         //   transactionResponse.crc32EZ=transactionResponseData.transactionRequest!!.crc32EZ
            transactionResponse.cipherCounter=transactionResponseData.transactionRequest!!.cipherCounter
       //     transactionResponse.chipherDataBlocks=transactionResponseData.transactionRequest!!.chipherDataBlocks
            transactionResponse.rrn=transactionResponseData.transactionRequest!!.rrn
          //  transactionResponse.cipherData=transactionResponseData.transactionRequest!!.cipherData
            transactionResponse.serverName=transactionResponseData.transactionRequest!!.serverName
            transactionResponse.transactionDirection= TransactionDirection.Request

        }

    }
}