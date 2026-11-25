package com.viergegroup.evopayment.managers

import com.google.gson.GsonBuilder
import com.shared.commons.*
import com.viergegroup.crypto.pandora.managers.VaultDtos.*
import com.viergegroup.crypto.pandora.managers.PandoraManager
import com.viergegroup.crypto.pandora.managers.VaultDocumentManager
import com.viergegroup.crypto.pandora.managers.VaultSecureManager
import com.viergegroup.crypto.security.* 
import com.viergegroup.evopayment.dtos.*

import com.viergegroup.swmain.dtosgsonadapters.*
import com.viergegroup.swmain.webwatcher.WatcherContext
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.*
import java.util.Base64
import java.lang.System.currentTimeMillis

/**
 * Cliente HTTP que se comunica con el gateway Evo para enviar transacciones y recibir respuestas.
 * Implementa lógica de retry, bypass de SSL para entornos de prueba y logeo detallado.
 */
class EvoHttpClient {
    data class TokenResponse(
        val access_token: String,
        val scope: String,
        val token_type: String,
        val expires_in: Int
    )
    // URL base del gateway (configurable desde AppSettings)
    var baseUrl: String = AppSettings.getInstance().evoUrl
  
    // Token estático en caso de no usar Watcher
    var authToken: String = ""

    // Token Bearer (opcional, puede ser configurado externamente)
    private var authTokenBearer: String = ""

    // Instancia de logger
    private val watcher = WatcherContext.getInstance()
    private var tokenExpirationTimestamp:Long=0L
    private val prefixURL="SinMutual/5.6.9"
    // Serializador Gson para logs legibles
    private val gsonPrint = GsonBuilder()
    
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        .setPrettyPrinting()
        .create()

    // Serializador Gson para llamadas reales a la API
    private val gsonAPI = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        .setPrettyPrinting()
        .create()

    init {
        WatcherGlobalLog.info("evoUrl: $baseUrl")
    }
  fun doConfirmTxn(dto: EvoConfirmRequestDto): EvoConfirmResponseDto {
        watcher.debug("doConfirmTxn invoked")
 
        val result = doApiRequest(
            endpoint = "$prefixURL/Confirm",
            methodType = "POST",
            data = dto,
            responseType = EvoConfirmResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doReturnReverse SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doReturnReverse ERROR: ${result.error}")
        throw EvoException("doReturnReverse ERROR: ${result.error}")
    }
    /**
     * Clase interna para encapsular respuestas de la API.
     */
    data class Response<T>(val data: T?, val error: String?)
         
    fun getToken(): String {
        val tokenUrl = "${baseUrl}/token?grant_type=client_credentials"
        val clientId = AppSettings.getInstance().evoClientId
        val clientSecret = AppSettings.getInstance().evoClientSecret
        val basicAuth = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

        watcher.debug("Getting token from $tokenUrl")

        val url = URL(tokenUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Basic $basicAuth")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connectTimeout = 45000
            readTimeout = 45000
            doOutput = false
        }

        val responseCode = connection.responseCode
        val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val error = connection.errorStream?.bufferedReader()?.readText()
            watcher.error("Token ERROR [$responseCode]: $error")
            throw EvoException("Token request failed with code $responseCode")
        }

        watcher.debug("Token response: $responseText")
        val tokenResponse = gsonAPI.fromJson(responseText, TokenResponse::class.java)

        // Actualiza token y expiración
        authTokenBearer = "Bearer ${tokenResponse.access_token}"
        tokenExpirationTimestamp = currentTimeMillis() + (tokenResponse.expires_in - 60) * 1000 // margen de 60s

        return authTokenBearer 
    }
    private fun ensureValidToken() {
        if (authTokenBearer.isBlank() || currentTimeMillis() >= tokenExpirationTimestamp) {
            watcher.debug("Token expired or missing. Requesting new token.")
            getToken()
        }
    }

