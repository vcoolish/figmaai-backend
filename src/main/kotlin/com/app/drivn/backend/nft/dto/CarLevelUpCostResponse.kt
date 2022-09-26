package com.app.drivn.backend.nft.dto

import java.math.BigDecimal

data class CarLevelUpCostResponse(
  val cost: BigDecimal,
  val newLevel: Int
)
