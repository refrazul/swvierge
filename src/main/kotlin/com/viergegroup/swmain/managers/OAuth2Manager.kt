package com.viergegroup.swmain.managers

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date
import com.viergegroup.swmain.webwatcher.Watcher
import  com.shared.commons.StringUtils

class TokenResponse {
    var token: String? = null
    var expiry: Date? = null
    var error: String? = null
}

object OAuth2Manager {
    private var TOKEN_URL = StringUtils.destransformPassword("TFg3NzAxbWp5dHgvL21hemp0bHJsaHR0dW5mbHloLnR4ZmpmeDYteS5mci1lenNtdHpzLnl6Lnd0ai93Zm55Lzc6eXVweXNEWDc3MDE=")
        private var CLIENT_ID = StringUtils.destransformPassword("TFg3NzAxMm55OTZ3aWg4Z2dxdjJtczF0bGxhMzVndG1EWDc3MDE=")
        private var CLIENT_SECRET = StringUtils.destransformPassword("TFg3NzAxdGFrZmx5YWhhM3hsbGkycG91bTBhNDRsNGpxaWFydHo4NDN1Nm8yNzUycWo5a283b25wRFg3NzAx")
  
    private val TAG = "GatewayHelper"

    // --- Cache compartido del token (1 por JVM, reusado hasta ~1 min antes de expirar) ---
    @Volatile private var cachedToken: String = ""
    @Volatile private var expiryEpochMs: Long = 0L
    private val tokenLock = Any()
    private const val REFRESH_MARGIN_MS = 60_000L   // renovar 60s antes de expirar

    fun getValidToken(watcher: Watcher): String {
        val now = System.currentTimeMillis()
        // Fast path: token vigente, sin lock
        if (cachedToken != "" && now < expiryEpochMs - REFRESH_MARGIN_MS) {
            return cachedToken
        }
        // Slow path: renovar UNA sola vez (double-checked locking)
        synchronized(tokenLock) {
            val now2 = System.currentTimeMillis()
            if (cachedToken != "" && now2 < expiryEpochMs - REFRESH_MARGIN_MS) {
                return cachedToken   // otro hilo ya lo renovó mientras esperábamos
            }
            watcher.debug("$TAG: token expirado/ausente -> generando nuevo (compartido)")
            val resp = generateToken(watcher)
            if (resp.token != null) {
                cachedToken = resp.token!!
                expiryEpochMs = resp.expiry?.time ?: (now2 + 3_600_000L) // fallback 1h
            }
            // si falló, NO piso el token bueno anterior; devuelvo lo que haya
            return cachedToken
        }
    }

    fun generateToken(watcher:Watcher): TokenResponse {
        val responseObj = TokenResponse()
        watcher.debug("$TAG: gatewayResponse_INIT")
        try {
            val url = URL(TOKEN_URL)
            val postData = "grant_type=client_credentials" +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8.toString()) +
                    "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8.toString())

            watcher.debug("$TAG: generateToken_URL $TOKEN_URL")
            watcher.debug("$TAG: generateToken_postData prepared $postData")

            // Abrir la conexión HTTP
            with(url.openConnection() as HttpURLConnection) {
                connectTimeout = 5000  // 5 segundos de tiempo de espera para conectar
                readTimeout = 5000     // 5 segundos de tiempo de espera para leer
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
                doOutput = true

                // Escribir datos en el cuerpo de la solicitud
                val outputStream = outputStream
                val input = postData.toByteArray(StandardCharsets.UTF_8)
                outputStream.write(input, 0, input.size)
                outputStream.close()

                // Leer la respuesta del servidor
                val responseCode = responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    var responseLine: String?
                    while (bufferedReader.readLine().also { responseLine = it } != null) {
                        response.append(responseLine!!.trim())
                    }
                    bufferedReader.close()

                    watcher.debug("$TAG: Response received from server: $response")

                    // Parsear la respuesta JSON
                    val jsonObject = Gson().fromJson(response.toString(), JsonObject::class.java)
                    val token = jsonObject.get("access_token").asString
                    val expiresIn = jsonObject.get("expires_in").asInt
                    val expiryTimeMillis = System.currentTimeMillis() + expiresIn * 1000L
                    val expiryDate = Date(expiryTimeMillis)

                    watcher.debug("$TAG: Token parsed from response. Expires in: ${expiresIn} seconds")

                    responseObj.token = token
                    responseObj.expiry = expiryDate
                } else {
                    watcher.debug("$TAG: UNSUCCESS HTTP RESPONSE: $responseCode")
                    responseObj.error = "Error: Unexpected response code $responseCode"
                }
            }
        } catch (e: Exception) {
            responseObj.error = "Error: ${e.message}"
            watcher.debug("$TAG: generateToken_Exception occurred: ${e.message}")
        }

        watcher.debug("$TAG: generateToken function completed")
        return responseObj
    }
}
