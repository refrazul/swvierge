package com.viergegroup.swmain.websecurity.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.viergegroup.swmain.websecurity.model.ErrorMessage
import com.viergegroup.swmain.websecurity.model.RestErrorList
import org.apache.http.HttpStatus.SC_FORBIDDEN
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper
import  com.viergegroup.swmain.websecurity.model.ResponseWrapper
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog

class RestAccessDeniedHandler : AccessDeniedHandler{
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?
    ) {

        WatcherGlobalLog.info("RestAccessDeniedHandler.createExceptionResponse")

        val errorList = RestErrorList(SC_FORBIDDEN, ErrorMessage(accessDeniedException!!.message))
        val responseWrapper =
            ResponseWrapper(null, Collections.singletonMap("status", SC_FORBIDDEN), errorList)
        val objMapper = ObjectMapper()

        val wrapper = HttpServletResponseWrapper(response)
        wrapper.status = SC_FORBIDDEN
        wrapper.contentType = MediaType.APPLICATION_JSON_VALUE
        wrapper.writer.println(objMapper.writeValueAsString(responseWrapper))
        wrapper.writer.flush()
    }


}