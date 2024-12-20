package com.app.figmaai.backend.user.mapper

import com.app.figmaai.backend.image.dto.ImageInfoDto
import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.user.dto.UserExtendedDto
import com.app.figmaai.backend.user.dto.UserInfoDto
import com.app.figmaai.backend.user.model.User

object UserMapper {

  fun toExtendedDto(user: User): UserExtendedDto =
    toExtendedDto(user, toDto(user.images, user.isSubscribed))

  fun toDto(user: User): UserInfoDto = toExtendedDto(user, UserInfoDto())

  fun toDto(nfts: List<ImageAI>, subscription: Boolean): UserExtendedDto = UserExtendedDto(
    nfts.map(ImageAI::getSafeId).map { ImageInfoDto(it) },
    subscription,
  )

  private fun <D : UserInfoDto> toExtendedDto(user: User, dto: D): D {
    dto.energy = user.energy
    dto.maxEnergy = user.maxEnergy
    dto.nextEnergyRenew = user.nextEnergyRenew
    dto.generations = user.generations
    dto.maxGenerations = user.maxGenerations
    dto.animations = user.animations
    dto.maxAnimations = user.maxAnimations
    dto.copyCredits = user.credits
    dto.uxCredits = user.uxCredits
    dto.maxCredits = user.maxCredits
    dto.email = user.email

    return dto
  }
}
