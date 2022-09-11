package com.app.drivn.backend.drive.dto

import java.math.BigDecimal

data class DriveInfoDto(
  val distance: Float,
  val energy: Float,
  val maxEnergy: Float,
  val tokenClaimable: BigDecimal,
  val tokensLimitPerDay: BigDecimal
)
