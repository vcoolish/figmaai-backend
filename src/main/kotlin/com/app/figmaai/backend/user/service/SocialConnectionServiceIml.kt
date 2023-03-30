package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.exception.NotFoundException
import com.app.figmaai.backend.user.dto.SocialConnectionCreateData
import com.app.figmaai.backend.user.dto.SocialConnectionUpdateData
import com.app.figmaai.backend.user.model.SocialConnection
import com.app.figmaai.backend.user.repository.SocialConnectionRepository
import org.springframework.stereotype.Service

@Service
class SocialConnectionServiceImpl(
  private val repository: SocialConnectionRepository
) : SocialConnectionService {

  override fun create(createData: SocialConnectionCreateData): SocialConnection =
    SocialConnection(
      state = createData.state,
      provider = createData.provider,
      redirectUrl = createData.redirectUrl,
      prodApiKey = createData.prodApiKey
    )

  override fun save(entity: SocialConnection): SocialConnection = repository.save(entity)

  override fun update(entity: SocialConnection, updateData: SocialConnectionUpdateData): SocialConnection =
    entity
      .apply {
        state = updateData.state ?: state
        provider = updateData.provider ?: provider
        redirectUrl = updateData.redirectUrl ?: redirectUrl
        prodApiKey = updateData.prodApiKey ?: prodApiKey
      }.run(::save)

  override fun delete(entity: SocialConnection) = repository.delete(entity)

  override fun getByStateOrNull(state: String): SocialConnection? = repository.findByState(state)

  override fun getByState(state: String): SocialConnection =
    getByStateOrNull(state) ?: throw NotFoundException(
      message = "Info about social connection not found."
    )
}
