package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.RecoveryToken
import org.springframework.stereotype.Repository


@Repository
interface RecoveryRepository : JpaSpecificationRepository<RecoveryToken> {

  fun findByWriteToken(writeToken: String): RecoveryToken?

  fun findByEmail(email: String): RecoveryToken?
}
