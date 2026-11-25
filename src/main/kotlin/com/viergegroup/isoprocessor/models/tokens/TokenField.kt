package com.viergegroup.isoprocessor.models.tokens

class TokenField {

    var fieldNumber:Int=0
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var fieldValueString:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }


    var fieldName:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }


    var fieldDescription:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }



    var fieldLength:Int=0
        get() {
            return field
        }
        set(value) {
            field = value
        }


    var fieldVarName:String=""
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var isMandatory=false
    var hasDefaultValue=false

}