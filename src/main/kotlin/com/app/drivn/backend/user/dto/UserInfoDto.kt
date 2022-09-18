package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto
import java.math.BigDecimal
import java.time.ZonedDateTime

data class UserInfoDto(
  val distance: Float,
  val energy: Float,
  val maxEnergy: Float,
  val tokenClaimable: BigDecimal,
  val tokensLimitPerDay: BigDecimal,
  val nextEnergyRenew: ZonedDateTime?,
  val nfts: List<NftInfoDto>
)
