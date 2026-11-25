package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.shared.commons.StringUtils.isNullOrEmpty
import com.viergegroup.swmain.dbmodels.TransactionDBData
import java.io.IOException
import com.shared.commons.StringUtils.maskString

class TransactionDBDataPCIGSONAdapter : TypeAdapter<TransactionDBData?>(){
    @Throws(IOException::class) 
    override fun write(out: JsonWriter, value: TransactionDBData?) {
        out.beginObject()
        if (value == null) return
 
        if (value.uuid.size<=0) {
            out.name("uuid").value("NO_BYTES")
        }else{
            out.name("uuid").value(StringUtils.encodeHexString(value.uuid))
        }
  
        if (!isNullOrEmpty(value.errorCode.name)) {
            out.name("errorCode").value(value.errorCode.name)
        }
        out.name("errorCodeNumber").value(value.errorCodeNumber)

        if (!isNullOrEmpty(value.errorMessage)) {
            out.name("errorMessage").value(value.errorMessage)
        }

        if (!isNullOrEmpty(value.rrn)) {
            out.name("rrn").value(value.rrn)
        }
        out.name("paymentPlanType").value(value.paymentPlanType.toString())
        out.name("hostType").value(value.hostType.toString())
        if (!isNullOrEmpty(value.serverName)) {
            out.name("serverName").value(value.serverName)
        }

        if (!isNullOrEmpty(value.bank.name)) {
            out.name("bank").value(value.bank.name)
        }

        if (!isNullOrEmpty(value.clientIP)) {
            out.name("clientIP").value(value.clientIP)
        }
        if (!isNullOrEmpty(value.clientEmail)) {
            out.name("clientEmail").value(value.clientEmail)
        }
        if (!isNullOrEmpty(value.clientUserName)) {
            out.name("clientUserName").value(value.clientUserName)
        }

        if (!isNullOrEmpty(value.deviceModel)) {
            out.name("deviceModel").value(value.deviceModel)
        }


        if (!isNullOrEmpty(value.merchantType.merchantTypeName)) {
            out.name("merchantType").value(value.merchantType.name)
        }
        if (!isNullOrEmpty(value.transactionType.name)) {
            out.name("transactionType").value(value.transactionType.name)
        }
        if (!isNullOrEmpty(value.pointOfServiceEntryMode.name)) {
            out.name("pointOfServiceEntryMode").value(value.pointOfServiceEntryMode.name)
        }

        if (!isNullOrEmpty(value.transactionAmount)) {
            out.name("transactionAmount").value(value.transactionAmount)
        }
        out.name("cardType").value(value.cardType.toString())


        if (!isNullOrEmpty(value.authorizationIdResponse)) {
            out.name("authorizationIdResponse").value(value.authorizationIdResponse)
        }

        if (!isNullOrEmpty(value.terminalSerialNumber)) {
            out.name("terminalSerialNumber").value(value.terminalSerialNumber)
        }

        if (!isNullOrEmpty(value.keySerialNumber)) {
            out.name("keySerialNumber").value(value.keySerialNumber)
        }

        if (!value.emvData.isEmpty()) {
            out.name("emvData").value( maskString(String(value.emvData)))
        }
      
        out.endObject() 
    }
    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): TransactionDBData {
        return  TransactionDBData()
    }
}
