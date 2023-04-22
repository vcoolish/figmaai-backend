package com.app.figmaai.backend.user.dto

import com.app.figmaai.backend.user.model.Providers
import javax.validation.constraints.NotBlank

data class UpdateSocialCredentialsDto(
  val network: Providers,
  @field: NotBlank
    val clientId: String,
  @field: NotBlank
    val clientSecret: String,
  @field: NotBlank
    val scope: String,
    val key: String,
)
