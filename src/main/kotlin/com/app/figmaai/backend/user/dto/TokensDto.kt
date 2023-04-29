package com.app.figmaai.backend.user.dto

import java.io.Serializable

class TokensDto(
  val accessToken: String,
  val refreshToken: String,
  val email: String,
) : Serializable
