package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.viergegroup.swmain.dtos.gateway.GatewayRequestDto
import com.shared.commons.StringUtils.maskString
import java.io.IOException

class GatewayTxnReqPCIGsonAdapterDto : TypeAdapter<GatewayRequestDto>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: GatewayRequestDto) {
        out.beginObject() 

        // Enmascarar y escribir los campos sensibles del objeto `GatewayRequestDto`
        out.name("instanceRequest").beginObject()

        // MerchantId (enmascarado)
        val maskedMerchantId = maskString(value.instanceRequest.merchantId)
        out.name("merchantId").value(maskedMerchantId)
  
        // RetrievalReferenceNumber (enmascarado) 
        val maskedRRN = maskString(value.instanceRequest.retrievalReferenceNumber)
        out.name("retrievalReferenceNumber").value(maskedRRN)

        // EMV Request Payload (enmascarado)
        val maskedEmvPayload = maskString(value.instanceRequest.emvRequestPayload)
        out.name("emvRequestPayload").value(maskedEmvPayload)

        // GatewayId (sin enmascarar, ya que no es sensible según el contexto)
        out.name("gatewayId").value(value.instanceRequest.gatewayId)

        // Otros campos (puedes expandir según necesidad)
        out.endObject()
        out.endObject()
    }

    override fun read(`in`: JsonReader): GatewayRequestDto {
        throw UnsupportedOperationException("Deserialization is not supported in this adapter")
    }
}
