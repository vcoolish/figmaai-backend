package com.app.figmaai.backend.user.repository

import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionData
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository

class ProviderSignInAttemptCustom(connection: Connection<*>) {

  private val connectionData: ConnectionData = connection.createData()

  fun getConnection(connectionFactoryLocator: ConnectionFactoryLocator): Connection<*> {
    return connectionFactoryLocator.getConnectionFactory(connectionData.providerId).createConnection(connectionData)
  }

  fun addConnection(
    userId: String?,
    connectionFactoryLocator: ConnectionFactoryLocator,
    connectionRepository: UsersConnectionRepository
  ) {
    connectionRepository.createConnectionRepository(userId).addConnection(getConnection(connectionFactoryLocator))
  }
}