package com.viergegroup.swmain

import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoggingInterceptor : HandlerInterceptor {
    private val logger: Logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val json = request.reader.lines().reduce("") { acc, line -> acc + line }
      //  logger.info("JSON: $json")
        //logger.info("Request Name: ${request.requestURI}")
        WatcherGlobalLog.info("Request Name: ${request.requestURI}")

        // Puedes realizar acciones adicionales aquí antes de que se ejecute el método del controlador

        return true
    }

    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any, modelAndView: ModelAndView?) {
        // Puedes realizar acciones adicionales aquí después de que se ejecute el método del controlador
    }
}
