package com.viergegroup.swmain.managers

import com.shared.commons.ErrorCode
import com.shared.commons.GsonConfig
import com.viergegroup.swmain.webwatcher.Watcher
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.toBitSet
import com.viergegroup.crypto.security.BitSet
import com.viergegroup.crypto.security.PasswordUtil
import com.viergegroup.swmain.exceptions.DBKeyCRCFailed
import com.viergegroup.swmain.exceptions.ErrorReadingDBKey
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.shared.commons.AppSettings
 
object DBCipherManager {
    private const val DATA_VARIANT_ENCRIPTION_KEY = "063A3A0839353A35353A3537083A3A0B0B3A3A0837353A35353A3539083A3A0637073B3E063D3509093538063E3B073E3E073B3E0638350909353D063E3B0737"
    private  val  watcher= WatcherContext.getInstance()
    private val gson=GsonConfig.gson
    class EncryptionKey { 
        var errorCode = 0
        var errorMessage = ""   
        var key = ByteArray(0)
        var kcv = "" 
    }
  
    private  fun toEncryptionKey(derivedKey: ByteArray, watcher: Watcher): ByteArray {
       watcher.debug("Starting toEncryptionKey")
        val derivedKeyBitSet = StringUtils.byteArrayToBitSet(derivedKey)
       watcher.debug("Derived key BitSet: $derivedKeyBitSet")
        val resultBitSet = BitSet(derivedKeyBitSet.size())
       watcher.debug("Result BitSet initialized with size: ${resultBitSet.size()}")

        var counter = 1
        var DATA_VARIANT_ENCRIPTION_KEY_BIT_SET = toBitSet(StringUtils.hexStringToByteArray(PasswordUtil.destransformPassword(DATA_VARIANT_ENCRIPTION_KEY)))
       watcher.debug("DATA_VARIANT_ENCRIPTION_KEY_BIT_SET: $DATA_VARIANT_ENCRIPTION_KEY_BIT_SET")

        for (i in 0 until resultBitSet.size()) {

            var originalBit = derivedKeyBitSet[i]
            val encriptionBit = DATA_VARIANT_ENCRIPTION_KEY_BIT_SET[i]
            if (counter <= 4) {
                originalBit = originalBit xor encriptionBit
            } else {
                originalBit = !(originalBit xor encriptionBit)
            }
            counter++
            if (counter > 8) {
                counter = 1
            }
            resultBitSet[i] = originalBit
        }
        DATA_VARIANT_ENCRIPTION_KEY_BIT_SET = java.util.BitSet(0)
       watcher.debug("Finished toEncryptionKey")

        val derivatedkey=StringUtils.toByteArray(resultBitSet)
        val derivatedkeystr=StringUtils.encodeHexString(derivatedkey)
       watcher.debug("readdbkeydebug_derivatedkeystr  ${derivatedkeystr} ")

        return derivatedkey
    }

