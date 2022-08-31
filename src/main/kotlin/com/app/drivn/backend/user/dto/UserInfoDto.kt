package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto
import java.math.BigDecimal

data class UserInfoDto(
  val distance: Float,
  val energy: Float,
  val tokenClaimable: BigDecimal,
  val maxEnergy: Float,
  val nfts: List<NftInfoDto>
)
