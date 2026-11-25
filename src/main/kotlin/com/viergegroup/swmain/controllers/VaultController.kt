package com.viergegroup.swmain.controllers


import com.shared.commons.StringUtils
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.security.BankRsaKey
import com.viergegroup.crypto.security.DukptData
import com.viergegroup.swmain.dtos.vault.BankRsaKeyResponse
import com.viergegroup.swmain.dtos.vault.DukptDataResponse
import com.viergegroup.swmain.managers.PandoraneoManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
//sudo lsof -i :80
import com.viergegroup.swmain.entities.catalogs.Authorizer 
import com.shared.commons.ErrorCode
import org.springframework.context.annotation.Profile
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadRSAData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.ResponseReadClientCredData
import com.viergegroup.crypto.pandora.managers.VaultDtos.RequestReadClientCredData
import com.shared.commons.GsonConfig

import com.viergegroup.swmain.webwatcher.WatcherContext


//sudo kill
//
@RestController 
@RequestMapping("vault")
class VaultController {
    protected lateinit var  pandoraneoManager: PandoraneoManager
    private lateinit var  watcher:Watcher

    @PostMapping("/unseal")
    fun unseal(@RequestBody dto: VaultDtos.UnsealRequest): VaultDtos.UnsealResponse {
        pandoraneoManager= PandoraneoManager()
        return pandoraneoManager.unseal(dto)
    }  

     @PostMapping("/generate-ksn")
     fun generateKsn(@RequestBody dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        pandoraneoManager= PandoraneoManager()
        return pandoraneoManager.generateKsn(dto)
    }

 
    @PostMapping("/save-secret") 
    fun saveSecret(@RequestBody dto: VaultDtos.SecretValue): VaultDtos.SecretValueResponse {
        pandoraneoManager= PandoraneoManager() 
        return pandoraneoManager.saveSecret(dto)
    }
    @PostMapping("/read-secret")
    fun readSecret(@RequestBody dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        pandoraneoManager= PandoraneoManager()
        return pandoraneoManager.readSecret(dto)
    }


    @PostMapping("/save-client-credentials")
    fun saveClientCredentials(@RequestBody dto: VaultDtos.RequestReadClientCredData):  VaultDtos.ResponseReadClientCredData {
        val json=GsonConfig.gsonLogs.toJson(dto)
        watcher= WatcherContext.getInstance()
        watcher.debug("saveClientCredentials: [${json}]")
        var newDto=ClientCredData()
        StringUtils.copyNonEmptyProperties(dto, newDto) 
        val data = pandoraneoManager.saveClientCredentials(newDto)
        val response =  VaultDtos.ResponseReadClientCredData()
        StringUtils.copyNonEmptyProperties(data, response)
        watcher.debug("response: [${GsonConfig.gsonLogs.toJson(response)}]")
        return response
    }
 
    @PostMapping("/read-client-credentials")
    fun readClientCredentials(@RequestBody dto: VaultDtos.RequestReadClientCredData): VaultDtos.ResponseReadClientCredData {
        watcher= WatcherContext.getInstance()
        val json=GsonConfig.gsonLogs.toJson(dto)
        watcher.debug("saveClientCredentials: [${json}]")

        val data = pandoraneoManager.readClientCredentials(dto)
        val response =  VaultDtos.ResponseReadClientCredData() 
        StringUtils.copyNonEmptyProperties(data, response)
        watcher.debug("response: [${GsonConfig.gsonLogs.toJson(response)}]")

        return response
    }


    @PostMapping("/save-dukpt-data")
    fun saveDukptData(@RequestBody dto: DukptData): VaultDtos.DukptMetaResponseMin {
        pandoraneoManager= PandoraneoManager()
        if(dto.hostType.authorizer==Authorizer.OPENPAY){
            if(dto.keyId==""||dto.keyId!="BCMER001"&&dto.keyId!="A000OPFT48"&&dto.keyId!="OPENPUBLIC"&&dto.keyId!="OPENPRIVAD"){
                val response=VaultDtos.DukptMetaResponseMin()
                response.errorCode=ErrorCode.RSADOesntExists.errorCodeInt
                response.errorMessage=ErrorCode.RSADOesntExists.errorCodeMessage
                response.errorCodeEnum=ErrorCode.RSADOesntExists
                return response
            } 
        }
        if(dto.ipek!=""){
            dto.ipekDecrypted=dto.ipek
            dto.ipekClear=dto.ipek

        } 
        return pandoraneoManager.saveDukptData(dto)
    } 


    @PostMapping("/read-dukpt-data")
    fun readDukptData(@RequestBody dto: VaultDtos.RequestReadDukptData):DukptDataResponse{
        pandoraneoManager= PandoraneoManager()
        val data=pandoraneoManager.readDukptData(dto)
        val response=DukptDataResponse() 
        StringUtils.copyNonEmptyProperties(data,response)
        return response
    } 
   
    @PostMapping("/read-dukpt-pin-data")
    fun readDukptPinData(@RequestBody dto: VaultDtos.RequestReadDukptData):DukptDataResponse{
        pandoraneoManager= PandoraneoManager()
        dto.terminalSerialNumber=dto.terminalSerialNumber+"_PIN"
        val data=pandoraneoManager.readDukptData(dto)
        val response=DukptDataResponse() 
        StringUtils.copyNonEmptyProperties(data,response)
        return response 
    }   
    

    @PostMapping("/save-rsa")
    fun saveRsa(@RequestBody dto: BankRsaKey): BankRsaKeyResponse {
        pandoraneoManager= PandoraneoManager()
        val data=pandoraneoManager.saveRsa(dto)
        val response=BankRsaKeyResponse()
        StringUtils.copyNonEmptyProperties(data,response)
        return response

    }
    @PostMapping("/read-rsa")
    fun readRsa(@RequestBody dto: VaultDtos.RequestReadRSAData): BankRsaKeyResponse {
        pandoraneoManager= PandoraneoManager()
        val data=pandoraneoManager.readRsa(dto)
        val response=BankRsaKeyResponse()
        StringUtils.copyNonEmptyProperties(data,response)
        return response
    }
 


}
