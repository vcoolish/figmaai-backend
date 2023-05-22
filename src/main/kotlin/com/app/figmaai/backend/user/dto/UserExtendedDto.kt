package com.app.figmaai.backend.user.dto

import com.app.figmaai.backend.image.dto.ImageInfoDto

data class UserExtendedDto(
  val images: List<ImageInfoDto>,
  val hasSubscription: Boolean,
) : UserInfoDto()
