package com.app.figmaai.backend.user.dto

interface UserUpdateData {

  val email: String?
    get() = null

  val password: String?
    get() = null

  val verified: Boolean?
    get() = null

  val enabled: Boolean?
    get() = null

  val googleId: String?
    get() = null
}

data class SocialUserUpdateData(
  override val email: String? = null,
  override val password: String? = null,
  override val verified: Boolean? = null,
  override val enabled: Boolean? = null,
  override val googleId: String? = null,
): UserUpdateData
