package com.viergegroup.swmain.websecurity.common

import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse


object CorsHelper {
    fun addResponseHeaders(res: ServletResponse): HttpServletResponse {
        val httpResponse = res as HttpServletResponse
        httpResponse.setHeader("Access-Control-Allow-Origin", "*")
        httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
        httpResponse.setHeader("Access-Control-Max-Age", "3600")
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true")
        httpResponse.setHeader("Access-Control-Allow-Headers", "content-type,Authorization")
        return httpResponse
    }
}