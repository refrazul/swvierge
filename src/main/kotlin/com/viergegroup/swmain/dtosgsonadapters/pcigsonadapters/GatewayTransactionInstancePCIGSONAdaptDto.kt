package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.viergegroup.swmain.dtos.gateway.GatewayTransactionInstanceDto
import com.viergegroup.swmain.dtos.transactions.TransactionTerminalDto
import com.shared.commons.StringUtils.maskString
import java.io.IOException

class GatewayTransactionInstancePCIGSONAdaptDto : TypeAdapter<GatewayTransactionInstanceDto>() {
 
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: GatewayTransactionInstanceDto) {
        out.beginObject()
 
        // Escribir los campos de GatewayTransactionInstanceDto
        out.name("id").value(value.id)
        out.name("instanceId").value(value.instanceId)

        // Enmascarar y escribir los campos sensibles
        out.name("merchantId").value(maskString(value.merchantId))
        out.name("emvRequestPayload").value(maskString(value.emvRequestPayload))
        out.name("hostType").value(value.hostType.name)
        out.name("retrievalReferenceNumber").value(maskString(value.retrievalReferenceNumber))
        out.name("gatewayId").value(value.gatewayId)
        out.name("meta").value(value.meta)
 
        // Serializar `metaData` (TransactionTerminalDto) si no es nulo
        out.name("metaData")
        if (value.metaData != null) {
            // Usar un adaptador interno para `TransactionTerminalDto`
            writeTransactionTerminalDto(out, value.metaData!!)
        } else {
            out.nullValue()
        }

        out.endObject()
    }

    private fun writeTransactionTerminalDto(out: JsonWriter, value: TransactionTerminalDto) {
        out.beginObject()

        // Serializar todos los campos, enmascarando los sensibles
        out.name("fiid").value(value.fiid)
        out.name("affiliation").value(value.affiliation)
        out.name("merchantName").value(value.merchantName)
        out.name("merchantTypeCode").value(value.merchantTypeCode)
        out.name("merchantAddress").value(value.merchantAddress)
        out.name("retailerDataAddOns").value(value.retailerDataAddOns)
        out.name("bankName").value(value.bankName)

        // Enmascarar `merchantId`
        out.name("merchantId").value(maskString(value.merchantId))

        out.name("orderId").value(value.orderId)
        out.name("sendToProd").value(value.sendToProd)
        out.name("serverName").value(value.serverName)

        // Serializar enums como strings
        out.name("pointOfServiceEntryMode").value(value.pointOfServiceEntryMode.name)
        out.name("cardType").value(value.cardType.name)
        out.name("paymentPlanType").value(value.paymentPlanType.name)

        out.name("noSkipCRC32").value(value.noSkipCRC32)

        // Campos financieros
        out.name("transactionAmount").value(value.transactionAmount)
        out.name("terminalData").value(value.terminalData)
        out.name("affiliationNumber").value(value.affiliationNumber)
        out.name("retailerData").value(value.retailerData)

        // Enmascarar `track2` y `cardNumber`
        out.name("track2").value(maskString(value.track2))
        out.name("cardNumber").value(maskString(value.cardNumber))

        out.name("cardExpirationDate").value(value.cardExpirationDate)
        out.name("authorizationIdResponse").value(value.authorizationIdResponse)
        out.name("originalRrn").value(value.originalRrn)

        out.name("hasChip").value(value.hasChip)
        out.name("tokenEX").value(value.tokenEX)
        out.name("keyId").value(value.keyId)
        out.name("keyId1").value(value.keyId1)
        out.name("keyId2").value(value.keyId2)

        // Otros campos adicionales
        out.name("terminalSoftwareVersion").value(value.terminalSoftwareVersion)
        out.name("cipherConfiguration").value(value.cipherConfiguration)
        out.name("localBinsTableIdPrimary").value(value.localBinsTableIdPrimary)
        out.name("localBinsTableIdSecondary").value(value.localBinsTableIdSecondary)
        out.name("binsTableVersion").value(value.binsTableVersion)
        out.name("keySerialNumber").value(value.keySerialNumber)

        out.name("criptoInfoData").value(value.criptoInfoData)
        out.name("tvr").value(value.tvr)
        out.name("otherAmount").value(value.otherAmount)
        out.name("aip").value(value.aip)
        out.name("arqc").value(value.arqc)

        // Más campos si es necesario...

        out.endObject()
    }

    override fun read(`in`: JsonReader): GatewayTransactionInstanceDto {
        throw UnsupportedOperationException("Deserialization is not supported in this adapter")
    }
}
