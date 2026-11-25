package com.viergegroup.swmain.emvparserneo

class EmvTagData {
    var emvTag:EmvTag=EmvTag.Unknown
    var tag=""
    var value=""
    var innerEmvTags =mutableListOf<EmvTagData>()
    constructor()
    constructor(tag:String, value:String)
    {
        this.tag=tag
        emvTag=EmvTag.fromTagStr(tag)
        this.value=value
    }
    val isConstructed: Boolean
        get() {
            return emvTag.isConstructed
        }
}