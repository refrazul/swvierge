package com.viergegroup.crypto.pandora.managers

import com.google.gson.GsonBuilder
import com.shared.commons.AppSettings
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.*
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.swmain.dbmanagers.VaultDataDBManager

import com.viergegroup.swmain.dtos.pandora.TDesDto
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import com.shared.commons.ErrorCode

import java.io.BufferedReader 
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import com.shared.commons.StringUtils.maskString
import org.springframework.beans.BeanUtils

import javax.net.ssl.X509TrustManager
import com.viergegroup.swmain.entities.catalogs.Authorizer 
class VaultSecureDBManager {
  
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private  var isVaultConsole=true 
    private var simulateHSM=false 
    val useDocuments=false
    val baseAuthToken=""  
    var vaultVersion=""  
    var authToken="" 
    var pandoraLocal=false
    var lastUpdateToken= Date() 
    var leaseDuration:Long=0L
    private  val  watcher= WatcherContext.getInstance()
    constructor(){ 
        vaultVersion="_"+AppSettings.getInstance().vVersion 
        simulateHSM=AppSettings.getInstance().simulateHSM 
        isVaultConsole=AppSettings.getInstance().isVaultConsole
        pandoraLocal=AppSettings.getInstance().pandoraLocal
        watcher.debug("CONFIGURING VaultSecureManager  \n \n  pandoraLocal [$pandoraLocal] simulateHSM [$simulateHSM] vaultVersion [$vaultVersion] isVaultConsole [$isVaultConsole]    \n\n")

    } 
    fun unseal(dto: VaultDtos.UnsealRequest): VaultDtos.UnsealResponse {
        return VaultDtos.UnsealResponse()
    }   
 

    fun saveKeyValue(dto: VaultDtos.SecretValue): VaultDtos.SecretValueResponse {
        watcher.debug("saveKeyValue=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveKeyValue(dto) 
        } 
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        val response= VaultDtos.SecretValueResponse()
        BeanUtils.copyProperties(dto,response)
        VaultDataDBManager.saveVaultData(dto.hostType.name, dto.keyId, dto.data)
        response.errorCode=0
        return response
    } 
    
    fun saveClientCredentials(dto: VaultDtos.ClientCredData):  VaultDtos.ClientCredData {
        watcher.debug("saveClientCredentials=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        var response =  VaultDtos. ClientCredData() 
        if (AppSettings.getInstance().isDocumentVault) {
            return VaultDocumentManager.saveClientCredentials(dto)
        }
        VaultDataDBManager.saveVaultData(dto.hostType.name,dto.packageName, dto.data)
        BeanUtils.copyProperties(dto,response)
        response.errorCode=0
        return response
    }
      
 
    fun readClientCredentials(dto: VaultDtos.RequestReadClientCredData): VaultDtos.ClientCredData {
        watcher.debug("readClientCredentials=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        var response =  VaultDtos. ClientCredData() 
        if (AppSettings.getInstance().isDocumentVault) {
            return VaultDocumentManager.readClientCredentials(dto)
        }
        BeanUtils.copyProperties(dto,response)
        response.data=  VaultDataDBManager.getVaultData(dto.hostType.name,dto.packageName)
        response.errorCode=0
        return response
    }
    
    

    fun saveRsaKey(dto: BankRsaKey): BankRsaKey {
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        watcher.debug("saveRsaKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveRsaKey(dto)
        }
        val newDto= VaultDtos.RsaValueMetaRequest()
        newDto.data=dto
       
        var response= BankRsaKey()
        val json=gson.toJson( dto)
        VaultDataDBManager.saveVaultData(dto.hostType.name,dto.id,json)
        BeanUtils.copyProperties(dto,response)
        response.errorCode=0
        return response 
    }
 
    
    fun readRsaKey(dto: VaultDtos.RequestReadRSAData): BankRsaKey {
        // Convertir el número de serie del terminal a mayúsculas
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        // Registrar un mensaje de depuración
        watcher.debug("readRsaKey => isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readRsaKey(dto)
        }
        // Obtener los datos del almacén (vault) en formato JSON
        val json = VaultDataDBManager.getVaultData(dto.hostType.name, dto.id)
        // Convertir el JSON a un objeto BankRsaKey
        val response = gson.fromJson(json, BankRsaKey::class.java)
        // Copiar las propiedades del DTO a la respuesta (si es necesario)
        BeanUtils.copyProperties(dto, response)
        // Establecer el código de error (asumiendo que 0 significa éxito)
        response.errorCode=0
        // Retornar la respuesta
        return response
    }


     
    fun readDbKey(dto: DBKeyRequestDto):  DBKeyResponseDto{
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        watcher.debug("readDbKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}") 
         
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readDbKey(dto)
        }
        val id=dto.terminalSerialNumber+"_DB"
        val response= DBKeyResponseDto()
        BeanUtils.copyProperties(dto, response)
        response.dbKey = VaultDataDBManager.getVaultData(dto.hostType.name, dto.keySerialNumber)
        response.errorCode=0
        return response
    }

    fun saveDbKey(dto: DBKeyRequestDto): DBKeyResponseDto {
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveDbKey(dto)
        }
        val response=DBKeyResponseDto()
        VaultDataDBManager.saveVaultData(dto.hostType.name,dto.keySerialNumber,dto.dbKey)
        response.errorCode=0
        return response
    }
 

