package com.app.drivn.backend.drive.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

data class DriveInfoDto(
  val distance: BigDecimal,
  val energy: BigDecimal,
  val maxEnergy: BigDecimal,
  val balance: BigDecimal,
  val tokenClaimable: BigDecimal,
  val tokensLimitPerDay: BigDecimal,
  val nextEnergyRenew: ZonedDateTime?
)
