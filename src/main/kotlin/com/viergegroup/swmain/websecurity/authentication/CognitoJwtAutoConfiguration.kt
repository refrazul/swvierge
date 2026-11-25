package com.viergegroup.swmain.websecurity.authentication

import com.nimbusds.jose.JWSAlgorithm.RS256
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.jose.util.ResourceRetriever
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.viergegroup.swmain.websecurity.common.AWSConfig
import com.viergegroup.swmain.websecurity.filter.AwsCognitoIdTokenProcessor
import com.viergegroup.swmain.websecurity.filter.AwsCognitoJwtAuthenticationFilter
import com.viergegroup.swmain.websecurity.model.CognitoJwtIdTokenCredentialsHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.*
import java.net.MalformedURLException
import java.net.URL


@Configuration
@Import(AWSConfig::class)
@ConditionalOnClass(
    value = [AwsCognitoJwtAuthenticationFilter::class, AwsCognitoIdTokenProcessor::class]
)
class CognitoJwtAutoConfiguration {
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun awsCognitoCredentialsHolder(): CognitoJwtIdTokenCredentialsHolder {
        return CognitoJwtIdTokenCredentialsHolder()
    }

    @Bean
    fun awsCognitoIdTokenProcessor(): AwsCognitoIdTokenProcessor {
        return AwsCognitoIdTokenProcessor()
    }

    @Bean
    fun jwtAuthenticationProvider(): CognitoJwtAuthenticationProvider {
        return CognitoJwtAuthenticationProvider()
    }

    @Bean
    fun awsCognitoJwtAuthenticationFilter(): AwsCognitoJwtAuthenticationFilter {
        return AwsCognitoJwtAuthenticationFilter(awsCognitoIdTokenProcessor())
    }

    @Autowired(required = true)
    private lateinit var jwtConfiguration: AWSConfig

    /**
     * Method that exposes the cognito configuration.
     * @return ConfigurableJWTProcessor
     * @throws MalformedURLException
     */
    @Bean
    @Throws(MalformedURLException::class)
    fun configurableJWTProcessor(): ConfigurableJWTProcessor<SecurityContext>? {
        val resourceRetriever: ResourceRetriever = DefaultResourceRetriever(
            jwtConfiguration.connectionTimeout,
            jwtConfiguration.readTimeout
        )
        //https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json.
        val jwkSetURL = URL(jwtConfiguration.jwkUrl)
        //Creates the JSON Web Key (JWK)
        val keySource: JWKSource<SecurityContext> = RemoteJWKSet(jwkSetURL, resourceRetriever)
        val keySelector: JWSKeySelector<SecurityContext> = JWSVerificationKeySelector<SecurityContext>(RS256, keySource)
        val jwtProcessor: ConfigurableJWTProcessor<SecurityContext> = DefaultJWTProcessor<SecurityContext>()

        jwtProcessor.setJWSKeySelector(keySelector)

        return jwtProcessor
    }




}