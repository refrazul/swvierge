package com.viergegroup.swmain.websecurity.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

 class SpringSecurityUser : UserDetails {



     private   var username: String=""
    @JsonIgnore
    private var password: String=""
    var email: String=""
    var accessToken: String=""
    var expiresIn: Int=0
    var tokenType: String=""
    var refreshToken: String=""
    var idToken: String=""
    var lastPasswordReset: Date=Date()
   private var authorities: Collection<out GrantedAuthority>? = null
    var accountNonExpired: Boolean= true
    var accountNonLocked: Boolean = true
    var credentialsNonExpired: Boolean = true
    var enabled: Boolean? = true
    companion object {
        private const val serialVersionUID: Long = 4089895915973854812
    }



     // override fun getAuthorities(): Collection<GrantedAuthority> = authorities as Collection<GrantedAuthority>
    override fun isAccountNonExpired(): Boolean = accountNonExpired ?: true
    override fun isAccountNonLocked(): Boolean = accountNonLocked ?: true
    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired ?: true
    override fun isEnabled(): Boolean = enabled ?: true


     override fun getAuthorities(): Collection<out GrantedAuthority>? {
         return authorities
     }

     override fun getPassword(): String {
         return password
     }

     override fun getUsername(): String {
         return username
     }


     fun setAuthorities(authorities: Collection<GrantedAuthority?>?) {
         this.authorities = authorities as Collection<out GrantedAuthority>?
     }

     fun setUsername(username: String) {
         this.username = username
     }
     fun setPassword(password: String) {
         this.password = password
     }

 }