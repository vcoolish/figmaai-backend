package com.app.drivn.backend.user.dto

import com.app.drivn.backend.nft.dto.NftInfoDto

data class UserExtendedDto(
  val nfts: List<NftInfoDto>
) : UserInfoDto()
