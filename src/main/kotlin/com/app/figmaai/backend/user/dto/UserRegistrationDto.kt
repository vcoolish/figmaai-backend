package com.app.figmaai.backend.user.dto

import com.app.figmaai.backend.common.validator.ValidEmail
import com.app.figmaai.backend.common.validator.ValidPassword
import com.app.figmaai.backend.user.model.UserCreateData
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class UserRegistrationDto(

  @field: NotBlank(message = "{email.not_valid}")
  @field: Size(max = 255)
  @field: ValidEmail(message = "{email.not_valid}")
  override val email: String,

  @field: Size(min = 8, max = 255)
  @field: ValidPassword(message = "{password.not_valid}")
  override val password: String,

  override val figma: String,

) : UserCreateData