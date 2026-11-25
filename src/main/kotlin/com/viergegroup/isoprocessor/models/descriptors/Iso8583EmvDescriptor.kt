package com.viergegroup.isoprocessor.models.descriptors

import com.payneteasy.tlv.BerTlvParser
import com.payneteasy.tlv.BerTlvs
import com.shared.commons.*
import com.viergegroup.swmain.webwatcher.Watcher
import java.util.*

class Iso8583EmvDescriptor {

    var emvTagDescriptors: ArrayList<Iso8583EmvTagDescriptor> = arrayListOf()

    fun parseEmvData(emvData: String, watcher: Watcher): MutableMap<String,String>{
        val tagMap = HashMap<String, String>()
        var tlvs: BerTlvs? = null
        val parser = BerTlvParser()


        try {
            tlvs = parser.parse(StringUtils.hexStringToByteArray(emvData))
        } catch (ex: Exception) {
           watcher.debug("parseEmvData EXCEPTIN 2 ${ex.message} ")

            throw WebServiceException(ErrorCode.EMVDataFormatError,ex.message,watcher)
        }

        if (tlvs != null) {
            try {

                for (berTlv in tlvs.list) {
                    val tag = berTlv.tag.toString().replace(" ", "").replace("-", "").uppercase()

                   watcher.debug("tag  ${tag} ")

                    //   val tag = Integer.decode("0x" + berTlv.tag.toString().replace(" ", "").replace("-", ""))
                    val value = berTlv.hexValue.uppercase(Locale.getDefault())
                    if(!tag.equals("57"))
                       watcher.debug("value  ${value} ")
                    else
                       watcher.debug("value  ${value.substring(0,4)}***** ")

                    if (tagMap[tag] == null) {
                        tagMap[tag] = value.uppercase()

                    }
                }

            } catch (ex: Exception) {
               watcher.debug("parseEmvData EXCEPTION 3 ${ex.message} ")
                throw WebServiceException(ErrorCode.EMVDataFormatError,ex.message,watcher)
            }
        }
        return tagMap
    }



    fun describeEmvTagsMap(map:MutableMap<String,String>):String{
        var description=""
        for((key,value) in map){
            val descriptor=emvTagDescriptors.firstOrNull{it.emvTag==key}
            if(descriptor!=null){
                description+="\t[${descriptor.tagName}] [$key]=>\t\"$value\"\n"
            }
        }

        return description
    }
}