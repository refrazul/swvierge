package com.viergegroup.swmain.controllers

import com.viergegroup.swmain.websecurity.common.Constants
import com.viergegroup.swmain.websecurity.model.api.*
import com.viergegroup.swmain.websecurity.service.CognitoAuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("auth")
class AuthenticationController {

    @Autowired(required = false)
   lateinit var authenticationManager: AuthenticationManager

    @Autowired(required = false)
    lateinit var authService: CognitoAuthenticationService

    @CrossOrigin
    @RequestMapping(method = [RequestMethod.POST])
    fun authenticationRequest(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse?>? {
        var expiresIn: String=""
        var token: String=""
        var accessToken: String=""
        val username: String = authenticationRequest.username
        val password: String = authenticationRequest.password
        val newPassword: String = authenticationRequest.newPassword
        var authenticationResponse: AuthenticationResponse? = null
        val credentials: MutableMap<String, String> = HashMap()
        credentials[Constants.PASS_WORD_KEY] = password
        credentials[Constants.NEW_PASS_WORD_KEY] = newPassword

        // throws authenticationException if it fails !
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, credentials)
        )
        val authenticatedCredentials = authentication.credentials as Map<String, String>
        token = authenticatedCredentials[Constants.ID_TOKEN_KEY].toString()
        expiresIn = authenticatedCredentials[Constants.EXPIRES_IN_KEY].toString()
        accessToken = authenticatedCredentials[Constants.ACCESS_TOKEN_KEY].toString()
        val userResponse: UserResponse? = authService.getUserInfo(accessToken!!)
        SecurityContextHolder.getContext().authentication = authentication
        authenticationResponse = AuthenticationResponse()


        authenticationResponse.accessToken=(token)
        authenticationResponse.expiresIn=(expiresIn)
        authenticationResponse.accessToken=(accessToken)
        authenticationResponse.userData=(userResponse)


        // Return the token
        return ResponseEntity.ok<AuthenticationResponse?>(
            authenticationResponse
        )
    }


    @RequestMapping(value = ["/sign-up"], method = [RequestMethod.POST])
    fun signUpRequest(@RequestBody signUpRequest: UserSignUpRequest): ResponseEntity<UserSignUpResponse>? {
        var user: UserSignUpResponse? = null
        //Calls the service that Signs up an specific User
        user = authService.signUp(signUpRequest)
        authService.signUpConfirmation(signUpRequest)
        return ResponseEntity.ok<UserSignUpResponse?>(user)
    }

    @RequestMapping(value = ["/reset-password"], method = [RequestMethod.POST])
    fun resetPassword(@RequestBody resetPasswordRequest: PasswordRequest?): ResponseEntity<PasswordResponse?>? {

        //Calls the service that Signs up an specific User
        val response = authService.resetPassword(resetPasswordRequest!!)
        return ResponseEntity.ok(response)
    }

    @RequestMapping(value = ["/confirm-reset-password"], method = [RequestMethod.POST])
    fun confirmResetPassword(@RequestBody resetPasswordRequest: PasswordRequest?): ResponseEntity<PasswordResponse?>? {

        //Calls the service that Signs up an specific User
        val response = authService.confirmResetPassword(resetPasswordRequest!!)
        return ResponseEntity.ok(response)
    }

    @RequestMapping(value = ["/sign-out"], method = [RequestMethod.POST])
    fun signOut(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse?>? {
        val response = AuthenticationResponse()
        //Calls the service that Signs up an specific User
        val message = authService.signOut(authenticationRequest.accessToken, authenticationRequest.username)
        response.message=(message)
        response.username=(authenticationRequest.username)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    fun deleteUser(@RequestBody userRequest: UserRequest): ResponseEntity<AuthenticationResponse?>? {
        val response = AuthenticationResponse()
        authService.deleteUser(userRequest.username)
        response.message=("User Deleted")
        response.username=(userRequest.username)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/check")
    fun getTestPage(): String {
        return "test"
    }




}