package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.swmain.dtos.gateway.GatewayResponseMeta
import java.io.IOException
import com.shared.commons.StringUtils.maskString

class MetaDataPCIGSONAdapter: TypeAdapter<GatewayResponseMeta?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: GatewayResponseMeta?) {
        outstr.beginObject()
        if(valueNull==null) return ; 

        val value=valueNull 
        outstr.name("errorCode")
        outstr.value(value.errorCode.toLong())
        if (!StringUtils.isNullOrEmpty(value.uuid)) {
            outstr.name("uuid")
            outstr.value(value.uuid)
        } 
        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }
        if (!StringUtils.isNullOrEmpty(value.rrn)) {
            outstr.name("rrn")
            outstr.value(value.rrn)
        }
        if (!StringUtils.isNullOrEmpty(value.ksn)) {
            outstr.name("ksn")
            outstr.value(value.ksn)
        }
        if (!StringUtils.isNullOrEmpty(value.ksn)) {
            outstr.name("ksn")
            outstr.value(value.ksn)
        }
        if (!StringUtils.isNullOrEmpty(value.responseCode)) {
            outstr.name("responseCode")
            outstr.value(value.responseCode)
        } 
        if (!StringUtils.isNullOrEmpty(value.authorizationIdResponse)) {
            outstr.name("authorizationIdResponse")
            outstr.value(value.authorizationIdResponse)
        }
        if (!StringUtils.isNullOrEmpty(value.transactionAmount)) {
            outstr.name("transactionAmount")
            outstr.value(value.transactionAmount)
        }
        if (!StringUtils.isNullOrEmpty(value.otherAmount)) {
            outstr.name("otherAmount")
            outstr.value(value.otherAmount)
        }
        if (!StringUtils.isNullOrEmpty(value.tokenEZ)) {
            outstr.name("tokenEZ")
            outstr.value(value.tokenEZ)
        }
        if (!StringUtils.isNullOrEmpty(value.emvData)) {
            outstr.name("emvData") 
            outstr.value(maskString(value.emvData))
        }
        if (!StringUtils.isNullOrEmpty(value.tokenEW)) {
            outstr.name("tokenEW")
            outstr.value(value.tokenEW)
        } 
        if (!StringUtils.isNullOrEmpty(value.tokenOpenPayClear)) {
            outstr.name("tokenOpenPayClear")
            outstr.value(maskString(value.tokenOpenPayClear))
        }
        if (!StringUtils.isNullOrEmpty(value.tokenOpenPay)) {
            outstr.name("tokenOpenPay")
            outstr.value(value.tokenOpenPay)
        }
        if (!StringUtils.isNullOrEmpty(value.tvr)) {
            outstr.name("tvr")
            outstr.value(value.tvr)
        }
        if (!StringUtils.isNullOrEmpty(value.aip)) {
            outstr.name("aip")
            outstr.value(value.aip)
        }
        if (!StringUtils.isNullOrEmpty(value.arqc)) {
            outstr.name("arqc")
            outstr.value(value.arqc)
        }

        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): GatewayResponseMeta {
        return GatewayResponseMeta()
    }

}