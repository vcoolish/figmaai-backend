package com.app.figmaai.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import java.time.Duration
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Validated
@ConstructorBinding
@ConfigurationProperties("application")
data class AppProperties(

  @NotBlank
  val sigKey: String,

  @NotBlank
  val key: String,

  @NotBlank
  val dalleKey: String,

  @NotBlank
  val stableKey: String,

  @Positive
  val defaultUserEnergyLimit: BigDecimal = BigDecimal.valueOf(30),

  @Positive
  val defaultUserTokensLimitPerDay: BigDecimal,

  @NotNull
  val energyRenewRate: Duration,

  @Positive
  val energyRenewPercent: BigDecimal = BigDecimal.valueOf(0.2),

  @NotBlank
  val arweaveUrl: String,

  @NotBlank
  val paypalId: String,

  @NotBlank
  val paypalSecret: String,

  @NotBlank
  val paypalUrl: String,
)
