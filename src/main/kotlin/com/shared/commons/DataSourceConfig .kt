package com.shared.commons

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource
@Configuration
@Profile("!pandora") // Solo se ejecutará si NO está el perfil 'pandora'
class DataSourceConfig {

    @Value("\${spring.datasource.driver-class-name}")
    private lateinit var driverClassName: String

    @Value("\${spring.datasource.url}")
    private lateinit var url: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    @Bean
    fun dataSource(): DataSource {
        println("DataSourceConfig_dataSource")
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(driverClassName)
        dataSource.url = url
        dataSource.username = username
        dataSource.password = password
        return dataSource
    }
}
