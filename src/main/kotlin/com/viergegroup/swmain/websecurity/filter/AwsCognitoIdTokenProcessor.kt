package com.viergegroup.swmain.websecurity.filter

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.proc.BadJOSEException
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.viergegroup.swmain.websecurity.common.AWSConfig
import com.viergegroup.swmain.websecurity.config.CognitoJwtAuthentication
import com.viergegroup.swmain.websecurity.model.api.CognitoException
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.text.ParseException
import java.util.function.Function
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest


//@Component
class AwsCognitoIdTokenProcessor {

    companion object{
        val INVALID_TOKEN = "Invalid Token"
        val NO_TOKEN_FOUND = "Invalid Action, no token found"

        val ROLE_PREFIX = "ROLE_"
        val EMPTY_STRING = ""
    }


    @Autowired
    private lateinit var configurableJWTProcessor: ConfigurableJWTProcessor<*>

    @Autowired
    private lateinit var  jwtConfiguration: AWSConfig

    private fun extractAndDecodeJwt(token: String): String {
       // println("AwsCognitoIdTokenProcessor.extractAndDecodeJwt, token: $token")

        var tokenResult = token
        if (token != null && token.startsWith("Bearer ")) {
            tokenResult = token.substring("Bearer ".length)
        }
        return tokenResult
    }

    @Throws(ParseException::class, BadJOSEException::class, JOSEException::class)
    fun getAuthentication(request: HttpServletRequest): Authentication {
        var idToken = request.getHeader(jwtConfiguration.httpHeader)
       // println("AwsCognitoIdTokenProcessor.getAuthentication idToken $idToken, IDHEADER: ${jwtConfiguration.httpHeader}")

        return if (idToken == null) {
            throw CognitoException(NO_TOKEN_FOUND, CognitoException.NO_TOKEN_PROVIDED_EXCEPTION, "No token found in Http Authorization Header")
        } else {
            idToken = extractAndDecodeJwt(idToken)
            val claimsSet: JWTClaimsSet?

            // To verify JWT claims:
            // 1. Verify that the token is not expired.
            // 2. The audience (aud) claim should match the app client ID created in the Amazon Cognito user pool.
            // 3. The issuer (iss) claim should match your user pool. For example, a user pool created in the us-east-1 region will have an iss value of: https://cognito-idp.us-east-1.amazonaws.com/<userpoolID>.
            // 4. Check the token_use claim.
            // 5. If you are only accepting the access token in your web APIs, its value must be access.
            // 6. If you are only using the ID token, its value must be id.
            // 7. If you are using both ID and access tokens, the token_use claim must be either id or access.
            // 8. You can now trust the claims inside the token.
            claimsSet = configurableJWTProcessor.process(idToken, null)

            if (!isIssuedCorrectly(claimsSet)) {
                throw CognitoException(INVALID_TOKEN, CognitoException.INVALID_TOKEN_EXCEPTION_CODE, String.format("Issuer %s in JWT token doesn't match cognito idp %s", claimsSet.issuer, jwtConfiguration.cognitoIdentityPoolUrl))
            }

            /*if (!isIdToken(claimsSet)) {
                throw CognitoException(INVALID_TOKEN, CognitoException.NOT_A_TOKEN_EXCEPTION, "JWT Token doesn't seem to be an ID Token")
            }*/

          /*  val username = claimsSet.claims[jwtConfiguration.userNameField]?.toString() ?: ""
            println(" username $username")
            val groups = claimsSet.claims[jwtConfiguration.groupsField] as List<String>
            for(group in groups){
                println(" group $group")
            }
            val grantedAuthorities = convertList(groups) { group -> SimpleGrantedAuthority("${ROLE_PREFIX}${group.toUpperCase()}") }
            val user = User(username, EMPTY_STRING, grantedAuthorities)
            println("USER-> user.username ${user.username}")
            return CognitoJwtAuthentication(user, claimsSet, grantedAuthorities)*/

            var grantedAuthorities= listOf<GrantedAuthority>()
            var user=User("rodrigo","_Hol@mund01",grantedAuthorities)
            var jwtClaimsSet: JWTClaimsSet?=null
            val username = claimsSet.claims[jwtConfiguration.userNameField]?.toString() ?: ""
            try{
             //   println(" username $username")
                if( claimsSet.claims[jwtConfiguration.groupsField]!=null) {
                    val groups = claimsSet.claims[jwtConfiguration.groupsField] as List<String>
                    for (group in groups) {
                      //  println(" group $group")
                    }
                    grantedAuthorities = convertList(groups) { group -> SimpleGrantedAuthority("${ROLE_PREFIX}${group.toUpperCase()}") }
                    user = User(username, EMPTY_STRING, grantedAuthorities)
                }
                // println("USER-> user.username ${user.username}")
            }catch (e:Exception){
                WatcherGlobalLog.error(" e.message ${e.message}")

            }
            return CognitoJwtAuthentication(user, claimsSet, grantedAuthorities)
        }
    }



    private fun isIssuedCorrectly(claimsSet: JWTClaimsSet): Boolean {
        //  println("isIssuedCorrectly ->claimsSet.issuer: ${claimsSet.issuer} , jwtConfiguration.cognitoIdentityPoolUrl: ${jwtConfiguration.cognitoIdentityPoolUrl} ")
        return claimsSet.issuer == jwtConfiguration.cognitoIdentityPoolUrl
    }
    private fun isIdToken(claimsSet: JWTClaimsSet): Boolean {
        // println("isIdToken ->claimsSet.getClaim(\"token_use\"): ${claimsSet.getClaim("token_use")}  ")
        return claimsSet.getClaim("token_use") == "id"
    }

    private fun <T, U> convertList(from: List<T>, func: Function<T, U>): List<U> {
        return from.stream().map(func).collect(Collectors.toList())
    }
}