package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto
import java.math.BigDecimal

data class UserInfoDto(
  val distance: Long,
  val energy: Long,
  val tokenClaimable: BigDecimal,
  val nfts: List<NftInfoDto>
)