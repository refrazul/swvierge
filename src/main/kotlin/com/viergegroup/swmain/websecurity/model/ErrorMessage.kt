package com.viergegroup.swmain.websecurity.model

class ErrorMessage {

     var message: String=""
    var code: String=""
    var detail: String=""
    constructor(message: String, code: String, detail: String) {

        this.message = message!!
        this.code = code!!
        this.detail = detail!!
    }

 constructor(message: String?) {
     this.message = message!!

 }


}