    fun readDbKeyFull(dto: DBKeyRequestDto): DBKeyResponseDto {
      dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        watcher.debug("readDbKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}") 
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readDbKey(dto)
        }
 
        val id=dto.terminalSerialNumber+"_DB"
        val response= DBKeyResponseDto()
        BeanUtils.copyProperties(dto, response)
        response.dbKey = VaultDataDBManager.getVaultData(dto.hostType.name, dto.keySerialNumber)
        response.errorCode=0
        return response

    }

    fun saveDukptData(dto: DukptData): VaultDtos.DukptMetaResponseMin {
        watcher.debug("saveDukptData => isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType = StringUtils.getVaultType(dto.hostType)
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
    
        watcher.debug("vaultType [$vaultType]")
        
        if (dto.tk.isNotEmpty()) {
            try {
                watcher.debug("saveDukptData_tk [${dto.tk}]")
                val checkValue = RSAUtils.TDesCipher(ByteArray(8), StringUtils.hexStringToByteArray(dto.tk))
                val checkValueEW = if (dto.hostType.authorizer == Authorizer.OPENPAY) {
                    StringUtils.encodeHexString(checkValue).substring(0, 8)  // RDM_KCV
                } else {
                    StringUtils.encodeHexString(checkValue).substring(0, 6)  // RDM_KCV
                }
                dto.kcv = checkValueEW
            } catch (ex: Exception) {
                watcher.error("saveDukptData_exception [${ex.message}]")
                // Considera relanzar la excepción o manejarla de manera más específica
            }
        }
    
        return if (AppSettings.getInstance().isDocumentVault) {
            VaultDocumentManager.saveDukptData(dto)
        } else {
            val response = VaultDtos.DukptMetaResponseMin()
            val keyId = "${dto.terminalSerialNumber}$vaultType$vaultVersion${dto.keyId}"
            val json = gson.toJson(dto)
            VaultDataDBManager.saveVaultData(dto.hostType.name, keyId, json)
            BeanUtils.copyProperties(dto, response)
            response.errorCode=0
            response
        }
    }
    
    fun readDukptData(dto: VaultDtos.RequestReadDukptData): DukptData {
        watcher.debug("readDukptData => isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType = StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [$vaultType]")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
    
        return if (AppSettings.getInstance().isDocumentVault) {
            val response = VaultDocumentManager.readDukptData(dto)
            watcher.debug("readDukptData => result ${response.errorCode == 0}")
            response
        } else {
            val keyId = "${dto.terminalSerialNumber}$vaultType$vaultVersion${dto.keyId}"
            watcher.debug("readDukptData \"${dto.terminalSerialNumber}$vaultType${dto.keyId}\"")
            val json = VaultDataDBManager.getVaultData(dto.hostType.name, keyId)
            val response=  gson.fromJson(json, DukptData::class.java)
            response.errorCode=0
            response
        }
    }


 
    fun readKeyValue( dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        watcher.debug("readKeyValue=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readKeyValue(dto)
        } 
        val response= VaultDtos.SecretValueResponse()
        response.keyId=dto.keyId
        response.hostType=dto.hostType
        BeanUtils.copyProperties(dto, response)
        response.data = VaultDataDBManager.getVaultData(dto.hostType.name, dto.keyId)
        response.errorCode=0
        return response
    }

 
}

