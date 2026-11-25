package com.viergegroup.swmain.websecurity.model.api

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)

class AuthenticationResponse {


    var accessToken: String=""
    var sessionToken: String=""
    var expiresIn: String=""
    var actualDate: String=""
    var expirationDate: String=""
    var userData: UserResponse? = null
    var username: String=""
    var message: String=""


}