package com.viergegroup.swmain.entities.catalogs

import com.shared.commons.ErrorCode
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

enum class CardBin(var cardBinId:Int,var cardType: CardType,var lowerBin:Long=0L,var upperBin:Long=0L){
    Undefined(-1, CardType.Undefined, -1, -1),
  
    // AMEX
    AMEX_01(1, CardType.AMEX, 340000, 349999),
    AMEX_02(2, CardType.AMEX, 370000, 379999),

    // CARNET
    CARNET_01(3, CardType.CARNET, 506204, 506204),
    CARNET_02(4, CardType.CARNET, 636300, 639999),

    // DISCOVER
    DISCOVER_01(5, CardType.DISCOVER, 300000, 309999),
    DISCOVER_02(6, CardType.DISCOVER, 380000, 389999), 
    DISCOVER_03(7, CardType.DISCOVER, 390000, 399999),
    DISCOVER_04(8, CardType.DISCOVER, 601100, 601199),
    DISCOVER_05(14, CardType.DISCOVER, 622126, 622925),
    DISCOVER_06(15, CardType.DISCOVER, 644000, 649999),
    DISCOVER_07(16, CardType.DISCOVER, 650000, 659999),
    DISCOVER_08(9, CardType.DISCOVER, 360000, 369999),

    // JCB 
    JCB_01(9, CardType.JCB, 352800, 358999),

    // MASTERCARD
    MASTERCARD_01(10, CardType.MASTERCARD, 500000, 509999),
    MASTERCARD_02(11, CardType.MASTERCARD, 222100, 272099),
    MASTERCARD_03(17, CardType.MASTERCARD, 510000, 559999),
    MASTERCARD_04(18, CardType.MASTERCARD, 560000, 589999),
    MASTERCARD_05(19, CardType.MASTERCARD, 590000, 599999),

    // UNIONPAY
    UNIONPAY_01(12, CardType.UNIONPAY, 620000, 629999),
    UNIONPAY_02(13, CardType.UNIONPAY, 621000, 621099),
    UNIONPAY_03(20, CardType.UNIONPAY, 622126, 622925),
    UNIONPAY_04(21, CardType.UNIONPAY, 624000, 626999),

    // VISA
    VISA_01(22, CardType.VISA, 400000, 499999),
    VISA_02(23, CardType.VISA, 402360, 402360),
    VISA_03(24, CardType.VISA, 404137, 404137),
    VISA_04(25, CardType.VISA, 407441, 407441),
    VISA_05(26, CardType.VISA, 410504, 410504),
    VISA_06(27, CardType.VISA, 411298, 411298),
    VISA_07(28, CardType.VISA, 414099, 414099),
    VISA_08(29, CardType.VISA, 415874, 415874),
    VISA_09(30, CardType.VISA, 421323, 421323),
    VISA_10(31, CardType.VISA, 424327, 424327),
    VISA_11(32, CardType.VISA, 425907, 425907),

    // MAESTRO
    MAESTRO_01(33, CardType.MASTERCARD, 500000, 509999),
    MAESTRO_02(34, CardType.MASTERCARD, 560000, 589999),
    MAESTRO_03(35, CardType.MASTERCARD, 600000, 699999)
    
    
    ;




