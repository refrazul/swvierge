package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.crypto.pandora.managers.VaultDtos
import java.io.IOException

class DukptMetaResponseMinGSONAdapter: TypeAdapter< VaultDtos.DukptMetaResponseMin?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull:  VaultDtos.DukptMetaResponseMin?) {
        outstr.beginObject()
        if(valueNull==null) return ;

        val value=valueNull

        outstr.name("errorCodeEnum")
        outstr.value(value.errorCodeEnum.name )

        outstr.name("errorCode")
        outstr.value(value.errorCode.toLong())

        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }

        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?):  VaultDtos.DukptMetaResponseMin {
        return  VaultDtos.DukptMetaResponseMin()
    }

}