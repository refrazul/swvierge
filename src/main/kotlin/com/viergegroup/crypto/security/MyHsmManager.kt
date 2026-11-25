package com.viergegroup.crypto.security 
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.StringUtils
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.ResponseEntity
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.webwatcher.WatcherContext

import java.io.DataOutputStream
import java.io.DataInputStream
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import com.viergegroup.crypto.pandora.managers.VaultDocumentManager

import com.viergegroup.swmain.entities.catalogs.Authorizer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
class MyHsmDto{
    class DUKPTRequest:RequestEntity(){
        var pin=""
        var pan="" 
        var bdk=""
        var ksn="" 
        var data=""

    }

    class DUKPTResponse:ResponseEntity(){
        var data=""
        var command=""
        var crc32=""
        var ksn=""
        var versionName=""
        var cipherCounter=""
        var lastPan4Digits=""

       var cardNumber=""

    }


    class PINHSMRequest : RequestEntity() {
        // Para desencriptar un PIN Block
        var pin: String = ""   // PIN Block decuifrado
        var pan: String = ""   // PAN
 
        var pinBlock: String = ""   // PIN Block cifrado
        var pinKey: String = ""     // Clave de cifrado de PIN (debe estar cargada en el HSM)
        var pinFormat: Int = 0      // Formato del PIN Block (Ej: 1 = ANSI X9.8, 2 = ISO 9564-1)
        var pinBlockData: String = "" // Datos adicionales dependiendo del formato
    
        // Para trasladar un PIN de una clave a otra
        var sourceKey: String = ""  // Clave de origen en la que actualmente está cifrado el PIN
        var destinationKey: String = "" // Clave de destino a la que se quiere trasladar el PIN
    
        // Para cifrado/descifrado de datos con DUKPT
        var bdk: String = ""        // Base Derivation Key para DUKPT (debe estar cargada en el HSM)
        var ksn: String = ""        // Key Serial Number (KSN), cambia en cada transacción
        var data: String = ""       // Datos a cifrar o descifrar (PIN, número de tarjeta, etc.)
        var encrypt: Boolean = true // Indica si la operación es cifrado (true) o descifrado (false)
    }
    
 
    class PINHSMResponse : ResponseEntity() {
        var pinBlock: String = ""   // PIN Block cifrado

        var decryptedPIN: String = ""  // PIN en texto claro (si se usó desencriptación)
        var translatedPIN: String = "" // PIN traducido (si se usó traslado de PIN)
        var encryptedData: String = "" // Datos cifrados (si se usó DUKPT para cifrar)
        var decryptedData: String = "" // Datos descifrados (si se usó DUKPT para descifrar)
        var command: String = ""       // Comando enviado al HSM
        var responseCode: String = ""  // Código de respuesta del HSM
        var versionName: String = ""   // Versión del comando o del servicio
    }
     

} 
class MyHsmManager { 
   // val dataWindows="C:\\Git\\pandoraneo\\data\\myhsm\\certs\\"
  //  val dataLinux="/home/ec2-user/pandoraneo/data/myhsm/certs/"
    val hsmIP="8.18.37.186"
    val hsmPort=16305 
    val  myHsmChainCrt="myhsmca.jks"
    val  myHsmMYHSMClient="client_MYHSM.jks"
    private  val  watcher= WatcherContext.getInstance()


      // ================== BALANCEO HSM ==================

    private data class HsmEndpoint(
        val ip: String,
        val port: Int
    )

    /**
     * Índices de round-robin por "key" 
     */
        private val rrIndex = AtomicInteger(0)

  
    private fun getGlobalHsmEndpoints(): List<HsmEndpoint> {
        val settings = AppSettings.getInstance()
        val configured = settings.hsmEndpoints
            watcher.debug("getGlobalHsmEndpoints: usando ${configured.size} endpoints desde AppSettings")

        return if (configured.isNotEmpty()) {
            watcher.debug("getGlobalHsmEndpoints: usando ${configured.size} endpoints desde AppSettings")
            configured.map { HsmEndpoint(it.ip, it.port) }
        } else {
            watcher.debug(
                "getGlobalHsmEndpoints: NO hay endpoints configurados, " +
                "usando fallback [$hsmIP:$hsmPort]" 
            )
            listOf(HsmEndpoint(hsmIP, hsmPort))
        }
    }