  private  fun generateEncryptionKey(transactionDBData: TransactionDBData, watcher: Watcher): EncryptionKey {
       watcher.debug("Starting generateEncryptionKey")
        var dbkey = ""
        val response = EncryptionKey()
        try {
            val pandora = PandoraneoManager()
           watcher.debug("PandoraneoManager initialized")
            val dto = DBKeyRequestDto().apply {
                hostType = transactionDBData.hostType
                terminalSerialNumber = transactionDBData.terminalSerialNumber
                keySerialNumber = transactionDBData.keySerialNumber
                rrn = transactionDBData.rrn
            }
        watcher.debug("""
            transactionDBData:
                hostType: ${transactionDBData.hostType}
                terminalSerialNumber: ${transactionDBData.terminalSerialNumber}
                keySerialNumber: ${transactionDBData.keySerialNumber}
                rrn: ${transactionDBData.rrn}
        """.trimIndent())

           watcher.debug("generateEncryptionKey_readDbKey_request:  ${GsonConfig.gsonLogs.toJson(dto)}") 
            val res = pandora.readDbKey(dto)
           watcher.debug("generateEncryptionKey_readDbKey_response:  ${GsonConfig.gsonLogs.toJson(res)}")
         

            if (res.errorCode != 0) {
                response.errorCode = res.errorCode
                response.errorMessage = res.errorMessage
               watcher.error("Error in response: ${res.errorCode}, ${res.errorMessage}")
                return response
            }
            transactionDBData.dbKcv = res.kcv
           watcher.debug("transactionDBData.dbKcv set to ${res.kcv}")
            dbkey = res.dbKey
           watcher.debug("dbkey set to $dbkey")
            response.key = toEncryptionKey(StringUtils.hexStringToByteArray(dbkey), watcher)
           watcher.debug("response.key set to ${response.key}")
            response.kcv = res.kcv
           watcher.debug("response.kcv set to ${response.kcv}")
        } catch (ex: Exception) { 
           watcher.error("Error reading DB key: ${ex.message}")
            throw ErrorReadingDBKey(ErrorCode.UnHandledException.errorCodeInt, ex.message.toString())
        }
       watcher.debug("Finished generateEncryptionKey")
        return response
    } 

    fun encryptTransactionData(transactionDBData: TransactionDBData, watcher: Watcher): TransactionDBData {
       watcher.debug("Starting encryptTransactionData")
        val encryptionKey = generateEncryptionKey(transactionDBData, watcher)
       watcher.debug("Generated encryption key: ${gson.toJson(encryptionKey)}")
        if (encryptionKey.errorCode != 0) {
           watcher.error("encryptTransactionData_Error in encryption key: ${encryptionKey.errorCode}, ${encryptionKey.errorMessage}")
            throw ErrorReadingDBKey(encryptionKey.errorCode, encryptionKey.errorMessage)
        }
        try {
           watcher.debug("Encrypting EMV data") 
            transactionDBData.emvData = StringUtils.encryptAESAscii(transactionDBData.emvData, encryptionKey.key,watcher)
           watcher.debug("EMV data encrypted: ${transactionDBData.emvData}")
        } catch (ex: Exception) {
           watcher.error("Error encrypting EMV data: ${ex.message}")
            //  throw Exception(ex.message.toString() + " ERROR ENCRYPTING EMVDATA: ${transactionDBData.emvData}")
        }
        try {
           watcher.debug("Encrypting metadata")
            transactionDBData.metaData = StringUtils.encryptAESAscii(transactionDBData.metaData, encryptionKey.key,watcher)
           watcher.debug("Metadata_encrypted: ${String(transactionDBData.metaData)}")
        } catch (ex: Exception) { 
           watcher.error("Error encrypting metadata: ${ex.message}")
         //   throw Exception(ex.message.toString() + " ERROR ENCRYPTING METADATA: ${transactionDBData.metaData}")
        }
       watcher.debug("Finished encryptTransactionData")
        return transactionDBData
    }

