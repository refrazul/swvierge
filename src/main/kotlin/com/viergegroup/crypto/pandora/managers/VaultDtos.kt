package com.viergegroup.crypto.pandora.managers

import com.google.gson.GsonBuilder
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.*
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto

import com.viergegroup.swmain.dtos.pandora.TDesDto
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

import java.util.*

import com.shared.commons.StringUtils.maskString

import javax.net.ssl.X509TrustManager
import com.viergegroup.swmain.entities.catalogs.Authorizer 
data class Response<T>(val data: T?, val error: String?)

class VaultDtos{
    class LoginRequest{
        var  policies = listOf(
            "vierge-policy", 
            "banorte-policy",
            "prosa-policy",
            "openpay-policy"
        ) 
        var   renewable = true
    }
    class LoginResponse{
        var errorCode=-1
        var errorMessage=""
        var request_id: String=""
        val auth: VaultDtos.AuthDto =
            VaultDtos.AuthDto()

    }
    data class KeyVaultData(
        val keyData: String
    )
    class ReadKeyRequest:RequestEntity(){
        var keyId="" 

    } 
    class RequestReadDukptData:RequestEntity(){
        var keyId=""
      


    }
    class RequestReadRSAData:RequestEntity(){
        var id=""
 
    }
    class RequestReadClientCredData:RequestEntity(){
        var expirationDateTime: Date =Date()
        var data: String = "" 
        var packageName: String = "" 

    }
    class ClientCredData:RequestEntity(){
        var data: String = ""
        var expirationDateTime: Date =Date()
        var errorCode=-1
        var errorMessage="" 
        var packageName: String = "" 

    }
    data class CredentialMetadata(
        val hostType: HostType,
        var expirationDateTime: Date
    )

    class AuthDto{
        var client_token: String=""

    }

    class ReadKeyResponse{
        var errorCode=-1
        var errorMessage=""
        var requestId = ""
        var leaseId = ""
        var renewable = false
        var leaseDuration = 0
        var data = VaultDtos.KeyVaultData(
            keyData = ""
        )
    }


    class SecretData{
        var keyId=""
        var hostType: HostType = HostType.ProsaDUKPT
        var data:Any?=null
        var errorCode=-1
        var errorMessage=""
    }
    class SecretValueMetaRequest{
        var data: VaultDtos.SecretValueMin =
            VaultDtos.SecretValueMin()
    }


    class SecretValueMetaResponse{
        var data: VaultDtos.SecretValueMetaRequest =
            VaultDtos.SecretValueMetaRequest()
    }
  class ClientCredentialsMetaRequest{
        var data: VaultDtos.ClientCredData =
            VaultDtos.ClientCredData()
    }

    
    class ClientCredentialsMetaResponse{
        var data: VaultDtos.ClientCredData =
            VaultDtos.ClientCredData()
    }


    class RsaValueMetaRequest{
        var data:BankRsaKey=BankRsaKey()
    }
    class RsaValueMetaResponse{
        var data: VaultDtos.RsaValueMetaRequest =
            VaultDtos.RsaValueMetaRequest()
    }
    class DukptMetaRequest{
        var data:DukptData=DukptData()
    }
    class DBKeyMetaRequest{
        var data: DBKeyResponseDto= DBKeyResponseDto()
    }
    class DBKeyMetaResponse{
        var data: VaultDtos.DBKeyMetaRequest =
            VaultDtos.DBKeyMetaRequest()
    }
    class ResponseReadClientCredData : ResponseEntity() {
        var data: String = "" // Usa el constructor para obtener la fecha actual
        var expirationDateTime: Date = Date() // Usa el constructor para obtener la fecha actual
    }

    class DukptMetaResponseMin:ResponseEntity(){
        var ipekEncrypted=""
    }
    class DukptMetaResponse{ 
        var data: VaultDtos.DukptMetaRequest =
            VaultDtos.DukptMetaRequest()
    }

    class SecretValue:RequestEntity(){
        var keyId=""
        var data=""

    }

    class CalculateHashResponse:ResponseEntity(){
        var hostType: HostType = HostType.Undefined
        var hash=""
        var expectedHash=""
        var hashSha1=""
        var hashSha256 = "" 
        var hashSha512 = ""
        var hashAes = ""
        var hashTripleDes = ""
        var kcv=""
  
    }  
    class SecretValueResponse:ResponseEntity(){
        var hostType: HostType = HostType.Undefined
        var keyId=""
        var data="" 

    }
    class SecretValueMin{

        var value=""

    }


    class UnsealRequestAlone{
        var key=""


    }

    class UnsealRequest:RequestEntity(){

        var key01=""
        var key02=""
        var key03=""
        var key04=""

    }
    class UnsealResponse:ResponseEntity(){
        var keyValue=""
    }
}