    private fun nextIndex(size: Int): Int {
        return Math.floorMod(rrIndex.getAndIncrement(), size)
    }
        
    private fun selectHsmEndpoint(): HsmEndpoint {
        val endpoints = getGlobalHsmEndpoints()
        if (endpoints.isEmpty()) {
            return HsmEndpoint(hsmIP,hsmPort)
        } 

        val idx = nextIndex(endpoints.size)
        val endpoint = endpoints[idx]

        watcher.debug(
            "selectHsmEndpoint → idx=[$idx], endpoint=[${endpoint.ip}:${endpoint.port}]"
        )

        return endpoint
    } 


    fun decryptWithDUKPT( data:String,ksn:String): MyHsmDto.DUKPTResponse {
        val request=MyHsmDto.DUKPTRequest()
        request.data=data
        request.ksn=ksn 

        return decryptWithDUKPT(request) 
    }
    fun encryptWithDUKPT( data:String,ksn:String): MyHsmDto.DUKPTResponse {

        val request=MyHsmDto.DUKPTRequest()
        request.data=data
        request.ksn=ksn
        return encryptWithDUKPT(request)
    }
 
    fun encryptWithDUKPT( dukptData:DukptData): MyHsmDto.DUKPTResponse {
        val request=MyHsmDto.DUKPTRequest()
        request.data=dukptData.clearData
        request.ksn=dukptData.ksn
        if(dukptData.hostType.authorizer==Authorizer.EVOPAYMENT){
             val dto= VaultDtos.ReadKeyRequest()
             dto.terminalSerialNumber=dukptData.terminalSerialNumber
             dto.rrn=dukptData.rrn
             dto.hostType=dukptData.hostType

            val vaultRes=VaultDocumentManager.generateOrIncrementKsn(dto) 
            if(vaultRes.errorCode!=0){ 
                val myhsmRes= MyHsmDto.DUKPTResponse()
                myhsmRes.errorCode=vaultRes.errorCode
                myhsmRes.errorMessage="ERROR AL GENERAR KSN "+vaultRes.errorMessage
                return myhsmRes
            } else{
                request.ksn=vaultRes.data
            }
        }
        return encryptWithDUKPT(request)
    }

 
     
    
    fun encryptWithDUKPT(request:MyHsmDto.DUKPTRequest): MyHsmDto.DUKPTResponse {
        val responseDto=MyHsmDto.DUKPTResponse()
        val reqDto= VaultDtos.ReadKeyRequest()
        watcher.debug("encryptWithDUKPT")
        reqDto.keyId="bdkhost" 
        reqDto.hostType= request.hostType 
        reqDto.rrn=request.rrn 
       reqDto.terminalSerialNumber= request.terminalSerialNumber 

        if(request.hostType.authorizer==Authorizer.EVOPAYMENT){
            request.data=request.data.padEnd(48,'F')
            watcher.debug("data padded: [${request.data}]")
  
        }
        if(request.hostType.authorizer==Authorizer.EVOPAYMENT&&request.ksn==""){
            val dto= VaultDtos.ReadKeyRequest()
             dto.terminalSerialNumber=request.terminalSerialNumber
            dto.rrn=request.rrn
            dto.hostType=request.hostType 
            val vaultRes=VaultDocumentManager.generateOrIncrementKsn(dto) 
        
            watcher.debug("VaultDocumentManager_generateOrIncrementKsn")
            watcher.debug("vaultRes.errorCode [${vaultRes.errorCode}]")
            if(vaultRes.errorCode!=0){ 
                val myhsmRes= MyHsmDto.DUKPTResponse()
                myhsmRes.errorCode=vaultRes.errorCode
                myhsmRes.errorMessage="ERROR AL GENERAR KSN "+vaultRes.errorMessage
                return myhsmRes
            } else{
                request.ksn=vaultRes.data
                watcher.debug("request.ksn [${request.ksn}]")
            }
        }   


        responseDto.ksn=request.ksn
        responseDto.cipherCounter=StringUtils.extractCipherCounterFromKsn(request.ksn)
        responseDto.lastPan4Digits=StringUtils.extractLast4DigitsFromAsciiTrack2(request.data)
        responseDto.cardNumber=StringUtils.extractPanFromAsciiTrack2(request.data)
  

        reqDto.terminalSerialNumber=request.terminalSerialNumber
        watcher.debug(" simulateHSM for ${request.hostType} 01=>${AppSettings.getHostConfig(request.hostType).simulateHSM}")
        val newBdk= VaultSecureManager().readKeyValue(reqDto)
        if(newBdk.errorCode!=0){ 
            responseDto.errorMessage=newBdk.errorMessage
            responseDto.errorCode=newBdk.errorCode
            watcher.debug(" VaultSecureManager ERROR=>  KSN [${ request.ksn}]  | [${ ErrorCode.fromId(newBdk.errorCode)}]  [${ newBdk.errorMessage}]")
            return responseDto
        }
        val bdk=newBdk.data 
         watcher.debug(" ENCRYPT_DUKPT_BDK : [${bdk}]")

        val command = createDUKPTCommand(bdk,request.ksn, request.data)
        responseDto.command=StringUtils.byteArrayToASCII(command)
        watcher.debug(" rrn [${ reqDto.rrn}]  reqDto.keyId [${ reqDto.keyId}]")

         watcher.debug(" ENCRYPT_DUKPT_COMMAND_REQUEST : [${responseDto.command}]")

        watcher.debug(" simulateHSM ${AppSettings.getHostConfig(request.hostType).simulateHSM}")
        if(AppSettings.getHostConfig(request.hostType).simulateHSM ){
            try {
                val bytesData = RSAUtils.encrypt3DES(
                    request.data,
                    request.ksn.padStart(48,'0')
                )
                responseDto.data=StringUtils.encodeHexString(bytesData)
                responseDto.command="ENCRYPTED OK"
            }catch (e:Exception){
                responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
                responseDto.command=e.message.toString()
               watcher.error("ERROR encryptWithDUKPT ${ responseDto.command}")
                return responseDto

            } 
            responseDto.errorCode=0
            responseDto.errorMessage="ENCRYPTED OK"
             try {
                    val hsm = connectToHSM(hsmIP, hsmPort)
                    hsm.sendCommand(command)
                   val  response = hsm.receiveResponse()


                    hsm.close()
                }catch (ex:Exception){
                   
                watcher.error("HSM EXCEPTION:${ responseDto.errorMessage}")

                }
            
            return responseDto

        }
        var response:ByteArray=ByteArray(0)
        try {
            val hsm = connectToHSM(hsmIP, hsmPort)
            hsm.sendCommand(command)

             response = hsm.receiveResponse()
            hsm.close()
            responseDto.errorMessage="HSM SUCCESS"
            responseDto.errorCode=0

        }catch (ex:Exception){
            responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage="HSM EXCEPTION: ${ex.message}"
           watcher.error("HSM EXCEPTION:${ responseDto.errorMessage}")
            return responseDto

        }
        val commandResponse=StringUtils.byteArrayToASCII(response)
        val cmdAscii=StringUtils.byteArrayToASCII(command)

        val responseClean=commandResponse.replace("<","").replace(">","")
         watcher.debug(" ENCRYPT_DUKPT_COMMAND_RESPONSE : [${responseClean}]")

        val parts=responseClean.split('#')
        if(parts.size<2){
            responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage= "REQUEST_CMD: $cmdAscii HSM_CMD_ERROR: $commandResponse BDK: $bdk"
            return responseDto
        }else{
            if(parts[0].equals("488")){
                responseDto.data=parts[1]
                try{ 
                        var crc32Bytes =ByteArray(0)
                        if(request.hostType.authorizer==Authorizer.EVOPAYMENT){
                           crc32Bytes = RSAUtils.CRC32Calculate(StringUtils.hexStringAsAsciiByteArray(responseDto.data) )
                           watcher.debug(" CRC32 CALCULADO PARA AUTHORIZER EVO") 

                        }else{
                          crc32Bytes = RSAUtils.CRC32Calculate(StringUtils.hexStringToByteArrayNeo(responseDto.data) )

                        }
 

                      val crc32Hex = StringUtils.encodeHexString(crc32Bytes)
                      watcher.debug(" CRC32 del Track2 cifrado: $crc32Hex") 
                      responseDto.crc32=crc32Hex

                }catch(ex:Exception){
                    watcher.error("CRC32 EX :${ex.message}")

                }
             
                return responseDto

            }else{
                responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
              responseDto.errorMessage="REQUEST_CMD: ${cmdAscii} , BDK: $bdk, HSM_CMD_ERROR: "+parts[1]+" "+commandResponse
              watcher.error("HSM ERROR:${ responseDto.errorMessage}")

                return responseDto

            }

        }

    }

