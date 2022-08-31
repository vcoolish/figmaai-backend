package com.app.drivn.backend.drive.dto

import java.math.BigDecimal

data class DriveInfoDto(
  val distance: Float,
  val energy: Float,
  val tokenClaimable: BigDecimal,
  val maxEnergy: Float,
)
