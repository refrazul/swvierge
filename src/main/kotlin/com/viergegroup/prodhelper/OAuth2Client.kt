package com.viergegroup.prodhelper

import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.boot.web.client.RestTemplateBuilder


object OAuth2Client{
    private val tokenUrl: String="https://viergegroup.auth.us-east-1.amazoncognito.com/oauth2/token"
    private val clientId: String="3sa13618lfn4os8ad5bqbnei20"
    private val clientSecret: String="6a494gv7im93p9ad44dq4idjc0cbbh87pmjng4q9m9fsrfcd569"

    private val restTemplate: RestTemplate = RestTemplateBuilder().build()

    fun getAccessToken(): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val body = "grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"
        val entity = HttpEntity(body, headers)

        val response: ResponseEntity<OAuth2TokenResponse> = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            entity,
            OAuth2TokenResponse::class.java
        )

        return response.body?.access_token ?: throw RuntimeException("Failed to retrieve access token")
    }

    data class OAuth2TokenResponse(val access_token: String)
}