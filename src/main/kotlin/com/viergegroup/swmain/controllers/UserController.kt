//@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.viergegroup.swmain.controllers;

import com.shared.commons.AppSettings
import org.springframework.web.bind.annotation.*
import java.security.Principal
import org.springframework.context.annotation.Profile

//https://github.com/erathorus/spring-kotlin-oauth2-jdbc
//mvnw.cmd spring-boot:run
@RestController
@RequestMapping("/users")
class UserController{

    @RequestMapping("/validateUser")
    fun user(user: Principal): Principal {
        return user 
    }

    @GetMapping("/check")
    fun getTestPage(): String {
        return AppSettings.getInstance().swVGVersion
    }
 
}