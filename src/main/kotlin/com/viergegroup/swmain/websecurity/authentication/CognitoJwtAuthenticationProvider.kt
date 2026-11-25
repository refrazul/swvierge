package com.viergegroup.swmain.websecurity.authentication

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication

class CognitoJwtAuthenticationProvider: AuthenticationProvider {
   override fun authenticate(authentication: Authentication?): Authentication? {
        return authentication
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return true
    }

}