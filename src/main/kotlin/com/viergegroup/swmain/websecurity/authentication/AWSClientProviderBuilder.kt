package com.viergegroup.swmain.websecurity.authentication

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.viergegroup.swmain.websecurity.common.AWSConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class AWSClientProviderBuilder {
    @Autowired
    private lateinit var cognitoConfig: AWSConfig

    private  var  cognitoIdentityProvider: AWSCognitoIdentityProvider?=null
    private  var   s3Client: AmazonS3?=null
    private  lateinit var propertiesFileCredentialsProvider: ClasspathPropertiesFileCredentialsProvider
    private  lateinit var region: String

    private  fun initCommonInfo() {
            propertiesFileCredentialsProvider = ClasspathPropertiesFileCredentialsProvider()
            region = cognitoConfig.region
    }


    fun getAWSCognitoIdentityClient(): AWSCognitoIdentityProvider {
        if (null == cognitoIdentityProvider) {
            initCommonInfo()
            cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.standard()
                .withCredentials(propertiesFileCredentialsProvider)
                .withRegion(region)
                .build()
        }
        return cognitoIdentityProvider!!
    }

    fun getAWSS3Client(): AmazonS3 {
        if (null == s3Client) {
            initCommonInfo()
            s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(propertiesFileCredentialsProvider)
                .withRegion(region)
                .build()
        }
        return s3Client!!
    }



}