    fun decryptPIN(request: MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        val responseDto = MyHsmDto.PINHSMResponse()
        
        watcher.debug("decryptPIN")
    
        val command = createDecryptPINCommand(request.pinBlock, request.pinKey, request.pinFormat, request.pinBlockData)
        responseDto.command = StringUtils.byteArrayToASCII(command)
    
        if (AppSettings.getHostConfig(request.hostType ).simulateHSM) {
            responseDto.decryptedPIN = "1234" // Simulación de PIN desencriptado
            responseDto.errorCode = 0
            responseDto.errorMessage = "DECRYPTED OK"
            return responseDto
        }
    
        return try {
            val hsm = connectToHSM(hsmIP, hsmPort)
            hsm.sendCommand(command)
            val response = hsm.receiveResponse()
            hsm.close()
    
            val commandResponse = StringUtils.byteArrayToASCII(response)
            val parts = commandResponse.replace("<", "").replace(">", "").split('#')
    
            if (parts.isNotEmpty() && parts[0] == "A0") {
                responseDto.decryptedPIN = parts[1]
                responseDto.errorCode = 0
                responseDto.errorMessage = "HSM SUCCESS"
            } else {
                responseDto.errorCode = ErrorCode.HSMError.errorCodeInt
                responseDto.errorMessage = "HSM ERROR: $commandResponse"
            }
    
            responseDto
        } catch (ex: Exception) {
            responseDto.errorCode = ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage = "HSM EXCEPTION: ${ex.message}"
            watcher.error("HSM ERROR: ${responseDto.errorMessage}")
            responseDto
        }
    }
    
