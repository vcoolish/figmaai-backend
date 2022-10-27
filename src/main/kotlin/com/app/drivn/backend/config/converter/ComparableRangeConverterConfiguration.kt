package com.app.drivn.backend.config.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.ConverterRegistry

@Configuration
class ComparableRangeConverterConfiguration(
  val registry: ConverterRegistry
) {

  @Bean
  fun comparableRangeConverter(): StringToComparableRangeConverterFactory {
    val factory = StringToComparableRangeConverterFactory()
    registry.addConverterFactory(factory)
    return factory
  }
}
