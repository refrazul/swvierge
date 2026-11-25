package com.viergegroup.swmain.websecurity.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.viergegroup.swmain.websecurity.model.ErrorMessage
import com.viergegroup.swmain.websecurity.model.ResponseWrapper
import com.viergegroup.swmain.websecurity.model.RestErrorList
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class SecurityAuthenticationEntryPoint : AuthenticationEntryPoint{


    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        WatcherGlobalLog.info("SecurityAuthenticationEntryPoint.commence")

        val errorList = RestErrorList(
            HttpServletResponse.SC_UNAUTHORIZED, ErrorMessage(
                authException!!.message
            )
        )
        val responseWrapper =
            ResponseWrapper(null, Collections.singletonMap("status", HttpServletResponse.SC_UNAUTHORIZED), errorList)
        val objMapper = ObjectMapper()

        val wrapper = HttpServletResponseWrapper(response)
        wrapper.status = HttpServletResponse.SC_UNAUTHORIZED
        wrapper.contentType = MediaType.APPLICATION_JSON_VALUE
        wrapper.writer.println(objMapper.writeValueAsString(responseWrapper))
        wrapper.writer.flush()

    }
}