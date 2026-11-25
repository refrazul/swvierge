package com.viergegroup.swmain.websecurity.config

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException
import com.amazonaws.services.cognitoidp.model.AttributeType
import com.amazonaws.services.cognitoidp.model.GetUserRequest
import com.viergegroup.swmain.websecurity.common.Constants
import com.viergegroup.swmain.websecurity.model.SpringSecurityUser
import com.viergegroup.swmain.websecurity.model.api.AuthenticationRequest
import com.viergegroup.swmain.websecurity.model.api.CognitoException
import com.viergegroup.swmain.websecurity.model.api.UserResponse
import com.viergegroup.swmain.websecurity.service.CognitoAuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider: AuthenticationProvider {

    @Autowired
  lateinit  var cognitoService: CognitoAuthenticationService


    override fun authenticate(authentication: Authentication?): UsernamePasswordAuthenticationToken? {

        var authenticationRequest: AuthenticationRequest? = null


        return if (null != authentication) {
            authenticationRequest = AuthenticationRequest()
            val credentials = authentication.credentials as Map<String, String>
            authenticationRequest.newPassword= (credentials[Constants.NEW_PASS_WORD_KEY].toString())
            authenticationRequest.password= (credentials[Constants.PASS_WORD_KEY].toString())
            authenticationRequest.username=(authentication.name)
            val userAuthenticated: SpringSecurityUser = cognitoService.authenticate(authenticationRequest)
            if (null != userAuthenticated) {
                // use the credentials
                // and authenticate against the third-party system
                val authenticatedCredentials: MutableMap<String, String> = HashMap()
                authenticatedCredentials[Constants.ACCESS_TOKEN_KEY] = userAuthenticated.accessToken
                authenticatedCredentials[Constants.EXPIRES_IN_KEY] = userAuthenticated.expiresIn.toString()
                authenticatedCredentials[Constants.ID_TOKEN_KEY] = userAuthenticated.idToken
                authenticatedCredentials[Constants.PASS_WORD_KEY] = userAuthenticated.getPassword()
                authenticatedCredentials[Constants.REFRESH_TOKEN_KEY] = userAuthenticated.refreshToken
                authenticatedCredentials[Constants.TOKEN_TYPE_KEY] = userAuthenticated.tokenType
                UsernamePasswordAuthenticationToken(
                    userAuthenticated.getUsername(), authenticatedCredentials, userAuthenticated.getAuthorities()
                )
            } else {
                null
            }
        } else {
            throw UsernameNotFoundException(String.format("No appUser found with username '%s'.", ""))
        }

    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication ==
                UsernamePasswordAuthenticationToken::class.java
    }
    fun getUserInfo(accessToken: String): UserResponse {
        val cognitoClient: AWSCognitoIdentityProvider = cognitoService.getAmazonCognitoIdentityClient()
        return try {
            if (accessToken.isBlank()) {
                throw CognitoException(
                    "User must provide an access token",
                    CognitoException.INVALID_ACCESS_TOKEN_EXCEPTION,
                    "User must provide an access token"
                )
            }
            val userRequest =
                GetUserRequest()
                    .withAccessToken(accessToken)
            val userResult = cognitoClient.getUser(userRequest)
            val userAttributes =
                userResult.userAttributes
            getUserAttributesData(userAttributes, userResult.username)
        } catch (e: AWSCognitoIdentityProviderException) {
           // classLogger.error(e.message, e)
            throw CognitoException(e.message.toString(), e.errorCode, e.message + e.errorCode)
        } catch (e: Exception) {
            //  classLogger.error(e.message, e)
            throw CognitoException(e.message.toString(), CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


    fun getUserAttributesData(userAttributes: List<AttributeType>, username: String): UserResponse {
        val userResponse = UserResponse()
        userResponse.username=(username)
        for (attribute in userAttributes) {
            if (attribute.name == "email") {
                userResponse.email=(attribute.value)
            } else if (attribute.name == "phone_number") {
                userResponse.phoneNumber=(attribute.value)
            } else if (attribute.name == "name") {
                userResponse.name=(attribute.value)
            } else if (attribute.name == "family_name") {
                userResponse.lastname=(attribute.value)
            } else if (attribute.name == "custom:companyPosition") {
                userResponse.companyPosition=(attribute.value)
            }
        }
        return userResponse
    }




}