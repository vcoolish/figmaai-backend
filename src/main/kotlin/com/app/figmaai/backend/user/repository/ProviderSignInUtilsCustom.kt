package com.app.figmaai.backend.user.repository

import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.web.HttpSessionSessionStrategy
import org.springframework.social.connect.web.SessionStrategy

class ProviderSignInUtilsCustom(
  private val sessionStrategy: SessionStrategy,
  private val connectionFactoryLocator: ConnectionFactoryLocator,
  private val connectionRepository: UsersConnectionRepository
) {

  constructor(connectionFactoryLocator: ConnectionFactoryLocator, connectionRepository: UsersConnectionRepository) : this(
    HttpSessionSessionStrategy(),
    connectionFactoryLocator,
    connectionRepository
  )

  fun doPostSignUp(userId: String?, signInAttempt: ProviderSignInAttemptCustom?) {
    signInAttempt?.addConnection(userId, connectionFactoryLocator, connectionRepository)
  }

}
