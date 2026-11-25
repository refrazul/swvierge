package com.shared.commons
import com.viergegroup.swmain.dbmodels.TransactionDBData
import com.viergegroup.crypto.security.BankRsaKey
import com.viergegroup.crypto.security.RSAUtils
import com.viergegroup.swmain.dtos.transactions.*
import com.viergegroup.swmain.entities.catalogs.Authorizer
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.catalogs.TransactionType
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog 
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl
import com.viergegroup.swmain.managers.PandoraneoManager
import  com.viergegroup.crypto.pandora.managers.VaultDtos
import java.util.Base64
import kotlin.random.Random

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import com.google.gson.JsonParser
import com.shared.commons.ErrorCode

import java.util.UUID


object StringUtils {  
    val sensitiveTags = setOf("5A", "57", "5F24", "5F25", "5F30")
    var bankRsaKey: BankRsaKey? = null
 
        
    private val HEX32 = Regex("(?i)^[0-9a-f]{32}$")
    private val UUID_CANON = Regex("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")


    private const val SHIFT = 5
    private const val PREFIX = "LX7701"
    private const val SUFFIX = "DX7701"
 
    fun extractLast4DigitsFromAsciiTrack2(track2: String): String {
        val sepIndex = track2.indexOfFirst { it == 'D' || it == '=' }
        if (sepIndex in 4..track2.length) {
            val pan = track2.substring(0, sepIndex)
            return if (pan.length >= 4) pan.takeLast(4) else ""
        }
        return ""
    }
 

            // ===== Utils =====
        private fun ByteArray?.toAsciiSafe(): String =
            this?.let {
                if (it.isEmpty()) "" else try { String(it, Charsets.US_ASCII) } catch (_: Exception) { it.toHexUpper() }
            } ?: ""

        private fun ByteArray?.toHexUpper(): String =
            this?.joinToString("") { b -> "%02X".format(b) } ?: ""

        // Si vas a guardar UUID legible en logs
        private fun ByteArray?.toUuidLike(): String =
            this?.takeIf { it.size >= 16 }?.let { it.toHexUpper() } ?: ""


        fun hexStr2UUID(input: String): UUID {
            var s = input.trim()
            if (s.startsWith("{") && s.endsWith("}")) s = s.substring(1, s.length - 1)
            s = s.removePrefix("urn:uuid:").removePrefix("URN:UUID:")

            if (UUID_CANON.matches(s)) {
                return try {
                    UUID.fromString(s)
                } catch (_: IllegalArgumentException) {
                    UUID.nameUUIDFromBytes(s.toByteArray(Charsets.UTF_8))
                }
            }

            if (HEX32.matches(s)) {
                val hyphened = buildString(36) {
                    append(s, 0, 8).append('-')
                    append(s, 8, 12).append('-')
                    append(s, 12, 16).append('-')
                    append(s, 16, 20).append('-')
                    append(s, 20, 32)
                }
                return try {
                    UUID.fromString(hyphened)
                } catch (_: Exception) {
                    UUID.nameUUIDFromBytes(s.toByteArray(Charsets.UTF_8))
                }
            }

            // Intento relajado: si parece hex lo convierto a bytes, si no, uso UTF-8 directo
            val bytes = hexToBytesRelaxed(s) ?: s.toByteArray(Charsets.UTF_8)
            return UUID.nameUUIDFromBytes(bytes)
        }

        private fun hexToBytesRelaxed(src: String): ByteArray? {
            val clean = src.replace(Regex("[^0-9A-Fa-f]"), "")
            if (clean.isEmpty()) return null
            val even = if (clean.length % 2 == 0) clean else "0$clean"
            val out = ByteArray(even.length / 2)
            var i = 0
            while (i < even.length) {
                val b = try {
                    even.substring(i, i + 2).toInt(16).toByte()
                } catch (_: NumberFormatException) {
                    return null
                }
                out[i / 2] = b
                i += 2
            }
            return out
        }
        // ===== Mapper principal =====
        fun dbData2Dto(data:TransactionDBData): TransactionDto {
            val dto = TransactionDto()

            // --- Datos de Afiliación/Comercio/Ubicación ---
            dto.fiid = data.fiid
            dto.affiliation = data.affiliation
            dto.merchantName = data.merchantName
            dto.merchantAddress = data.merchantAddress
            dto.bankName = data.bankName
            dto.serverName = data.serverName

            // --- Identificadores/Dispositivo ---
            dto.deviceModel = data.deviceModel
            dto.hostType = data.hostType
            dto.terminalSerialNumber = data.terminalSerialNumber
          //  dto.deviceType = data.deviceType RDM_MATRIX 
          //  dto.clientId = data.clientId RDM_MATRIX 

            // --- Enums de la transacción (negocio) ---
            dto.merchantType = data.merchantType
            dto.transactionType = data.transactionType
            dto.pointOfServiceEntryMode = data.pointOfServiceEntryMode
            dto.transactionDirection = data.transactionDirection
            dto.cardType = data.cardType
            dto.paymentPlanType = data.paymentPlanType
            dto.transactionStatus = data.transactionStatus

            // --- Datos de tarjeta / ISO básicos --- 
            dto.cardBin = data.cardBin
            dto.cardNumber = data.cardNumber
            dto.authorizationIdResponse = data.authorizationIdResponse      // F38
            dto.responseCode = data.responseCode                            // F39 (normalizado)
            dto.responseCodeAuthorizer = data.responseCode                  // espejo si lo usas distinto
            dto.merchantTypeCode = data.merchantTypeCode                    // F18
            dto.transactionAmount = data.transactionAmount                  // F4
            dto.transactionCurrencyCode = data.transactionCurrencyCode      // F49 (alfanum)
            dto.merchantCurrencyCode = data.transactionCurrencyCodeNumber.toString() // F19 numérico (p.ej. 484)

            // --- RRN / Original RRN ---
            dto.originalRrn = data.originalRrn  // (tu DTO no tiene rrn directo)

            // --- Claves / Tokens / EMV ---
            dto.keySerialNumber = data.keySerialNumber
            dto.emvData = data.emvData.toHexUpper() // si prefieres TLV ASCII, cámbialo a toAsciiSafe()

            // --- Campos de red/contacto/cliente ---
            dto.clientIP = data.clientIP
            dto.clientEmail = data.clientEmail
            dto.clientUserName = data.clientUserName
            dto.clientLocation = data.clientLocation

            // --- Fechas ---
            dto.transactionDateTime = data.creationDateTime

            // --- ISO ASCII (elige qué guardar: request/response). Aquí priorizo la respuesta. ---
            val isoRespAscii = data.isoASCIIResponse.toAsciiSafe()
            val isoReqAscii = data.isoASCIIRequest.toAsciiSafe()
            dto.isoASCII = if (isoRespAscii.isNotEmpty()) isoRespAscii else isoReqAscii
           
            // --- Extra útiles para depuración (opcional) ---
            // Puedes guardar el UUID binario como hex para correlación
            dto.uuid = byteArrayToHex(data.uuid)  // Solo si quieres “esconderlo” en algún campo libre

            return dto
        }

    fun stringToBCD(inputStr: String): ByteArray {
        var input = inputStr
        require(input.all { it.isDigit() }) { "Solo se permiten dígitos 0-9 para BCD." }

        if (input.length % 2 != 0) {
            input = "0$input" // pad a la izquierda si es impar
        }

        val bcd = ByteArray(input.length / 2)
        var j = 0
        for (i in input.indices step 2) {
            val high = (input[i] - '0')
            val low  = (input[i + 1] - '0')
            bcd[j++] = ((high shl 4) or low).toByte()
        }
        return bcd
    }


    fun parseIsoHeaderError(iso: String): Pair<ErrorCode, String> {
        require(iso.length >= 16) {   ErrorCode.Approved to "demasiado corto el ISO "  }
  
        // --- Paso 1: validar si hay error en la posición del '9' 
        // Ejemplo: ISO0260043509200
        //                         ↑ aquí está el '9' que indica error
        val isError = iso[12] == '9'   // índice basado en 0
 
        // --- Paso 2: extraer el status code (ejemplo "043")
        val statusCode = iso.substring(7, 10)

        // --- Paso 3: armar resultado
        return if (isError) { 
            ErrorCode.TxnFailed to "se declinó por status $statusCode"
        } else {
            ErrorCode.Approved to "transacción aprobada"
        }
    }  

