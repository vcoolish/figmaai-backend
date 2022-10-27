package com.app.drivn.backend.config.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
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
  lateinit var sigKey: String

  @NotBlank
  lateinit var key: String

  @Positive
  var defaultUserEnergyLimit: BigDecimal = BigDecimal.valueOf(30)

  @Positive
  lateinit var defaultUserTokensLimitPerDay: BigDecimal

  @NotNull
  lateinit var energyRenewRate: Duration

  @Positive
  var energyRenewPercent: BigDecimal = BigDecimal.valueOf(0.25)

  @Positive
  var durabilityRepairCost: Double = 0.625

  @Positive
  var levelUpCarCost: BigDecimal = BigDecimal.valueOf(100)

  var carLevelDistanceRequirement: Map<@Positive Short, @Positive Int> = emptyMap()

  @Value("classpath:erc20.json")
  lateinit var ercFile: Resource

  @NotBlank
  lateinit var adminAddress: String

  @NotBlank
  lateinit var contractAddress: String

  @NotBlank
  lateinit var collectionAddress: String

  @NotBlank
  lateinit var clientUrl: String

  var chainId: Long = 56

  @NotBlank
  lateinit var arweaveUrl: String

  var bernoulliRequestedPercentage: Short = 1
}
