package com.app.figmaai.backend.subscription.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import com.app.figmaai.backend.user.dto.SubscriptionProvider
import com.app.figmaai.backend.user.model.User
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(
  name = "subscriptions"
)
class Subscription : AbstractJpaPersistable<String>() {

  @Id
  lateinit var subscriptionId: String
  override fun getId(): String = subscriptionId

  @ManyToOne
  @JoinColumn(name = "user_id")
  lateinit var user: User
  var status: String? = null
  @Enumerated(EnumType.STRING)
  var provider: SubscriptionProvider? = null
  var subscriptionName: String? = null
  var generations: Long = 0L
  var tokens: Long = 0L
  var renewsAt: ZonedDateTime? = null
  var endsAt: ZonedDateTime? = null
  var createdAt: ZonedDateTime? = null
  var trialEndsAt: ZonedDateTime? = null
  var orderId: String? = null
  var variantId: String? = null
  var updatePaymentMethodUrl: String? = null
}
