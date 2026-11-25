package com.viergegroup.swmain.websecurity.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.proc.BadJOSEException
import com.viergegroup.swmain.controllers.ExceptionController
import com.viergegroup.swmain.dtos.errors.ExceptionResponse
import com.viergegroup.swmain.websecurity.common.CorsHelper
import com.viergegroup.swmain.websecurity.model.api.CognitoException
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

@Component
class AwsCognitoJwtAuthenticationFilter: OncePerRequestFilter {


    companion object{
        private val ERROR_OCCURED_WHILE_PROCESSING_THE_TOKEN = "Error occured while processing the token"
        private val INVALID_TOKEN_MESSAGE = "Invalid Token"

        private val classLogger = LoggerFactory.getLogger(AwsCognitoJwtAuthenticationFilter::class.java)

    }

    private lateinit var awsCognitoIdTokenProcessor: AwsCognitoIdTokenProcessor

    @Autowired
    private lateinit var appContext: ApplicationContext
    constructor()

    constructor(awsCognitoIdTokenProcessor: AwsCognitoIdTokenProcessor) {
        this.awsCognitoIdTokenProcessor = awsCognitoIdTokenProcessor
    }

    @Throws(IOException::class)
    private fun createExceptionResponse(
        request: ServletRequest,
        response: ServletResponse,
        exception: CognitoException
    ) {
        val req = request as HttpServletRequest
        var exceptionController: ExceptionController? = null
        val objMapper = ObjectMapper()
        try {
            exceptionController = appContext.getBean(ExceptionController::class.java)
            val exceptionResponse: ExceptionResponse = exceptionController.handleJwtException(req, exception).body!!
            val httpResponse: HttpServletResponse = CorsHelper.addResponseHeaders(response)
            val wrapper = HttpServletResponseWrapper(httpResponse)
            val uri = req.requestURI
            if (uri == "/users/check") {
                wrapper.status = HttpStatus.OK.value()
            }  
            if (uri == "/sw/restart") {
                wrapper.status = HttpStatus.OK.value()
            }
            if (uri == "/sw/reboot") {    
                wrapper.status = HttpStatus.OK.value() 
            } 
            else {
                wrapper.status = HttpStatus.UNAUTHORIZED.value()
            }
            wrapper.contentType = MediaType.APPLICATION_JSON_VALUE
            wrapper.writer.println(objMapper.writeValueAsString(exceptionResponse))
            wrapper.writer.flush()
        } catch (e: Exception) {
            WatcherGlobalLog.error("EXCEPTION: ${e.message}")
        }
    }




    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var authentication: Authentication? = null
        try {

            authentication = awsCognitoIdTokenProcessor.getAuthentication(request)

            SecurityContextHolder.getContext().authentication = authentication

        } catch (e: BadJOSEException) {
            SecurityContextHolder.clearContext()
            WatcherGlobalLog.error("EXCEPTION TOKEN: ${e.message}")
       //     classLogger.error(e.message)
            createExceptionResponse(
                request,
                response,
                CognitoException(INVALID_TOKEN_MESSAGE, CognitoException.INVALID_TOKEN_EXCEPTION_CODE, e.message!!)
            )
            return
        } catch (e: CognitoException) {
            SecurityContextHolder.clearContext()
           // println("EXCEPTION TOKEN2: ${e.message}")

            createExceptionResponse(
                request,
                response,
                CognitoException(
                    e.errorMessage,
                    CognitoException.INVALID_TOKEN_EXCEPTION_CODE,
                    e.detailErrorMessage
                )
            )
            return
        } catch (e: Exception) {
            SecurityContextHolder.clearContext()
            //println("EXCEPTION TOKEN3: ${e.message}")
            createExceptionResponse(
                request,
                response,
                CognitoException(
                    ERROR_OCCURED_WHILE_PROCESSING_THE_TOKEN,
                    CognitoException.INVALID_TOKEN_EXCEPTION_CODE,
                    e.message!!
                )
            )
            return
        }

        filterChain.doFilter(request, response)
    }


}