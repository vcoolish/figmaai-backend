package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.OAuthToken
import org.springframework.stereotype.Repository


@Repository
interface OauthRepository : JpaSpecificationRepository<OAuthToken> {

  fun findByWriteToken(writeToken: String): OAuthToken?

  fun findByReadToken(readToken: String): OAuthToken?

  fun findByFigma(figma: String): OAuthToken?
}
