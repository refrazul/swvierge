package com.viergegroup.swmain.dtosgsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils
import com.viergegroup.crypto.pandora.managers.VaultDtos
import com.viergegroup.swmain.dtos.vault.DukptDataResponse
import java.io.IOException

class DukptDataResponseGSONAdapter : TypeAdapter<DukptDataResponse?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: DukptDataResponse?) {
        out.beginObject()
        if (value == null) return

        out.name("errorCode")
        out.value(value.errorCode.toLong())

        if (!StringUtils.isNullOrEmpty(value.errorMessage)) {
            out.name("errorMessage")
            out.value(value.errorMessage)
        }
        if (!StringUtils.isNullOrEmpty(value.bdk)) {
            out.name("bdk")
            out.value(value.bdk)
        }
        if (!StringUtils.isNullOrEmpty(value.errorMessageHSM)) {
            out.name("errorMessageHSM")
            out.value(value.errorMessageHSM)
        }
        if (value.clearCipherBlocks.isNotEmpty()) {
            out.name("clearCipherBlocks")
            out.beginArray()
            for (block in value.clearCipherBlocks) {
                out.value(block)
            }
            out.endArray()
        }
        if (value.chipherDataBlocks.isNotEmpty()) {
            out.name("chipherDataBlocks")
            out.beginArray()
            for (block in value.chipherDataBlocks) {
                out.value(block)
            }
            out.endArray()
        }
        if (!StringUtils.isNullOrEmpty(value.clearData)) {
            out.name("clearData")
            out.value(value.clearData)
        }
        if (!StringUtils.isNullOrEmpty(value.dataEncrypted)) {
            out.name("dataEncrypted")
            out.value(value.dataEncrypted)
        }
        if (!StringUtils.isNullOrEmpty(value.tk)) {
            out.name("tk")
            out.value(value.tk)
        }
        if (!StringUtils.isNullOrEmpty(value.lastModification)) {
            out.name("lastModification")
            out.value(value.lastModification)
        }
        if (!StringUtils.isNullOrEmpty(value.prevKsn)) {
            out.name("prevKsn")
            out.value(value.prevKsn)
        }
        if (!StringUtils.isNullOrEmpty(value.ksn)) {
            out.name("ksn")
            out.value(value.ksn)
        }
        if (!StringUtils.isNullOrEmpty(value.ipek)) {
            out.name("ipek")
            out.value(value.ipek)
        }
        if (!StringUtils.isNullOrEmpty(value.ipekEncrypted)) {
            out.name("ipekEncrypted")
            out.value(value.ipekEncrypted)
        }
        if (!StringUtils.isNullOrEmpty(value.ksnEncrypted)) {
            out.name("ksnEncrypted")
            out.value(value.ksnEncrypted)
        }
        if (!StringUtils.isNullOrEmpty(value.hsmCommandEncrypt)) {
            out.name("hsmCommandEncrypt")
            out.value(value.hsmCommandEncrypt)
        }
        if (!StringUtils.isNullOrEmpty(value.hsmCommandDecrypt)) {
            out.name("hsmCommandDecrypt")
            out.value(value.hsmCommandDecrypt)
        }
        if (!StringUtils.isNullOrEmpty(value.ipekDecrypted)) {
            out.name("ipekDecrypted")
            out.value(value.ipekDecrypted)
        }
        if (value.previousCounter != 1) {
            out.name("previousCounter")
            out.value(value.previousCounter)
        }
        if (value.cipherCounter != 1) {
            out.name("cipherCounter")
            out.value(value.cipherCounter)
        }
        if (!StringUtils.isNullOrEmpty(value.keyId)) {
            out.name("keyId")
            out.value(value.keyId)
        }

        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): DukptDataResponse {
        return DukptDataResponse()
    }
}