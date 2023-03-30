package com.app.figmaai.backend.user.dto

import com.app.figmaai.backend.user.model.AuthenticationMethod
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LoginDto(
  @field: NotBlank
  override val login: String,

  @field: NotBlank
  override val password: String,

  val twoFactor: AuthenticationMethod? = null,

  @field: Size(min = 4, max = 6)
  val code: String? = null
): LoginData