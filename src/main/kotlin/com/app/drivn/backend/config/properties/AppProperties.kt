package com.app.drivn.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import javax.validation.constraints.NotBlank

@Validated
@ConfigurationProperties("application")
class AppProperties {

  @NotBlank
  lateinit var baseRole: String

  @NotBlank
  lateinit var sigKey: String

  var defaultUserEnergyLimit: Float = 30F

  lateinit var defaultUserTokensLimitPerDay: BigDecimal
}
