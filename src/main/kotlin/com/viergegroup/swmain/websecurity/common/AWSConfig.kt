package com.viergegroup.swmain.websecurity.common

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:aws.properties")
@ConfigurationProperties
class AWSConfig {
    /**
     * @return the clientId
     */
    /**
     * @param clientId the clientId to set
     */
    var clientId: String="3sa13618lfn4os8ad5bqbnei20"
    /**
     * @return the poolId
     */
    /**
     * @param poolId the poolId to set
     */
    var poolId: String="us-east-1_0zxZspCeh"
    /**
     * @return the endpoint
     */
    /**
     * @param endpoint the endpoint to set
     */
    var endpoint: String="https://viergegroup.auth.us-east-1.amazoncognito.com/oauth2/token"
    /**
     * @return the region
     */
    /**
     * @param region the region to set
     */
    var region: String="us-east-1"
    /**
     * @return the identityPoolId
     */
    /**
     * @param identityPoolId the identityPoolId to set
     */
    var identityPoolId: String="us-east-1_0zxZspCeh"
    /**
     * @return the developerGroup
     */
    /**
     * @param developerGroup the developerGroup to set
     */
    var developerGroup: String? = null

    /**
     * @return the userNameField
     */
    val userNameField = "cognito:username"

    /**
     * @return the groupsField
     */
    val groupsField = "cognito:groups"

    /**
     * @return the connectionTimeout
     */
    val connectionTimeout = 2000

    /**
     * @return the readTimeout
     */
    val readTimeout = 2000



    val httpHeader = "Authorization"

    //val httpHeader = "access_token"


    val jwkUrl: String
        get() {
            val cognitoURL = StringBuilder()
            cognitoURL.append(COGNITO_IDENTITY_POOL_URL)
            cognitoURL.append(JSON_WEB_TOKEN_SET_URL_SUFFIX)
            return String.format(cognitoURL.toString(), region, poolId)
        }
    val cognitoIdentityPoolUrl: String
        get() = String.format(COGNITO_IDENTITY_POOL_URL, region, poolId)

    companion object {
        private const val COGNITO_IDENTITY_POOL_URL = "https://cognito-idp.%s.amazonaws.com/%s"
        private const val JSON_WEB_TOKEN_SET_URL_SUFFIX = "/.well-known/jwks.json"
    }
}
