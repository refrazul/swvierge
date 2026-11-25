package com.viergegroup.swmain

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringApplicationContext : ApplicationContextAware {

    companion object {
        lateinit var CONTEXT: ApplicationContext
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        CONTEXT = applicationContext
    }

    fun getBean(beanName: String): Any {
        return CONTEXT.getBean(beanName)
    }

    fun <T> getBean(beanClass: Class<T>): T {
        return CONTEXT.getBean(beanClass)
    }

    /*@Bean
    @RequestScope
    fun logger(): Logger {
        return Logger()
    }*/

}
