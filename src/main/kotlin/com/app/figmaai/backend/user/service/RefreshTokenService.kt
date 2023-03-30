package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.CreateTokenData
import com.app.figmaai.backend.user.model.RefreshToken
import com.app.figmaai.backend.user.model.UpdateTokenData
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RefreshTokenService(
  private val refreshTokenRepository: RefreshTokenRepository
) {

  fun existsByHash(user: User, hash: String): Boolean = refreshTokenRepository.existsByUserAndHash(user, hash)

  fun getOne(user: User, hash: String): RefreshToken? = refreshTokenRepository.findByUserAndHash(user, hash)
  fun getOne(token: String): RefreshToken? = refreshTokenRepository.findByToken(token)

  fun create(createData: CreateTokenData) =
    RefreshToken(
      user = createData.user,
      token = createData.token,
      expirationDate = createData.expirationDate,
      hash = createData.hash
    )

  @Transactional
  fun save(entity: RefreshToken): RefreshToken = refreshTokenRepository.save(entity)

  fun update(entity: RefreshToken, updateData: UpdateTokenData): RefreshToken =
    entity.apply {
      this.user = updateData.user ?: this.user
      this.token = updateData.token ?: this.token
      this.hash = updateData.hash ?: this.hash
      this.expirationDate = updateData.expirationDate ?: this.expirationDate
    }.run { save(this) }

  @Transactional(readOnly = false)
  fun delete(entity: RefreshToken) = refreshTokenRepository.delete(entity)
}
