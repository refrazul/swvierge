package com.viergegroup.swmain.websecurity.model

class ResponseWrapper {

    var data: Any? = null
     var metadata: Any? = null
    var errors:RestErrorList?=null

    constructor(data: Any?, metadata: Any, errors: RestErrorList) {

        this.data = data
        this.metadata = metadata
        this.errors = errors
    }


}