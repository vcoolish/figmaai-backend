package com.app.drivn.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
class AppProperties {
    lateinit var sigKey: String
}