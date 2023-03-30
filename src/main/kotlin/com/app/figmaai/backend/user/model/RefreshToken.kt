package com.app.figmaai.backend.user.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(

  @Id
  @SequenceGenerator(name = "refresh_tokens_id_seq", sequenceName = "refresh_tokens_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_tokens_id_seq")
  private var id: Long? = null,

  var token: String? = null,

  @OneToOne(targetEntity = User::class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  var user: User? = null,

  var expirationDate: Date? = null,

  var hash: String? = null

) : AbstractJpaPersistable<Long>() {
  override fun getId(): Long? = id
}