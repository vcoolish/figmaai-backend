package com.app.figmaai.backend.user.dto

interface SocialConnectionUpdateData {
  val state: String?
    get() = null

  val provider: String?
    get() = null
  val redirectUrl: String?
    get() = null

  val prodApiKey: String?
    get() = null
}