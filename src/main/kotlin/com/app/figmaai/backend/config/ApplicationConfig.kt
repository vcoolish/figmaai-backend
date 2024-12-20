package com.app.figmaai.backend.config

import com.app.figmaai.backend.config.properties.AppProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import java.time.Clock
import java.util.*

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class ApplicationConfig {

  @Bean
  fun clock(): Clock = Clock.systemUTC()

  @Bean
  @Description("For legacy Date API only")
  fun setDefaultTimezone(clock: Clock): InitializingBean =
    InitializingBean { TimeZone.setDefault(TimeZone.getTimeZone(clock.zone)) }

}