    fun translatePIN(request: MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        val responseDto = MyHsmDto.PINHSMResponse()
    
        watcher.debug("translatePIN")
    
        val command = createTranslatePINCommand(request.sourceKey, request.destinationKey, request.pinBlock)
        responseDto.command = StringUtils.byteArrayToASCII(command)
    
        if (AppSettings.getHostConfig(request.hostType ).simulateHSM) {
            responseDto.translatedPIN = "FAKE_PIN_TRANSLATED" // Simulación de PIN trasladado
            responseDto.errorCode = 0
            responseDto.errorMessage = "TRANSLATED OK"
            return responseDto
        }
     
        return try {
            val hsm = connectToHSM(hsmIP, hsmPort)
            hsm.sendCommand(command)
            val response = hsm.receiveResponse()
            hsm.close()
    
            val commandResponse = StringUtils.byteArrayToASCII(response)
            val parts = commandResponse.replace("<", "").replace(">", "").split('#')
    
            if (parts.isNotEmpty() && parts[0] == "31") {
                responseDto.translatedPIN = parts[1]
                responseDto.errorCode = 0
                responseDto.errorMessage = "HSM SUCCESS"
            } else { 
                responseDto.errorCode = ErrorCode.HSMError.errorCodeInt
                responseDto.errorMessage = "HSM ERROR: $commandResponse"
            }
    
            responseDto
        } catch (ex: Exception) {
            responseDto.errorCode = ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage = "HSM EXCEPTION: ${ex.message}"
            watcher.error("HSM ERROR: ${responseDto.errorMessage}")
            responseDto
        }
    }
    
    
    fun encryptPIN(request:MyHsmDto.PINHSMRequest): MyHsmDto.PINHSMResponse {
        val responseDto=MyHsmDto.PINHSMResponse()
        val reqDto= VaultDtos.ReadKeyRequest()
        watcher.debug("encryptWithDUKPT")
        reqDto.keyId="bdkvierge"
        reqDto.hostType= HostType.Vierge  
        reqDto.rrn=request.rrn
        reqDto.terminalSerialNumber=request.terminalSerialNumber
        watcher.debug(" simulateHSM for ${request.hostType} 01=>${AppSettings.getHostConfig(request.hostType).simulateHSM}")
        val newBdk= VaultSecureManager().readKeyValue(reqDto)
        if(newBdk.errorCode!=0){
            responseDto.errorMessage=newBdk.errorMessage
            responseDto.errorCode=newBdk.errorCode
            watcher.debug(" VaultSecureManager ERROR=> [${ ErrorCode.fromId(newBdk.errorCode)}]  [${ newBdk.errorMessage}]")
            return responseDto
        }  
        val bdk=newBdk.data
        val command = createEncryptPINCommand(request.pinBlock, request.pan, request.pinKey)
        responseDto.command=StringUtils.byteArrayToASCII(command) 
        watcher.debug(" rrn [${ reqDto.rrn}]  reqDto.keyId [${ reqDto.keyId}]")
        watcher.debug(" simulateHSM ${AppSettings.getHostConfig(request.hostType).simulateHSM}")
        if(AppSettings.getHostConfig(request.hostType).simulateHSM ){
            try {
                val bytesData = RSAUtils.encrypt3DES(
                    request.data,
                    request.ksn.padStart(48,'0')
                )
                responseDto.pinBlock=StringUtils.encodeHexString(bytesData)
                responseDto.command="ENCRYPTED OK"
            }catch (e:Exception){
                responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
                responseDto.command=e.message.toString()
               watcher.error("ERROR encryptWithDUKPT ${ responseDto.command}")
                return responseDto

            } 
            responseDto.errorCode=0
            responseDto.errorMessage="ENCRYPTED OK"
            return responseDto

        }
        var response:ByteArray=ByteArray(0)
        try {
            val hsm = connectToHSM(hsmIP, hsmPort)
            hsm.sendCommand(command)

             response = hsm.receiveResponse()
            hsm.close()
            responseDto.errorMessage="HSM SUCCESS"
            responseDto.errorCode=0

        }catch (ex:Exception){
            responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage="HSM EXCEPTION: ${ex.message}"
           watcher.error("HSM EXCEPTION:${ responseDto.errorMessage}")
            return responseDto

        }
        val commandResponse=StringUtils.byteArrayToASCII(response)
        val cmdAscii=StringUtils.byteArrayToASCII(command)

        val responseClean=commandResponse.replace("<","").replace(">","")
        val parts=responseClean.split('#')
        if(parts.size<2){
            responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage= "REQUEST_CMD: $cmdAscii HSM_CMD_ERROR: $commandResponse BDK: $bdk"
            return responseDto
        }else{
            if(parts[0].equals("488")){
                responseDto.pinBlock=parts[1]
                return responseDto

            }else{
                responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
              responseDto.errorMessage="REQUEST_CMD: $command , BDK: $bdk, HSM_CMD_ERROR: "+parts[1]+" "+commandResponse
              watcher.error("HSM ERROR:${ responseDto.errorMessage}")

                return responseDto

            }

        }

    }




