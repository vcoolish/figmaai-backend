package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.Providers
import org.springframework.social.connect.Connection

interface ProviderConnectionService {
  fun createConnectionUrl(provider: Providers, redirectUrl: String): String
  fun getConnection(provider: Providers, code: String, redirectUrl: String): Connection<*>
  fun getState(connectUrl: String): String
  fun isExistUserConnection(connection: Connection<*>): Boolean
  fun doPostSignUp(connection: Connection<*>)
  fun deleteUserConnections(provider: Providers)
}