       /**
     * Envia una transacción tipo venta al gateway.
     */
    fun doKeepAlive(dto: EvoKeepAliveDto): EvoKeepAliveResponseDto {
        watcher.debug("doSale invoked")

        val result = doApiRequest(
            endpoint = "$prefixURL/Sale",
            methodType = "POST",
            data = dto,
            responseType = EvoKeepAliveResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doSale SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doSale ERROR: ${result.error}")
        throw EvoException("doSale ERROR: ${result.error}")
    }



    /**
     * Envia una transacción tipo venta al gateway.
     */
    fun doSale(dto: EvoSaleRequestDto): EvoPaymentResponseDto {
        watcher.debug("doSale invoked")

        val result = doApiRequest(
            endpoint = "$prefixURL/Sale",
            methodType = "POST",
            data = dto,
            responseType = EvoPaymentResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doSale SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doSale ERROR: ${result.error}")
        throw EvoException("doSale ERROR: ${result.error}")
    }



    /**
     * Envia una transacción tipo venta con propina al gateway.
     */
    fun doSaleTip(dto: EvoSaleTipRequestDto): EvoPaymentResponseDto {
        watcher.debug("doSaleTip invoked")

        val result = doApiRequest(
            endpoint = "$prefixURL/Sale",
            methodType = "POST",
            data = dto,
            responseType = EvoPaymentResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doSaleTip SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doSaleTip ERROR: ${result.error}")
        throw EvoException("doSaleTip ERROR: ${result.error}")
    }


    /**
     * Envia una transacción tipo venta con propina al gateway.
     */
    fun doVoid(dto: EvoVoidRequestDto): EvoVoidResponseDto {
        watcher.debug("doVoid invoked")

        val result = doApiRequest( 
            endpoint = "$prefixURL/Void",
            methodType = "POST",
            data = dto,
            responseType = EvoVoidResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doVoid SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doVoid ERROR: ${result.error}")
        throw EvoException("doVoid ERROR: ${result.error}")
    }


    /**
     * Envia una transacción tipo reverso al gateway.
     */
    fun doReverse(dto: EvoSaleReverseRequestDto): EvoPaymentResponseDto {
        watcher.debug("doSale invoked")
 
        val result = doApiRequest(
            endpoint = "$prefixURL/Sale",
            methodType = "POST",
            data = dto,
            responseType = EvoPaymentResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doSale SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doSale ERROR: ${result.error}")
        throw EvoException("doSale ERROR: ${result.error}")
    }
  

  /**
     * Envia una transacción tipo reverso al gateway.
     */
    fun doReturnReverse(dto: EvoReturnReverseRequestDto): EvoReturnResponseDto {
        watcher.debug("doReturnReverse invoked")
 
        val result = doApiRequest(
            endpoint = "$prefixURL/Return",
            methodType = "POST",
            data = dto,
            responseType = EvoReturnResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doReturnReverse SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doReturnReverse ERROR: ${result.error}")
        throw EvoException("doReturnReverse ERROR: ${result.error}")
    }


    /**
     * Envia una transacción tipo devolucion al gateway.
     */
    fun doReturn(dto: EvoReturnRequestDto): EvoReturnResponseDto {
        watcher.debug("doReturn invoked")
 
        val result = doApiRequest(
            endpoint = "$prefixURL/Return",
            methodType = "POST",
            data = dto,
            responseType = EvoReturnResponseDto::class.java
        )

        result.data?.let {
            watcher.debug("doReturn SUCCESS: ${gsonPrint.toJson(it)}")
            return it
        }

        watcher.error("doReturn ERROR: ${result.error}")
        throw EvoException("doReturn ERROR: ${result.error}")
    }


     
    /**
     * Ejecuta una petición genérica a la API del gateway.
     */
    fun <T> doApiRequest(endpoint: String, methodType: String, data: Any, responseType: Class<T>): Response<T> {
        val url = URL("$baseUrl/$endpoint")
        val useHttps = baseUrl.startsWith("https")
        ensureValidToken()
        watcher.debug("API Request URL: $url")

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })

        val maxRetries = 1 
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                val authHeader =authTokenBearer

                val connection = if (useHttps) {
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
                    (url.openConnection() as HttpsURLConnection).apply {
                        sslSocketFactory = sslContext.socketFactory
                        hostnameVerifier = HostnameVerifier { _, _ -> true }
                    }
                } else {
                    url.openConnection() as HttpURLConnection
                }

                connection.requestMethod = methodType
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", authHeader)
                connection.connectTimeout = 45000
                connection.readTimeout = 45000
                connection.doOutput = methodType != "GET"

                if (methodType != "GET") {
                    val jsonInput = gsonAPI.toJson(data)
                    OutputStreamWriter(connection.outputStream).use { it.write(jsonInput) }
                    watcher.debug("JSON_REQUEST: ${gsonPrint.toJson(data)}")
                }

                val code = connection.responseCode
                val responseJson = when (code) {
                    HttpURLConnection.HTTP_OK,
                    HttpURLConnection.HTTP_CREATED -> BufferedReader(InputStreamReader(connection.inputStream)).use { it.readLine() }

                    HttpURLConnection.HTTP_NO_CONTENT -> return Response(data = null, error = null)

                    else -> throw Exception("HTTP error: $code")
                }

                watcher.debug("JSON_RESPONSE: $responseJson")
                val parsed = gsonAPI.fromJson(responseJson, responseType)
                connection.disconnect()
                return Response(data = parsed, error = null)

            } catch (e: Exception) {
                lastException = e
                watcher.error("Attempt ${attempt + 1} failed: ${e.message}")
                attempt++
                if (attempt < maxRetries) Thread.sleep(1000)
            }
        }

        watcher.error("All $maxRetries attempts failed. Last error: ${lastException?.message}")
        return Response(data = null, error = "ERROR: ${lastException?.message}")
    }
}
