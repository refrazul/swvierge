package com.viergegroup.crypto.pandora.managers

import com.shared.commons.ErrorCode

import com.viergegroup.crypto.security.KeyPairData
import com.viergegroup.crypto.security.RSARequestData
import com.viergegroup.crypto.security.RSAUtils
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import com.viergegroup.swmain.entities.catalogs.Authorizer

import com.viergegroup.swmain.webwatcher.WatcherContext
 
import org.springframework.beans.BeanUtils
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadRSAData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData

import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadDukptData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ReadKeyRequest
import  com.viergegroup.crypto.security.DerivatedKeysData
import  com.viergegroup.crypto.security.DerivatedKeysHsmData





import com.viergegroup.crypto.pandora.managers.VaultDtos.ResponseReadClientCredData
import com.viergegroup.crypto.security.PINCrypto
 
 
import com.shared.commons.StringUtils

import com.viergegroup.crypto.pandora.managers.VaultDtos.CalculateHashResponse
import com.shared.commons.StringUtils.maskString

class PandoraManager {  

    private  val  watcher= WatcherContext.getInstance()

   




    fun calculateRsaHash(dto:VaultDtos.ReadKeyRequest):VaultDtos.CalculateHashResponse{
        val response=VaultDtos.CalculateHashResponse()
        watcher.debug("calculateRsaHash")
        watcher.debug("keyId: ${dto.keyId},terminalSerialNumber: ${dto.terminalSerialNumber}")
        val rsaReaddto=RequestReadRSAData()
        rsaReaddto.id=dto.keyId
        BeanUtils.copyProperties(dto,rsaReaddto) 
        val vaultmanager=VaultSecureManager()
        val rsaData=vaultmanager.readRsaKey(rsaReaddto) 
        if(rsaData.errorCode!=0){
            BeanUtils.copyProperties(rsaData,response)
            watcher.debug("errorCode: ${rsaData.errorCode},errorMessage: ${rsaData.errorMessage}")
            return response
        }else{ 
            watcher.debug("OK READ RSA")
        }
        try{ 

            watcher.debug(" rsaData.modulus [${maskString( rsaData.modulus)}] rsaData.exponent [${rsaData.exponent}]")


            val modulusBytes=StringUtils.hexStringToByteArrayNeo( rsaData.modulus)
            val exponentBytes=StringUtils.hexStringToByteArrayNeo( rsaData.exponent)

            response.hashSha1 = RSAUtils.calculateRsaHashSha1( modulusBytes, exponentBytes)
            response.hashSha256 = RSAUtils.calculateRsaHashSha256(modulusBytes,exponentBytes)
            response.hashSha512 = RSAUtils.calculateRsaHashSha512(modulusBytes, exponentBytes)
           
            response.hashAes = RSAUtils.calculateAesHash(modulusBytes,exponentBytes)
            response.hashTripleDes = RSAUtils.calculateTripleDesHash(modulusBytes,exponentBytes)
            response.hash = RSAUtils.calculateHashRsa(rsaData.modulus, rsaData.exponent)
            response.expectedHash = rsaData.hash 
   
          //  response.hash = response.hashSha1 // Puedes elegir cuál hash devolver
  
            watcher.debug("calculateRsaHash_hash ${response.hash}")
            response.errorCode = 0

        }catch(ex:Exception){
            response.errorCode=ErrorCode.RSAHashError.errorCodeInt
            response.errorMessage=ErrorCode.RSAHashError.errorCodeMessage+"|"+ex.message
            return response

        }  
        
        val readrukptdto=VaultDtos.RequestReadDukptData()
        BeanUtils.copyProperties(dto,readrukptdto)
        val dukptData=vaultmanager.readDukptData(readrukptdto)
        response.kcv=dukptData.kcv
        watcher.debug("readDukptData kcv [${dukptData.kcv}]")
 
        return response


    }
    fun encryptData( dto: PandoraTransactionDto): PandoraTransactionDto {

       val response= PandoraTransactionDto()
       BeanUtils.copyProperties(dto,response)
       watcher.debug(" EXECUTE encryptData ${dto.hostType.authorizer}")
       watcher.debug(" dukptDto.hostType : ${  dto.hostType}")
       watcher.debug(" dukptDto.rrn : ${  dto.rrn}")
       watcher.debug(" dukptDto.terminalSerialNumber : ${  dto.terminalSerialNumber}")
       watcher.debug(" dukptDto.keyId1 : ${  dto.keyId1}")
       watcher.debug(" dukptDto.keyId2 : ${  dto.keyId2}")
        if(dto.hostType.authorizer== Authorizer.OPENPAY){
            val resDoOpenPay= SpecialCipherManager().doSpecialCipherOpenPay(dto)
            if(resDoOpenPay.errorCode!=0){
               watcher.debug(" PandoraneoManager response Error : ${ resDoOpenPay.errorCode}, ${ resDoOpenPay.errorMessage}")
                dto.errorMessage=resDoOpenPay.errorMessage
                dto.errorCode=resDoOpenPay.errorCode 
                return dto
            } 
            response.tokenEZ=resDoOpenPay.tokenEZ
            response.tokenEZ2=resDoOpenPay.tokenEZ2
            response.tokenOpenPay=resDoOpenPay.tokenOpenPay 
            response.tokenOpenPayClear=resDoOpenPay.tokenOpenPayClear

        }else{
            val track2Length =  dto.track2.replace("F","").length.toString().padStart(2,'0')
           watcher.debug("EXECUTE SpecialCipherManager().buildTokenEZ")
            var resTokenEZ=  com.viergegroup.crypto.pandora.managers.SpecialCipherManager().buildTokenEZ(dto)
            if(resTokenEZ.errorCode== ErrorCode.KeyLoadIsNecesary.errorCodeInt){ 
                val resCompute= com.viergegroup.crypto.pandora.managers.DukptManager().computeKeys(dto)
                if(resCompute.errorCode!=0){
                    dto.errorMessage="ERROR COMPUTING KEYS: "+resCompute.errorMessage
                    dto.errorCode=resCompute.errorCode
                }
                resTokenEZ=  com.viergegroup.crypto.pandora.managers.SpecialCipherManager().buildTokenEZ(dto)
            }
            if(resTokenEZ.errorCode!=0){ 
               watcher.debug(" PandoraneoManager.buildTokenEZ response Error : ${ resTokenEZ.errorCode}, ${ resTokenEZ.errorMessage}")
                dto.errorMessage=resTokenEZ.errorMessage
                dto.errorCode=resTokenEZ.errorCode
                return dto 
            }
            response.cipherData=resTokenEZ.cipherData
            response.cipherCounter=resTokenEZ.cipherCounter
            response.keySerialNumber=resTokenEZ.keySerialNumber
            response.track2Length = track2Length
            response.failCipherCounter ="00" 
            response.track1Flag ="0"
            response.crc32EZ = resTokenEZ.crc32EZ
            response.clearCvvLength = resTokenEZ.clearCvvLength

           watcher.debug(" resTokenEZ.cipherData : ${  resTokenEZ.cipherData}")
           watcher.debug(" resTokenEZ.cipherCounter : ${  resTokenEZ.cipherCounter}")
           watcher.debug(" resTokenEZ.keySerialNumber : ${  resTokenEZ.keySerialNumber}")
           watcher.debug("track2Length : ${  track2Length}")

        } 
       return response  
    } 
  
