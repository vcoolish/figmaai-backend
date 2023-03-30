package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.dto.SocialUserUpdateData
import com.app.figmaai.backend.user.model.SocialUserCreateData
import org.springframework.social.connect.Connection
import org.springframework.social.google.api.Google
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class GoogleUserInfoCollector(
  private val passwordGenerator: PasswordGenerator,
) : SocialUserInfoCollector<SocialUserCreateData, SocialUserUpdateData> {

  override fun collectCreateInfo(connection: Connection<*>, figma: String): SocialUserCreateData {
    val googleApi = (connection as Connection<Google>).api
    val userInfo = googleApi.oauth2Operations().userinfo
    return SocialUserCreateData(
      email = userInfo.email.orEmpty(),
      password = passwordGenerator.generate(),
      googleId = userInfo.id,
      figma = figma,
    )
  }

  override fun collectUpdateInfo(connection: Connection<*>): SocialUserUpdateData {
    val googleApi = (connection as Connection<Google>).api
    val userInfo = googleApi.oauth2Operations().userinfo
    return SocialUserUpdateData(
      googleId = userInfo.id,
    )
  }
}
