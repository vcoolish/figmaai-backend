package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.dto.SocialConnectionCreateData
import com.app.figmaai.backend.user.dto.SocialConnectionUpdateData
import com.app.figmaai.backend.user.model.SocialConnection

interface SocialConnectionService {
  fun create(createData: SocialConnectionCreateData): SocialConnection
  fun save(entity: SocialConnection): SocialConnection
  fun update(entity: SocialConnection, updateData: SocialConnectionUpdateData): SocialConnection
  fun delete(entity: SocialConnection)
  fun getByStateOrNull(state: String): SocialConnection?
  fun getByState(state: String): SocialConnection
}