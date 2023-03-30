package com.app.figmaai.backend.user.dto

interface SocialConnectionCreateData {
  val state: String

  val provider: String

  val redirectUrl: String

  val prodApiKey: String?
    get() = null
}
