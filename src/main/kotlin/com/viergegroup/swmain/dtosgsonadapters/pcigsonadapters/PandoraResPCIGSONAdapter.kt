package com.viergegroup.swmain.dtosgsonadapters.pcigsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.shared.commons.StringUtils.isNullOrEmpty
import com.viergegroup.swmain.dtos.pandora.PandoraResponse
import com.viergegroup.swmain.dtos.pandora.PandoraTransactionDto
import java.io.IOException
import com.shared.commons.StringUtils.maskString

class PandoraResPCIGSONAdapter : TypeAdapter<PandoraResponse?>(){
    @Throws(IOException::class)
    override fun write(outstr: JsonWriter, valueNull: PandoraResponse?) {
        outstr.beginObject()
        if(valueNull==null) return ; 

        val value=valueNull 
        if (!isNullOrEmpty(value.uuid)) {
            outstr.name("uuid")
            outstr.value(value.uuid)
        }
        outstr.name("hostType")
        outstr.value(value.hostType.toString())
        outstr.name("errorCodeEnum")
        outstr.value(value.errorCodeEnum.name )

        outstr.name("errorCode")
        outstr.value(value.errorCode.toLong())





        if (!isNullOrEmpty(value.errorMessage)) {
            outstr.name("errorMessage")
            outstr.value(value.errorMessage)
        }



        if (!isNullOrEmpty(value.keyId)) {
            outstr.name("keyId")
            outstr.value(value.keyId)
        }
        if (!isNullOrEmpty(value.keyId1)) {
            outstr.name("keyId1")
            outstr.value(value.keyId1)
        }

        if (!isNullOrEmpty(value.rsaKeyId)) {
            outstr.name("rsaKeyId")
            outstr.value(value.rsaKeyId)
        }
        if (!isNullOrEmpty(value.ipek)) {
            outstr.name("ipek")
            outstr.value(maskString(value.ipek))
        }
        if (!isNullOrEmpty(value.logData)) {
            outstr.name("logData")
            outstr.value(value.logData)
        }
        if (!isNullOrEmpty(value.pandoraVersion)) {
            outstr.name("pandoraVersion")
            outstr.value(value.pandoraVersion)
        }




        if (!isNullOrEmpty(value.cvv)) {
            outstr.name("cvv")
            outstr.value(maskString(value.cvv))
        }
        if (!isNullOrEmpty(value.transactionAmount)) {
            outstr.name("transactionAmount")
            outstr.value(value.transactionAmount)
        }
        if (!isNullOrEmpty(value.validateData)) {
            outstr.name("validateData")
            outstr.value(value.validateData)
        }



        if (!isNullOrEmpty(value.tokenEX)) {
            outstr.name("tokenEX")
            outstr.value(value.tokenEX)
        }

        if (!isNullOrEmpty(value.pekEX)) {
            outstr.name("pekEX")
            outstr.value(value.pekEX)
        }

        if (!isNullOrEmpty(value.ksnEX)) {
            outstr.name("ksnEX")
            outstr.value(value.ksnEX)
        }

        if (!isNullOrEmpty(value.checkValueEX)) {
            outstr.name("checkValueEX")
            outstr.value(value.checkValueEX)
        }


        if (!isNullOrEmpty(value.newKeyRequestStatusEX)) {
            outstr.name("newKeyRequestStatusEX")
            outstr.value(value.newKeyRequestStatusEX)
        }


        if (!isNullOrEmpty(value.crc32EX)) {
            outstr.name("crc32EX")
            outstr.value(value.crc32EX)
        }
        if (!isNullOrEmpty(value.tokenEZ)) {
            outstr.name("tokenEZ")
            outstr.value(value.tokenEZ)
        }
        if (!isNullOrEmpty(value.tokenEZ2)) {
            outstr.name("tokenEZ2")
            outstr.value(value.tokenEZ2)
        }
        if (!isNullOrEmpty(value.keySerialNumber)) {
            outstr.name("keySerialNumber")
            outstr.value(value.keySerialNumber)
        }
        if (!isNullOrEmpty(value.cipherCounter)) {
            outstr.name("cipherCounter")
            outstr.value(value.cipherCounter)
        }
        if (!isNullOrEmpty(value.failCipherCounter)) {
            outstr.name("failCipherCounter")
            outstr.value(value.failCipherCounter)
        }

        if (!isNullOrEmpty(value.track2)) {
            outstr.name("track2")
            outstr.value(maskString(value.track2)+"|len: ${value.track2.length}")
        }

        if (!isNullOrEmpty(value.flagTrack2)) {
            outstr.name("flagTrack2")
            outstr.value(value.flagTrack2)
        }


        if (!isNullOrEmpty(value.readCardMode)) {
            outstr.name("readCardMode")
            outstr.value(value.readCardMode)
        }
        if (!isNullOrEmpty(value.track2Length)) {
            outstr.name("track2Length")
            outstr.value(value.track2Length)
        }
        if (!isNullOrEmpty(value.cvvFlag)) {
            outstr.name("cvvFlag")
            outstr.value(value.cvvFlag)
        }



        if (!isNullOrEmpty(value.clearCvvLength)) {
            outstr.name("clearCvvLength")
            outstr.value(value.clearCvvLength)
        }

        if (!isNullOrEmpty(value.track1Flag)) {
            outstr.name("track1Flag")
            outstr.value(value.track1Flag)
        }
        if (!isNullOrEmpty(value.cipherData)) {
            outstr.name("cipherData")
            outstr.value(value.cipherData)
        }
        if (!isNullOrEmpty(value.lastPan4Digits)) {
            outstr.name("lastPan4Digits")
            outstr.value(value.lastPan4Digits)
        }
        if (!isNullOrEmpty(value.crc32EZ)) {
            outstr.name("crc32EZ")
            outstr.value(value.crc32EZ)
        }

        if (!isNullOrEmpty(value.tokenEW)) {
            outstr.name("tokenEW")
            outstr.value(value.tokenEW)
        }

        if (!isNullOrEmpty(value.tkEW)) {
            outstr.name("tkEW")
            outstr.value(value.tkEW)
        }

        if (!isNullOrEmpty(value.randomTkCipherEW)) {
            outstr.name("randomTkCipherEW")
            outstr.value(value.randomTkCipherEW)
        }

        if (!isNullOrEmpty(value.rsaVersionEW)) {
            outstr.name("rsaVersionEW")
            outstr.value(value.rsaVersionEW)
        }


        if (!isNullOrEmpty(value.rsaPaddingEW)) {
            outstr.name("rsaPaddingEW")
            outstr.value(value.rsaPaddingEW)
        }
        if (!isNullOrEmpty(value.checkValueEW)) {
            outstr.name("checkValueEW")
            outstr.value(value.checkValueEW)
        }

        if (!isNullOrEmpty(value.crc32EW)) {
            outstr.name("crc32EW")
            outstr.value(value.crc32EW)
        }

        if (!isNullOrEmpty(value.tokenOpenPay)) {
            outstr.name("tokenOpenPay")
            outstr.value(value.tokenOpenPay)
        }
 


        outstr.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader?): PandoraResponse {
        return PandoraResponse()
    }

}
