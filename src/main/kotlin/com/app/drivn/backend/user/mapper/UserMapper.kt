package com.app.drivn.backend.user.mapper

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.UserExtendedDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.model.User

object UserMapper {

  fun toDto(user: User, nfts: List<NftInfoDto>): UserExtendedDto =
    toDto(user, UserExtendedDto(nfts))

  fun toDto(user: User): UserInfoDto = toDto(user, UserInfoDto())

  private fun <D : UserInfoDto> toDto(user: User, dto: D): D {
    dto.distance = user.distance
    dto.energy = user.energy
    dto.maxEnergy = user.maxEnergy
    dto.tokenClaimable = user.tokensToClaim
    dto.tokensLimitPerDay = user.tokensLimitPerDay
    dto.nextEnergyRenew = user.nextEnergyRenew
    dto.donation = user.donation
    dto.balance = user.balance

    return dto
  }
}
