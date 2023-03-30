package com.app.figmaai.backend.user.model

interface UserCreateData {

  val email: String

  val password: String

  val figma: String

  val verified: Boolean?
    get() = null

  val enabled: Boolean?
    get() = null

  val googleId: String?
    get() = null
}

data class SocialUserCreateData(
  override val email: String,
  override val password: String,
  override val figma: String,
  override val verified: Boolean? = null,
  override val enabled: Boolean? = null,
  override val googleId: String? = null,
): UserCreateData