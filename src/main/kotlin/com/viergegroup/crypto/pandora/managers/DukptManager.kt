package com.viergegroup.crypto.pandora.managers

import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils

import com.viergegroup.crypto.security.Dukpt
import com.viergegroup.swmain.dtos.pandora.DukptDataDto
import com.viergegroup.swmain.dtos.pandora.DukptExistsResponse
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.webwatcher.WatcherContext

import org.springframework.beans.BeanUtils

class DukptManager {
    private  val  watcher= WatcherContext.getInstance()


    fun computeKeys(dto: PandoraTransactionDto): DukptDataDto {

        val data= DukptDataDto()
        try {

            if(!dto.hostType.authorizer.computeInternalKeys){
                data.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                data.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return data
            }
           watcher.debug("INI computeKeys")
            val key = StringUtils.generateRandomHexadecimal(32)
           watcher.debug("key $key ")
            val tk = StringUtils.generateRandomHexadecimal(32)
           watcher.debug(" tk $tk terminalSerialNumber ${dto.terminalSerialNumber}")
            val ksn = StringUtils.generateRandomHexadecimal(16)+ "0001"
           watcher.debug("key $key tk $tk KSN $ksn")
            val dataTemp = Dukpt.saveComputedKeys(dto.hostType, tk, dto.terminalSerialNumber,dto.keyId, ksn, key, watcher)
            BeanUtils.copyProperties(dataTemp,data)
        }catch (e:Exception){
            data.errorMessage=e.message.toString()
           watcher.debug("EXCEPTION COMPUTEKEYS: ${e.message}")
        }
        closeLogs()
        return data
    }
    fun existKeys(dto: PandoraTransactionDto): DukptExistsResponse {

        val data= DukptExistsResponse()
        try {
            data.existsKeys= Dukpt.existsKey(dto.hostType,dto.terminalSerialNumber,dto.keyId,dto.rrn,watcher)
            data.errorCode=0
            data.errorMessage=""
        }catch (e:Exception){
            data.errorMessage=e.message.toString()
           watcher.debug("EXCEPTION existKeys: ${e.message}")
        }
        closeLogs()
        return data
    }



    fun decryptDukpt(dto: PandoraTransactionDto):PandoraTransactionDto{

        val response=PandoraTransactionDto() 
        BeanUtils.copyProperties(dto,response)
        try {
            if(!dto.hostType.authorizer.computeInternalKeys){
                response.errorCode=ErrorCode.InvalidHVOperation.errorCodeInt
                response.errorMessage=ErrorCode.InvalidHVOperation.errorCodeMessage
                return response
            }
            if(!Dukpt.existsKey(dto.hostType,dto.terminalSerialNumber,dto.keyId,dto.rrn,watcher)){
                response.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
                response.errorMessage=ErrorCode.KeyLoadIsNecesary.errorCodeMessage
                return response
            }
            val resDukpt = Dukpt.decryptData(dto.hostType, dto.terminalSerialNumber,dto.keyId,dto.rrn,dto.keySerialNumber,dto.cipherData, watcher)
            response.errorCode=resDukpt.errorCode
            response.errorMessage=resDukpt.errorMessage
            response.track2=resDukpt.clearData
        }catch (e:Exception){
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            response.errorMessage="EXCEPTION DECRYPT "+e.message.toString()
           watcher.debug("EXCEPTION DECRYPT: ${e.message}")
            return response
        }
        closeLogs()
        return response
    }


    fun encryptDukpt(dto: PandoraTransactionDto): DukptDataDto {

        val resDto= DukptDataDto()
        resDto.hostType=dto.hostType
        var computedKeys=false
        try {
            if(!Dukpt.existsKey(dto.hostType,dto.terminalSerialNumber,dto.keyId,dto.rrn,watcher)){
               if(dto.hostType.authorizer.computeInternalKeys){
                  watcher.debug("KEYS DOESNT EXIST ${dto.hostType} ${dto.terminalSerialNumber} ${dto.keyId} ${dto.rrn}")
                  watcher.debug("COMPUTE KEYS")
                   computeKeys(dto)
                   computedKeys=true
               }else{
                   resDto.errorCode=ErrorCode.KeyLoadIsNecesary.errorCodeInt
                   resDto.errorMessage=ErrorCode.KeyLoadIsNecesary.errorCodeMessage
                   return resDto
               }
            }
            val resultDukpt =
                Dukpt.encryptData(dto.hostType, dto.terminalSerialNumber,dto.keyId,dto.rrn, dto.track2, watcher);
            resDto.setDukptData(resultDukpt)
            resDto.errorCode=0
            if(computedKeys)
              resDto.errorMessage="AUTO COMPUTED KEYS SUCCESS!"

            //  Dukpt.saveDerivatedKey(data,logger)
        }catch (e:Exception){
            resDto.errorCode=-1
            resDto.errorMessage=e.message.toString()
           watcher.debug("EXCEPTION ENCRYPTDATA: ${e.message}")
        }
        closeLogs()
        return resDto
    }


    fun closeLogs(){ 
       watcher.close()
    }

}