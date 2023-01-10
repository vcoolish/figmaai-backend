package com.app.surnft.backend.drive.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

data class DriveInfoDto(
  val distance: BigDecimal,
  val energy: BigDecimal,
  val maxEnergy: BigDecimal,
  val balance: BigDecimal,
  val tokenClaimable: BigDecimal,
  val tokensLimitPerDay: BigDecimal,
  val tokensEarnedForDay: BigDecimal,
  val donation: Short,
  val nextEnergyRenew: ZonedDateTime?
)