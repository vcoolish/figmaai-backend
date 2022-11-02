package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto
import java.math.BigDecimal

data class UserExtendedDto(
  val nfts: List<NftInfoDto>,
  val tokensEarnedForDay: BigDecimal
) : UserInfoDto()
