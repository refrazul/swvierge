package com.viergegroup.swmain.websecurity.config

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User;

class CognitoJwtAuthentication(
    val user: User,
    val jwtClaimsSet: JWTClaimsSet?,
    val authorities: List<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {

    companion object {
        private const val serialVersionUID = 4780644483172376731L
    }
    @Transient
    private var principal: Any? = null

    init {
        this.principal = user.username
        super.setAuthenticated(true)
    }


    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any? {
        return principal
    }

}
