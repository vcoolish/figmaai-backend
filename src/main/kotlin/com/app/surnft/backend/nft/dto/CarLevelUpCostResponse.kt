package com.app.surnft.backend.nft.dto

import java.math.BigDecimal

data class CarLevelUpCostResponse(
  val cost: BigDecimal,
  val newLevel: Short,
  val requiredDistance: Int
)
