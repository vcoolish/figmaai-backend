package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.dto.UserUpdateData
import com.app.figmaai.backend.user.model.Providers
import com.app.figmaai.backend.user.model.UserCreateData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.social.connect.Connection
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProviderSocialUserInfoCollector(
  @Qualifier("googleUserInfoCollector")
  private val googleUserCollector: SocialUserInfoCollector<UserCreateData, UserUpdateData>
) {
  private val socialUserInfoCollectorHelper =
    EnumMap<Providers, SocialUserInfoCollector<UserCreateData, UserUpdateData>>(Providers::class.java)
      .apply {
        put(Providers.GOOGLE, googleUserCollector)
      }

  fun collectCreateInfo(provider: Providers, connection: Connection<*>): UserCreateData =
    getSocialUserCreator(provider).collectCreateInfo(connection)

  fun collectUpdateInfo(provider: Providers, connection: Connection<*>): UserUpdateData =
    getSocialUserCreator(provider).collectUpdateInfo(connection)

  private fun getSocialUserCreator(provider: Providers): SocialUserInfoCollector<UserCreateData, UserUpdateData> =
    socialUserInfoCollectorHelper[provider]
      ?: throw IllegalArgumentException("No social creator for service provider ${provider.id} is registered")
}