    fun decryptWithDUKPT( dukptData:DukptData): MyHsmDto.DUKPTResponse {
        val request=MyHsmDto.DUKPTRequest()
        request.data=dukptData.clearData
        request.ksn=dukptData.ksn
        return decryptWithDUKPT(request)
    } 
    fun decryptWithDUKPT(request:MyHsmDto. DUKPTRequest): MyHsmDto.DUKPTResponse {


        val responseDto=MyHsmDto.DUKPTResponse()
        val reqDto= VaultDtos.ReadKeyRequest()
        reqDto.keyId="bdkhost" 
        reqDto.hostType= request.hostType
        reqDto.rrn=request.rrn
        reqDto.terminalSerialNumber=request.terminalSerialNumber
        responseDto.ksn=request.ksn

        var commandResponse=""
        var response:ByteArray=ByteArray(0)
        val newBdk= VaultSecureManager().readKeyValue(reqDto)
        if(newBdk.errorCode!=0){
            responseDto.errorMessage=newBdk.errorMessage 
            responseDto.errorCode=newBdk.errorCode
            return responseDto
        }

        val command = createDUKPTCommand(newBdk.data ,request.ksn, request.data,false)
        watcher.debug(" simulateHSM for ${request.hostType} ${AppSettings.getHostConfig(request.hostType).simulateHSM}")
        if(AppSettings.getHostConfig(request.hostType).simulateHSM ){
            try {
                val bytesData = RSAUtils.decrypt3DES(
                    request.data, 
                    request .ksn.padStart(48,'0')
                )
                responseDto.data=StringUtils.encodeHexString(bytesData)
                responseDto.command="DECRYPTED OK "
            }catch (e:Exception){
                responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
                responseDto.command=e.message.toString()
               watcher.error("ERROR decryptWithDUKPT ${ responseDto.command}")
                return responseDto
            }
            responseDto.errorCode=0
            responseDto.errorMessage="DECRYPTED OK "

            try {
                val hsm = connectToHSM(hsmIP, hsmPort)
                hsm.sendCommand(command)
                response = hsm.receiveResponse()
                commandResponse=StringUtils.byteArrayToASCII(response)
                responseDto.command=commandResponse
                hsm.close()
            }catch (ex:Exception){
     
            }


            return responseDto
        }


        try {
            val hsm = connectToHSM(hsmIP, hsmPort)
            hsm.sendCommand(command)
             response = hsm.receiveResponse()
             commandResponse=StringUtils.byteArrayToASCII(response)
            responseDto.command=commandResponse
            hsm.close()
        }catch (ex:Exception){
            responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
            responseDto.errorMessage="HSM EXCEPTION: ${ex.message}"
           watcher.error("HSM EXCEPTION: ${ex.message}")

                try {
                    val bytesData = RSAUtils.decrypt3DES(
                        request.data, 
                        request .ksn.padStart(48,'0')
                    )
                    responseDto.data=StringUtils.encodeHexString(bytesData)
                    responseDto.command="DECRYPTED OK "
                        responseDto.errorCode=0
                responseDto.errorMessage="DECRYPTED OK "
                }catch (e:Exception){
                    responseDto.errorCode=ErrorCode.HSMError.errorCodeInt
                    responseDto.command=e.message.toString()
                    return responseDto
                }
            


            return responseDto

        }
 


        val responseClean=commandResponse.replace("<","").replace(">","")
        val parts=responseClean.split('#')
        if(parts.size<2){
            responseDto.errorMessage= "CMD REQUEST: $command|| HSM_CMD_ERROR: $commandResponse"
            return responseDto
        }else{
            if(parts[0].equals("488")){
                 responseDto.errorCode=0
              responseDto.errorMessage="DECRYPTED OK "
                responseDto.data=parts[1]
                return responseDto

            }else{
              responseDto.errorCode=ErrorCode.HSMError.errorCodeInt;
              responseDto.errorMessage="DECRYPTED OK "
                responseDto.errorMessage="CMD REQUEST: "+command+"HSM_CMD_ERROR: "+parts[1]+" "+commandResponse

               watcher.error("CMD REQUEST: "+command+"HSM_CMD_ERROR: "+parts[1]+" "+commandResponse)

                return responseDto

            }

        }
    }




