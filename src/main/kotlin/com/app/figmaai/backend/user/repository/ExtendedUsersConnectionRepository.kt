package com.app.figmaai.backend.user.repository

import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.sql.DataSource

open class ExtendedUsersConnectionRepository(
  private val entityManager: EntityManager,
  dataSource: DataSource,
  connectionFactoryLocator: ConnectionFactoryLocator,
  textEncryptor: TextEncryptor
) : JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, textEncryptor) {

  @Suppress("SpellCheckingInspection")
  @Transactional(readOnly = false)
  open fun deleteAll(providerId: String) {
    entityManager.createNativeQuery("DELETE FROM userconnection WHERE providerid = :provider")
      .setParameter("provider", providerId)
      .executeUpdate()
  }

}