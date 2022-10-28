package com.app.drivn.backend.user.mapper

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.user.dto.UserExtendedDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.model.User

object UserMapper {

  fun toExtendedDto(user: User): UserExtendedDto =
    toExtendedDto(user, toDto(user.nfts))

  fun toDto(user: User): UserInfoDto = toExtendedDto(user, UserInfoDto())

  fun toDto(nfts: List<CarNft>): UserExtendedDto = UserExtendedDto(
    nfts.map { NftInfoDto(it.id.toString(), it.collectionId.toString()) }
  )

  private fun <D : UserInfoDto> toExtendedDto(user: User, dto: D): D {
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
