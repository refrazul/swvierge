package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.logs.LogsResponseDto
import java.io.IOException

class LogsResponseDtoGSONAdapter: TypeAdapter<LogsResponseDto?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: LogsResponseDto?) {
        outstr.beginObject()
        if(valueNull==null) return ;
        val value=valueNull
        outstr.name("errorCodeEnum") 
        outstr.value(value.errorCodeEnum.name )

        outstr.name("errorCode")
        outstr.value(value.errorCode)

        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }
      
        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?):  LogsResponseDto {
        return  LogsResponseDto()
    }

}