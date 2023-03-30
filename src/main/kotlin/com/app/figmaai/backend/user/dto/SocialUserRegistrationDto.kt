package com.app.figmaai.backend.user.dto

import com.app.figmaai.backend.user.model.Providers
import org.hibernate.validator.constraints.URL
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class SocialUserRegistrationDto(
  @field: Size(max = 45)
  val prodApiKey: String? = null,

  @field: Size(max = 500)
  @field: NotBlank
  @field:URL
  val redirectUrl: String,

  val provider: Providers
)
