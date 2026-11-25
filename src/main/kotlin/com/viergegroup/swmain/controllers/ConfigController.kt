package com.viergegroup.swmain.controllers

import com.google.gson.GsonBuilder
import com.shared.commons.AppSettings
import com.shared.commons.ErrorCode
import com.shared.commons.GsonConfig
import com.viergegroup.swmain.dtos.config.ConfigResponse
import com.viergegroup.swmain.dtos.config.JsonDto

import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileWriter
import java.io.IOException

@RestController
@RequestMapping("/config")
class ConfigController {


    @PostMapping("/get-iso-desc")
    fun getDescriptorPost(@RequestBody dto: JsonDto): ConfigResponse {
        val config = AppSettings.getAuthorizerConfig(dto.hostType)
        val descriptorFile = File(config.isoDescriptorsPath)
        val response=ConfigResponse()
        return try {
            if (descriptorFile.exists()) {
                val content = descriptorFile.readText()
                response.data=GsonConfig.gson.fromJson(content,Any::class.java)
                response.errorCode=0
                response.errorMessage="OK"
                response
            } else {
                response.errorCode=ErrorCode.UnHandledException.errorCodeInt
                response.errorMessage="NOT FOUND "
                response
            }
        } catch (e: IOException) {
            e.printStackTrace()
            response.errorCode=ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage=e.message.toString()
            response
        }
    }


    @PostMapping("/get-app-config")
    fun getAppConfig(@RequestBody dto: JsonDto): ConfigResponse {
        val path=AppSettings.getInstance().getBasePath()+"/config.json"
        val configFile = File(path)
        val response=ConfigResponse()

        return try {
            if (configFile.exists()) {
                val content = configFile.readText()
                response.data=GsonConfig.gson.fromJson(content,Any::class.java)
                response.errorCode=0
                response.errorMessage="OK"
                response
            } else {
                response.errorCode=ErrorCode.UnHandledException.errorCodeInt
                response.errorMessage="NOT FOUND"
                return response
            }
        } catch (e: IOException) {
            response.errorCode=ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage=e.message.toString()
            return response
        }
    }





    @PostMapping("/save-app-config")
    fun saveAppConfig(@RequestBody dto: JsonDto): ConfigResponse {
        val path=AppSettings.getInstance().getBasePath()+"/config.json"
        val descriptorFile = File(path)
        val response=ConfigResponse()
      
        if (!descriptorFile.parentFile.exists()) {
            if (!descriptorFile.parentFile.mkdirs()) {
                response.errorCode=ErrorCode.UnHandledException.errorCodeInt
                response.errorMessage="NOT FOUND "
                return response
            }
        }
        return try {
            FileWriter(descriptorFile, false).use { writer ->
               
                    writer.write(dto.data)
            }
            response.errorCode=0
            response.errorMessage=""
            return response
        } catch (e: IOException) {
            response.errorCode=ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage=e.message.toString()
            return response
        }
    }






    @PostMapping("/save-iso-desc")
    fun saveDescriptor(@RequestBody dto: JsonDto): ConfigResponse {
        val config = AppSettings.getAuthorizerConfig(dto.hostType)
        val descriptorFile = File(config.isoDescriptorsPath)
        val response=ConfigResponse()

        // Crear el directorio si no existe
        if (!descriptorFile.parentFile.exists()) {
            if (!descriptorFile.parentFile.mkdirs()) {
                response.errorCode=ErrorCode.UnHandledException.errorCodeInt
                response.errorMessage="NOT FOUND "
                return response
            }
        }

        return try {
            FileWriter(descriptorFile, false).use { writer ->
              
                    writer.write(dto.data)
            }
            response.errorCode=0
            response.errorMessage="OK"
            return response
        } catch (e: IOException) {
            response.errorCode=ErrorCode.UnHandledException.errorCodeInt
            response.errorMessage=e.message.toString()
            return response
        }
    }

}