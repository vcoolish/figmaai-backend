package com.app.figmaai.backend.user.dto

import javax.validation.constraints.NotBlank

data class LoginDto(
  @field: NotBlank
  override val login: String,

  @field: NotBlank
  override val password: String,

  override val writeToken: String? = null,
): LoginData