package com.viergegroup.swmain.websecurity.model

import org.springframework.http.HttpStatus

class RestErrorList {
    private val serialVersionUID = -721424777198115589L
    private lateinit var status: HttpStatus

    constructor(status: HttpStatus, vararg errors: ErrorMessage) : super() {
        this.status = status
       // addAll(errors.asList())
    }

    constructor(status: Int, vararg errors: ErrorMessage) : super() {
        this.status = HttpStatus.valueOf(status)
      //  addAll(errors.asList())
    }

}