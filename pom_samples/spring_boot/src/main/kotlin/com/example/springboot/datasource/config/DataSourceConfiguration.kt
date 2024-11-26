package com.example.springboot.datasource.config

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class DataSourceConfiguration {

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  fun dataSourceProperties(): DataSourceProperties {
    return DataSourceProperties()
  }

  @Bean
  fun dataSource(dataSourceProperties: DataSourceProperties): DriverManagerDataSource {
    val driverManagerDataSource = DriverManagerDataSource()
    driverManagerDataSource.setDriverClassName(com.mysql.cj.jdbc.Driver::class.java.name)
    driverManagerDataSource.url = dataSourceProperties.url!!
    driverManagerDataSource.username = dataSourceProperties.username!!
    driverManagerDataSource.password = dataSourceProperties.password!!
    return driverManagerDataSource
  }

  @Bean
  fun transactionManager(driverManagerDataSource: DriverManagerDataSource): DataSourceTransactionManager {
    return DataSourceTransactionManager(driverManagerDataSource)
  }

  @Bean
  fun jdbcTemplate(driverManagerDataSource: DriverManagerDataSource): JdbcTemplate {
    return JdbcTemplate(driverManagerDataSource)
  }
}
