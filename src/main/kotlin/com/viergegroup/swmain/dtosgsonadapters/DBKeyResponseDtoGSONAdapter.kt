package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.pandora.DBKeyResponseDto
import java.io.IOException

class DBKeyResponseDtoGSONAdapter  : TypeAdapter<DBKeyResponseDto?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: DBKeyResponseDto?) {
        outstr.beginObject()
        if(valueNull==null) return ; 

        val value=valueNull 
 
        outstr.name("errorCodeEnum")
        outstr.value(value.errorCodeEnum.name )

        outstr.name("errorCode")
        outstr.value(value.errorCode)

        if (!StringUtils.isNullOrEmpty(value.uuid)) {
            outstr.name("uuid")
            outstr.value(value.uuid)
        }



        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }
        outstr.name("hostType")
        outstr.value(value.hostType.toString())


        if (!StringUtils.isNullOrEmpty(value.dbKey)&&value.dbKey.length>0) {
            outstr.name("dbKey")
            outstr.value(value.dbKey.substring(0,(value.dbKey.length*0.5f).toInt()))
        }
        if (!StringUtils.isNullOrEmpty(value.kcv)) {
            outstr.name("kcv")
            outstr.value(value.kcv)
        }
        if (!StringUtils.isNullOrEmpty(value.creationDateTime)) {
            outstr.name("creationDateTime")
            outstr.value(value.creationDateTime)
        }

        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): DBKeyResponseDto {
        return DBKeyResponseDto()
    }

}