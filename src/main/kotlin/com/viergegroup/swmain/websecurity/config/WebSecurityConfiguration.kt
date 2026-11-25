package com.viergegroup.swmain.websecurity.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.viergegroup.swmain.entities.catalogs.OperationId
import com.viergegroup.swmain.websecurity.EncryptionFilter
import com.viergegroup.swmain.websecurity.filter.AwsCognitoJwtAuthenticationFilter
import com.viergegroup.swmain.websecurity.filter.CorsFilter
import com.viergegroup.swmain.websecurity.filter.RestAccessDeniedHandler
import com.viergegroup.swmain.websecurity.filter.SecurityAuthenticationEntryPoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
internal class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var  authProvider: CustomAuthenticationProvider

    @Autowired 
    private lateinit var awsCognitoJwtAuthenticationFilter: AwsCognitoJwtAuthenticationFilter
 
    @Throws(Exception::class)
    override fun configure(
        auth: AuthenticationManagerBuilder
    ) {
        auth.authenticationProvider(authProvider).eraseCredentials(false)
    }

    @Bean
    @Throws(java.lang.Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        /*
          Overloaded to expose Authenticationmanager's bean created by configure(AuthenticationManagerBuilder).
           This bean is used by the AuthenticationController.
         */
        return super.authenticationManagerBean()
    }

    @Throws(Exception::class)
    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/gateway/**")

        // Añadir dinámicamente las rutas accesibles desde OperationId
        OperationId.values().filter { it.isAccessible }.forEach { operation ->
            if (operation.apiPath.isNotBlank()) {
                web.ignoring().antMatchers(operation.apiPath)
                println("Ignoring: ${operation.apiPath}")

            }
        }  
    }

    @Throws(java.lang.Exception::class)
    override fun configure(httpSecurity: HttpSecurity) { 

        httpSecurity
            .csrf().disable()
            .addFilterAfter(corsFilter(), ExceptionTranslationFilter::class.java)
            .addFilterBefore(awsCognitoJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling()
            .authenticationEntryPoint(SecurityAuthenticationEntryPoint())
            .accessDeniedHandler(RestAccessDeniedHandler())
            .and()
            .anonymous()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests { authorizeRequests ->
                authorizeRequests 
                    .antMatchers("/logs/save").permitAll()
                    .antMatchers("/logs/view/**").permitAll()
                    .antMatchers("/auth").permitAll()
                    .antMatchers("/auth/sign-up").permitAll()
                    .antMatchers("/auth/**").permitAll()
                    .antMatchers("/gateway").permitAll()
                    .antMatchers("/config/save-app-config").permitAll()
                // Permitir dinámicamente las rutas accesibles desde OperationId
                OperationId.values().filter { it.isAccessible }.forEach { operation ->
                    if (operation.apiPath.isNotBlank()) {
                        authorizeRequests.antMatchers(operation.apiPath).permitAll()
                        println("Ignoring: ${operation.apiPath}")

                    }
                }

                authorizeRequests.anyRequest().authenticated()
            }

    }
    @Bean
    fun encryptionFilter(objectMapper: ObjectMapper): FilterRegistrationBean<EncryptionFilter> {
        val registrationBean = FilterRegistrationBean<EncryptionFilter>()
        registrationBean.filter = EncryptionFilter(objectMapper)
        registrationBean.addUrlPatterns("/*") // Aplica el filtro a todas las rutas
        return registrationBean
    }


      fun corsFilter(): CorsFilter{
        /*
         CORS requests are managed only if headers Origin and Access-Control-Request-Method are available on OPTIONS requests
         (this filter is simply ignored in other cases).

         This filter can be used as a replacement for the @Cors annotation.
        */
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("*")
        config.addAllowedHeader(HttpHeaders.ORIGIN)
        config.addAllowedHeader(HttpHeaders.CONTENT_TYPE)
        config.addAllowedHeader(HttpHeaders.ACCEPT)
        config.addAllowedHeader(HttpHeaders.AUTHORIZATION)
        config.addAllowedMethod(HttpMethod.GET)
        config.addAllowedMethod(HttpMethod.PUT)
        config.addAllowedMethod(HttpMethod.POST)
        config.addAllowedMethod(HttpMethod.OPTIONS)
        config.addAllowedMethod(HttpMethod.DELETE)
        config.addAllowedMethod(HttpMethod.PATCH)
        config.maxAge = 3600L
        source.registerCorsConfiguration("/v2/api-docs", config)
        source.registerCorsConfiguration("/**", config)
        return CorsFilter()
    }
  /* @Configuration
    class WebMvcConfig : WebMvcConfigurer {

        @Autowired
        private lateinit var encryptionInterceptor: EncryptionInterceptor

        override fun addInterceptors(registry: InterceptorRegistry) {
            registry.addInterceptor(encryptionInterceptor)
            println("EncryptionInterceptor registered")
        }
    }*/
}
