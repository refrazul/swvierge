package com.viergegroup.crypto.pandora.managers

import com.google.gson.GsonBuilder
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.BankRsaKey
import com.viergegroup.crypto.security.DukptData
import com.viergegroup.crypto.security.MyHsmManager
import com.viergegroup.crypto.security.RSAUtils
import com.viergegroup.swmain.dtos.pandora.DBKeyRequestDto
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import  com.viergegroup.crypto.pandora.managers.VaultDtos.ClientCredData
import  com.viergegroup.crypto.pandora.managers.VaultDtos.CredentialMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.security.MessageDigest

import java.util.Date
import java.util.concurrent.TimeUnit

import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.Watcher
import java.io.File
import java.io.FileReader
import java.io.FileWriter   
import java.io.IOException
import java.io.FileOutputStream
import  com.viergegroup.swmain.entities.RequestEntity
import java.io.FileInputStream
import com.shared.commons.StringUtils.maskString


object VaultDocumentManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    var vaultVersion = ""
 
    init {   
        vaultVersion = "_" + AppSettings.getInstance().vVersion
    } 
 

    fun generateOrIncrementKsn(dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        val watcher = WatcherContext.getInstance()
        val response = VaultDtos.SecretValueResponse()
        watcher.debug("Procesando KSN para: ${dto.keyId}")
       
        dto.keyId="KSN_"+dto.terminalSerialNumber
        val existing = readKeyValue(dto)
        val newKsn = if (existing.errorCode == 0 && !existing.data.isNullOrEmpty()) {
            watcher.debug("KSN existente encontrado: ${existing.data}")
            incrementKsn(existing.data!!)
        } else {
            watcher.debug("No se encontró KSN, generando nuevo desde serial")
            createInitialKsnFromSerial(dto.keyId)
        }
       val saveDto = VaultDtos.SecretValue().apply {
            keyId = dto.keyId
            data = newKsn
            hostType = dto.hostType
        }
 
        val saveResult = saveKeyValue(saveDto)

        return VaultDtos.SecretValueResponse().apply {
            keyId = dto.keyId
            hostType = dto.hostType
            data = newKsn
            errorCode = saveResult.errorCode 
            errorMessage = saveResult.errorMessage
        }
    }

    private fun createInitialKsnFromSerial(serial: String): String {
        val watcher = WatcherContext.getInstance()
        watcher.debug("Generando KSN con KSID fijo para serial: $serial")

        // === Parte fija del KSID (proporcionada) ===
        val ksid = AppSettings.getInstance().evoKsid //"08D02" 
        watcher.debug("evoKsid: $ksid") 
        
        // === Derivar Device ID (hash del serial) ===
        val deviceId = MessageDigest.getInstance("SHA-256")
            .digest(serial.toByteArray())
            .joinToString("") { "%02X".format(it) }
            .take(9)  // 9 caracteres hexadecimales (~4.5 bytes)

        // === Contador inicial ===
        val counter = "000001"

        // === Construcción final ===
        val ksn = (ksid + deviceId + counter).uppercase()

        watcher.debug("KSN generado (KSID+DeviceID+Counter): $ksn")

        return ksn
    }

    /*  private fun incrementKsn(currentKsn: String): String {
        if (currentKsn.length != 20)
            throw IllegalArgumentException("KSN debe tener longitud de 20 caracteres")

        val base = currentKsn.take(14)
        val counterHex = currentKsn.takeLast(6)
        val counterInt = counterHex.toInt(16)
        val newCounter = (counterInt + 1).coerceAtMost(0xFFFFFF) 

        return base + "%06X".format(newCounter)
    }
*/

    private fun incrementKsn(currentKsn: String): String {
        if (currentKsn.length != 20)
            throw IllegalArgumentException("KSN debe tener longitud de 20 caracteres")

        val base = currentKsn.take(14)
        val counterPart = currentKsn.takeLast(6)

        // Intentar parsear como decimal primero
        val counterInt = try {
            counterPart.toInt()
        } catch (e: NumberFormatException) {
            // Si falla, significa que venía en hexa → lo convertimos
            counterPart.toInt(16)
        }

        // Incrementamos, límite 999999 (6 dígitos decimales)
        val newCounter = (counterInt + 1).coerceAtMost(999999)

        // Formato en DECIMAL con 6 dígitos, rellenando con ceros a la izquierda
        return base + "%06d".format(newCounter)
    }


    fun readKeyValue(dto: VaultDtos.ReadKeyRequest): VaultDtos.SecretValueResponse {
        val watcher = WatcherContext.getInstance()
        watcher.debug("\n")
        val response = VaultDtos.SecretValueResponse()
        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.keyId+ vaultVersion}"
        return readFromFile(documentPath,dto, VaultDtos.SecretValueResponse::class.java,watcher)?.apply {
            keyId = dto.keyId
            errorCode=0
            hostType = dto.hostType
        } ?: response.apply {
            errorCode = ErrorCode.VaultError.errorCodeInt
            errorMessage = "File not found [$documentPath] "
        } 
    }
    fun saveKeyValue(dto: VaultDtos.SecretValue): VaultDtos.SecretValueResponse {
        val watcher = WatcherContext.getInstance()
        val vaultType = StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [$vaultType]")
        
        val response = VaultDtos.SecretValueResponse()
        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.keyId + vaultVersion}"
        watcher.debug("documentPath [$documentPath]")

        return try { 
            saveToFile(documentPath, dto, watcher)
            response.apply {
                errorCode = 0
                errorMessage = "SUCCESS"
            }
        } catch (ex: IOException) { 
            response.apply {
                errorCode = ErrorCode.VaultError.errorCodeInt
                errorMessage = "Failed to save key value: ${ex.message}"
            }
        }
    }

    fun saveRsaKey(dto: BankRsaKey): BankRsaKey {
        val watcher = WatcherContext.getInstance()
        watcher.debug("saveRsaKey [${dto.hostType}]") 
        val vaultType = StringUtils.getVaultType(dto.hostType)
        
        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.id + vaultType + vaultVersion}"
        watcher.debug("documentPath [$documentPath]") 

        return try {
            saveToFile(documentPath, dto, watcher)
            dto.apply {
                errorMessage = "SUCCESS"
                errorCode = 0
            }
        } catch (ex: IOException) {
            dto.apply { 
                errorMessage = "Failed to save RSA key: ${ex.message}"
                errorCode = ErrorCode.VaultError.errorCodeInt
            }
        }
    } 


    fun saveCredentialMetadata(dto: ClientCredData): CredentialMetadata? {
        val metadataFilePath = "${AppSettings.getInstance().vaultDir}/credential_metadata.json"
        val gson = Gson()
    
        // Leer el archivo JSON existente o inicializar un nuevo ArrayList.
        val metadataList: MutableList<CredentialMetadata> = try {
            val file = File(metadataFilePath)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<MutableList<CredentialMetadata>>() {}.type
                gson.fromJson(json, type)
            } else {
                mutableListOf()
            }
        } catch (e: IOException) {
            mutableListOf()
        }
    
        // Verificar si el `hostType` ya existe y actualizar la fecha de expiración.
        val updatedMetadata: CredentialMetadata = metadataList.find { it.hostType == dto.hostType }?.apply {
            expirationDateTime = dto.expirationDateTime
        } ?: CredentialMetadata(dto.hostType, dto.expirationDateTime).also {
            metadataList.add(it)
        }
    
        // Guardar la lista actualizada en el archivo JSON.
        try {
            File(metadataFilePath).writeText(gson.toJson(metadataList))
        } catch (e: IOException) {
            dto.apply {
                errorMessage = "Failed to save credential metadata: ${e.message}"
                errorCode = ErrorCode.VaultError.errorCodeInt
            }
            return null // Retornar nulo si ocurre un error al guardar el archivo.
        }
    
        // Guardar las credenciales completas en el Vault.
      //  saveClientCredentials(dto)
    
        // Retornar el metadata actualizado.
        return updatedMetadata
    }
    
    
    fun getExpiringCredentials(daysThreshold: Int): List<CredentialMetadata> {
        val metadataFilePath = "${AppSettings.getInstance().vaultDir}/credential_metadata.json"
        val gson = Gson()
        val currentDate = Date()
     
        // Leer el archivo JSON.
        val metadataList: List<CredentialMetadata> = try {
            val file = File(metadataFilePath)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<CredentialMetadata>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        }
    
        // Filtrar credenciales próximas a expirar.
        return metadataList.filter { 
            val diff = it.expirationDateTime.time - currentDate.time
            TimeUnit.MILLISECONDS.toDays(diff) <= daysThreshold
        }
    }


    fun saveClientCredentials(dto: ClientCredData): ClientCredData {
            saveCredentialMetadata(dto)
            val watcher = WatcherContext.getInstance()
            watcher.debug("saveRsaKey [${dto.hostType}]") 
            val vaultType = StringUtils.getVaultType(dto.hostType)
            
            val documentPath = "${AppSettings.getInstance().vaultDir}/vierge/{dto.packageName}_CLIENT_CREDENTIALS"
            watcher.debug("documentPath [$documentPath]") 
            WatcherGlobalLog.debug("documentPath [$documentPath]") 

            return try {
                saveToFile(documentPath, dto, watcher)
                dto.apply {
                    errorMessage = "SUCCESS"
                    errorCode = 0
                } 
            } catch (ex: IOException) {
                dto.apply { 
                    errorMessage = "Failed to save RSA key: ${ex.message}"
                    errorCode = ErrorCode.VaultError.errorCodeInt
                }
            }
        }  

    fun readClientCredentials(dto: VaultDtos.RequestReadClientCredData): ClientCredData {
        val watcher = WatcherContext.getInstance()
        watcher.debug("\n")
        val response = ClientCredData()
        val vaultType = StringUtils.getVaultType(dto.hostType)
         
        val documentPath = "${AppSettings.getInstance().vaultDir}/vierge/{dto.packageName}_CLIENT_CREDENTIALS"
        watcher.debug("documentPath [$documentPath]") 
        WatcherGlobalLog.debug("documentPath [$documentPath]")  

        return readFromFile(documentPath, dto, ClientCredData::class.java, watcher)?.apply {
            errorCode = 0 
            hostType = dto.hostType 
        } ?: response.apply { 
            errorCode = ErrorCode.VaultError.errorCodeInt
            errorMessage = "File not found [$documentPath] "
        }
    }


    fun readRsaKey(dto: VaultDtos.RequestReadRSAData): BankRsaKey {
        val watcher = WatcherContext.getInstance()
        watcher.debug("\n")
        val response = BankRsaKey()
        val vaultType = StringUtils.getVaultType(dto.hostType)
        
        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.id + vaultType + vaultVersion}"
        watcher.debug("documentPath [$documentPath]") 
        
        return readFromFile(documentPath, dto, BankRsaKey::class.java, watcher)?.apply {
            id = dto.id
            errorCode = 0
            hostType = dto.hostType
        } ?: response.apply { 
            errorCode = ErrorCode.VaultError.errorCodeInt
            errorMessage = "File not found [$documentPath] "
        }
    }





    fun readDbKey(dto: DBKeyRequestDto): DBKeyResponseDto {
        val watcher = WatcherContext.getInstance()
        watcher.debug("\n")
    
        // Convertir el número de serie de terminal a mayúsculas
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()
    
        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.terminalSerialNumber}_DB"
        watcher.debug("readdbkeydebug_documentPath $documentPath")
    
        // Intentar leer el archivo
        var result = readFromFile(documentPath, dto, DBKeyResponseDto::class.java, watcher)
    
        if (result != null) {
            // Si el archivo existe pero `dbKey` está vacío, generar nueva clave y KCV
            if (result.dbKey.trim().isEmpty()) {
                val newDbKey = StringUtils.generateRandomHexadecimal(32)
                val newKcv = StringUtils.encodeHexString(RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(newDbKey)))
                result.dbKey = newDbKey
                result.kcv = newKcv
                watcher.debug("EXISTS_readdbkeydebug_dbKey ${result.dbKey}")
                watcher.debug("EXISTS_readdbkeydebug_kcv ${result.kcv}")
                result.errorCode = 0
                result.errorMessage = "FILE_FOUND => $documentPath AND GENERATED_DB_KEY"
    
                // Guardar nuevo `dbKey` en el dto y persistir
                dto.dbKey = newDbKey
                dto.kcv = newKcv
                saveDbKey(dto)
    
                return result
            }
    
            // Retornar el resultado si ya existe y tiene `dbKey`
            result.apply {
                errorCode = 0
                errorMessage = "FILE_FOUND => $documentPath"
            }
            return result 
        } else {
            // El archivo no existe, se genera un nuevo `DBKeyResponseDto`
            watcher.debug("readdbkeydebug_GEN_NEW_KEY")
    
            // Generar nuevo dbKey y kcv
            val newDbKey = StringUtils.generateRandomHexadecimal(32)
            val newKcv = StringUtils.encodeHexString(RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArray(newDbKey)))
    
            // Crear una nueva instancia de DBKeyResponseDto
            result = DBKeyResponseDto().apply {
                dbKey = newDbKey
                kcv = newKcv
                errorCode = 0
                errorMessage = "FILE_NOT_FOUND => $documentPath, NEW_KEY_GENERATED"
            }
    
            watcher.debug("readdbkeydebug_dbKey ${maskString(result.dbKey)}")
            watcher.debug("readdbkeydebug_kcv ${result.kcv}")
    
            // Guardar el nuevo dbKey en el archivo
            dto.dbKey = newDbKey
            dto.kcv = newKcv
            saveDbKey(dto)
    
            return result
        }
    }
    
    

    fun saveDbKey(dto: DBKeyRequestDto): DBKeyResponseDto {
        val watcher = WatcherContext.getInstance()
        val response = DBKeyResponseDto()
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        watcher.debug("\n")
        if (dto.dbKey.isEmpty()) {
            response.errorMessage = "DB KEY IS EMPTY"
            response.errorCode = ErrorCode.VaultError.errorCodeInt
            return response
        }

        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.terminalSerialNumber}_DB"
        val file = File(documentPath)

        if (file.exists()) {
            return response.apply {
                errorCode = 0
                errorMessage = "FILE EXISTS:  $documentPath"
            }
        }

        return try {
            saveToFile(documentPath, dto, watcher)
            response.apply {
                errorCode = 0
                dbKey = dto.dbKey
                hostType = dto.hostType
                keySerialNumber = dto.keySerialNumber
                kcv = dto.kcv
                errorMessage = "SUCCESS"
            }
        } catch (ex: IOException) {
            response.apply {
                errorCode = ErrorCode.VaultError.errorCodeInt
                errorMessage = "Failed to save DB key: ${ex.message}"
            }
        }
    }

    fun saveDukptData(dto: DukptData): VaultDtos.DukptMetaResponseMin {
        val watcher = WatcherContext.getInstance() 
        watcher.debug("\n")
        val response = VaultDtos.DukptMetaResponseMin()
        val vaultType = StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [$vaultType]")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.terminalSerialNumber + vaultType + dto.keyId}"
        watcher.debug("saveDukptData documentPath $documentPath")
 
        if (dto.ipek.isNotEmpty()) { 
            val responseDukpt = MyHsmManager().encryptWithDUKPT(dto.ipek, dto.ksn)
            if (responseDukpt.errorCode == 0) {
                dto.apply {
                    ksnEncrypted = ksn
                    ipekEncrypted = responseDukpt.data
                    ipek = ""
                    bdk = "" 
                }
                response.ipekEncrypted = dto.ipekEncrypted
            } else {
                dto.errorCode = ErrorCode.HSMError.errorCodeInt
                dto.errorMessageHSM = "${responseDukpt.errorCode} ${responseDukpt.errorMessage}"
                response.errorMessage = "HSM ERROR: ${dto.errorMessageHSM}"
                response.errorCode = ErrorCode.HSMError.errorCodeInt
                return response
            }
        } 

        return try {
            saveToFile(documentPath, dto, watcher)
            watcher.debug("saveDukptData documentPath SAVED $documentPath")
            response.apply {
                errorCode = 0
                errorMessage = "SUCCESS"
            }
        } catch (ex: IOException) {
            response.apply {
                errorCode = ErrorCode.VaultError.errorCodeInt
                errorMessage = "Failed to save DUKPT data: ${ex.message}"
            }
        }
    }
    fun readDukptData(dto: VaultDtos.RequestReadDukptData): DukptData {
        val watcher = WatcherContext.getInstance()
        watcher.debug("\n")
        var response = DukptData()
        val vaultType=StringUtils.getVaultType(dto.hostType)
        watcher.debug("vaultType [${vaultType}]")
        dto.terminalSerialNumber = dto.terminalSerialNumber.uppercase()

        val documentPath = "${AppSettings.getInstance().vaultDir}/${dto.hostType.vaultName}/${dto.terminalSerialNumber +vaultType+ dto.keyId}"
        watcher.debug("readDukptData documentPath $documentPath")
 
         response= readFromFile(documentPath,dto, DukptData::class.java,watcher)?.apply { 
            errorCode=0
            terminalSerialNumber = dto.terminalSerialNumber
            keyId = dto.keyId
            hostType = dto.hostType
        } ?: response.apply {
            errorCode = ErrorCode.VaultError.errorCodeInt
            errorMessage = "File not found [$documentPath] "
        }

        if(response.errorCode!=0){
            watcher.debug("ERROR READING DUKPT:[${ErrorCode.fromId(response.errorCode)}]  [${response.errorCode}]  [${response.errorMessage}]")
            return response
        }
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

        return response
    }
  
