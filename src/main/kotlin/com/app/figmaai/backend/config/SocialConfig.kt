package com.app.figmaai.backend.config

import com.app.figmaai.backend.config.CustomOverloadingSingletonEventScopeRegistryBeanFactoryPostProcessor.Companion.overloadingScope
import com.app.figmaai.backend.credentials.DbCredentialsService
import com.app.figmaai.backend.user.model.Providers
import com.app.figmaai.backend.user.repository.ExtendedUsersConnectionRepository
import com.app.figmaai.backend.user.repository.ProviderSignInUtilsCustom
import com.app.figmaai.backend.credentials.SocialNetworksContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.support.ConnectionFactoryRegistry
import org.springframework.social.google.connect.GoogleConnectionFactory
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.sql.DataSource

@Configuration
class SocialConfig {

    companion object {
        const val socialNetworksContextBeanName = "socialNetworksContext"
        const val connectionFactoryLocatorBeanName = "connectionFactoryLocator"
        const val usersConnectionRepositoryBeanName = "usersConnectionRepository"
        const val googleConnectionFactoryBeanName = "googleConnectionFactory"
        const val providerSignInUtilsCustomBeanName = "providerSignInUtilsCustom"
    }

    @Bean(socialNetworksContextBeanName)
    @Scope(overloadingScope)
    fun socialNetworksContext(dbPropertiesService: DbCredentialsService): SocialNetworksContext =
        dbPropertiesService.getActiveSocialNetworks().networks
            .split(',')
            .mapNotNull(::toProviders)
            .let { SocialNetworksContext(it) }

    private fun toProviders(value: String): Providers? =
        value.runCatching(Providers::valueOf).getOrNull()

    @Primary
    @Bean(name = [connectionFactoryLocatorBeanName])
    @Scope(overloadingScope)
    fun connectionFactoryLocator(
      @Qualifier(googleConnectionFactoryBeanName) googleConnectionFactory: Provider<GoogleConnectionFactory>,
    ): ConnectionFactoryLocator = ConnectionFactoryRegistry().apply {
        addConnectionFactory(googleConnectionFactory.get())
    }

    @Primary
    @Bean(name = [usersConnectionRepositoryBeanName])
    @Scope(overloadingScope)
    fun usersConnectionRepository(
        entityManager: EntityManager,
        dataSource: DataSource,
        connectionFactoryLocator: Provider<ConnectionFactoryLocator>
    ): UsersConnectionRepository = ExtendedUsersConnectionRepository(
        entityManager,
        dataSource,
        connectionFactoryLocator.get(),
        Encryptors.noOpText()
    )

    @Bean(name = [googleConnectionFactoryBeanName])
    @Scope(overloadingScope)
    fun googleConnectionFactory(dbPropertiesService: DbCredentialsService): GoogleConnectionFactory {
        val credentials = dbPropertiesService.getGoogleCredentials()
        return GoogleConnectionFactory(credentials.clientId, credentials.clientSecret)
            .apply { scope = credentials.scope }
    }

    @Bean(name = [providerSignInUtilsCustomBeanName])
    @Scope(overloadingScope)
    fun providerSignInUtilsCustom(
        connectionFactoryLocator: Provider<ConnectionFactoryLocator>,
        usersConnectionRepository: Provider<UsersConnectionRepository>
    ): ProviderSignInUtilsCustom =
        ProviderSignInUtilsCustom(connectionFactoryLocator.get(), usersConnectionRepository.get())
}
