package com.app.figmaai.backend.user.dto

import javax.validation.constraints.NotBlank

class SocialUserRegistrationErrorDto(
  @field: NotBlank
  val state: String
)