    companion object{

        private var validCardTypesDesc=""
        fun getCardType(dto: TransactionDto, watcher: Watcher):CardType{
            var bin=0L
           watcher.debug("getCardType 1") 
    
            try{
                if(dto.cardType!=CardType.Undefined){
                    watcher.debug("getCardType different to Undefined -> ${dto.cardType}")
                    return dto.cardType
                } 
                dto.cardBin=dto.cardBin.replace("*","0")
                dto.cardNumber=dto.cardNumber.replace("*","0")

                watcher.debug("dto.cardBin ${dto.cardBin} ")

                if(dto.cardBin.trim()!=""&&dto.cardBin.length>=6){
                    bin=dto.cardBin.toLong()
                }
            }catch (e:Exception){
               watcher.error("EXCEPTION getCardType ${e.message} ")
                dto.errorCode= ErrorCode.InvalidFormatField.errorCodeInt
                dto.errorMessage= ErrorCode.InvalidFormatField.errorCodeMessage+" EXCEPTION getCardType ${e.message} "
                return  CardType.Undefined
            }
            try{
               watcher.debug("dto.cardNumber ${dto.cardNumber} ")

                if(dto.cardNumber.trim()!=""&&dto.cardNumber.length>=6){
                    bin=dto.cardNumber.substring(0,6).toLong()
                }
            }catch (e:Exception){
               watcher.error("EXCEPTION getCardType ${e.message} ")
               watcher.error("EXCEPTION getCardType ${e.message} ")
                dto.errorCode= ErrorCode.InvalidFormatField.errorCodeInt
                dto.errorMessage= ErrorCode.InvalidFormatField.errorCodeMessage+" EXCEPTION getCardType ${e.message} "
                return  CardType.Undefined
            }
            if(bin>0){
                val cardType=getCardType(bin)
               watcher.debug("cardType  bin ${bin} ->   ${cardType} ")
                return getCardType(bin)
            }
            return CardType.Undefined
        }
        fun getCardType(dto: TransactionDto):CardType{
            var bin=0L
            WatcherGlobalLog.debug("getCardType 1")
 
            try{
                WatcherGlobalLog.debug("dto.cardBin ${dto.cardBin} ")
                dto.cardBin=dto.cardBin.replace("*","0")
                dto.cardNumber=dto.cardNumber.replace("*","0")

                if(dto.cardBin.trim()!=""&&dto.cardBin.length>=6){
                    bin=dto.cardBin.toLong()

                }
            }catch (e:Exception){
                WatcherGlobalLog.error("EXCEPTION getCardType ${e.message} ")
                dto.errorCode= ErrorCode.InvalidFormatField.errorCodeInt
                dto.errorMessage= ErrorCode.InvalidFormatField.errorCodeMessage+" EXCEPTION getCardType ${e.message} "
                return  CardType.Undefined
            }
            try{
                WatcherGlobalLog.debug("dto.cardNumber ${dto.cardNumber} ")

                if(dto.cardNumber.trim()!=""&&dto.cardNumber.length>=6){
                    bin=dto.cardNumber.substring(0,6).toLong()
                }
            }catch (e:Exception){
                WatcherGlobalLog.error("EXCEPTION getCardType ${e.message} ")
                WatcherGlobalLog.error("EXCEPTION getCardType ${e.message} ")
                dto.errorCode= ErrorCode.InvalidFormatField.errorCodeInt
                dto.errorMessage= ErrorCode.InvalidFormatField.errorCodeMessage+" EXCEPTION getCardType ${e.message} "
                return  CardType.Undefined
            }
            if(bin>0){
                val cardType=getCardType(bin)
                WatcherGlobalLog.debug("cardType  bin ${bin} ->   ${cardType} ")
                return getCardType(bin)
            }
            return CardType.Undefined
        }


        fun getCardType(dto: TransactionData, watcher: Watcher):CardType{
            var bin=0L
           watcher.debug("getCardType 2")

            try{
                dto.cardBin=dto.cardBin.replace("*","0")
                dto.cardNumber=dto.cardNumber.replace("*","0")

                if(dto.cardBin.trim()!=""&&dto.cardBin.length>=6){
                    bin=dto.cardBin.toLong()

                }
            }catch (e:Exception){
               watcher.error("EXCEPTION getCardType ${e.message} ")

                return  CardType.Undefined
            }
            try{
                if(dto.cardNumber.trim()!=""&&dto.cardNumber.length>=6){
                    bin=dto.cardNumber.substring(0,6).toLong()
                }
            }catch (e:Exception){
               watcher.error("EXCEPTION getCardType ${e.message} ")
                return  CardType.Undefined

            }
            if(bin>0){
                val cardType=getCardType(bin)
               watcher.debug("cardType  bin ${bin} ->   ${cardType} ")

                return getCardType(bin)
            }
            return CardType.Undefined
        }

        fun getCardType(bin:Long):CardType{
            val ct=CardBin.values().firstOrNull{bin>=it.lowerBin&&bin<=it.upperBin}
            if(ct!=null)
                return ct.cardType
            return CardType.Undefined 
        }




    }

}