private fun <T> readFromFile(documentPath: String, dto: Any, clazz: Class<T>, watcher: Watcher): T? {
    return try {
        val encryptedData = FileInputStream(documentPath).use { it.readBytes() } // Leemos el contenido del archivo como bytes
        val key = generateKeyFromRequestEntity(dto) // Generamos la clave
        
        // Intentamos desencriptar los datos
        val decryptedData = StringUtils.decryptAESAscii(encryptedData, key, watcher)

        // Convertimos los datos desencriptados a String
        val decryptedResult = String(decryptedData)

        // Intentamos deserializar primero el resultado desencriptado
        try {
            gson.fromJson(decryptedResult, clazz) // Deserializamos el objeto
        } catch (ex: Exception) {
            // Si falla, intentamos deserializar los datos originales
            try {
                val originalResult = String(encryptedData) // Convertimos los bytes originales a String
                gson.fromJson(originalResult, clazz) // Deserializamos el objeto
            } catch (innerEx: Exception) {
                watcher.error("Error deserializing original data: ${innerEx.message}")
                null // En caso de error, retornamos null
            }
        }
    } catch (ex: IOException) {
        watcher.debug("Error reading from file: ${ex.message}")
        null
    }
}
private fun <T> saveToFile(documentPath: String, dto: T, watcher: Watcher) {
    watcher.debug("saveToFile Saving to file: $documentPath")
    try {
        val json = gson.toJson(dto) // Serializamos el objeto a JSON
        watcher.debug("saveToFile Serialized JSON: $json")

        val key = generateKeyFromRequestEntity(dto as RequestEntity) // Generamos la clave
        watcher.debug("saveToFile Generated encryption key.")
 
        val encryptedJson = StringUtils.encryptAESAscii(json.toByteArray(), key, watcher) // Encriptamos el JSON
        watcher.debug("Encrypted JSON data successfully.")

        // Escribimos el JSON cifrado en el archivo
        FileOutputStream(documentPath).use { outputStream ->
            outputStream.write(encryptedJson)
            watcher.debug("Encrypted JSON written to file: $documentPath")
            WatcherGlobalLog.debug("Encrypted JSON written to file: $documentPath")
 
        }
    } catch (ex: Exception) {
        watcher.error("Error saving to file $documentPath: ${ex.message}")
        WatcherGlobalLog.error("Error saving to file $documentPath: ${ex.message} ")
    }
} 


// Función para generar la clave (fija o basada en RequestEntity)
fun generateKeyFromRequestEntity(requestEntity: Any): ByteArray {
    return  "lYN_AKyyy7777@@!".toByteArray().copyOf(16)
    
}
}