    fun decryptData( dto: PandoraTransactionDto): PandoraTransactionDto {
        return DukptManager().decryptDukpt(dto)
    }

    fun transferPinBlock( dto: PandoraTransactionDto): PandoraTransactionDto {
        return PINCrypto.transferPinBlock(dto, watcher) 
    } 
    fun encryptPinBlock( dto: PandoraTransactionDto): PandoraTransactionDto {
        return PINCrypto.encryptPinBlock(dto, watcher)
    }
     
    fun decryptPinBlock( dto: PandoraTransactionDto): PandoraTransactionDto {
        return PINCrypto.decryptPinBlock(dto, watcher)
    }

    fun calculatePinPek( dto: PandoraTransactionDto): PandoraTransactionDto {
        return PINCrypto.calculatePinPek(dto, watcher)
    }


    fun computePinKeys(dto: PandoraTransactionDto):DerivatedKeysHsmData{
        return PINCrypto.computePinKeys(dto, watcher)
    }

    fun buildTokenEW( dto: PandoraTransactionDto): PandoraTransactionDto {
        return SpecialCipherManager().buildTokenEW(dto)
    }   
    fun buildTokenEZ( dto: PandoraTransactionDto): PandoraTransactionDto {
        return SpecialCipherManager().buildTokenEZ(dto)
    }
    fun injectTokenEX( dto: PandoraTransactionDto): PandoraTransactionDto {
        return SpecialCipherManager().injectTokenEX(dto)
    }
    fun generateRsaKeys( dto: PandoraTransactionDto): KeyPairData {
        val rsarequestData= RSARequestData()
        rsarequestData.terminalSerialNumber=dto.terminalSerialNumber
        rsarequestData.hostType=dto.hostType
        return RSAUtils.generateRSAKeyPair(rsarequestData)
    }
    fun keyLoad(dto: PandoraTransactionDto):PandoraTransactionDto{
        return com.viergegroup.crypto.pandora.managers.SpecialCipherManager().keyLoad(dto)
    }

    fun decryptRsa(dto: PandoraTransactionDto):PandoraTransactionDto{
        return com.viergegroup.crypto.pandora.managers.SpecialCipherManager().decryptRsa(dto)

    } 

}