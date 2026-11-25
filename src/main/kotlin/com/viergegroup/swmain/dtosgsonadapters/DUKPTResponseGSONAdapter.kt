package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.crypto.security.MyHsmDto
import java.io.IOException

class DUKPTResponseGSONAdapter: TypeAdapter<MyHsmDto.DUKPTResponse?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: MyHsmDto.DUKPTResponse?) {
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
        if (!StringUtils.isNullOrEmpty(value.data)) {
            outstr.name("data")
            outstr.value(value.data)
        }
        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?):  MyHsmDto.DUKPTResponse {
        return  MyHsmDto.DUKPTResponse()
    }

}