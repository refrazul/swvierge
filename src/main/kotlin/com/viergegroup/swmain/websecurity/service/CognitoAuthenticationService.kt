package com.viergegroup.swmain.websecurity.service

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.model.*
import com.viergegroup.swmain.websecurity.authentication.AWSClientProviderBuilder
import com.viergegroup.swmain.websecurity.common.AWSConfig
import com.viergegroup.swmain.websecurity.common.StringValidationHelper
import com.viergegroup.swmain.websecurity.model.SpringSecurityUser
import com.viergegroup.swmain.websecurity.model.api.*
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Boolean

@Service
class CognitoAuthenticationService {


    /**New password key */
    private val NEW_PASS_WORD = "NEW_PASSWORD"

    /**New password required challenge key */
    private val NEW_PASS_WORD_REQUIRED = "NEW_PASSWORD_REQUIRED"

    /**Password key */
    private val PASS_WORD = "PASSWORD"

    /**Username key */
    private val USERNAME = "USERNAME"

    private val classLogger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var cognitoBuilder: AWSClientProviderBuilder

    @Autowired
    lateinit var cognitoConfig: AWSConfig

    fun getAmazonCognitoIdentityClient(): AWSCognitoIdentityProvider {
        return cognitoBuilder.getAWSCognitoIdentityClient()
    }

