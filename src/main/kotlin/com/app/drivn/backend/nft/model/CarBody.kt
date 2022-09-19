package com.app.drivn.backend.nft.model

import java.math.BigDecimal
import java.math.BigDecimal.*

enum class CarBody(
  val durabilityCoefficient: BigDecimal,
  val fuelEfficiency: BigDecimal,
  val earnEfficiency: BigDecimal
) {
  WORKER(valueOf(0.8), valueOf(0.1), valueOf(1.3)),
  BASIC(ONE, ZERO, ONE),
  RACING(valueOf(1.3), valueOf(-0.2), valueOf(0.8)),
  SUPERSPORT(valueOf(1.2), valueOf(-0.15), valueOf(0.85));

}
