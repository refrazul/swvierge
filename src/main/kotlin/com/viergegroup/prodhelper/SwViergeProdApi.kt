package com.viergegroup.prodhelper


import com.google.gson.GsonBuilder
import com.shared.commons.*

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object SwViergeProdApi {
    var baseUrl = "https://switchvgprod.viergegroup.com"
   //var authToken = OAuth2Client.getAccessToken()
    private val gsonPrint = GsonBuilder()
        .setPrettyPrinting().create()

    private val gsonAPI = GsonBuilder()
        .setPrettyPrinting().create()



    fun <T> doApiRequest(endpoint: String, authToken:String, methodType: String, data: Any, responseType: Class<T>): Response<T> {
        val url = URL("$baseUrl/$endpoint")
        val protocolHttpps = (baseUrl.startsWith("https"))

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })

        if (protocolHttpps) {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            val connection = url.openConnection() as HttpsURLConnection
            connection.sslSocketFactory = sslContext.socketFactory
            connection.hostnameVerifier = HostnameVerifier { _, _ -> true } 
            connection.requestMethod = methodType
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", authToken)
            connection.doOutput = true
            println("URL:$baseUrl/$endpoint  TOKEN:  $authToken")
            return try {
                if (methodType != "GET") {
                    val jsonInputString = gsonAPI.toJson(data)
                    val wr = OutputStreamWriter(connection.outputStream)
                    wr.write(jsonInputString)
                    wr.flush()
                    val jsonInputStringPrint = gsonPrint.toJson(data)
                    println("PASS_TRUE: JSON REQUEST: ${jsonInputStringPrint}")
                }

                val responseCode = connection.responseCode

                 if (responseCode == HttpURLConnection.HTTP_OK ) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonstr = reader.readLine()
                    println("PASS_TRUE: JSON RESPONSE: ${jsonstr}")
                    try {
                        val response: T = gsonAPI.fromJson(jsonstr, responseType)
                        return Response(data = response, error = null)
                    }catch (ex:Exception){
                        println("PASS_TRUE EXCEPTION: ${ex.message}")

                    }
                 } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    return Response(data = null, error = null)
                } else {
                    println("PASS_TRUE ERROR: $responseCode")
                    throw Exception("PASS_TRUE ERROR: $responseCode")
                }
                return Response(data = null, error = null)

            } catch (e: Exception) {
                println("PASS_TRUE ERROR: ${e.message}")
                Response(data = null, error = "PASS_TRUE ERROR: ${e.message}")
            } finally {
                connection.disconnect()
            }
        } else {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = methodType
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", authToken)
            connection.doOutput = true

            return try {
                if (methodType != "GET") {
                    val jsonInputString = gsonAPI.toJson(data)
                    val wr = OutputStreamWriter(connection.outputStream)
                    wr.write(jsonInputString)
                    wr.flush()
                    val jsonInputStringPrint = gsonPrint.toJson(data)

                    println("PASS_TRUE JSON REQUEST: ${jsonInputStringPrint}")
                }

                val responseCode = connection.responseCode

                val response: T = if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonstr = reader.readLine()
                    println(" PASS_TRUE JSON RESPONSE: ${jsonstr}")
                    gsonAPI.fromJson(jsonstr, responseType)
                } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    return Response(data = null, error = null)
                } else {
                    throw Exception("Error al obtener la respuesta. Código de estado: $responseCode")
                }

                Response(data = response, error = null)
            } catch (e: Exception) {
                println("PASS_TRUE ERROR ${e.message}")
                Response(data = null, error = "PASS_TRUE ERROR: ${e.message}")
            } finally {
                connection.disconnect()
            }
        }
    }
}

data class Response<T>(val data: T?, val error: String?)