    fun decryptTransactionData(transactionDBData: TransactionDBData, watcher: Watcher): TransactionDBData {
       watcher.debug("Starting decryptTransactionData")
        val encryptionKey = generateEncryptionKey(transactionDBData, watcher)
       watcher.debug("Generated encryption key: ${gson.toJson(encryptionKey)}")
       val isReverse=transactionDBData.transactionType==TransactionType.Reverse;
        if(isReverse)
          WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Generated encryption key: ${gson.toJson(encryptionKey)}")

        if (encryptionKey.errorCode != 0) {
           watcher.error("decryptTransactionData_Error in encryption key: ${encryptionKey.errorCode}, ${encryptionKey.errorMessage}")
          if(isReverse)
             WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"decryptTransactionData_Error in encryption key: ${encryptionKey.errorCode}, ${encryptionKey.errorMessage}")

            throw ErrorReadingDBKey(encryptionKey.errorCode, encryptionKey.errorMessage)
        } 
        try {
            if (!encryptionKey.kcv.equals(transactionDBData.dbKcv)) {

                   if(isReverse)
                       WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"DB KCV mismatch: expected ${transactionDBData.dbKcv}, got ${encryptionKey.kcv}")

                  watcher.error("DB KCV mismatch: expected ${transactionDBData.dbKcv}, got ${encryptionKey.kcv}")
                throw DBKeyCRCFailed("DB KCV FAILED , OPERATION CAN'T BE COMPLETED")
            }
        } catch (ex: Exception) {
           watcher.error("Error validating KCV: ${ex.message}")
              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

           
            if(isReverse)
                       WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Error validating KCV: ${ex.message} ${err}")

            throw ErrorReadingDBKey(ErrorCode.UnHandledException.errorCodeInt, ex.message.toString())
        }
        try {
           watcher.debug("Decrypting EMV data")
            transactionDBData.emvData = StringUtils.decryptAESAscii(transactionDBData.emvData, encryptionKey.key,watcher)
           watcher.debug("EMV data decrypted: ${StringUtils.encodeHexString( transactionDBData.emvData)}")
        } catch (ex: Exception) {

              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

            if(isReverse)
                    WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Error decrypting EMV data: ${err}")

           watcher.error("Error decrypting EMV data: ${ex.message}")
          //  throw Exception(ex.message.toString() + " ERROR DECRYPTING EMVDATA: ${transactionDBData.emvData}")
        }
        try {
           watcher.debug("Decrypting metadata")
             if(isReverse)
                       WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Decrypting metadata")

            transactionDBData.metaData = StringUtils.decryptAESAscii(transactionDBData.metaData, encryptionKey.key,watcher)
            val metaDatASCII=String(transactionDBData.metaData)
             val maskPCI=  AppSettings.getInstance().maskPCI
             if(!maskPCI)
                  watcher.debug("JSON_ORIGINAL : ${metaDatASCII}")
            try {
                            // Asignamos la transacción a una variable local para evitar el smart cast
                val transactionDto = GsonConfig.gsonRaw.fromJson(metaDatASCII, TransactionDto::class.java)
                
                transactionDBData.transactionDto = transactionDto
                
                watcher.debug("transactionDBData.transactionDto != null : ${transactionDBData.transactionDto != null}")
                if(isReverse)
                        WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"transactionDBData.transactionDto != null : ${transactionDBData.transactionDto != null}")

                // Verificamos si la transacción tiene datos EMV
                if (transactionDto != null && transactionDto.emvData != null) {
                    watcher.debug("GET_EMV_DATA_FROM_METADATA: ${transactionDto.emvData}")
                    
                    // Aquí corregimos el nombre de la variable (transactionDBData)
                    transactionDBData.emvData = StringUtils.hexStringToByteArrayNeo( transactionDto.emvData)
  
                    watcher.debug("transactionDBData.emvData: ${StringUtils.encodeHexString(transactionDBData.emvData)}")
                }
            } catch (ex: Exception) {
               val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

                 if(isReverse)
                    WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"decryptTransactionData ERROR PARSING METADATA: ${ex.message} ${err}")

                watcher.error("decryptTransactionData ERROR PARSING METADATA: ${ex.message}")
            }

        } catch (ex: Exception) {
              val err = "${ex.javaClass.name}: ${ex.localizedMessage ?: "sin mensaje"}\n${ex.stackTraceToString()}"

            if(isReverse)
                    WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Error decrypting metadata: ${ex.message} ${err}")

           watcher.error("Error decrypting metadata: ${ex.message}")
          //  throw Exception(ex.message.toString() + " ERROR DECRYPTING METADATA: ${transactionDBData.metaData}")
        } 

             if(isReverse)
                    WatcherGlobalLog.reverseDebugRrn(transactionDBData.rrn,"Finished decryptTransactionData")

       watcher.debug("Finished decryptTransactionData")
        return transactionDBData
    }
}