    fun extractPanFromAsciiTrack2(track2: String): String {
        val sepIndex = track2.indexOfFirst { it == 'D' || it == '=' }
        return if (sepIndex in 12..19) {
            track2.substring(0, sepIndex)
        } else {
            ""
        }
    }


    fun extractCipherCounterFromKsn(ksn: String): String {
        try{
             if (ksn.length != 20 || !ksn.matches(Regex("^[0-9A-Fa-f]{20}$"))) {
              return ""
            }

            val ksnBytes = ksn.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

            val counterBytes = ksnBytes.takeLast(3).toByteArray()

            val counter = ((counterBytes[0].toInt() and 0x1F) shl 16) or
                        ((counterBytes[1].toInt() and 0xFF) shl 8) or
                        (counterBytes[2].toInt() and 0xFF)

            return counter.toString(16).padStart(7, '0').uppercase()
        }catch(ex:Exception){
                return ex.message.toString()
        }
       
    }

    fun parseTokenEW(dto: TransactionDto, watcher: Watcher): TransactionDto {
            if (dto.tokenEW.isEmpty()) {
                watcher.debug("tokenEW está vacío, se retorna DTO sin cambios.")
                return dto
            }

            watcher.debug("Iniciando parseo de dto.tokenEW: ${dto.tokenEW}")

            val parts = dto.tokenEW.split(" ")

            if (parts.size < 3) {
                watcher.debug("Formato incorrecto: tokenEW tiene menos de 3 partes. Valor: ${dto.tokenEW}")
                return dto.apply {
                    errorCode = ErrorCode.TokeEWBadFormat.errorCodeInt
                    errorMessage = ErrorCode.TokeEWBadFormat.errorCodeMessage
                }
            }

            val dataEW = parts[2]

            try {
                // Se calcula por longitudes fijas conocidas:
                // randomTkCipherEW = 512 caracteres (256 bytes hex) = 512
                // checkValueEW     = 6
                // rsaVersionEW     = 10
                // rsaPaddingEW     = 2
                // crc32EW          = 8
                // Total esperado = 512 + 6 + 10 + 2 + 8 = 538

                if (dataEW.length < 538) {
                    watcher.debug("Longitud incorrecta de dataEW: ${dataEW.length}. Se esperaban al menos 538 caracteres. Valor: $dataEW")
                    return dto.apply {
                        errorCode = ErrorCode.TokeEWBadFormat.errorCodeInt
                        errorMessage = ErrorCode.TokeEWBadFormat.errorCodeMessage
                    }
                }

                dto.randomTkCipherEW = dataEW.substring(0, 512)
                dto.checkValueEW     = dataEW.substring(512, 518)
                dto.rsaVersionEW     = dataEW.substring(518, 528)
                dto.rsaPaddingEW     = dataEW.substring(528, 530)
                dto.crc32EW          = dataEW.substring(530, 538)

                watcher.debug("Parseo exitoso:")
                watcher.debug("randomTkCipherEW: ${dto.randomTkCipherEW}")
                watcher.debug("checkValueEW: ${dto.checkValueEW}")
                watcher.debug("rsaVersionEW: ${dto.rsaVersionEW}")
                watcher.debug("rsaPaddingEW: ${dto.rsaPaddingEW}")
                watcher.debug("crc32EW: ${dto.crc32EW}")

            } catch (e: Exception) {
                watcher.error("Error al parsear tokenEW: ${e.message}. DataEW: $dataEW")
                return dto.apply {
                    errorCode = ErrorCode.TokeEWBadFormat.errorCodeInt
                    errorMessage = ErrorCode.TokeEWBadFormat.errorCodeMessage
                }
            }

            return dto
        }



    
    fun parseTokenEx(dto: TransactionDto,watcher:Watcher): TransactionDto {
        // Si tokenEX está vacío, no hay nada que parsear, devuelve el DTO original.
        if (dto.tokenEX.isEmpty()) {
            return dto
        } 

        watcher.debug("Iniciando parseo de dto.tokenEX: ${dto.tokenEX}")

        val parts = dto.tokenEX.split(' ')

        // Validar el número de partes
        if (parts.size < 3) {
            watcher.debug("Formato incorrecto: tokenEX tiene menos de 3 partes. Valor: ${dto.tokenEX}")
            return dto.apply {
                errorCode = ErrorCode.TokeEXBadFormat.errorCodeInt
                errorMessage = ErrorCode.TokeEXBadFormat.errorCodeMessage
            }
        }

        val dataEX = parts[2] // No es necesario .toString() ya que parts[2] ya es un String

        // Validar la longitud de dataEX
        if (dataEX.length < 68) {
            watcher.debug("Longitud incorrecta de dataEX: ${dataEX.length}. Se esperaban al menos 68 caracteres. Valor: $dataEX")
            return dto.apply {
                errorCode = ErrorCode.TokeEXBadFormat.errorCodeInt
                errorMessage = ErrorCode.TokeEXBadFormat.errorCodeMessage
            }
        }

        // Usar un bloque try-catch para manejar posibles errores en substring, aunque menos probable con las validaciones previas.
        try {
            dto.pekEX = dataEX.substring(0, 32)
            dto.ksnEX = dataEX.substring(32, 52)
            dto.checkValueEX = dataEX.substring(52, 58)
            dto.newKeyRequestStatusEX = dataEX.substring(58, 62)
            dto.crc32EX = dataEX.substring(62)
        } catch (e: StringIndexOutOfBoundsException) {
            // Esto podría ocurrir si las longitudes esperadas no se cumplen a pesar de la validación inicial,
            // o si hay un error en las constantes de los índices.
            watcher.error("Error al extraer subcadenas de dataEX: ${e.message}. DataEX: $dataEX")
            return dto.apply {
                errorCode = ErrorCode.TokeEXBadFormat.errorCodeInt
                errorMessage = ErrorCode.TokeEXBadFormat.errorCodeMessage
            }
        } 

        watcher.debug("Parseo de tokenEX completado exitosamente.")
        return dto
    }
    fun transformPassword(password: String): String {
        // Desplazar caracteres
        val shiftedPassword = password.map { shiftChar(it, SHIFT) }.joinToString("")
        // Invertir la cadena
        val reversedPassword = shiftedPassword.reversed()
        // Intercambiar caracteres en posiciones específicas
        val swappedPassword = swapCharacters(reversedPassword)
        // Añadir prefijo y sufijo
        val obfuscatedPassword = "$PREFIX$swappedPassword$SUFFIX"
        // Codificar en Base64
        return Base64.getEncoder().encodeToString(obfuscatedPassword.toByteArray(Charsets.UTF_8))
    }
    


    
     fun formatDoubleToAmountIso(amount: Double, watcher: Watcher): String {
        var conversion = "000000000000"
        try {
            val amountStr=amount.toString()
            val amntStrParts=amountStr.split(".")
             val amountIntStr=amntStrParts[0]
             val amountDecStr=amntStrParts[1]
            conversion =(amountIntStr+amountDecStr.padEnd(2,'0')).padStart(12,'0')
            watcher.debug("conversion " + conversion)
            return conversion
        } catch (e: Exception) {
            watcher.debug("formatDoubleToAmountIso EXCEPTION " + e.message)
            return conversion
        }
    }


    
    fun parseAmountToDouble(amount: String, watcher: Watcher): Double {
            return try {
                if (amount.isNullOrBlank()) {
                    watcher.debug("transactionAmount vacío o nulo → fallback 0.0")
                    return 0.0
                }

                // Aseguramos que tenga exactamente 12 dígitos
                val amountFormatted = amount.padStart(12, '0')

                // Los primeros 10 dígitos son la parte entera
                val integerPart = amountFormatted.substring(0, 10).toLong()
                // Los últimos 2 dígitos son la parte decimal
                val decimalPart = amountFormatted.substring(10, 12).toInt()

                val result = integerPart + (decimalPart / 100.0)

                watcher.debug("transactionAmount → $amount")
                watcher.debug("amountFormatted → $amountFormatted")
                watcher.debug("integerPart → $integerPart")
                watcher.debug("decimalPart → $decimalPart")
                watcher.debug("Parsed amount: $amountFormatted → $result")

                result
            } catch (ex: Exception) {
                watcher.debug("Error parsing transactionAmount='$amount', fallback to 0.0. Ex: ${ex.message}")
                0.0
            }
        }



