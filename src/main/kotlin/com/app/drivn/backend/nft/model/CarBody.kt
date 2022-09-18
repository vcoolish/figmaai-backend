package com.app.drivn.backend.nft.model

enum class CarBody(
  val durabilityCoefficient: Float,
  val fuelEfficiency: Float,
  val earnEfficiency: Float
) {
  WORKER(0.8F, 0.1F, 1.3F),
  BASIC(1F, 0F, 1F),
  RACING(1.3F, -0.2F, 0.8F),
  SUPERSPORT(1.2F, -0.15F, 0.85F);

}
