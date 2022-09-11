package com.app.drivn.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import java.time.Duration
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Validated
@ConfigurationProperties("application")
class AppProperties {

  @NotBlank
  lateinit var baseRole: String

  @NotBlank
  lateinit var sigKey: String

  @Positive
  var defaultUserEnergyLimit: Float = 30F

  @Positive
  lateinit var defaultUserTokensLimitPerDay: BigDecimal

  @NotNull
  lateinit var energyRenewRate: Duration
}