   /* fun cleanAsciiBytes(input: ByteArray): ByteArray {
        return input.filter { it in 0x20.toByte()..0x7E.toByte() }.toByteArray()
    }
    */ 

    fun cleanAsciiBytes(input: ByteArray): ByteArray {
        try{ 
            val asciiString = input.filter { it in 0x20..0x7E }.toByteArray().toString(Charsets.US_ASCII)
                    val isoIndex = asciiString.indexOf("ISO")
                    if (isoIndex == -1) return input  // No se encontró "ISO"
                    val cleaned = asciiString.substring(isoIndex)
                    return cleaned.toByteArray(Charsets.US_ASCII)
        }catch(ex:Exception){
            return input
        }
       
    }
    
    



     fun byteArraySplit(byteArray: ByteArray, delimiter: Byte): List<ByteArray> {
        val parts = mutableListOf<ByteArray>()
        var start = 0
        for (i in byteArray.indices) {
            if (byteArray[i] == delimiter) {
                if (i > start) {
                    parts.add(byteArray.copyOfRange(start, i))
                }
                start = i + 1
            }
        }
        if (start < byteArray.size) {
            parts.add(byteArray.copyOfRange(start, byteArray.size))
        }
        return parts
    }
    
    
    fun destransformPassword(transformed: String): String {
        // Decodificar Base64
        val decodedBytes = Base64.getDecoder().decode(transformed)
        val decodedPassword = String(decodedBytes, Charsets.UTF_8)
        // Remover prefijo y sufijo
        val withoutPrefixSuffix = decodedPassword.removePrefix(PREFIX).removeSuffix(SUFFIX)
        // Revertir el intercambio de caracteres
        val swappedPassword = unswapCharacters(withoutPrefixSuffix)
        // Invertir la cadena
        val reversedPassword = swappedPassword.reversed()
        // Revertir el desplazamiento de caracteres
        return reversedPassword.map { shiftChar(it, -SHIFT) }.joinToString("")
    }
  
    private fun shiftChar(c: Char, shift: Int): Char {
        return when {
            c.isLowerCase() -> 'a' + (c - 'a' + shift + 26) % 26
            c.isUpperCase() -> 'A' + (c - 'A' + shift + 26) % 26
            c.isDigit() -> '0' + (c - '0' + shift + 10) % 10
            else -> c
        }
    }

    private fun swapCharacters(input: String): String {
        val chars = input.toCharArray()
        if (chars.size > 1) {
            // Intercambiar el primer y último carácter, el segundo y el penúltimo, etc.
            for (i in 0 until chars.size / 2 step 2) {
                val temp = chars[i]
                chars[i] = chars[chars.size - 1 - i]
                chars[chars.size - 1 - i] = temp
            }
        }
        return String(chars)
    }

    private fun unswapCharacters(input: String): String {
        // El intercambio de caracteres es simétrico, por lo que simplemente se llama de nuevo a la misma función
        return swapCharacters(input)
    }
     
    fun getDateTimeLog(localDateTime: LocalDateTime): String {

        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
        return localDateTime.format(formatter)
    }

    fun getVaultType(hostType:HostType):String{
        if(hostType==HostType.ProsaDUKPT){
            return "" 
        }
        return "_"+hostType.vaultType +"_"
    }
    fun toServerLocalDatetime(hostType: HostType, localDateTime: LocalDateTime): LocalDateTime {
        val hostConfig = AppSettings.getHostConfig(hostType)
        val timezone = hostConfig.zoneTime

        // Obtener la zona horaria especificada
        val zoneId = ZoneId.of(timezone)

        // Ajustar LocalDateTime a la zona horaria especificada
        val zonedDateTime = localDateTime.atZone(zoneId)

        // Convertir ZonedDateTime a LocalDateTime (sin información de zona horaria)
        return zonedDateTime.toLocalDateTime()
    }

    fun getServerZoneTime(hostType: HostType): LocalDateTime {
        val hostConfig = AppSettings.getHostConfig(hostType)
        val timezone = hostConfig.zoneTime

        // Obtener la zona horaria especificada
        val zoneId = ZoneId.of(timezone)

        // Obtener la hora actual en la zona horaria especificada
        val zonedDateTime = ZonedDateTime.now(zoneId)

        // Convertir ZonedDateTime a LocalDateTime (sin información de zona horaria)
        return zonedDateTime.toLocalDateTime()
    }

    fun getDateTimeLog(hostType: HostType, localDateTime: LocalDateTime): String {
        val hostConfig = AppSettings.getHostConfig(hostType)
        val timezone = hostConfig.zoneTime

        // Ajustar LocalDateTime a la zona horaria especificada
        val zonedDateTime = localDateTime.atZone(ZoneId.of(timezone))

        // Formatear la fecha y hora con la nueva zona horaria
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd")
        return zonedDateTime.format(formatter)
    }
 
    fun copyNonEmptyProperties(src: Any, target: Any): Any {
        BeanUtils.copyProperties(src, target, *getNullPropertyNames(src))
        return target
    } 

    private val hexArray = "0123456789ABCDEF".toCharArray()
    fun generateUUID(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().replace("-", "")
    }  
    
    fun generateUUIDAsByteArray(): ByteArray {
        val uuid = UUID.randomUUID().toString().replace("-", "") // Genera UUID sin guiones
        return hexStringToByteArray(uuid)
    }
    fun generateRandomString(length: Int): String {
        val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val secureRandom = SecureRandom()
        val stringBuilder = StringBuilder()

        for (i in 0 until length) {
            val randomIndex = secureRandom.nextInt(charPool.length)
            stringBuilder.append(charPool[randomIndex])
        }

        return stringBuilder.toString()
    }

    fun setAttributeValue(target: Any, attributeName: String, attributeValue: Any): Any {
        val property = target::class.memberProperties.find { it.name == attributeName }
        if (property is KMutableProperty<*>) {
            property.isAccessible = true
            val setter = property.setter
            val paramType = setter.parameters[1].type.classifier as? KClass<*>
            val value =
                    when (paramType) {
                        Int::class -> (attributeValue as Double).toInt()
                        String::class -> attributeValue as String
                        Double::class -> attributeValue as Double
                        is KClass<*> -> {
                            if (paramType.java.isEnum) {
                                paramType.java.enumConstants?.firstOrNull {
                                    it.toString() == attributeValue.toString()
                                }
                            } else {
                                attributeValue
                            }
                        }
                        else -> attributeValue 
                    }
            setter.call(target, value)
        }
        return target
    }