    private fun createDUKPTCommand(bdk: String, ksn: String, data: String, encrypt: Boolean = true): ByteArray {
        val padding = "0" //if (data.length % 16 == 0) "0" else "1"  // múltiplo de 8 bytes (16 hex) = no padding

        var commandTemplate="<388#@encryptionMode#0#@bdk#@ksn##@data#@padding#>"
        var mode= "E"
        if(!encrypt){ 
            mode="D"
        }
        commandTemplate=commandTemplate.replace("@encryptionMode",mode)
        commandTemplate=commandTemplate.replace("@bdk",bdk)
        commandTemplate=commandTemplate.replace("@data",data)
        commandTemplate=commandTemplate.replace("@ksn",ksn)
         commandTemplate=commandTemplate.replace("@padding",padding)

        return asciiToByteArray(commandTemplate)

    } 


        /** pagina 484
         * Construye un comando para desencriptar un PIN block en el HSM Atalla.
         *
         * @param pinBlock PIN Block cifrado que se quiere descifrar.
         * @param pinKey Clave de cifrado de PIN (PIN Encryption Key). Debe estar cargada en el HSM.
         * @param pinFormat Formato del PIN Block (1 = ANSI X9.8, 2 = ISO 9564-1, etc.).
         * @param pinBlockData Datos adicionales requeridos dependiendo del formato de PIN Block.
         * @return ByteArray que representa el comando en formato ASCII.
         */
        private fun createDecryptPINCommand(pinBlock: String, pinKey: String, pinFormat: Int, pinBlockData: String): ByteArray {
            var commandTemplate = "<90#@pinFormat#@pinBlock#@pinKey#@pinBlockData#>"

            commandTemplate = commandTemplate.replace("@pinFormat", pinFormat.toString())
                .replace("@pinBlock", pinBlock)
                .replace("@pinKey", pinKey)
                .replace("@pinBlockData", pinBlockData)

            return asciiToByteArray(commandTemplate)
        }

