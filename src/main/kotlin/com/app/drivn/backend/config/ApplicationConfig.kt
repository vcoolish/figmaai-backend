package com.app.drivn.backend.config

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import java.time.Clock
import java.util.*

@Configuration
class ApplicationConfig {

  @Bean
  fun clock(): Clock {
    return Clock.systemUTC()
  }

  @Bean
  @Description("For legacy Date API only")
  fun setDefaultTimezone(clock: Clock): InitializingBean {
    return InitializingBean { TimeZone.setDefault(TimeZone.getTimeZone(clock.zone)) }
  }

}
