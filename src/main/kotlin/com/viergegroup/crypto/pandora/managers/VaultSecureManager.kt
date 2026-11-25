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
 
import javax.net.ssl.X509TrustManager
import com.viergegroup.swmain.entities.catalogs.Authorizer 
class VaultSecureManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()
   // val baseUrlProd="https://internal-prod-vault-1768334386.us-east-1.elb.amazonaws.com"
    //val baseUrl="https://internal-prod-vault-1768334386.us-east-1.elb.amazonaws.com"
     // val baseUrlDev="http://127.0.0.1:8200"
   // val baseUrlDev="https://127.0.0.1:8200"
  //  val baseUrlDevWin="https://localhost:8200"
      private  var baseUrl="https://localhost:8200"
    private  val additionalUri="secret"
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
        baseUrl=AppSettings.getInstance().vUrl 
        simulateHSM=AppSettings.getInstance().simulateHSM 
        isVaultConsole=AppSettings.getInstance().isVaultConsole
        pandoraLocal=AppSettings.getInstance().pandoraLocal
        watcher.debug("CONFIGURING VaultSecureManager  \n \n  pandoraLocal [$pandoraLocal] simulateHSM [$simulateHSM] vaultVersion [$vaultVersion] isVaultConsole [$isVaultConsole]   baseUrl [$baseUrl]  \n\n")

    }

    fun checkLogin(){
        if(authToken==""&&!isVaultConsole){
            // login()
            val dto= TDesDto()
            dto.data= AppSettings.getInstance().vToken
            dto.key="01111993ACF"
            val resdto= SpecialCipherManager().decryptTDesAscii(dto)
            authToken=resdto.data
            //    println("authToken :$authToken")
        }
        if(isVaultConsole){
            val dto= TDesDto()
            dto.data= AppSettings.getInstance().vToken
            dto.key="01111993ACD"
            val resdto= SpecialCipherManager().decryptTDesAscii(dto)
            authToken=resdto.data
        }
    }
    fun login(): VaultDtos.LoginResponse {
        var request= VaultDtos.LoginRequest()
        var response= VaultDtos.LoginResponse()
        authToken=baseAuthToken
        val resapi=doApiRequest("v1/auth/token/create","POST",request,
            VaultDtos.LoginResponse::class.java)
        if(resapi.data!=null){
            response.errorCode=0

            response=resapi.data!!
            authToken=response.auth.client_token
        }else{
            response.errorMessage=resapi.error!!
            response.errorCode=-1
        }

        authToken=baseAuthToken

        return response

    }

 

    fun unseal(dto: VaultDtos.UnsealRequest): VaultDtos.UnsealResponse {
        checkLogin()

        val request= VaultDtos.UnsealRequestAlone()
        val successList= mutableListOf<Boolean>()


        request.key=PasswordUtil.destransformPassword( dto.key01)
        var response= VaultDtos.UnsealResponse()
        var resapi=doApiRequest("v1/sys/unseal","PUT",request,
            VaultDtos.UnsealResponse::class.java)
        if(resapi.data!=null){
            response= resapi.data!!
            response.errorCode=0
            successList.add(true)
        }else   if(resapi.error!=null){
            response.errorMessage=resapi.error.toString()
            response.errorCode=-1
            successList.add(false)
        }
        request.key=PasswordUtil.destransformPassword( dto.key02)
         response= VaultDtos.UnsealResponse()
         resapi=doApiRequest("v1/sys/unseal","PUT",request,
            VaultDtos.UnsealResponse::class.java)
        if(resapi.data!=null){
            response= resapi.data!!
            response.errorCode=0
            successList.add(true)
        }else   if(resapi.error!=null){
            response.errorMessage=resapi.error.toString()
            response.errorCode=-1
            successList.add(false)
        }

 
        request.key=PasswordUtil.destransformPassword( dto.key03)
        response= VaultDtos.UnsealResponse()
        resapi=doApiRequest("v1/sys/unseal","PUT",request,
            VaultDtos.UnsealResponse::class.java)
        if(resapi.data!=null){
            response= resapi.data!!
            response.errorCode=0
            successList.add(true)
        }else   if(resapi.error!=null){
            response.errorMessage=resapi.error.toString()
            response.errorCode=-1
            successList.add(false) 
        }
        request.key=PasswordUtil.destransformPassword( dto.key04)
        response= VaultDtos.UnsealResponse()
        resapi=doApiRequest("v1/sys/unseal","PUT",request,
            VaultDtos.UnsealResponse::class.java)
        if(resapi.data!=null){
            response= resapi.data!!
            response.errorCode=0
            successList.add(true)
        }else   if(resapi.error!=null){
            response.errorMessage=resapi.error.toString()
            response.errorCode=-1
            successList.add(false)
        }

        response.errorCode=0
        response.errorMessage="success"


        var counter=1
        for(success in successList){
            if(!success){
                response.errorCode=-1
                response.errorMessage="Error In Key $counter"
            }
            counter++
        }




        return response

    }


    fun saveKeyValue(dto: VaultDtos.SecretValue): VaultDtos.SecretValueResponse {
        checkLogin()
        watcher.debug("saveKeyValue=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
 
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveKeyValue(dto) 
        } 
        //   logger.debug(" saveKey authToken : "+authToken+"")
       watcher.debug("\n")
        val newDto= VaultDtos.SecretValueMetaRequest()
        newDto.data.value=dto.data
        val response= VaultDtos.SecretValueResponse()
        var uri="${dto.hostType.vaultName}/data/${dto.keyId}"
        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.keyId+vaultVersion}"
            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.keyId+vaultVersion}"
            }
        }
        val url="v1/$uri"
       watcher.debug("URL_VAULT : $url")
        val resapi=doApiRequest(url,"POST",newDto,
            VaultDtos.SecretValueMetaRequest::class.java)
        if(resapi.data!=null){
           watcher.debug(" SAVE SUCCESS: "+ GsonBuilder().create().toJson(newDto) )
            response.errorCode=0
            response.data=dto.data
        }else   if(resapi.error!=null){
           watcher.debug(" SAVE ERROR: "+ GsonBuilder().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage=resapi.error
            response.errorCode=-1
            return response
        }
        response.errorCode=0
        response.errorMessage="SUCCESS"
        return response
    }

    fun saveClientCredentials(dto: VaultDtos.ClientCredData):  VaultDtos.ClientCredData {
        checkLogin() // Asegúrate de que el usuario está autenticado.
        watcher.debug("saveClientCredentials=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType = StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]")
        
        if (AppSettings.getInstance().isDocumentVault) {
            return VaultDocumentManager.saveClientCredentials(dto)
        }
        VaultDocumentManager.saveCredentialMetadata(dto)
         
        watcher.debug("saveClientCredentials")
        val newDto = VaultDtos.ClientCredentialsMetaRequest() 
        newDto.data = dto
        var response =  VaultDtos. ClientCredData() 
        val uri = "vierge/data/{dto.packageName}_CLIENT_CREDENTIALS"
        val url = if (isVaultConsole && additionalUri.isNotEmpty()) {
            "$additionalUri/$uri"
        } else { 
            "v1/$uri"
        }
    
        watcher.debug("URL_VAULT : $url")
    
        val resapi = doApiRequest(url, "POST", newDto, VaultDtos.ClientCredentialsMetaRequest::class.java)
        return if (resapi.data != null) {
            watcher.debug("SAVE SUCCESS: " + GsonBuilder().create().toJson(newDto))
            dto.apply {
                errorMessage = "SUCCESS"
                errorCode = 0
            }
        } else {
            watcher.debug("SAVE ERROR: " + GsonBuilder().create().toJson(resapi.error))
            dto.apply {
                errorMessage = resapi.error ?: "Unknown error"
                errorCode = ErrorCode.VaultError.errorCodeInt
            }
        }
    }

 
    fun readClientCredentials(dto: VaultDtos.RequestReadClientCredData): VaultDtos.ClientCredData {
        checkLogin() // Asegúrate de que el usuario está autenticado.
        watcher.debug("readClientCredentials=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType = StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]")
    
        if (AppSettings.getInstance().isDocumentVault) {
            return VaultDocumentManager.readClientCredentials(dto)
        }
      
        watcher.debug("readClientCredentials")
        val response = VaultDtos.ClientCredData()
        val uri = "vierge/data/{dto.packageName}_CLIENT_CREDENTIALS"
        val url = if (isVaultConsole && additionalUri.isNotEmpty()) {
            "$additionalUri/$uri"
        } else {
            "v1/$uri"
        }
    
        watcher.debug("URL_VAULT : $url")
    
        val resapi = doApiRequest(url, "GET", "", VaultDtos.ClientCredentialsMetaResponse::class.java)
        return if (resapi.data != null) {
            watcher.debug("READ SUCCESS: " + GsonBuilder().setPrettyPrinting().create().toJson(resapi.data))
            resapi.data.data.apply {
                errorCode = 0 
            }
        } else {
            watcher.debug("READ ERROR: " + GsonBuilder().setPrettyPrinting().create().toJson(resapi.error))
            response.apply {
                errorMessage = resapi.error ?: "Unknown error"
                errorCode = if (resapi.error?.contains("404") == true) {
                    ErrorCode.CredentialsNotFound.errorCodeInt
                } else {
                    ErrorCode.VaultError.errorCodeInt
                }
            }
        }
    }
    
    

    fun saveRsaKey(dto: BankRsaKey): BankRsaKey {
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        checkLogin()
        watcher.debug("saveRsaKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType=StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]")
		
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveRsaKey(dto)
        }
       watcher.debug("saveRsaKey")
        val newDto= VaultDtos.RsaValueMetaRequest()
        newDto.data=dto
        var response= BankRsaKey()
        var uri="${dto.hostType.vaultName}/data/${dto.id+vaultType+vaultVersion}"

      
        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.id+vaultType+vaultVersion}"

            }else{ 
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.id+vaultType+vaultVersion}"
            }
        }

        val url="v1/$uri"
       watcher.debug("URL_VAULT : $url")


        val resapi=doApiRequest(url,"POST",newDto,
            VaultDtos.RsaValueMetaRequest::class.java)
        if(resapi.data!=null){
           watcher.debug(" SAVE SUCCESS: "+ GsonBuilder().create().toJson(newDto) )
            response=dto

        }else if(resapi.error!=null){
           watcher.debug(" SAVE ERROR: "+ GsonBuilder().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage=resapi.error!!
            response.errorCode=-1
            return response
        }
        response.errorMessage="SUCCESS"
        response.errorCode=0
        return response
    }

    
    fun readRsaKey(dto: VaultDtos.RequestReadRSAData): BankRsaKey {
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        checkLogin()
        watcher.debug("readRsaKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
         val vaultType=StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]") 
		
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readRsaKey(dto)
        }

       watcher.debug("\n")
        var response= BankRsaKey() 
        response.id=dto.id
        response.hostType=dto.hostType
        var uri="${dto.hostType.vaultName}/data/${dto.id+vaultType+vaultVersion}"
    
        if(isVaultConsole){
 
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.id+vaultType+vaultVersion}"


            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.id+vaultType+vaultVersion}"

            }
        }
        val url="v1/$uri"
       watcher.debug(" readRsaKey \"${dto.id}\" url: "+url )
        val resapi=doApiRequest(url,"GET", "",
            VaultDtos.RsaValueMetaResponse::class.java)
        if(resapi.data!=null){
           watcher.debug(" readRsaKey SUCCESS: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorCode=0
            response=resapi.data.data.data
        }else  if(resapi.error!=null) {
           watcher.debug(" readRsaKey ERROR: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage=resapi.error
            response.errorCode=-1
            if(resapi.error.contains("404")){
                response.errorCode= ErrorCode.RSADOesntExists.errorCodeInt

            }
        }
        response.errorCode=0
        return response
    }

    fun readDbKey(dto: DBKeyRequestDto):  DBKeyResponseDto{
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
 
        checkLogin()
        watcher.debug("readDbKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}") 
        
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readDbKey(dto)
        }

       watcher.debug("\n")
        val response= DBKeyResponseDto()
        var uri="${dto.hostType.vaultName}/data/${dto.terminalSerialNumber+"_DB"}"

        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.terminalSerialNumber+"_DB"+vaultVersion}"

            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.terminalSerialNumber+"_DB"+vaultVersion}"
            }
        }

        val url="v1/$uri"
       watcher.debug(" readDbKey \"${dto.terminalSerialNumber}\" url: "+url )
        val resapi=doApiRequest(url,"GET", "",
            VaultDtos.DBKeyMetaResponse::class.java)
        if(resapi.data!=null){
           watcher.debug(" readDbKey SUCCESS: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorCode=0
            val dtoResponse=resapi.data.data.data
            if(dtoResponse.dbKey!=""){
                response.errorCode=0
                response.dbKey=dtoResponse.dbKey
            }else{
                response.errorCode=ErrorCode.KeyIdDoesntExist.errorCodeInt
                response.errorMessage=ErrorCode.KeyIdDoesntExist.errorCodeMessage
                return response
            }
            response.errorCode=0
            response.hostType=dto.hostType
            response.keySerialNumber=dto.keySerialNumber

            response.errorMessage="SUCCESS READED IN $uri"
            //  response.errorCode=0
           watcher.debug("response.errorCode ${response.errorCode}")

        }else  if(resapi.error!=null) {
           watcher.debug(" readDbKey ERROR: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage=resapi.error!!
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            if(resapi.error.contains("404")){
                response.errorCode= ErrorCode.KeyIdDoesntExist.errorCodeInt
                response.errorMessage= ErrorCode.KeyIdDoesntExist.errorCodeMessage+" IN "+uri+"| "+resapi.error


            }
        }
        return response
    }

    fun saveDbKey(dto: DBKeyRequestDto): DBKeyResponseDto {
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        checkLogin()
        watcher.debug("saveDbKey=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")

        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveDbKey(dto)
        }

        val response= DBKeyResponseDto()
        // logger.debug(" saveKey authToken : "+authToken+"")
       watcher.debug("\n")
        val newDto= VaultDtos.DBKeyMetaRequest()
        response.dbKey=dto.dbKey
        response.hostType=dto.hostType
        response.keySerialNumber=dto.keySerialNumber
        response.kcv=dto.kcv


        if(response.dbKey.isEmpty()){
            response.errorMessage= "DB KEY IS EMPTY"
            response.errorCode= ErrorCode.VaultError.errorCodeInt
            return response
        }
        newDto.data=response
        var uri="${dto.hostType.vaultName}/data/${dto.terminalSerialNumber+"_DB"}"
        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.terminalSerialNumber+"_DB"+vaultVersion}"

            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.terminalSerialNumber+"_DB"+vaultVersion}"
            }
        }
        val url="v1/$uri"
       watcher.debug("URL_VAULT : $url")
        val resapi=doApiRequest(url,"POST",newDto,
            VaultDtos.DBKeyMetaRequest::class.java)
        response.errorCode=0
        response.errorMessage="SUCCESS SAVED IN $uri"
        if(resapi.data!=null){
           watcher.debug(" saveDbKey SUCCESS: "+ GsonBuilder().create().toJson(newDto) )
            response.errorCode=0
            response.errorMessage="SUCCESS"
        }else if(resapi.error!=null){
           watcher.debug(" SAVE  ERROR: "+ GsonBuilder().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage= ErrorCode.VaultError.errorCodeMessage+" "+resapi.error
            response.errorCode= ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }

    fun readDbKeyFull(dto: DBKeyRequestDto): DBKeyResponseDto {
        watcher.debug("readDbKeyFull=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.readDbKey(dto)
        }
 
        var response=DBKeyResponseDto()
        try{
            response=readDbKey(dto)
            if(response.kcv.equals("")){
                response.kcv= StringUtils.encodeHexString(RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(response.dbKey)))
            }
        }catch (e:Exception){
            response.errorCode=ErrorCode.KeyIdDoesntExist.errorCodeInt
        }
        if(response.errorCode!=0){
            response.dbKey=StringUtils.generateRandomHexadecimal(32)
            dto.dbKey=response.dbKey
            dto.kcv= StringUtils.encodeHexString(RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(dto.dbKey)))
            response= saveDbKey(dto)
        }

       // response.errorMessage= ErrorCode.VaultError.errorCodeMessage
       // response.errorCode= ErrorCode.VaultError.errorCodeInt

        return response

    }



    fun saveDukptData(dto: DukptData): VaultDtos.DukptMetaResponseMin {
        checkLogin() 
        watcher.debug("saveDukptData=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType=StringUtils.getVaultType(dto.hostType)
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        watcher.debug("vaultType [${vaultType}]") 
        if(dto.tk!=""){ 
            try{  
                watcher.debug("saveDukptData_tk [${dto.tk}]")
              val checkValue = RSAUtils.TDesCipher(ByteArray(8),StringUtils.hexStringToByteArray( dto.tk)) 
              if(dto.hostType.authorizer==Authorizer.OPENPAY){
                val checkValueEW =StringUtils.encodeHexString(checkValue).substring(0,8)  //RDM_KCV  
                dto.kcv=checkValueEW
              }else{
                val checkValueEW =StringUtils.encodeHexString(checkValue).substring(0,6)  //RDM_KCV  
                dto.kcv=checkValueEW 
              }
             
            }catch(ex:Exception){
                watcher.error("saveDukptData_exception [${ex.message}]")
            } 
        }
        if(AppSettings.getInstance().isDocumentVault){
            return VaultDocumentManager.saveDukptData(dto)
        } 
        val response= VaultDtos.DukptMetaResponseMin()
        // logger.debug(" saveKey authToken : "+authToken+"")
       watcher.debug("\n")
        dto.clearCipherBlocks= mutableListOf()
        dto.chipherDataBlocks= mutableListOf()
        val newDto= VaultDtos.DukptMetaRequest()
        watcher.debug("dto.ipek is empty =>[${dto.ipek!=""}]")

        if(dto.ipek!=""){
            watcher.debug("dto.ipek is empty =>[${dto.ipek!=""}]")
            val responseDukpt= MyHsmManager().encryptWithDUKPT(dto.ipek,dto.ksn)
            if(responseDukpt.errorCode==0){
                dto.ksnEncrypted=dto.ksn
                dto.ipekEncrypted=responseDukpt.data
                watcher.debug(" dto.ipekEncrypted ${dto.ipekEncrypted} ")
                dto.ipek=""
                dto.bdk=""
                response.ipekEncrypted=dto.ipekEncrypted
            }else{
                dto.errorCode= ErrorCode.HSMError.errorCodeInt
                dto.errorMessageHSM=responseDukpt.errorCode.toString()+" "+responseDukpt.errorMessage
                response.errorMessage="HSM ERROR: "+ dto.errorMessageHSM
                response.errorCode=ErrorCode.HSMError.errorCodeInt
                watcher.error("HSM ERROR: "+ dto.errorMessageHSM)
                return response
            }
        }

        newDto.data=dto
        var uri="${dto.hostType.vaultName}/data/${dto.terminalSerialNumber+vaultType+dto.keyId}"
        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.terminalSerialNumber+vaultType+dto.keyId+vaultVersion}"

            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.terminalSerialNumber+vaultType+dto.keyId+vaultVersion}"

            }
        }

        val url="v1/$uri"
       watcher.debug("URL_VAULT : $url")
        val resapi=doApiRequest(url,"POST",newDto,
            VaultDtos.DukptMetaRequest::class.java)
        response.errorCode=0
        response.errorMessage="SUCCESS SAVED IN $uri"
        if(resapi.data!=null){
           watcher.debug(" saveDukptData SUCCESS: "+ GsonBuilder().create().toJson(newDto) )
            response.errorCode=0
            response.errorMessage="SUCCESS"
        }else if(resapi.error!=null){
           watcher.debug(" SAVE ERROR: "+ GsonBuilder().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage= ErrorCode.VaultError.errorCodeMessage+" "+resapi.error
            response.errorCode= ErrorCode.VaultError.errorCodeInt
            return response
        }
        return response
    }





    fun readDukptData(dto: VaultDtos.RequestReadDukptData): DukptData {
        checkLogin()
        watcher.debug("readDukptData=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        val vaultType=StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        if(AppSettings.getInstance().isDocumentVault){
            val response= VaultDocumentManager.readDukptData(dto)
            watcher.debug("readDukptData=>result ${response.errorCode==0}")
             watcher.debugObj(response)

            return response
        }  

       watcher.debug("\n")
        var response= DukptData()
        response.terminalSerialNumber=dto.terminalSerialNumber
        response.keyId=dto.keyId
        response.hostType=dto.hostType
        var uri="${dto.hostType.vaultName}/data/${dto.terminalSerialNumber+vaultType+dto.keyId}"

        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.terminalSerialNumber+vaultType+vaultVersion+dto.keyId}"

            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.terminalSerialNumber+vaultType+vaultVersion+dto.keyId}"

            }
        }

        val url="v1/$uri" 
        watcher.debug(" readDukptData  uri: "+uri )
        watcher.debug(" readDukptData  url: "+url )
       watcher.debug(" readDukptData \"${dto.terminalSerialNumber+vaultType+dto.keyId}\" url: "+url )
        val resapi=doApiRequest(url,"GET", "",
            VaultDtos.DukptMetaResponse::class.java)
        if(resapi.data!=null){
           watcher.debug(" readKey SUCCESS: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorCode=0
            response=resapi.data.data.data
            if(response.ipekEncrypted!=""&&response.ksnEncrypted!=""){
                val responseDukpt= MyHsmManager().decryptWithDUKPT(response.ipekEncrypted,response.ksnEncrypted)
                if(responseDukpt.errorCode==0){
                    response.ipekDecrypted=responseDukpt.data
                   watcher.debug("readDukptData IPEK DECRYPT SUCCESS: ${response.ipekDecrypted}")
                    response.ipek=response.ipekDecrypted
                    // dto.bdk=""
                }else{
                    response.errorCode=ErrorCode.VaultError.errorCodeInt
                    response.errorMessageHSM=responseDukpt.errorCode.toString()+" "+responseDukpt.errorMessage+" ipek empty ${response.ipekEncrypted.isEmpty()} ipek empty ${response.ksnEncrypted.isEmpty()}"
                    return response
                }
            }
            response.errorCode=0
            response.errorMessage="SUCCESS SAVED IN $uri"
          //  response.errorCode=0
           watcher.debug("response.errorCode ${response.errorCode}")

        }else  if(resapi.error!=null) {
           watcher.debug(" readKey ERROR: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage=resapi.error!!
            response.errorCode=ErrorCode.VaultError.errorCodeInt
            if(resapi.error.contains("404")){
                response.errorCode= ErrorCode.KeyLoadIsNecesary.errorCodeInt
            }
        }
        return response
    }



    fun readKeyValue( dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        checkLogin() 
        watcher.debug("readKeyValue=>isDocumentVault ${AppSettings.getInstance().isDocumentVault}")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        if(AppSettings.getInstance().isDocumentVault){ 
            return VaultDocumentManager.readKeyValue(dto)
        }
       watcher.debug("\n")
        val response= VaultDtos.SecretValueResponse()
        response.keyId=dto.keyId
        response.hostType=dto.hostType

        var uri="${dto.hostType.vaultName}/data/${dto.keyId}"
        if(isVaultConsole){
            if(additionalUri.equals("")){
                uri="${dto.hostType.vaultName}/${dto.keyId+vaultVersion}"


            }else{
                uri="${additionalUri}/${dto.hostType.vaultName}/${dto.keyId+vaultVersion}"

            }
        }


        val url="v1/$uri"
       watcher.debug(" readKey url: "+url )

        val resapi=doApiRequest(url,"GET", "",
            VaultDtos.SecretValueMetaResponse::class.java)
        if(resapi.data!=null){
           watcher.debug(" readKey SUCCESS: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorCode=0

            response.data=resapi.data.data.data.value
        }else  if(resapi.error!=null) {
           watcher.debug(" readKey ERROR: "+ GsonBuilder().setPrettyPrinting().create().toJson(resapi.data) )
           watcher.debug("\n")
            response.errorMessage="${dto.keyId} "+ErrorCode.KeyIdDoesntExist.errorCodeMessage+" "+resapi.error!!
            response.errorCode=ErrorCode.KeyIdDoesntExist.errorCodeInt
            if(resapi.error.contains("404")){
                response.errorCode= ErrorCode.KeyIdDoesntExist.errorCodeInt

            }
        }
        return response

    }

 

    fun <T> doApiRequest(endpoint: String, methodType: String, data: Any, responseType: Class<T>): Response<T> {
        val startTime = System.currentTimeMillis()  // Inicio del tiempo
    

        val url = URL("$baseUrl/$endpoint")
        val protocolHttpps = (baseUrl.startsWith("https"))

  
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })
       watcher.debug("protocolHttpps : ${protocolHttpps}")
       watcher.debug("URL_rEQUEST : $baseUrl/$endpoint")
       watcher.debug("url  : ${url}") 
        
        if(protocolHttpps){
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            val connection = url.openConnection() as HttpsURLConnection
            connection.sslSocketFactory = sslContext.socketFactory
            connection.hostnameVerifier = HostnameVerifier { _, _ -> true } // Ignorar la verificación del nombre del host
            connection.requestMethod = methodType
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $authToken")


            connection.doOutput = true


            return try {
                if(methodType!="GET") {
                    val jsonInputString = gson.toJson(data)
                    val wr = OutputStreamWriter(connection.outputStream)
                    wr.write(jsonInputString)
                    wr.flush()
                   watcher.debug("JSON REQUEST: ${maskString(jsonInputString)}")
                   watcher.debug("\n")
                }


                val responseCode = connection.responseCode
            
                val response: T = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonstr=reader.readLine()
                   watcher.debug("JSON RESPONSE: ${maskString(jsonstr)}")
                   watcher.debug("\n")
                    gson.fromJson(jsonstr, responseType)
                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                    return Response(
                        data = null,
                        error = null
                    )
                } else {
                    throw Exception("Error al obtener la respuesta. Código de estado: $responseCode")
                }
                val endTime = System.currentTimeMillis()  // Fin del tiempo
                val duration = endTime - startTime        // Duración en milisegundos
                watcher.debug("Tiempo total de la solicitud: ${duration} ms")
                Response(data = response, error = null)
            } catch (e: Exception) {
                Response(
                    data = null,
                    error = "Error al realizar la solicitud: ${e.message}"
                )
            } finally {
                connection.disconnect()
            }
        }else{


            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = methodType
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $authToken")
            connection.doOutput = true

            return try {
                if(methodType!="GET") {
                    val jsonInputString = gson.toJson(data)
                    val wr = OutputStreamWriter(connection.outputStream)
                    wr.write(jsonInputString)
                    wr.flush()
                   watcher.debug("JSON REQUEST: ${maskString(jsonInputString)}")
                   watcher.debug("\n")
                }

 
                val responseCode = connection.responseCode

                val response: T = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonstr=reader.readLine()
                   watcher.debug("JSON RESPONSE: ${maskString(jsonstr)}")
                   watcher.debug("\n")
                    gson.fromJson(jsonstr, responseType)
                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                    return Response(
                        data = null,
                        error = null
                    )
                } else {
                    throw Exception("Error al obtener la respuesta. Código de estado: $responseCode")
                }
                val endTime = System.currentTimeMillis()  // Fin del tiempo
                val duration = endTime - startTime        // Duración en milisegundos
         
                watcher.debug("Tiempo total de la solicitud: ${duration} ms")
       
                Response(data = response, error = null)
            } catch (e: Exception) {
                Response(
                    data = null,
                    error = "Error al realizar la solicitud: ${e.message}"
                )
            } finally {
                connection.disconnect()
            }

        }

    }
}