        /**  PAGINA 195 
         * Construye un comando para trasladar un PIN de una clave a otra en el HSM Atalla.
         *
         * @param sourceKey Clave de origen en la que actualmente está cifrado el PIN.
         * @param destinationKey Clave de destino a la que se quiere trasladar el PIN.
         * @param pinBlock PIN Block cifrado que será trasladado.
         * @return ByteArray que representa el comando en formato ASCII.
         */
        private fun createTranslatePINCommand(sourceKey: String, destinationKey: String, pinBlock: String): ByteArray {
            var commandTemplate = "<31#@sourceKey#@destinationKey#@pinBlock#>"

            commandTemplate = commandTemplate.replace("@sourceKey", sourceKey)
                .replace("@destinationKey", destinationKey)
                .replace("@pinBlock", pinBlock)

            return asciiToByteArray(commandTemplate)
        } 

            //<30#PIN Encryption Key#PIN#PAN Digits#> PAGINA 192

        private fun createEncryptPINCommand(pin: String, pan: String, key: String): ByteArray {
            var commandTemplate="<30#@key#@pin#@pan#>"
            commandTemplate=commandTemplate.replace("@pan",pan)
            commandTemplate=commandTemplate.replace("@pin",pin)
            commandTemplate=commandTemplate.replace("@key",key)
            return asciiToByteArray(commandTemplate)

        }


        private fun getAvailableSlotsCommand(): ByteArray {
            val commandTemplate = "<9A#KEY#>"
            return asciiToByteArray(commandTemplate)
        }
        
        private fun createAKBFormatCommand(key: String): ByteArray {
            var commandTemplate = "<7E#@key#>"
            commandTemplate = commandTemplate.replace("@key", key)
            return asciiToByteArray(commandTemplate)
        } 
        
        private fun injectKeyToSlotCommand(key: String, slot: String): ByteArray {
            var commandTemplate = "<7F#@key#@slot#>"
            commandTemplate = commandTemplate.replace("@key", key)
            commandTemplate = commandTemplate.replace("@slot", slot)
            return asciiToByteArray(commandTemplate)
        }

        


    fun asciiToByteArray(asciiString: String): ByteArray {
        val byteArray = ByteArray(asciiString.length)

        for (i in asciiString.indices) {
            byteArray[i] = asciiString[i].toByte()
        }

        return byteArray
    }

