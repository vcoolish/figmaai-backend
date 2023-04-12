package com.app.figmaai.backend.user.dto

import java.io.Serializable

class OauthTokensDto(
  val readToken: String,
  val writeToken: String
) : Serializable
