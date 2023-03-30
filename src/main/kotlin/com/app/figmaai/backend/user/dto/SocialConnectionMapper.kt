package com.app.figmaai.backend.user.dto

import org.springframework.stereotype.Component

@Component
class SocialConnectionMapper {

  val toSocialConnectionCreateData: (SocialUserRegistrationDto, String) -> SocialConnectionCreateData = { dto, state ->
    object : SocialConnectionCreateData {
      override val state: String = state
      override val provider: String = dto.provider.id
      override val redirectUrl: String = dto.redirectUrl
      override val prodApiKey: String? = dto.prodApiKey
    }
  }
}