        private fun connectToHSM(hsmIp: String, hsmPort: Int): HSM {
            watcher.debug("=== connectToHSM: START ===")
            watcher.debug("Parámetros → hsmIp=[$hsmIp], hsmPort=[$hsmPort]")

            val context = SSLContext.getInstance("TLS")
            val hsmHost=selectHsmEndpoint()
              watcher.debug("selectHsmEndpoint → hsmIp=[${hsmHost.ip}], hsmPort=[${hsmHost.port}]")

              val finalIp=hsmHost.ip
                val finalPort=hsmHost.port

            var dataPath = AppSettings.getInstance().dataPath + "\\myhsm\\certs\\"
            val osName = System.getProperty("os.name")

            watcher.debug("Sistema operativo detectado → [$osName]")

            if (osName.contains("Linux", ignoreCase = true)) {
                dataPath = AppSettings.getInstance().dataPath + "/myhsm/certs/"
            }

            watcher.debug("dataPath final para certificados → [$dataPath]")

            // ---- Rutas de archivos ----
            val trustStorePath = dataPath + myHsmChainCrt
            val keyStorePath   = dataPath + myHsmMYHSMClient

            watcher.debug("TrustStore (CA chain) path → [$trustStorePath], exists=${java.io.File(trustStorePath).exists()}")
            watcher.debug("KeyStore (client) path → [$keyStorePath], exists=${java.io.File(keyStorePath).exists()}")

            // ---- TrustStore ----
            val trustStoreFile = FileInputStream(trustStorePath)
            watcher.debug("Abriendo trustStoreFile: $trustStorePath")

            val defaultKsType = KeyStore.getDefaultType()
            watcher.debug("KeyStore.getDefaultType() → [$defaultKsType] (usado para trustStore)")

            val trustStore = KeyStore.getInstance(defaultKsType)
            trustStore.load(trustStoreFile, "LilianaRX_2027_".toCharArray())
            watcher.debug("TrustStore cargado correctamente")

            // Imprimir alias del trustStore
            val trustAliases = trustStore.aliases().toList()
            watcher.debug("TrustStore aliases (${trustAliases.size}): $trustAliases")

            // ---- KeyStore (PKCS12 cliente) ----
            val keyStoreFile = FileInputStream(keyStorePath)
            watcher.debug("Abriendo keyStoreFile (PKCS12): $keyStorePath")

            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(keyStoreFile, "LilianaRX_2027_".toCharArray())
            watcher.debug("KeyStore PKCS12 cargado correctamente")

            val keyAliases = keyStore.aliases().toList()
            watcher.debug("KeyStore aliases (${keyAliases.size}): $keyAliases")

            val customTrustManager = createCustomTrustManager(trustStore)
            watcher.debug("CustomTrustManager creado")

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            watcher.debug("KeyManagerFactory algorithm → [${KeyManagerFactory.getDefaultAlgorithm()}]")

            keyManagerFactory.init(keyStore, "LilianaRX_2027_".toCharArray())
            watcher.debug("KeyManagerFactory inicializado con KeyStore PKCS12")

            val keyManagers = keyManagerFactory.keyManagers
            watcher.debug("KeyManagers.size → [${keyManagers.size}]")

            watcher.debug("SSLContext protocol antes de init → [${context.protocol}]")
            context.init(keyManagers, arrayOf(customTrustManager), SecureRandom())
            watcher.debug("SSLContext inicializado con keyManagers + customTrustManager")

            // ---- Creación del socket TLS ----
            val factory = context.socketFactory
            watcher.debug("SSLSocketFactory creado: ${factory.javaClass.name}")

            val hsmSocket = factory.createSocket(finalIp, finalPort) as SSLSocket
            watcher.debug(
                "SSLSocket creado. local=${hsmSocket.localAddress.hostAddress}:${hsmSocket.localPort}, " +
                "remote=${hsmSocket.inetAddress.hostAddress}:${hsmSocket.port}"
            )

            // No forzamos handshake (para no cambiar lógica), solo logueamos configuración
            watcher.debug("SSLSocket enabledProtocols → ${hsmSocket.enabledProtocols.joinToString()}")
            watcher.debug("SSLSocket enabledCipherSuites → ${hsmSocket.enabledCipherSuites.joinToString()}")

            val session = hsmSocket.session
            watcher.debug(
                "SSLSession (puede ser inicial/negociada más tarde) → " +
                "protocol=${session.protocol}, cipherSuite=${session.cipherSuite}, " +
                "peerHost=${session.peerHost}, peerPort=${session.peerPort}"
            )

            watcher.debug("=== connectToHSM: END (socket listo) ===")
            return HSM(hsmSocket)
        }


        
    private fun createCustomTrustManager(trustStore: KeyStore): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(trustStore)

        val defaultTrustManagers = trustManagerFactory.trustManagers
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                for (trustManager in defaultTrustManagers) {
                    (trustManager as X509TrustManager).checkClientTrusted(chain, authType)
                }
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                for (trustManager in defaultTrustManagers) {
                    (trustManager as X509TrustManager).checkServerTrusted(chain, authType)
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                val issuers = mutableListOf<X509Certificate>()
                for (trustManager in defaultTrustManagers) {
                    issuers.addAll((trustManager as X509TrustManager).acceptedIssuers)
                }
                return issuers.toTypedArray()
            }
        }
    }

    private inner class HSM(private val hsmSocket: SSLSocket) {
        private val output = DataOutputStream(hsmSocket.outputStream)
        private val input = DataInputStream(hsmSocket.inputStream)

        fun sendCommand(command: ByteArray) {
            output.write(command)
            output.flush()
        }

        /*fun receiveResponse(): ByteArray {
            a("receiveResponse: INIT ")
            val responseLength = input.readInt()
            a("receiveResponse: responseLength "+responseLength)
            val response = ByteArray(responseLength)
            input.readFully(response)
            a("receiveResponse: response "+response)
            return response
        }*/

        fun receiveResponse(): ByteArray {
            //a("receiveResponse ")
            val response = ByteArray(1024)
            input.read(response)
            //  a("receiveResponse: response " + response)

            return response
        }

        fun close() {
            hsmSocket.close()
        }


    }

}
