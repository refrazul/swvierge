package com.viergegroup.isoprocessor.models.iso

class Iso8583Field {

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

    var fieldValueBytes:ByteArray=ByteArray(0)
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var fieldValueLong:Long=0L
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

    var isMandatory:Boolean=false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var hasDefaultValue:Boolean=false
        get() {
            return field
        }
        set(value) {
            field = value
        }

    var defaultValueDescription=""

}