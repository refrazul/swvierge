package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter 
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.viergegroup.swmain.dtos.gateway.GatewayResponse
import com.viergegroup.swmain.dtos.gateway.GatewayResponseMeta
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionResponse
import java.io.IOException
import com.shared.commons.StringUtils.maskString
class GatewayTransactionResPCIGSONAdapter : TypeAdapter<GatewayTransactionResponse>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: GatewayTransactionResponse) {
        out.beginObject()
        out.name("gatewayResponse")
        if (value.gatewayResponse == null) {
            out.nullValue()
        } else { 
            out.beginObject()
            out.name("id").value(value.gatewayResponse.id)
            out.name("source").value(value.gatewayResponse.source)
            out.name("status").value(value.gatewayResponse.status)
            out.name("actionCode").value(value.gatewayResponse.actionCode)
            out.name("emvResponsePayload").value(maskString(value.gatewayResponse.emvResponsePayload))
            out.name("meta").value(maskString(value.gatewayResponse.meta))
            //out.name("metaData") 
            /*if (value.gatewayResponse.metaData == null) {
                out.nullValue() 
            } else { 
                out.beginObject()
                out.endObject()
            }*/
            out.endObject()
        }
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): GatewayTransactionResponse {
        val response = GatewayTransactionResponse()
        `in`.beginObject()
        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "gatewayResponse" -> {
                    response.gatewayResponse = GatewayResponse()
                    `in`.beginObject()
                    while (`in`.hasNext()) {
                        when (`in`.nextName()) {
                            "id" -> response.gatewayResponse.id = `in`.nextString()
                            "source" -> response.gatewayResponse.source = `in`.nextString()
                            "status" -> response.gatewayResponse.status = `in`.nextString()
                            "actionCode" -> response.gatewayResponse.actionCode = `in`.nextString()
                            "emvResponsePayload" -> response.gatewayResponse.emvResponsePayload = `in`.nextString()
                            "meta" -> response.gatewayResponse.meta = `in`.nextString()
                            "metaData" -> {
                                if (`in`.peek() == JsonToken.NULL) {
                                    `in`.nextNull()
                                    response.gatewayResponse.metaData = null
                                } else {
                                    response.gatewayResponse.metaData = GatewayResponseMeta()
                                    `in`.beginObject()
                                    // Aquí agregas los campos de metaData si los tiene.
                                    `in`.endObject()
                                }
                            }
                            else -> `in`.skipValue()
                        }
                    }
                    `in`.endObject()
                }
                else -> `in`.skipValue()
            }
        }
        `in`.endObject()
        return response
    }
}
