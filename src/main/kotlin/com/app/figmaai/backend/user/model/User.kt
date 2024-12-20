package com.app.figmaai.backend.user.model

import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.subscription.model.Subscription
import com.app.figmaai.backend.user.dto.SubscriptionProvider
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
import java.math.BigDecimal
import java.time.ZonedDateTime
import javax.persistence.*
import javax.validation.constraints.Email


@Entity
@Table(
  name = "users",
  uniqueConstraints = [UniqueConstraint(columnNames = ["email"])]
)
class User : com.app.figmaai.backend.common.model.AbstractJpaPersistable<Long>() {

  var figma: String? = null

  @Id
  @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
  var id: Long = 0L

  override fun getId(): Long = id

  @Column(updatable = false, unique = true, length = 255)
  lateinit var userUuid: String

  @Email
  @Column(nullable = false)
  lateinit var email: String

  @JsonIgnore
  lateinit var password: String

  @Enumerated(EnumType.STRING)
  lateinit var provider: AuthProvider

  @Enumerated(EnumType.STRING)
  var subscriptionProvider: SubscriptionProvider? = null

  var googleId: String? = null

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "user")
  var images: List<ImageAI> = listOf()

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "user")
  var subscription: List<Subscription> = listOf()

  @Column(nullable = false, precision = 12, scale = 2)
  var maxEnergy: BigDecimal = BigDecimal.valueOf(30)

  @Column(nullable = false, precision = 12, scale = 2)
  var energy: BigDecimal = this.maxEnergy

  var nextEnergyRenew: ZonedDateTime? = null

  @Column(nullable = false)
  var token: String = ""

  @CreatedDate
  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime

  @Enumerated(EnumType.STRING)
  lateinit var method: AuthenticationMethod

  var enabled: Boolean = true

  var verified: Boolean = true

  var deleted: Boolean = false

  var deletedDate: ZonedDateTime? = null

  var subscriptionId: String? = null

  var isSubscribed: Boolean = false

  var lastSubscriptionData: ZonedDateTime? = null

  var nextSubscriptionValidation: ZonedDateTime? = null

  var generations: Long = 0L
  var maxGenerations: Long = 0L
  var animations: Long = 0L
  var maxAnimations: Long = 0L
  var credits: Long = 0L
  var uxCredits: Long = 0L
  var maxCredits: Long = 0L
}
