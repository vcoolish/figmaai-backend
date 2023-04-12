package com.app.figmaai.backend.user.repository

import com.app.figmaai.backend.common.repository.JpaSpecificationRepository
import com.app.figmaai.backend.user.model.OAuthToken
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@Repository
interface OauthRepository : JpaSpecificationRepository<OAuthToken> {

  fun findByWriteToken(writeToken: String): OAuthToken?

  fun findByReadToken(readToken: String): OAuthToken?

  fun findByFigma(figma: String): OAuthToken?
}
