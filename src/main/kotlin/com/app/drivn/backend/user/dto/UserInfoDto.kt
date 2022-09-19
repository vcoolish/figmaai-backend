package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto
import java.math.BigDecimal
import java.time.ZonedDateTime

data class UserInfoDto(
  val distance: BigDecimal,
  val energy: BigDecimal,
  val maxEnergy: BigDecimal,
  val tokenClaimable: BigDecimal,
  val tokensLimitPerDay: BigDecimal,
  val nextEnergyRenew: ZonedDateTime?,
  val nfts: List<NftInfoDto>
)
