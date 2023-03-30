package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.SocialConnection
import org.springframework.stereotype.Repository

@Repository
interface SocialConnectionRepository : JpaSpecificationRepository<SocialConnection> {
  fun findByState(state: String): SocialConnection?
}
