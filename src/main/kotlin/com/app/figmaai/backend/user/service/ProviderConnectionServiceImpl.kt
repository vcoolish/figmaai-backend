package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.Providers
import com.app.figmaai.backend.user.repository.ExtendedUsersConnectionRepository
import com.app.figmaai.backend.user.repository.ProviderSignInAttemptCustom
import com.app.figmaai.backend.user.repository.ProviderSignInUtilsCustom
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.support.OAuth2ConnectionFactory
import org.springframework.social.oauth2.OAuth2Parameters
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import javax.inject.Provider

@Service
class ProviderConnectionServiceImpl(
  private val connectionFactoryLocator: Provider<ConnectionFactoryLocator>,
  private val usersConnectionRepository: Provider<UsersConnectionRepository>,
  private val providerSignInUtilsCustom: Provider<ProviderSignInUtilsCustom>
) : ProviderConnectionService {

  companion object {
    private const val stateParam = "state="
    private const val nextParam = "&"
  }

  override fun createConnectionUrl(provider: Providers, redirectUrl: String): String {
    val connectionFactory = connectionFactoryLocator.get()
      .getConnectionFactory(provider.id) as OAuth2ConnectionFactory<*>
    val oauthOperations = connectionFactory.oAuthOperations

    return OAuth2Parameters(LinkedMultiValueMap())
      .apply {
        this.state = connectionFactory.generateState()
        this.redirectUri = redirectUrl
        this.scope = connectionFactory.scope
      }.let(oauthOperations::buildAuthenticateUrl)
  }

  override fun getConnection(provider: Providers, code: String, redirectUrl: String): Connection<*> {
    val connectionFactory = connectionFactoryLocator.get()
      .getConnectionFactory(provider.id) as OAuth2ConnectionFactory<*>
    return connectionFactory.oAuthOperations
      .exchangeForAccess(code, redirectUrl, null)
      .let(connectionFactory::createConnection)
  }

  override fun getState(connectUrl: String): String =
    connectUrl.substringAfter(stateParam).substringBefore(nextParam)

  override fun isExistUserConnection(connection: Connection<*>): Boolean =
    usersConnectionRepository.get().findUserIdsWithConnection(connection).isNotEmpty()

  override fun doPostSignUp(connection: Connection<*>) {
    ProviderSignInAttemptCustom(connection)
      .run { providerSignInUtilsCustom.get().doPostSignUp(connection.fetchUserProfile().email, this) }
  }

  override fun deleteUserConnections(provider: Providers) {
    (usersConnectionRepository.get() as ExtendedUsersConnectionRepository).deleteAll(provider.id)
  }
}
