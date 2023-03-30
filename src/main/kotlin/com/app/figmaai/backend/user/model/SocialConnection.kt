package com.app.figmaai.backend.user.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "social_connection")
class SocialConnection(

  @Id
  @SequenceGenerator(name = "social_connection_id_seq", sequenceName = "social_connection_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "social_connection_id_seq")
  private var id: Long? = null,

  var state: String,

  var provider: String,

  var redirectUrl: String,

  var prodApiKey: String?,

): AbstractJpaPersistable<Long>() {
  override fun getId(): Long? = id
}
