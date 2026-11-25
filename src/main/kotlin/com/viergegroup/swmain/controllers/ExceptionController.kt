package com.viergegroup.swmain.controllers

import com.shared.commons.ErrorCode
import com.shared.commons.WebServiceException
import com.viergegroup.swmain.exceptions.DBKeyDoesntExists
import com.viergegroup.swmain.exceptions.ErrorReadingDBKey
import com.viergegroup.swmain.dbmodels.LogsMetaData
import com.viergegroup.swmain.dtos.errors.ExceptionResponse
import com.viergegroup.swmain.websecurity.model.api.CognitoException
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
/*
import com.viergegroup.swmain.websecurity.model.ErrorMessage
import com.viergegroup.swmain.websecurity.model.ResponseWrapper
import com.viergegroup.swmain.websecurity.model.RestErrorList
import com.viergegroup.swmain.websecurity.model.api.CognitoException
*/
import com.viergegroup.swmain.managers.AlarmEmailService
import com.viergegroup.swmain.managers.AlarmCause
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.persistence.EntityNotFoundException
import javax.servlet.http.HttpServletRequest

import java.io.PrintWriter
import java.io.StringWriter

@ControllerAdvice
@EnableWebMvc
class ExceptionController: ResponseEntityExceptionHandler() {


    fun saveErrorData(dto: ExceptionResponse){
        try{
            val data=LogsMetaData()
            BeanUtils.copyProperties(dto,data)
           // transactionsDBService.insertErrorData(errorData = data)
        }catch (ex:Exception){
            WatcherGlobalLog.error("ERROR SAVING ERROR DATA IN ExceptionController ${ex.message}")

        }
 
    }

    @ExceptionHandler(WebServiceException::class)
    fun handleWebServiceException(ex: WebServiceException, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{

        val errorResponseDto= ExceptionResponse()
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode= ex.errorCode
      

            // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleWebServiceException "+
            ex.errorMessage +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString


        saveErrorData(errorResponseDto)

        AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)

        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }
   
    @ExceptionHandler(DBKeyDoesntExists::class)
    fun handleDbException(ex: DBKeyDoesntExists, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{

        val errorResponseDto= ExceptionResponse(
        )
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode= ErrorCode.VaultError.errorCodeInt

               // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleDbException"+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString



        saveErrorData(errorResponseDto)
        AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)
        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }

    @ExceptionHandler(ErrorReadingDBKey::class)
    fun handleDbException(ex: ErrorReadingDBKey, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse()
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode= ErrorCode.VaultError.errorCodeInt

             // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleDbException "+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString



        saveErrorData(errorResponseDto)
        AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)

        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }


 
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex:Exception,webRequest: WebRequest):ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse(
        )
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode= ErrorCode.UnHandledException.errorCodeInt
        // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleGlobalException "+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString

        saveErrorData(errorResponseDto)
         AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)
    
        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse(
        )
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode=ErrorCode.UnHandledException.errorCodeInt

 // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleConstraintViolationException "+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString
        saveErrorData(errorResponseDto)
                     AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)
       
        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse(
        )
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode=ErrorCode.UnHandledException.errorCodeInt


 // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleEntityNotFoundException "+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString


        saveErrorData(errorResponseDto)
         AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)

        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }




    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(ex: DataAccessException, webRequest: WebRequest):ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse(
        )
        errorResponseDto.apiPath=webRequest.getDescription(false)
        errorResponseDto.errorCode=ErrorCode.UnHandledException.errorCodeInt



 // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleEntityNotFoundException "+
            ex.message +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString

        saveErrorData(errorResponseDto)
         AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)

        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }





   @ExceptionHandler(AuthenticationException::class)
    fun handleIOException(request: HttpServletRequest?, e: CognitoException, webRequest: WebRequest): ResponseEntity<ExceptionResponse> {


       val errorResponseDto= ExceptionResponse(
       )
       errorResponseDto.apiPath=webRequest.getDescription(false)
       errorResponseDto.errorCode=ErrorCode.AuthError.errorCodeInt


 // Convertir stacktrace completo a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val stackTraceString = sw.toString()

        // Poner mensaje + stacktrace
        errorResponseDto.errorMessage ="handleIOException "+
            e.errorMessage +
            " | API PATH: " + errorResponseDto.apiPath +
            " | STACKTRACE: " + stackTraceString

       saveErrorData(errorResponseDto)
         AlarmEmailService.sendAlarmEmail(AlarmCause.ENDPOINT_ERROR, errorResponseDto.errorMessage)

       return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }

    fun handleJwtException(request: HttpServletRequest, e: CognitoException):  ResponseEntity<ExceptionResponse>{
        val errorResponseDto= ExceptionResponse()
        errorResponseDto.errorCode=ErrorCode.AuthError.errorCodeInt
        errorResponseDto.errorMessage="Oh No , There is an error :c"
        saveErrorData(errorResponseDto)
        return ResponseEntity(errorResponseDto, HttpStatus.OK)
    }




}