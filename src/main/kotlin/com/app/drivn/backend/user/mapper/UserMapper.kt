package com.app.drivn.backend.user.mapper

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.model.User

object UserMapper {

  fun toDto(user: User, nfts: List<NftInfoDto>): UserInfoDto = UserInfoDto(
    user.distance,
    user.energy,
    user.maxEnergy,
    user.tokensToClaim,
    user.tokensLimitPerDay,
    nfts
  )
}