package com.app.surnft.backend.nft.data

import java.math.BigDecimal

data class CarRepairInfo(
  val repairableAmount: Float,
  val repairableCost: BigDecimal
) {

  constructor() : this(0F, BigDecimal.ZERO)
}
