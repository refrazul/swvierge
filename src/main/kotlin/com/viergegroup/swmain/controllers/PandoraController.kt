package com.viergegroup.swmain.controllers


import com.shared.commons.StringUtils
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.crypto.security.KeyPairData
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.swmain.dtos.pandora.*
import com.viergegroup.swmain.managers.PandoraneoManager

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.shared.commons.ErrorCode 
import com.viergegroup.swmain.entities.catalogs.Authorizer
import org.springframework.context.annotation.Profile
import com.viergegroup.crypto.security.JWTHelper


@RestController
@RequestMapping("pandora")
class PandoraController { 
 
   @PostMapping("/calculate-rsa-hash")
    fun calculateRsaHash(@RequestBody dto:  VaultDtos.ReadKeyRequest): VaultDtos.CalculateHashResponse {
       
        if(dto.hostType.authorizer==Authorizer.OPENPAY){
            if(dto.keyId==""||dto.keyId!="BCMER001"&&dto.keyId!="A000OPFT48"&&dto.keyId!="OPENPUBLIC"&&dto.keyId!="OPENPRIVAD"){
                val response= VaultDtos.CalculateHashResponse()
                response.errorCode=ErrorCode.RSADOesntExists.errorCodeInt
                response.errorMessage=ErrorCode.RSADOesntExists.errorCodeMessage
                response.errorCodeEnum=ErrorCode.RSADOesntExists
                return response
            }   
        }
        val response= PandoraneoManager().calculateRsaHash(dto)

        return response
    }
    

 

    @PostMapping("/encrypt-data")
    fun encryptData(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto= PandoraneoManager().encryptData(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }

 

    @PostMapping("/transfer-pinblock")
    fun transferPinBlock(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto=PandoraneoManager().transferPinBlock(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }
    

    @PostMapping("/encrypt-pinblock") 
    fun encryptPinBlock(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto=PandoraneoManager().encryptPinBlock(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }
   


    @PostMapping("/decrypt-pinblock")
    fun decryptPinBlock(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto= PandoraneoManager().decryptPinBlock(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }


    @PostMapping("/calculate-pek-pin")
    fun calculatePinPek(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto= PandoraneoManager().calculatePinPek(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }

    @PostMapping("/compute-pin-keys")
    fun computePinKeys(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto= PandoraneoManager().computePinKeys(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }


    




    @PostMapping("/decrypt-data")
    fun decryptData(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto=PandoraneoManager().decryptData(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }




     

    @PostMapping("/build-tokenew")
    fun buildTokenEW(@RequestBody dto: PandoraTransactionDto): PandoraResponse {
        val dto=PandoraneoManager().buildTokenEW(dto)
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }

   /*  @PostMapping("/read-rsa")
    fun readRSA(@RequestBody dto: VaultDtos.RequestReadRSAData): BankRsaKey {
       val response =  PandoraneoManager().readRsa(dto)
        return response
    }*/
    


    @PostMapping("/inject-tokenex")
    fun injectTokenEX(@RequestBody dto: PandoraTransactionDto): PandoraResponse {

        val dto= PandoraneoManager().injectTokenEX(dto,"")
        val response=PandoraResponse()
        StringUtils.copyNonEmptyProperties(dto,response)
        return response
    }

    /*@PostMapping("/decrypt-tdes")
    fun decryptTDesAscii(@RequestBody dto: TDesDto): TDesDto {
        return com.viergegroup.crypto.pandora.managers.SpecialCipherManager().decryptTDesAscii(dto)
    }
    @PostMapping("/encrypt-tdes")
    fun encryptTDesAscii(@RequestBody dto: TDesDto): TDesDto {
        return com.viergegroup.crypto.pandora.managers.SpecialCipherManager().encryptTDesAscii(dto)
    }
*/
 

    @PostMapping("/read-dbkey")
    fun readDbKey(@RequestBody dto: DBKeyRequestDto): DBKeyResponseDto  {
        return PandoraneoManager().readDbKey(dto)
    }



}