package com.app.surnft.backend.user.dto

import com.app.surnft.backend.nft.dto.NftInfoDto
import java.math.BigDecimal

data class UserExtendedDto(
  val nfts: List<NftInfoDto>,
  val tokensEarnedForDay: BigDecimal
) : UserInfoDto()
