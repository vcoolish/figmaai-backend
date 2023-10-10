package com.app.figmaai.backend.user.dto

data class UpdatePasswordDto(
  val password: String,
  val writeToken: String
)
