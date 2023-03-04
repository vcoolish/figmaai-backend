package com.app.surnft.backend.user.mapper

import com.app.surnft.backend.nft.dto.NftInfoDto
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.user.dto.UserExtendedDto
import com.app.surnft.backend.user.dto.UserInfoDto
import com.app.surnft.backend.user.model.User
import java.math.BigDecimal

object UserMapper {

  fun toExtendedDto(user: User, tokensEarnedForDay: BigDecimal): UserExtendedDto =
    toExtendedDto(user, toDto(user.nfts.filter { it.collectionId == 0L }, tokensEarnedForDay))

  fun toDto(user: User): UserInfoDto = toExtendedDto(user, UserInfoDto())

  fun toDto(nfts: List<ImageNft>, tokensEarnedForDay: BigDecimal): UserExtendedDto = UserExtendedDto(
    nfts.map(ImageNft::getSafeId).map { NftInfoDto(it.id.toString(), it.collectionId.toString()) },
    tokensEarnedForDay
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
    dto.collections = user.collections.map { it.id }

    return dto
  }
}
