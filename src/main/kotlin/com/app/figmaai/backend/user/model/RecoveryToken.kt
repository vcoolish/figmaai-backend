package com.app.figmaai.backend.user.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import javax.persistence.*

@Entity
@Table(name = "recovery_tokens")
class RecoveryToken(
  @Id
  @SequenceGenerator(name = "recovery_tokens_id_seq", sequenceName = "recovery_tokens_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recovery_tokens_id_seq")
  private var id: Long? = null,

  var writeToken: String? = null,
  var email: String? = null,
  var redeemed: Boolean = false,

) : AbstractJpaPersistable<Long>() {
  override fun getId(): Long? = id
}