    fun authenticate(authenticationRequest: AuthenticationRequest): SpringSecurityUser {
        WatcherGlobalLog.debug("CognitoAuthenticationService.authenticate")



        var authenticationResult: AuthenticationResultType? = null
        val cognitoClient = getAmazonCognitoIdentityClient()
        return try {
            val authParams: MutableMap<String, String> = HashMap()
            authParams[USERNAME] = authenticationRequest.username
            authParams[PASS_WORD] = authenticationRequest.password
            val authRequest = AdminInitiateAuthRequest()
            authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(cognitoConfig.clientId)
                .withUserPoolId(cognitoConfig.poolId)
                .withAuthParameters(authParams)
            val result = cognitoClient.adminInitiateAuth(authRequest)


            //Has a Challenge
            if (!result.challengeName.isEmpty()) {

                //If the challenge is required new Password validates if it has the new password variable.
                if (NEW_PASS_WORD_REQUIRED == result.challengeName) {
                    if (null == authenticationRequest.newPassword) {
                        throw CognitoException(
                            "User must provide a new password",
                            CognitoException.USER_MUST_CHANGE_PASS_WORD_EXCEPTION_CODE,
                            result.challengeName
                        )
                    } else {
                        //we still need the username
                        val challengeResponses: MutableMap<String, String> = HashMap()
                        challengeResponses[USERNAME] = authenticationRequest.username
                        challengeResponses[PASS_WORD] = authenticationRequest.password

                        //add the new password to the params map
                        challengeResponses[NEW_PASS_WORD] =
                            authenticationRequest.newPassword

                        //populate the challenge response
                        val request = AdminRespondToAuthChallengeRequest()
                        request.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                            .withChallengeResponses(challengeResponses)
                            .withClientId(cognitoConfig.clientId)
                            .withUserPoolId(cognitoConfig.clientId)
                            .withSession(result.session)
                        val resultChallenge = cognitoClient.adminRespondToAuthChallenge(request)
                        authenticationResult = resultChallenge.authenticationResult
                    }
                } else {
                    //has another challenge
                    throw CognitoException(
                        result.challengeName,
                        CognitoException.USER_MUST_DO_ANOTHER_CHALLENGE,
                        result.challengeName
                    )
                }
            } else {
                //Doesn't have a challenge
                authenticationResult = result.authenticationResult
            }

            //AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_MANAGER")
            val userAuthenticated = SpringSecurityUser(


            )
            userAuthenticated.username=   authenticationRequest.username
            userAuthenticated.password=  authenticationRequest.password
            userAuthenticated.accessToken=(authenticationResult.accessToken)
            userAuthenticated.expiresIn=(authenticationResult.expiresIn)
            userAuthenticated.tokenType=(authenticationResult.tokenType)
            userAuthenticated.refreshToken=(authenticationResult.refreshToken)
            userAuthenticated.idToken=(authenticationResult.idToken)
            if (classLogger.isInfoEnabled) {
                classLogger.debug(
                    "User successfully authenticated userInfo: username {}",
                    authenticationRequest.username
                )
            }
            userAuthenticated
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message.toString(), e.errorCode, e.message + e.errorCode)
        } catch (cognitoException: CognitoException) {
            throw cognitoException
        } catch (e: Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message.toString(), CognitoException.GENERIC_EXCEPTION_CODE, e.message.toString())
        }
    }


    fun getUserInfo(accessToken: String): UserResponse?{
        val cognitoClient = getAmazonCognitoIdentityClient()
         try {
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
             return  getUserAttributesData(userAttributes, userResult.username)
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
        return null
    }
    private fun getUserAttributesData(userAttributes: List<AttributeType>, username: String): UserResponse? {
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





    fun signUp(signUpRequest: UserSignUpRequest): UserSignUpResponse? {
        val cognitoClient = getAmazonCognitoIdentityClient()

        //If the username is empty we should put as username the email. Later on the
        //validate SignUpRequiredFields we are going to validate the email format.
        if (signUpRequest.username.isBlank()) {
            signUpRequest.username=(signUpRequest.email)
        }
        if (classLogger.isInfoEnabled) {
            classLogger.debug("creating user {}", signUpRequest.email)
        }
        return try {
            val cognitoRequest = AdminCreateUserRequest()
                .withUserPoolId(cognitoConfig.poolId)
                .withUsername(signUpRequest.username)
                .withUserAttributes(
                    AttributeType()
                        .withName("email")
                        .withValue(signUpRequest.email),
                    AttributeType()
                        .withName("name")
                        .withValue(signUpRequest.name),
                    AttributeType()
                        .withName("family_name")
                        .withValue(signUpRequest.lastname),
                    AttributeType()
                        .withName("phone_number")
                        .withValue(signUpRequest.phoneNumber),
                    AttributeType()
                        .withName("custom:brokerID")
                        .withValue(signUpRequest.brokerID),
                    AttributeType()
                        .withName("custom:companyPosition")
                        .withValue(signUpRequest.companyPosition),
                    AttributeType()
                        .withName("email_verified")
                        .withValue("true")
                )
                .withTemporaryPassword("QuhxmE472.")
                .withMessageAction("SUPPRESS")
                .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .withForceAliasCreation(Boolean.FALSE)
            val createUserResult = cognitoClient.adminCreateUser(cognitoRequest)
            val userResult = UserSignUpResponse()
            val cognitoUser = createUserResult.user
            userResult.username=(signUpRequest.username)
            userResult.email=(signUpRequest.email)
            userResult.enabled=(cognitoUser.enabled)
            userResult.userStatus=(cognitoUser.userStatus)
            userResult.lastModifiedDate=(
                    StringValidationHelper.convertDateToString(
                        cognitoUser.userLastModifiedDate,
                        "MM-dd-yyyy"
                    ).toString()
            )
            userResult.userCreatedDate=(
                    StringValidationHelper.convertDateToString(
                        cognitoUser.userCreateDate,
                        "MM-dd-yyyy"
                    ).toString()
            )
            userResult.brokerID=(signUpRequest.brokerID)
            if (classLogger.isInfoEnabled) {
                classLogger.debug("User created {} ", userResult)
            }
            userResult
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (cognitoException: CognitoException) {
            throw cognitoException
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


    fun signUpConfirmation(signUpRequest: UserSignUpRequest) {
        val temporaryPassword = "QuhxmE472."
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (signUpRequest.email.toString().isBlank()) {
            throw CognitoException("Invalid email", CognitoException.EMAIL_MISSING_EXCEPTION, "Invalid email")
        }
        if (signUpRequest.password.toString().isBlank()) {
            throw CognitoException("Invalid Password", CognitoException.INVALID_PASS_WORD_EXCEPTION, "Invalid password")
        }
        if (classLogger.isInfoEnabled) {
            classLogger.debug("confirming signup of user {}", signUpRequest.email)
        }
        try {

            //First Attempt must attempt signin with temporary password in order to establish session for password change
            val initialParams: MutableMap<String, String> = HashMap()
            initialParams[USERNAME] = signUpRequest.username
            initialParams[PASS_WORD] = temporaryPassword

            //Initializes the request.
            val initialRequest = AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withAuthParameters(initialParams)
                .withClientId(cognitoConfig.clientId)
                .withUserPoolId(cognitoConfig.poolId)

            //Invokes the cognito authentication
            val initialResponse = cognitoClient.adminInitiateAuth(initialRequest)

            //Validates if it has a new password.
            if (ChallengeNameType.NEW_PASSWORD_REQUIRED.name != initialResponse.challengeName) {
                throw CognitoException(
                    initialResponse.challengeName,
                    CognitoException.USER_MUST_DO_ANOTHER_CHALLENGE,
                    initialResponse.challengeName
                )
            }
            val challengeResponses: MutableMap<String, String> = HashMap()
            challengeResponses[USERNAME] = signUpRequest.username
            challengeResponses[PASS_WORD] = temporaryPassword
            challengeResponses[NEW_PASS_WORD] = signUpRequest.password

            //Initializes the challenge response
            val finalRequest = AdminRespondToAuthChallengeRequest()
                .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .withChallengeResponses(challengeResponses)
                .withClientId(cognitoConfig.clientId)
                .withUserPoolId(cognitoConfig.poolId)
                .withSession(initialResponse.session)

            //Invokes the cognito authentication
            val challengeResponse = cognitoClient.adminRespondToAuthChallenge(finalRequest)

            //Validates if it has another challenge
            if (!challengeResponse.challengeName.isBlank()) {
                throw CognitoException(
                    challengeResponse.challengeName,
                    CognitoException.USER_MUST_DO_ANOTHER_CHALLENGE,
                    challengeResponse.challengeName
                )
            }

            //Adds user to the developer group
            addUserToGroup(signUpRequest.username, cognitoConfig.developerGroup)
            if (classLogger.isInfoEnabled) {
                classLogger.debug("Sign up confirm successfully for user {} ", signUpRequest.username)
            }
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (cognitoException: CognitoException) {
            throw cognitoException
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


    fun addUserToGroup(username: String?, groupname: String?) {
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (classLogger.isInfoEnabled) {
            classLogger.debug(String.format("Adding user  %1\$s, to %2\$s group", username, groupname))
        }
        try {
            val addUserToGroupRequest = AdminAddUserToGroupRequest()
                .withGroupName(groupname)
                .withUserPoolId(cognitoConfig.poolId)
                .withUsername(username)
            cognitoClient.adminAddUserToGroup(addUserToGroupRequest)
            if (classLogger.isInfoEnabled) {
                classLogger.debug(String.format("User  %1\$s added to %2\$s group", username, groupname))
            }
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }

    fun deleteUser(user: String) {
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (classLogger.isInfoEnabled) {
            classLogger.debug("Delete user {}", user)
        }
        try {
            val deleteAdminUserRequest = AdminDeleteUserRequest()
                .withUserPoolId(cognitoConfig.poolId)
                .withUsername(user)
            cognitoClient.adminDeleteUser(deleteAdminUserRequest)
            if (classLogger.isInfoEnabled) {
                classLogger.debug("User deleted {}", user)
            }
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


    fun signOut(accessToken: String?, username: String?): String {
        var resultMessage: String? = null
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (classLogger.isInfoEnabled) {
            classLogger.debug("User sign out request {}", username)
        }
        return try {
            if (null != accessToken) {
                val globalSignOutRequest = GlobalSignOutRequest()
                    .withAccessToken(accessToken)
                cognitoClient.globalSignOut(globalSignOutRequest)
                resultMessage = "SUCCESS"
                if (classLogger.isInfoEnabled) {
                    classLogger.debug("User signed out {}", username)
                }
                resultMessage
            } else {
                throw CognitoException(
                    "Missing access token",
                    CognitoException.ACCESS_TOKEN_MISSING_EXCEPTION,
                    "Missing access token"
                )
            }
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (cognitoException: CognitoException) {
            throw cognitoException
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


    fun resetPassword(passwordRequest: PasswordRequest): PasswordResponse? {
        val username: String = passwordRequest.username
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (classLogger.isInfoEnabled) {
            classLogger.debug("Reset password {}", username)
        }
        return try {

            //If username is blank it throws an error
            if (username.isBlank()) {
                throw CognitoException(
                    "Invalid username",
                    CognitoException.INVALID_USERNAME_EXCEPTION,
                    "Invalid username"
                )
            }
            val forgotPasswordRequest = ForgotPasswordRequest()
                .withClientId(cognitoConfig.clientId)
                .withUsername(username)
            val forgotPasswordResult = cognitoClient.forgotPassword(forgotPasswordRequest)
            if (classLogger.isInfoEnabled) {
                classLogger.debug(
                    "Reset password response Delivery Details: {} ",
                    forgotPasswordResult.codeDeliveryDetails
                )
            }
            val passwordResponse = PasswordResponse()
            passwordResponse.destination=(forgotPasswordResult.codeDeliveryDetails.destination)
            passwordResponse.deliveryMedium=(forgotPasswordResult.codeDeliveryDetails.deliveryMedium)
            passwordResponse.username=(username)
            passwordResponse.message=("SUCCESS")
            passwordResponse
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (e: CognitoException) {
            classLogger.error(e.message, e)
            throw e
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }

    fun confirmResetPassword(passwordRequest: PasswordRequest): PasswordResponse? {
        val username: String = passwordRequest.username
        val cognitoClient = getAmazonCognitoIdentityClient()
        if (classLogger.isInfoEnabled) {
            classLogger.debug("Confirm Reset Password {}", username)
        }
        return try {
            val forgotPasswordRequest = ConfirmForgotPasswordRequest()
                .withClientId(cognitoConfig.clientId)
                .withUsername(username)
                .withPassword(passwordRequest.password)
                .withConfirmationCode(passwordRequest.confirmationCode)
            cognitoClient.confirmForgotPassword(forgotPasswordRequest)
            val passwordResponse = PasswordResponse()
            passwordResponse.username=(username)
            passwordResponse.message=("SUCCESS")
            if (classLogger.isInfoEnabled) {
                classLogger.debug("Confirm Reset Password successful for {} ", username)
            }
            passwordResponse
        } catch (e: AWSCognitoIdentityProviderException) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, e.errorCode, e.message + e.errorCode)
        } catch (e: CognitoException) {
            throw e
        } catch (e: java.lang.Exception) {
            classLogger.error(e.message, e)
            throw CognitoException(e.message!!, CognitoException.GENERIC_EXCEPTION_CODE, e.message!!)
        }
    }


}