    fun encryptAESAscii(data: ByteArray, key: ByteArray,watcher:Watcher): ByteArray {
        try{
            val keySpec = SecretKeySpec(key.copyOf(16), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            watcher.debug("encryptAESAscii")
            watcher.debug("data "+StringUtils.encodeHexString(data))
            watcher.debug("key "+StringUtils.encodeHexString(key))
    
            // Genera un IV aleatorio
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
        
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        
            // Realiza el cifrado
            val encryptedData = cipher.doFinal(data)
            watcher.debug("encryptAESAscii_result "+StringUtils.encodeHexString(iv + encryptedData)) 
    
            // Combina el IV con los datos cifrados para enviarlos juntos
            return iv + encryptedData // Prepend el IV
        }catch(ex:Exception){
            watcher.error("encryptAESAscii_Exception "+ex.message)
            return data
        }
    }
    
    fun decryptAESAscii(data: ByteArray, key: ByteArray,watcher:Watcher): ByteArray {
        try{
            // Verificar que la longitud de los datos sea suficiente
            watcher.debug("data "+maskString(StringUtils.encodeHexString(data)))
            watcher.debug("key "+maskString(StringUtils.encodeHexString(key)))
            if (data.size < 16) {
                watcher.debug("Data length is too short to contain an IV.")
                return ByteArray(0)
            }

            // Extraer el IV de los primeros 16 bytes
            val iv = data.take(16).toByteArray() 

            // Extraer los datos cifrados
            val encryptedData = data.copyOfRange(16, data.size)

            val keySpec = SecretKeySpec(key.copyOf(16), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decrypted= cipher.doFinal(encryptedData)
            watcher.debug("decryptAESAscii_result "+encodeHexString(decrypted) )
            // Descifrar los datos
            return decrypted
        }catch(ex:Exception){
            watcher.error("decryptAESAscii_Exception "+ex.message)
            return data
        }
    
    }
    
    fun encrypt3DESAscii(datas: String, key1: ByteArray): ByteArray {
        val data = asciiToByteArray(datas)
        val key = key1.copyOfRange(0, 8)
        WatcherGlobalLog.debug("key : ${key.size}")
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun decrypt3DESAscii(data: ByteArray, key: ByteArray): ByteArray {

        val keySpec = DESedeKeySpec(key)
        WatcherGlobalLog.debug("key : ${key.size}")
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun decrypt3DESAscii(datas: String, key1: ByteArray): ByteArray {
        val key = key1.copyOfRange(0, 8)
        WatcherGlobalLog.debug("key : ${key.size}")
        val data: ByteArray = hexStringToByteArray(datas)
        val keySpec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey: SecretKey = keyFactory.generateSecret(keySpec)
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(8))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun asciiToByteArray(asciiString: String): ByteArray {
        return if (asciiString.length <= 0) {
            ByteArray(0)
        } else {
            asciiString.toByteArray(Charsets.US_ASCII)
        }
    }
    fun byteArrayToHex(byteArray: ByteArray?): String {
        return byteArray?.joinToString("") { "%02x".format(it) } ?: ""
    }
    
    fun byteArrayToASCII(byteArray: ByteArray): String {
        return String(byteArray, Charsets.US_ASCII)
    }
    fun toByte(b: com.viergegroup.crypto.security.BitSet): Byte {
        var value: Byte = 0
        for (i in 0 until b.bitSize()) {
            if (b[i]) value = (value or ((1L shl 7 - i).toByte()))
        }
        return value
    }
    fun toByteArray(b: com.viergegroup.crypto.security.BitSet): ByteArray {
        val size = ceil(b.size() / 8.0).toInt()
        val value = ByteArray(size)
        for (i in 0 until size) {
            value[i] = toByte(b[i * 8, b.size().coerceAtMost((i + 1) * 8)])
        }
        return value
    }
    fun stringToByteArrayHSM(cadena: String): ByteArray {
        val charset = Charsets.US_ASCII // Elige la codificación adecuada

        val byteArray = cadena.toByteArray(charset)
        return byteArray
    }

    fun byteArrayToBase64(byteArray: ByteArray): String {
        val base64String = Base64.getEncoder().encodeToString(byteArray)
        return base64String
    }

    fun calculateKSN(terminal: String, counter: Int): String {
        val bytes = terminal.toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(bytes)

        // Convierte el hash en una cadena hexadecimal
        val hashStr = StringBuilder()
        for (byte in hashBytes) {
            hashStr.append(String.format("%02x", byte))
        }

        // Trunca el hash a 16 caracteres
        return hashStr.toString().substring(0, 16).toUpperCase() +
                counter.toString().padStart(4, '0')
    }
    fun calcularHash(cadena: String): String {
        val bytes = cadena.toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(bytes)

        // Convierte el hash en una cadena hexadecimal
        val hashStr = StringBuilder()
        for (byte in hashBytes) {
            hashStr.append(String.format("%02x", byte))
        }

        // Trunca el hash a 16 caracteres
        return hashStr.toString().substring(0, 16).toUpperCase()
    }

    fun gzip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
        return bos.toByteArray()
    }

    fun ungzip(content: ByteArray): String =
            GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }

    fun encodeBase64(data: Any): String {
        val json = GsonConfig.gsonB64.toJson(data)
        return Base64.getEncoder().encodeToString(gzip(json))
    }
 
    internal inline fun <reified T> decodeBase64(data: String): T? {
        val decodedBytes = Base64.getDecoder().decode(data)
        val decodedString = ungzip(decodedBytes) 
        val primaryConstructor = T::class.constructors.find { it.parameters.isEmpty() }
        if (primaryConstructor != null) {
            var classob: T? = primaryConstructor.call()
            WatcherGlobalLog.debug("decodedString $decodedString")
            classob = GsonConfig.gsonDecodeB64.fromJson(decodedString, classob!!::class.java)
            return classob
        }
        return null
    }
    fun removeEmvTagAndValue(emvDataHex: String, tagToRemove: String): String {
        val watcher = WatcherContext.getInstance()
        watcher.debug("removeEmvTagAndValue(emvDataHex:$emvDataHex, tagToRemove:$tagToRemove)")
    
        // Convierte el dato EMV hexadecimal en una lista de bytes
        val emvDataBytes = emvDataHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    
        // Convierte el tag que deseas eliminar a una lista de bytes
        val tagBytesToRemove = tagToRemove.chunked(2).map { 
            it.toInt(16).toByte() 
        }
    
        var i = 0
        while (i < emvDataBytes.size) {
            // Comprueba si el tag coincide
            if (emvDataBytes.copyOfRange(i, i + tagBytesToRemove.size).contentEquals(tagBytesToRemove.toByteArray())) {
                var lengthIndex = i + tagBytesToRemove.size
                var length = emvDataBytes[lengthIndex].toInt() and 0xFF
    
                // Maneja el caso en el que el primer  byte de longitud indica una longitud de longitud variable
                if (length > 0x80) {
                    val lengthBytesCount = length - 0x80
                    length = emvDataBytes.copyOfRange(lengthIndex + 1, lengthIndex + 1 + lengthBytesCount).fold(0) { acc, byte ->
                        (acc shl 8) or (byte.toInt() and 0xFF)
                    }
                    lengthIndex += lengthBytesCount
                }
    
                // Calcula el índice final del TLV
                val endIndex = lengthIndex + 1 + length
    
                // Elimina el tag y su valor del dato EMV
                val newEmvDataBytes = emvDataBytes.copyOfRange(0, i) + emvDataBytes.copyOfRange(endIndex, emvDataBytes.size)
    
                // Convierte los bytes nuevamente a formato hexadecimal
                val newEmvDataHex = newEmvDataBytes.joinToString("") { "%02x".format(it) }
    
                return newEmvDataHex
            } else {
                // Avanza al siguiente tag-length
                var lengthIndex = i + tagBytesToRemove.size
                var length = emvDataBytes[lengthIndex].toInt() and 0xFF
    
                // Maneja el caso en el que el primer byte de longitud indica una longitud de longitud variable
                if (length > 0x80) {
                    val lengthBytesCount = length - 0x80
                    length = emvDataBytes.copyOfRange(lengthIndex + 1, lengthIndex + 1 + lengthBytesCount).fold(0) { acc, byte ->
                        (acc shl 8) or (byte.toInt() and 0xFF)
                    }
                    lengthIndex += lengthBytesCount
                }
    
                // Calcula el índice final del TLV y avanza al siguiente TLV
                i = lengthIndex + 1 + length
            } 
        }
    
        // El tag no se encontró en el dato EMV, devuelve el dato original
        return emvDataHex
    }
    




    fun generateRandomHexadecimal(length: Int): String {
        require(length > 0) { "La longitud debe ser mayor que 0" }

        val hexChars = "0123456789ABCDEF"
        val random = Random.Default
        val stringBuilder = StringBuilder(length)

        repeat(length) {
            val randomIndex = random.nextInt(hexChars.length)
            val randomChar = hexChars[randomIndex]
            stringBuilder.append(randomChar)
        }

        return stringBuilder.toString().toUpperCase()
    }

    fun getCardNumber(track2: String): String {
          // Divide el string usando 'D' como delimitador
      val partes = track2.split("D")
    // Retorna la primera parte (el número de tarjeta) o un valor por defecto
         return if (partes.isNotEmpty()) maskCardNumber(partes[0]) else "0000000000000000"
    }
 

    fun getEmvTagValue(emvDataHex: String, tagToExtract: String): String {
        val watcher = WatcherContext.getInstance()
        watcher.debug("getEmvTagValue(emvDataHex:$emvDataHex, tagToExtract:$tagToExtract)")

        // Convierte el dato EMV hexadecimal en un array de bytes
        val emvDataBytes = emvDataHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        
        // Convierte el tag que deseas extraer a un array de bytes
        val tagBytesToExtract = tagToExtract.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        var i = 0
        while (i < emvDataBytes.size) {
            // 1. Determinar el tamaño real del tag actual (EMV permite tags de 1, 2 o más bytes)
            var currentTagSize = 1
            if (i < emvDataBytes.size && (emvDataBytes[i].toInt() and 0x1F) == 0x1F) {
                currentTagSize++
                while (i + currentTagSize - 1 < emvDataBytes.size && (emvDataBytes[i + currentTagSize - 1].toInt() and 0x80) == 0x80) {
                    currentTagSize++
                }
            }

            // Evitar IndexOutOfBounds si los datos están mal formados
            if (i + currentTagSize >= emvDataBytes.size) break

            // Comprueba si el tag actual coincide con el que estamos buscando
            val isMatch = (currentTagSize == tagBytesToExtract.size) && 
                        emvDataBytes.copyOfRange(i, i + currentTagSize).contentEquals(tagBytesToExtract)

            var lengthIndex = i + currentTagSize
            var length = emvDataBytes[lengthIndex].toInt() and 0xFF

            // Maneja el caso en el que el primer byte de longitud indica una longitud de longitud variable (BER-TLV)
            if (length > 0x80) {
                val lengthBytesCount = length - 0x80
                length = emvDataBytes.copyOfRange(lengthIndex + 1, lengthIndex + 1 + lengthBytesCount).fold(0) { acc, byte ->
                    (acc shl 8) or (byte.toInt() and 0xFF)
                }
                lengthIndex += lengthBytesCount
            }

            // Calcula el índice donde empieza el valor y donde termina
            val valueStartIndex = lengthIndex + 1
            val valueEndIndex = valueStartIndex + length

            if (isMatch) {
                // Verifica que los datos no estén truncados
                if (valueEndIndex <= emvDataBytes.size) {
                    // Extrae únicamente los bytes del VALOR
                    val valueBytes = emvDataBytes.copyOfRange(valueStartIndex, valueEndIndex)
                    
                    // Convierte el valor nuevamente a hexadecimal y lo retorna
                    return valueBytes.joinToString("") { "%02X".format(it) } 
                } else {
                    // El string terminó antes de que se completara el valor (datos mal formados)
                    return ""
                }
            } else {
                // El tag no coincide, avanza al siguiente bloque TLV completo
                i = valueEndIndex
            }
        }

        // El tag no se encontró en el dato EMV
        return ""
    }

 //correccion 5
    fun extractCardNumberFromTlv(emvTags:String):String{
        var tagValue = getEmvTagValue(emvTags, "5A")

        if(tagValue==""){ 
             tagValue = getEmvTagValue(emvTags, "57") 

           return if(tagValue.isNotEmpty())  maskCardNumber(getCardNumber(tagValue)) else "0000000000000000"
 
 
        }

        return if(tagValue.isNotEmpty()) maskCardNumber(tagValue) else "0000000000000000"
    }


    fun maskCardNumber(cardNumber2: String): String {
        val cardNumber=cardNumber2.replace(" ","")  
        if(cardNumber.contains("**"))
         return cardNumber
         val visibleDigitsFirst = 8

        val visibleDigits = 4 
        val maskedChars = "*" 
        val cardNumberNew = cardNumber.trim()
        if (cardNumberNew.length <= visibleDigits * 2) {
            return cardNumberNew // El número es demasiado corto para enmascarar
        }

        val firstVisiblePart = cardNumberNew.substring(0, visibleDigitsFirst)
        val lastVisiblePart = cardNumberNew.takeLast(visibleDigits) 
        val middleMaskedPart = maskedChars.repeat(cardNumberNew.length - (visibleDigits * 2))
        val maskedNumber = "$firstVisiblePart$middleMaskedPart$lastVisiblePart"
    
        return maskedNumber
    } 


    
    fun maskJson(inputJson: String): String {
        val maskChar: Char = '*'
        val  maskLength: Int = 4 
        return try {
            val gson = GsonBuilder().setPrettyPrinting().create() 
            val jsonElement = JsonParser.parseString(inputJson)
            gson.toJson(maskValues(jsonElement, maskChar, maskLength))
        } catch (ex: JsonSyntaxException) {
            println("Error parsing JSON: ${ex.message}")
            inputJson // Retorna un JSON vacío en caso de error
        } catch (ex: Exception) {
            println("Unexpected error: ${ex.message}")
            inputJson // Retorna un JSON vacío en caso de error desconocido
        }   
    }
    
    private fun maskValues(jsonElement: JsonElement, maskChar: Char, maskLength: Int): JsonElement {
        return try {
            when {
                jsonElement.isJsonObject -> {
                    val jsonObject = jsonElement.asJsonObject
                    val maskedObject = JsonObject()
                    jsonObject.entrySet().forEach { (key, value) ->
                        try {
                            maskedObject.add(key, maskValues(value, maskChar, maskLength))
                        } catch (ex: Exception) {
                            println("Error masking key $key: ${ex.message}")
                            maskedObject.add(key, JsonPrimitive("Error"))
                        }
                    }
                    maskedObject
                }
                jsonElement.isJsonArray -> {
                    val jsonArray = jsonElement.asJsonArray
                    val maskedArray = JsonArray()
                    jsonArray.forEach { element ->
                        try {
                            maskedArray.add(maskValues(element, maskChar, maskLength))
                        } catch (ex: Exception) {
                            println("Error masking array element: ${ex.message}")
                            maskedArray.add(JsonPrimitive("Error"))
                        }
                    }
                    maskedArray
                }
                jsonElement.isJsonPrimitive -> {
                    val primitive = jsonElement.asJsonPrimitive
                    if (primitive.isString) {
                        try {
                            JsonPrimitive(maskString(primitive.asString, maskChar, maskLength))
                        } catch (ex: Exception) {
                            println("Error masking string value: ${ex.message}")
                            JsonPrimitive("Error")
                        }
                    } else {
                        jsonElement
                    }
                }
                else -> jsonElement
            }
        } catch (ex: Exception) {
            println("Error processing JSON element: ${ex.message}")
            JsonPrimitive("Error")
        }
    }
    
    private fun maskString(value: String, maskChar: Char, maskLength: Int): String {
            if(value.contains("**"))
            return value
        return try {
            if (value.length <= maskLength) {
                maskChar.toString().repeat(value.length)
            } else {
                value.take(maskLength) + maskChar.toString().repeat(value.length - maskLength)
            }
        } catch (ex: Exception) {
            println("Error masking string: ${ex.message}")
            "Error"
        }
    }
    fun maskString(input2: String): String {
        var input=input2.replace(" ","")
        if(input.contains("**"))
         return input 
        if(!AppSettings.getInstance().maskPCI){
            return input
        }

        val length = input.length
        // If the length is 4 or less, do not mask
        if (length <= 4) {
            return input
        } 

        // If the length is exactly 8, mask everything except the first 4 characters
        if (length == 8) {
            return input.substring(0, 4) + "*".repeat(4)
        }

        // For lengths greater than 4 and different from 8, mask all except the first 4 and the last
        // 4 characters
        if (length > 4) {
            val start = input.substring(0, 4)
            val end = input.substring(length - 4)
            val middle = "*".repeat(length - 8) // Mask the middle part

            return start + middle + end
        }

        // Default return (should never reach here due to the previous conditions)
        return input + "|" + length
    } 

    // Function to mask sensitive EMV tags in the map
    fun maskEMVTags(
        terminalSerialNumber:String,
            hostType: HostType,
            emvTagsMap: MutableMap<String, String>, watcher: Watcher
    ): MutableMap<String, String> {
         watcher.debug("maskEMVTags ${hostType.authorizer.rsaKeyId}")
         if (hostType.authorizer == Authorizer.BANORTE)  {
              watcher.debug("NO_MASK_maskEMVTags ${hostType.authorizer }")

                return emvTagsMap
         }
         if (hostType.authorizer.sendClearData) {
            return emvTagsMap
        }
        if (hostType.authorizer == Authorizer.YOLOPAGO ||hostType.authorizer == Authorizer.CLAROPAY||hostType.authorizer==Authorizer.PAYNET)  {
    
            if (bankRsaKey == null||bankRsaKey != null && bankRsaKey!!.errorCode != 0) {  
                val vaultDto= VaultDtos.RequestReadRSAData()  
                vaultDto.id= hostType.authorizer.rsaKeyId;
                vaultDto.hostType= hostType;
                vaultDto.terminalSerialNumber= terminalSerialNumber;
                watcher.debug("bankRsaKey_terminalSerialNumber : [${vaultDto.terminalSerialNumber }]")
                watcher.debug("bankRsaKey_id : [${vaultDto.id }]")
                watcher.debug("bankRsaKey_hostType : [${ vaultDto.hostType }]") 
  
                bankRsaKey=PandoraneoManager().readRsa(vaultDto)
                watcher.debug("PandoraneoManager_readRsae : [${bankRsaKey!!.errorCode }]")
 
               // bankRsaKey = BankRsaKey.getBankRsaKeyByHost(hostType, hostType.authorizer.rsaKeyId)
            } 
            watcher.debug("maskEMVTags_bankRsaKey_errorCode : [${bankRsaKey!!.errorCode }]")

            if (bankRsaKey != null && bankRsaKey!!.errorCode == 0) {
                watcher.debug("maskEMVTags_IDRSA: [${bankRsaKey!!.id}]") 
                watcher.debug("maskEMVTags_modulus: [${ bankRsaKey!!.modulus}]")
                watcher.debug("maskEMVTags_exponent: [${ bankRsaKey!!.exponent}]")
 
                for (key in sensitiveTags) {
                    if (key in sensitiveTags) { 
                        if (emvTagsMap[key] != null) {
                            watcher.debug("key=>$key emvTagsMap[${key}] : [${emvTagsMap[key]}]")
                            emvTagsMap[key] =
                                    RSAUtils.RSACipherStr(
                                            emvTagsMap[key]!!,
                                            bankRsaKey!!.modulus,
                                            bankRsaKey!!.exponent
                                    )
                        }
                    }
                }
            } else {

                for (key in sensitiveTags) {
                    if (key in sensitiveTags) {
                        if (emvTagsMap[key] != null) emvTagsMap[key] = maskString(emvTagsMap[key]!!)
                    }
                }
            }

            return emvTagsMap
        }
     
        for (key in sensitiveTags) {
            if (key in sensitiveTags) {
                if (emvTagsMap[key] != null) emvTagsMap[key] = maskString(emvTagsMap[key]!!)
            }
        }
        return emvTagsMap
    }
    fun removeEmvDataSensitive(hostType: HostType, emvData: String, watcher: Watcher): String {
        var emvResult = emvData
        watcher.debug( 
                "0_removeEmvDataSensitive: hostType [${hostType}] emvData(${emvData.length}) [$emvData] authorizer [${hostType.authorizer}]"
        )
     
        if (hostType.authorizer.sendClearData) {
            return emvResult
        } 
        
        else {
            for (key in sensitiveTags) {
                emvResult = removeEmvTagAndValue(emvResult, key)
            }
        }
        watcher.debug(
            "1_removeEmvDataSensitive: hostType [${hostType}] emvResult(${emvResult.length}) [$emvResult] authorizer [${hostType.authorizer}]"
         )
        return emvResult
    }



    fun formatAmountToCompare(amount: String, watcher: Watcher): String {
        var conversion = "0.00"
        try {
             if ( amount == "") {
                watcher.debug("conversion AMOUNT_EMPTY " )
                return ""
            }
            val amountLong = amount.toLong()
            if (amountLong == 0L || amount == "") {
                watcher.debug("conversion " + conversion)
                return conversion
            }
            conversion = amount.substring(0, 10).toInt().toString() + "." + amount.substring(10, 12)
            watcher.debug("conversion " + conversion)
            return conversion
        } catch (e: Exception) {
            watcher.debug("formatAmountToCompare EXCEPTION " + e.message)
            return "0.00"
        }
    }

    fun isLetters(chars: String): Boolean {
        return chars.all { it.isLetter() }
    }

    fun isLettersOrDigits(chars: String): Boolean {
        return chars.all { it.isLetterOrDigit() }
    }

    fun getNullPropertyNames(source: Any): Array<String> {
        val src: BeanWrapper = BeanWrapperImpl(source)
        val pds = src.propertyDescriptors
        val emptyNames = mutableListOf<String>()
        for (pd in pds) {
            val srcValue = src.getPropertyValue(pd.name)
            if (srcValue == null || srcValue.toString().isEmpty()) emptyNames.add(pd.name)
        }
        // val result = arrayOfNulls<String>(emptyNames.size)
        return emptyNames.toTypedArray()
    }

    private fun cleanDto(dto: TransactionDto): TransactionDto {
        val newDto = TransactionDto()
        StringUtils.copyNonEmptyProperties(dto, newDto)
        if (dto.transactionType == TransactionType.KeyLoad) {
            if (dto.hostType.authorizer == Authorizer.OPENPAY) {
                newDto.tokenEW = dto.tokenEW
            }
        }
        return newDto
    }

    fun bitsetFromByteArray(bytes: ByteArray): BitSet {
        val bits = BitSet()
        for (i in 0 until bytes.size * 8) {
            if (bytes[bytes.size - i / 8 - 1] and (1 shl i % 8).toByte() > 0) {
                bits.set(i)
            }
        }
        return bits
    }
    fun bitSetToString(byteArray: BitSet): String {

        var bitArrayS =
                "" // Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)
        var counter = 0
        for (i in 0 until byteArray.size()) {
            val bit = byteArray[i]
            if (bit) bitArrayS += "1" else bitArrayS += "0"
            counter++
            if (counter >= 8) {
                bitArrayS += " "
                counter = 0
            }
        }

        return bitArrayS
    }
    fun byteArrayToBitString(byteArray: ByteArray): String {

        var bitArrayS =
                "" // Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)

        for (i in byteArray) {
            bitArrayS += i.toUByte().toString(2).replace("-1", "1").padStart(8, '0') + " "
        }

        return bitArrayS
    }
    fun byteArrayToBitSet(byteArray: ByteArray): com.viergegroup.crypto.security.BitSet {
        val bits = com.viergegroup.crypto.security.BitSet(8 * byteArray.size)

        var bitArrayS =
                "" // Conversions.strToBcd(.hexToBinaryArrayInt(),Conversions.EPaddingPosition.PADDING_RIGHT)

        for (i in byteArray) {
            bitArrayS += i.toUByte().toString(2).replace("-1", "1").padStart(8, '0') + ""
        }
        var i = 0
        for (bit in bitArrayS) {
            if (bit.toString() == "1") {
                bits[i] = true
            }
            i++
        }
        return bits
        //  println(" bitArrayS "+bitArrayS)
    }
    /* fun andB(byte:Byte,byteReslt:Byte ):Boolean{
        return (byte.and(byteReslt) ) > 0
    }
    fun applySHL(counter:Int ):Byte{

        return (1L shl counter).toByte()
    }
    fun checkSHL(byte:Byte,counter:Int ):Boolean{
        val byteReslt=(1L shl counter ).toByte()
        return (byte and byteReslt ) > 0
    }*/

    fun toBitSet(byteArray: ByteArray): BitSet {
        val bs = BitSet(8 * byteArray.size)
        var counter = 0
        for (byte in byteArray) {
            for (j in 0..7) {
                if ((byte and (1 shl j).toByte()) > 0) {
                    // println("COUNTER: " + counter + " VALUE " + (8 * counter + (7 - j)))

                    bs.set(8 * counter + (7 - j))
                }
            }
            counter++
        }
        return bs
    }

    /*fun toByteArray(b: BitSet): ByteArray {
            val size = ceil(b.size() / 8.0).toInt()
            val value = ByteArray(size)
            for (i in 0 until size) {
                value[i] = Dukpt.toByte(b[i * 8, Math.min(b.size(), (i + 1) * 8)] )
            }
            return value
        }
    */

    fun isNullOrEmpty(str: String?): Boolean {
        return str == null || str == ""
    }
    private fun boolsToByte(array: BooleanArray, start: Int): Byte {
        var b: Byte = 0
        for (i in 0..7) {
            if (array[start + i]) b = b or ((1 shl 7 - i).toByte())
        }
        return b
    }
    fun convertBoolArrayToByteArray(boolArr: BooleanArray): ByteArray {
        val byteArr = ByteArray(boolArr.size / 8)
        for (i in byteArr.indices) byteArr[i] = boolsToByte(boolArr, 8 * i)
        return byteArr
    }

    fun encodeHexString(boolArr: BooleanArray): String {
        return encodeHexString(convertBoolArrayToByteArray(boolArr))
    }
    fun fromByteArray(data: ByteArray): String {
        val hexChars = CharArray(data.size * 2)
        for (j in data.indices) {
            val v: Byte = data[j] and 0xFF.toByte()
            hexChars[j * 2] = hexArray[v.toInt() ushr 4]
            hexChars[j * 2 + 1] = hexArray[(v and 0x0F).toInt()]
        }
        return String(hexChars)
    }

    fun asciiFromByteArray(data: ByteArray): String {
        try {
            return String(data)
        } catch (e: Exception) {
            return "ERROR_EN_FORMATO:${e.message}"
        }
    }

    // it's come from http://www.baeldung.com/java-convert-hex-to-ascii
    fun asciiToHex(asciiStr: String): String {
        val chars = asciiStr.toCharArray()
        val hex = StringBuilder()
        for (ch in chars) {
            hex.append(Integer.toHexString(ch.code))
        }
        return hex.toString()
    }

    // it's come from http://www.baeldung.com/java-convert-hex-to-ascii
    fun hexToAscii(hexStr: String): String {
        val output = StringBuilder("")
        var i = 0
        while (i < hexStr.length) {
            val str = hexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }


    /*fun stringToByteArray(input: String): ByteArray {
        return input.toByteArray(Charsets.US_ASCII)
    }*/
    
    /* fun asciiToHex(data: ByteArray): ByteArray {
        var hexChars: CharArray? = CharArray(data.size * 2)
        for (j in data.indices) {
            val v: Byte = data[j] and 0xFF.toByte()
            hexChars!![j * 2] = hexArray[v.toInt() ushr 4]
            hexChars[j * 2 + 1] = hexArray[(v and 0x0F).toInt()]
        }
        val res = ByteArray(hexChars!!.size)
        for (i in hexChars.indices) {
            res[i] = hexChars[i].toByte()
        }
        Arrays.fill(hexChars, '\u0000')
        hexChars = null
        return res
    }*/

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
    private val HEX_CHARS2 = "0123456789ABCDEF".toCharArray()
    private const val HEX_CHARS = "0123456789ABCDEF"

    fun String.hexString2ByteArray(): ByteArray {

        val result = ByteArray(length / 2)

        for (i in 0 until length step 2) {
            val firstIndex = HEX_CHARS2.indexOf(this[i])
            val secondIndex = HEX_CHARS2.indexOf(this[i + 1])

            val octet = firstIndex.shl(4).or(secondIndex)
            result.set(i.shr(1), octet.toByte())
        }

        return result
    }
    fun ByteArray.toHex(): String {
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }

    /*   fun hexStringToByteArray(s: String): ByteArray {
        check(s.length % 2 == 0) { "Must have an even length" }

        return s.hexString2ByteArray()
    }*/

    fun hexStringToByteArrayNeo(s: String): ByteArray {

        return s.decodeHex()
    }
    fun generateRandomByteArray(length: Int): ByteArray {
        require(length > 0) { "La longitud debe ser mayor que cero" }
        val byteArray = ByteArray(length)
        Random.nextBytes(byteArray)
        return byteArray
    }

    fun hexStringToByteArray(sParam: String): ByteArray {
        var s = sParam
        var len = s.length
        if (len % 2 != 0) {
            s = "0$s"
            len++
        }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] =
                    ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    /*  fun fromByteBuffer(readBuffer: ByteBuffer): String {
        return fromByteArray(Arrays.copyOfRange(readBuffer.array(), 0, readBuffer.position()))
    }*/

    fun lenStringToHex(str: String, numberOfBytes: Int = 1): String {
        return Integer.toHexString(str.length).padStart(numberOfBytes * 2, '0')
    }

    fun intToHexString(value: Int): String {
        var hs = Integer.toHexString(value)
        if (hs.length % 2 != 0) hs = "0$hs"
        hs = hs.uppercase(Locale.getDefault())
        return hs
    }

    fun asciiToByteArray(bytes: ByteArray): ByteArray {
        return hexStringToByteArray(hexToAscii(fromByteArray(bytes)))
    } 
    fun hexStringAsAsciiByteArray(input: String): ByteArray {
        return input.toByteArray(Charsets.US_ASCII)
    }
    fun toHexString(str: String): String {
        val sb = StringBuffer()
        for (i in 0 until str.length) {
            sb.append(toHexString(str[i]))
        }
        return sb.toString()
    }
        
    /**
     * convert into Hexadecimal notation of Unicode.<br></br> example)a?\u0061
     *
     * @param ch
     * @return
     */
    fun toHexString(ch: Char): String {
        var hex = Integer.toHexString(ch.code)
        while (hex.length < 4) {
            hex = "0$hex"
        }
        hex = "\\u$hex"
        return hex
    }

    fun stringToByteArray(cadena: String): ByteArray {
        val charset = Charsets.UTF_8 // Elige la codificación adecuada

        val byteArray = cadena.toByteArray(charset)
        return byteArray
    }

    fun byteArrayToStringHSM(byteArray: ByteArray, charset: Charset): String {
        return String(byteArray, charset)
    }

    fun byteArrayToStringHSM(byteArray: ByteArray): String {
        return byteArrayToStringHSM(
                byteArray,
                Charsets.UTF_8
        ) // Utiliza UTF-8 u otra codificación según tus necesidades
    }

    fun encodeHexString(byteArray: ByteArray): String {

        return byteArray.toHex()
    }

    fun datetime2String(dateTime: LocalDateTime): String {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateTime.format(formatter)
    }

    /*  fun encodeHexString(byteArray: ByteArray): String {
            val stringBuffer = StringBuffer()
            for (i in byteArray.indices) {
                stringBuffer.append(byteToHex(byteArray[i]))
            }
            return stringBuffer.toString().toUpperCase()
        }
    */

    fun byteToHex(num: Byte): String {
        val hexDigits = CharArray(2)
        hexDigits[0] = Character.forDigit(num.toInt() shr 4 and 0xF, 16)
        hexDigits[1] = Character.forDigit((num and 0xF).toInt(), 16)
        return String(hexDigits)
    }

    /*fun byteArrToIp(ipByte: ByteArray): String {
            val ip = encodeHexString(ipByte)
            return ip.substring(0, 3).toInt().toString() + "." + ip.substring(3, 6).toInt() + "." + ip.substring(6, 9)
                .toInt() + "." + ip.substring(9, 12).toInt()
        }
    */

    fun byteArrToBinaryString(value: ByteArray): String {
        val binaryString = StringBuilder(value.size - 1)
        for (i in value.indices) {
            binaryString.append(
                    String.format(
                                    "%8s",
                                    Integer.toBinaryString((value[i] and 0xFF.toByte()).toInt())
                            )
                            .replace(' ', '0')
            )
        }
        return binaryString.toString()
    }

    fun stringToDateTime(string: String?): Date {
        val format = SimpleDateFormat("yyMMddHHmmss", Locale.US)
        val date: Date =
                try {
                    format.parse(string)
                } catch (e: ParseException) {
                    return Date()
                }
        return date
    }

    fun strictStringToBytes(s: String, charset: Charset): ByteArray {
        val x =
                charset.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .encode(CharBuffer.wrap(s))
        val b = ByteArray(x.remaining())
        x[b]
        return b
    }

    fun cleanTextContent(text: String): String {
        // strips off all non-ASCII characters
        var newtext = text
        newtext = newtext.replace("[^\\x00-\\x7F]".toRegex(), "")

        // erases all the ASCII control characters
        newtext = newtext.replace("[\\p{Cntrl}&&[^\r\n\t]]".toRegex(), "")

        // removes non-printable characters from Unicode
        newtext = newtext.replace("\\p{C}".toRegex(), "")
        return newtext.trim { it <= ' ' }
    }

    fun asciiToHexByteArray(ascciiParam: String): ByteArray {
        var asccii = ascciiParam
        if (asccii.length % 2 == 1) {
            asccii += "F"
        }
        val bOutput = ByteArrayOutputStream()
        val lengtString = asccii.length
        var i = 0
        while (i < lengtString) {
            val num = asccii.substring(i, i + 2).toInt()
            try {
                bOutput.write(Integer.toHexString(num).uppercase(Locale.getDefault()).toByteArray())
            } catch (io: IOException) {
                WatcherGlobalLog.debug(io.toString())
            }
            i += 2
        }
        return bOutput.toByteArray()
    }
    fun asciiToHexByteArrayNeo(input: String): ByteArray {
        try {
            // Validar que la entrada sea ASCII válida
            if (!input.matches(Regex("^[\\x20-\\x7E]*$"))) {
                throw IllegalArgumentException("La cadena contiene caracteres no ASCII.")
            }
    
            // Convertir cada carácter ASCII a su representación hexadecimal y luego a bytes
            return input.toByteArray(Charsets.US_ASCII)
        } catch (e: Exception) {
            // Manejar cualquier excepción inesperada y relanzarla con contexto adicional
            throw RuntimeException("Error al convertir la cadena ASCII a bytes: ${e.message}", e)
        }
    }
    
    fun asciiToHexByteArray2(asciiParam: String): ByteArray {
        var ascii = asciiParam.toUpperCase()
        if (ascii.length % 2 == 1) {
            ascii += "0"
        }
        
        val byteArray = ByteArray(ascii.length / 2)
        for (i in ascii.indices step 2) {
            val hexString = ascii.substring(i, i + 2)
            byteArray[i / 2] = hexString.toInt(16).toByte()
        }
        return byteArray
    }
    
    /* fun formatField62(field62: String): Map<String, String> {
            var length = field62.substring(0, 4).toInt() - 2
            val obj: MutableMap<String, String> = HashMap()
            obj[hexToAscii(field62.substring(4, 8))] = hexToAscii(field62.substring(8, 8 + length * 2))
            if (field62.length == 8 + length * 2) {
                return obj
            }
            length = field62.substring(8 + length * 2, 12 + length * 2).toInt() - 2
            obj[hexToAscii(field62.substring(12 + length * 2, 16 + length * 2))] =
                hexToAscii(field62.substring(16 + length * 2, 16 + length * 2 + length * 2))
            if (field62.length == 16 + length * 2 + length * 2) {
                return obj
            }
            length = field62.substring(16 + length * 2 + length * 2, 20 + length * 2 + length * 2).toInt() - 2
            obj[hexToAscii(field62.substring(20 + length * 2 + length * 2, 24 + length * 2 + length * 2))] =
                hexToAscii(field62.substring(20 + length * 2 + length * 2, 20 + length * 2 + length * 2 + length * 2))
            if (field62.length == 20 + length * 2 + length * 2 + length * 2) {
                return obj
            }
            length = field62.substring(20 + length * 2 + length * 2 + length * 2, 24 + length * 2 + length * 2 + length * 2)
                .toInt() - 2
            obj[hexToAscii(
                field62.substring(
                    24 + length * 2 + length * 2 + length * 2,
                    24 + length * 2 + length * 2 + length * 2 + length * 2
                )
            )] =
                hexToAscii(
                    field62.substring(
                        24 + length * 2 + length * 2 + length * 2 + length * 2,
                        24 + length * 2 + length * 2 + length * 2 + length * 2 + length * 2
                    )
                )
            return obj
        }

    */

    private const val PAD_LIMIT = 8192

    fun rightPad(str: String, size: Int): String {
        return rightPad(str, size, ' ')
    }

    /**
     * Right pad a String with a specified character.
     *
     * The String is padded to the size of `size`.
     *
     * <pre> StringUtils.rightPad(null, *, *) = null StringUtils.rightPad("", 3, 'z') = "zzz"
     * StringUtils.rightPad("bat", 3, 'z') = "bat" StringUtils.rightPad("bat", 5, 'z') = "batzz"
     * StringUtils.rightPad("bat", 1, 'z') = "bat" StringUtils.rightPad("bat", -1, 'z') = "bat"
     * </pre> *
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @param padChar the character to pad with
     * @return right padded String or original String if no padding is necessary, `null` if null
     * String input
     * @since 2.0
     */
    fun rightPad(str: String, size: Int, padChar: Char): String {

        val pads = size - str.length
        if (pads <= 0) {
            return str // returns original String when possible
        }
        return if (pads > PAD_LIMIT) {
            rightPad(str, size, padChar.toString())
        } else str + padding(pads, padChar)
    }

    /* @Throws(IllegalArgumentException::class)
    fun bytesToString(b: ByteArray?): String {
        requireNotNull(b) {
            // Log.e(TAG, "bcdToStr input arg is null");
            "bcdToStr input arg is null"
        }
        val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val sb = StringBuilder(b.size * 2)
        for (i in b.indices) {
            sb.append(HEX_DIGITS[(b[i] and 0xf0.toByte()).toInt() ushr 4])
            sb.append(HEX_DIGITS[(b[i] and 0x0f).toInt()])
        }
        return sb.toString()
    }*/

    /**
     * Right pad a String with a specified String.
     *
     * The String is padded to the size of `size`.
     *
     * <pre> StringUtils.rightPad(null, *, *) = null StringUtils.rightPad("", 3, "z") = "zzz"
     * StringUtils.rightPad("bat", 3, "yz") = "bat" StringUtils.rightPad("bat", 5, "yz") = "batyz"
     * StringUtils.rightPad("bat", 8, "yz") = "batyzyzy" StringUtils.rightPad("bat", 1, "yz") =
     * "bat" StringUtils.rightPad("bat", -1, "yz") = "bat" StringUtils.rightPad("bat", 5, null) =
     * "bat " StringUtils.rightPad("bat", 5, "") = "bat " </pre> *
     *
     * @param str the String to pad out, may be null
     * @param size the size to pad to
     * @param padStrParam the String to pad with, null or empty treated as single space
     * @return right padded String or original String if no padding is necessary, `null` if null
     * String input
     */
    fun rightPad(str: String?, size: Int, padStrParam: String): String {
        var padStr = padStrParam
        if (str == null) {
            return ""
        }
        if (isEmpty(padStr)) {
            padStr = " "
        }
        val padLen = padStr.length
        val strLen = str.length
        val pads = size - strLen
        if (pads <= 0) {
            return str // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr[0])
        }
        return if (pads == padLen) {
            str + padStr
        } else if (pads < padLen) {
            str + padStr.substring(0, pads)
        } else {
            val padding = CharArray(pads)
            val padChars = padStr.toCharArray()
            for (i in 0 until pads) {
                padding[i] = padChars[i % padLen]
            }
            str + String(padding)
        }
    }

    fun leftPad(strParam: String, size: Int, padStr: String): String {
        var str = strParam
        val total = size - str.length
        for (i in 0 until total) {
            str = padStr + str
        }
        return str
    }

    @Throws(IndexOutOfBoundsException::class)
    private fun padding(repeat: Int, padChar: Char): String {
        if (repeat < 0) {
            throw IndexOutOfBoundsException("Cannot pad a negative amount: $repeat")
        }
        val buf = CharArray(repeat)
        for (i in buf.indices) {
            buf[i] = padChar
        }
        return String(buf)
    }

    // Empty checks
    // -----------------------------------------------------------------------
    // Empty checks
    // -----------------------------------------------------------------------
    /**
     * Checks if a String is empty ("") or null.
     *
     * <pre> StringUtils.isEmpty(null) = true StringUtils.isEmpty("") = true StringUtils.isEmpty("
     * ") = false StringUtils.isEmpty("bob") = false StringUtils.isEmpty(" bob ") = false </pre> *
     *
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That
     * functionality is available in isBlank().
     *
     * @param str the String to check, may be null
     * @return `true` if the String is empty or null
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }
}
