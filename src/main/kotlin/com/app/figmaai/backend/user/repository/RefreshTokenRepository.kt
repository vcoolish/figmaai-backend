package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.RefreshToken
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RefreshTokenRepository : JpaSpecificationRepository<RefreshToken> {

  @Query(
    """
            SELECT rt FROM RefreshToken rt 
            JOIN FETCH rt.user u 
            WHERE u = :user
        """
  )
  fun findByUser(@Param("user") user: User): RefreshToken?

  fun existsByUserAndHash(user: User, hash: String): Boolean
  fun findByUserAndHash(user: User, hash: String): RefreshToken?
  fun findByToken(token: String): RefreshToken?
}