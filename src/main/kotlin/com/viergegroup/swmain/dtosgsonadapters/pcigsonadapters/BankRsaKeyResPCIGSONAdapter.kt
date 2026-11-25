package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.swmain.dtos.vault.BankRsaKeyResponse
import java.io.IOException
import com.shared.commons.StringUtils.maskString

class BankRsaKeyResPCIGSONAdapter : TypeAdapter<BankRsaKeyResponse?>(){
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: BankRsaKeyResponse?) {
        out.beginObject()
        if (value == null) return
        out.name("errorCodeEnum")
        out.value(value.errorCodeEnum.name )
        out.name("errorCode")
        out.value(value.errorCode.toLong())

        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            out.name("errorMessage")
            out.value(value.errorMessage)
        }
        if (!StringUtils.isNullOrEmpty(value.id)) {
            out.name("id")
            out.value(value.id) 
        }
        if (!StringUtils.isNullOrEmpty(value.modulus)) {
            out.name("modulus")
            out.value(maskString(value.modulus))
        }
        if (!StringUtils.isNullOrEmpty(value.exponent)) {
            out.name("exponent")
            out.value(value.exponent)
        }
        if (!StringUtils.isNullOrEmpty(value.rsaKey)) {
            out.name("rsaKey")
            out.value(value.rsaKey)
        }
        if (!StringUtils.isNullOrEmpty(value.kcv)) {
            out.name("kcv")
            out.value(value.kcv)
        }

        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?):  BankRsaKeyResponse{
        return  BankRsaKeyResponse()
    }

}