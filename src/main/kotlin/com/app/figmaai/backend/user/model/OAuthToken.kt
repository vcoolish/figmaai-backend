package com.app.figmaai.backend.user.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "oauth_tokens")
class OAuthToken(

  @Id
  @SequenceGenerator(name = "oauth_tokens_id_seq", sequenceName = "oauth_tokens_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oauth_tokens_id_seq")
  private var id: Long? = null,

  var readToken: String? = null,
  var writeToken: String? = null,
  var figma: String? = null,

) : AbstractJpaPersistable<Long>() {
  override fun getId(): Long? = id
}