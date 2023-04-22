package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.config.OverloadBeansEvent
import com.app.figmaai.backend.config.SocialConfig
import com.app.figmaai.backend.config.SocialConfig.Companion.connectionFactoryLocatorBeanName
import com.app.figmaai.backend.config.SocialConfig.Companion.providerSignInUtilsCustomBeanName
import com.app.figmaai.backend.config.SocialConfig.Companion.usersConnectionRepositoryBeanName
import com.app.figmaai.backend.credentials.DbCredentialsService
import com.app.figmaai.backend.credentials.GoogleCredentials
import com.app.figmaai.backend.user.dto.UpdateSocialCredentialsDto
import com.app.figmaai.backend.user.model.Providers
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialNetworkCredentialService(
  private val userService: UserService,
  private val dbCredentialsService: DbCredentialsService,
  private val appEventPublisher: ApplicationEventPublisher,
  private val providerConnectionService: ProviderConnectionService
) {

    @Transactional
    fun processUpdateSocialNetworksData(data: UpdateSocialCredentialsDto) {
        val updatedNetworkBeanName: String
        when (data.network) {
            Providers.GOOGLE -> {
                dbCredentialsService.updateGoogleCredentials(
                    GoogleCredentials(data.clientId, data.clientSecret, data.scope)
                )
                updatedNetworkBeanName = SocialConfig.googleConnectionFactoryBeanName
            }
        }
        userService.clearSignUpsForSocial(data.network)
        providerConnectionService.deleteUserConnections(data.network)
        appEventPublisher.publishEvent(
            OverloadBeansEvent(
                updatedNetworkBeanName,
                connectionFactoryLocatorBeanName,
                usersConnectionRepositoryBeanName,
                providerSignInUtilsCustomBeanName
            )
        )
    }
}
