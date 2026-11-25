package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import java.io.IOException
import com.viergegroup.crypto.pandora.managers.VaultDtos.CalculateHashResponse
 
class CalculateHashResponseGSONAdapter  : TypeAdapter<CalculateHashResponse?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: CalculateHashResponse?) {
        outstr.beginObject()
        if(valueNull==null) return ;

        val value=valueNull
 
        
        outstr.name("errorCodeEnum")
        outstr.value(value.errorCodeEnum.name )

        outstr.name("errorCode")
        outstr.value(value.errorCode)
        if (!StringUtils.isNullOrEmpty(value.uuid)) {
            outstr.name("hash")
            outstr.value(value.hash)
        }
   
      
        if (!StringUtils.isNullOrEmpty(value.hash)) {
            outstr.name("expectedHash")
            outstr.value(value.hash.toUpperCase()) 
        } 

        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }
    
        if (!StringUtils.isNullOrEmpty(value.kcv)) {
            outstr.name("kcv")
            outstr.value(value.kcv)
        }
     
        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): CalculateHashResponse {
        return CalculateHashResponse()
    }

}