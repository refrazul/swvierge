package com.viergegroup.swmain.websecurity.model.api

import org.springframework.security.core.AuthenticationException

class CognitoException(message: String, val errorCode: String, val detailErrorMessage: String) :
    AuthenticationException(message) {

    companion object {
        private const val serialVersionUID = 5840532488004509747L
        private const val MODULE_CODE = "cognito"
        const val GENERIC_EXCEPTION_CODE = "00"
        const val INVALID_PASS_WORD_EXCEPTION = "01"
        const val ACCESS_TOKEN_MISSING_EXCEPTION = "02"
        const val USER_MUST_CHANGE_PASS_WORD_EXCEPTION_CODE = "03"
        const val USER_MUST_DO_ANOTHER_CHALLENGE = "04"
        const val INVALID_USERNAME_EXCEPTION = "05"
        const val INVALID_ACCESS_TOKEN_EXCEPTION = "06"
        const val EMAIL_MISSING_EXCEPTION = "07"
        const val NO_TOKEN_PROVIDED_EXCEPTION = "08"
        const val INVALID_TOKEN_EXCEPTION_CODE = "09"
        const val NOT_A_TOKEN_EXCEPTION = "10"

        /**
         * getErrorCodeByService - Returns an error code using the serviceCode and the exception Code;
         * @param serviceCode
         * @param exceptionCode
         * @return errorCode
         * @user ihuaylupo
         * @since 2018-07-08
         */
        fun getErrorCodeByService(serviceCode: String, exceptionCode: String): String {
            return MODULE_CODE + serviceCode + exceptionCode
        }
    }

    val errorMessage: String
        get() = message ?: ""

}
