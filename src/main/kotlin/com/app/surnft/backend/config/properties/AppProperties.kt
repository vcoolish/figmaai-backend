package com.app.surnft.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
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

  @Positive
  val defaultUserEnergyLimit: BigDecimal = BigDecimal.valueOf(30),

  @Positive
  val defaultUserTokensLimitPerDay: BigDecimal,

  @NotNull
  val energyRenewRate: Duration,

  @Positive
  val energyRenewPercent: BigDecimal = BigDecimal.valueOf(0.2),

  @Positive
  val durabilityRepairCost: Double = 0.625,

  @Positive
  val levelUpCarCost: BigDecimal = BigDecimal.valueOf(100),

  val carLevelDistanceRequirement: Map<@Positive Short, @Positive Int> = emptyMap(),

  @NotBlank
  val adminAddress: String,

  @NotBlank
  val contractAddress: String,

  @NotBlank
  val collectionAddress: String,

  @NotBlank
  val clientUrl: String,

  @NotBlank
  val secondClientUrl: String,

  val chainId: Long = 56,

  @NotBlank
  val arweaveUrl: String,

  val bernoulliRequestedPercentage: Short = 1,
) {

  fun getErcFile(): Resource {
    return ClassPathResource("erc20.json")
  }
}
