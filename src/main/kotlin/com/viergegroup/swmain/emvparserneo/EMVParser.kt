package com.viergegroup.swmain.emvparserneo



import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import kotlin.collections.HashMap

object EMVParser {
    var maxInnerIterations=2
    var emvMap=mutableMapOf<String,String>()

    fun restart(){
        emvMap = HashMap()

    }
    fun init(){
        emvMap = HashMap()

    }


    private fun getLengthFromBytes(lengthByte: String): Int {
        val firstDigit = Character.digit(lengthByte[0], 16)
        val secondDigit = Character.digit(lengthByte[1], 16)
        return (firstDigit * 16) + secondDigit
    }
    fun parseAPDUResponseCommand(emvData: String): MutableList<EmvTagData> {
      val data=emvData.substring(0,emvData.length-2)
        return  parseEmvTagsRecursive(data)

    }
    fun getPrimitiveTags(emvData: String): MutableList<EmvTagData> {

      return getPrimitiveTags(parseEmvTagsRecursive(emvData))

    }
    fun getPrimitiveTags(tags: MutableList<EmvTagData>): MutableList<EmvTagData> {
        val primitiveTags = mutableListOf<EmvTagData>()

        for (tagData in tags) {
            if (!tagData.isConstructed) {
                primitiveTags.add(tagData)
            } else {
                primitiveTags.addAll(getPrimitiveTags(tagData.innerEmvTags))
            }
        }

        return primitiveTags
    }


   fun parseEmvTagsRecursive(emvData: String): MutableList<EmvTagData> {
        val emvList = mutableListOf<EmvTagData>()
       WatcherGlobalLog.debug("parseEmvTagsRecursive: $emvData")

        var index = 0
        while (index < emvData.length) {
            if (emvData.length <= index + 2) {
                break
            }
            var tag = emvData.substring(index, index + 2)
            val isTwoBytes = isTwoByteTag(tag)
            if (isTwoBytes) {
                tag = emvData.substring(index, index + 4)
                index += 4
            } else {
                index += 2
            }

            var length = emvData.substring(index, index + 2)
            index += 2
            val isLengthLong = length == "81"
            if (isLengthLong) {
                length = emvData.substring(index, index + 2)
                index += 2
            }
            val intLength = length.toInt(16)

            WatcherGlobalLog.debug("tag: $tag , twobytes: $isTwoBytes, isLengthLong: $isLengthLong ,intLength :$intLength ")
            if(!isConstructedTag(tag)){

            }
            if (emvData.length < index + (intLength * 2)) {
                break
            }
            val value = emvData.substring(index, index + (intLength * 2))
            index += intLength * 2
            if (value.trim() != "") {
                val emvTagData = EmvTagData(tag, value.uppercase())
                if (emvTagData.isConstructed) {
                    val innerTags = parseEmvTagsRecursive(value)
                    emvTagData.innerEmvTags.addAll(innerTags)
                }
                emvList.add(emvTagData)
            }
        }

        return emvList
    }



    fun parseEmvDataDeep(emvData: String):EmvTagsCollection  {
        val emvTagsCollection = EmvTagsCollection()


        var emvTagData=EmvTagData()

        var isConstructed=false
        WatcherGlobalLog.debug("parseEmvData: $emvData")
        var index = 0
        while (index < emvData.length) {
            if(emvData.length<index + 2){
                break;
            }
            var tag = emvData.substring(index, index + 2)
            var twoBytes=isTwoByteTag(tag)
            if(twoBytes){
                tag = emvData.substring(index, index + 4)
                WatcherGlobalLog.debug("newgtag: $tag ")
                index += 4
            } else {
                index += 2
            }
            if(emvData.length<index + 2)
                break
            var length = emvData.substring(index, index + 2)
            index += 2
            var isLengthLong = length == "81"
            if (isLengthLong) {
                length = emvData.substring(index, index + 2)
                index += 2
            }
            val intLength = length.toInt(16)
            if(emvData.length<index + (intLength * 2)){
                break;
            }
            try{
                val value = emvData.substring(index, index + (intLength * 2))
                index += intLength * 2
                isConstructed=isConstructedTag(tag)
                WatcherGlobalLog.debug("tagEMVDATA: $tag twoBytes: ${twoBytes} isConstructed: $isConstructed isLengthLong $isLengthLong")
                if(value.trim()!=""){
                    emvMap[tag]=value
                    emvTagData=EmvTagData(tag,value)
                    emvTagsCollection.emvTags.add(emvTagData)
                }


            }catch(ex:Exception){

            }



        }

        if(isConstructed) {
            while (isConstructed) {
                val tags=parseEmvTags(emvTagData.value)
                emvTagData.innerEmvTags=tags
                while(isConstructed){

                    isConstructed = false

                }

            }
        }else{
            emvTagsCollection.emvTags=parseEmvTags(emvData)
        }



        return emvTagsCollection
    }




    fun parseEmvTags(emvData: String):MutableList<EmvTagData>  {
        val emvList= mutableListOf<EmvTagData>()
        WatcherGlobalLog.debug("parseEmvNestedTags: $emvData")

        var index = 0
        while (index < emvData.length) {
            if(emvData.length<=index + 2){
                break;
            }
            var tag = emvData.substring(index, index + 2)
            if(isTwoByteTag(tag)){
                tag = emvData.substring(index, index + 4)

                index += 4
            } else {
                index += 2
            }

            var length = emvData.substring(index, index + 2)

            index += 2
            val isLengthLong = length == "81"
            if (isLengthLong) {
                length = emvData.substring(index, index + 2)
                index += 2
            }
            val intLength = length.toInt(16)

            WatcherGlobalLog.debug("tag: $tag , twobytes: ${isTwoByteTag(tag)}, isLengthLong: $isLengthLong ,intLength :$intLength ")

            if(emvData.length<index + (intLength * 2)){
                break;
            }
            val value = emvData.substring(index, index + (intLength * 2))
            index += intLength * 2
            if(value.trim()!=""){
                emvMap[tag] = value.uppercase() 
                emvList.add(EmvTagData(tag,value.uppercase()))
            }

        }


        return emvList

    }


    fun parsePDOL(pdol: String): List<String> {
        val tags = mutableListOf<String>()
        var index = 0
        while (index < pdol.length) {
            val tagByte = pdol.substring(index, index + 2)
            val isTwoByteTag = isTwoByteTag(tagByte)
            val tagLength = if (isTwoByteTag) 4 else 2

            if (index + tagLength <= pdol.length) {
                val tag = pdol.substring(index, index + tagLength)
                tags.add(tag)
                index += tagLength+ 2
            } else {
                // Manejar el caso en el que no hay suficientes caracteres para el tag
                break
            }
        }

        return tags
    }

    fun printMap(){
        WatcherGlobalLog.debug("\n\n\n printMap")
        for(tag in emvMap){
            WatcherGlobalLog.debug(" tag  ${tag.key} value: ${tag.value}")
        }
    }
    fun isTwoByteTag(tag: String): Boolean {
        return tag=="9F"||tag=="7D"||tag=="BF"||tag=="5F"
    }
    fun isConstructedTag(tag: String): Boolean {
      return  EmvTag.fromTagStr(tag).isConstructed
       /* if (tag.length < 2) {
            return false
        }
        val firstByte = tag.substring(0, 2)
        val hexValue = firstByte.toInt(16)

        return hexValue and 0x20 == 0x20*/
    }
}