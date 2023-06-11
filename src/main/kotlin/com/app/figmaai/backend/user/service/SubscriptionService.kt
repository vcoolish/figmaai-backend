package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.subscription.LemonSubscriptionValidator
import com.app.figmaai.backend.subscription.PaypalSubscriptionValidator
import com.app.figmaai.backend.subscription.model.Subscription
import com.app.figmaai.backend.subscription.model.SubscriptionType
import com.app.figmaai.backend.user.data.UserSubscribedEvent
import com.app.figmaai.backend.user.dto.SubscriptionProvider
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class SubscriptionService(
  private val appProperties: AppProperties,
  private val repository: UserRepository,
  private val lemonValidator: LemonSubscriptionValidator,
  private val paypalValidator: PaypalSubscriptionValidator,
  private val eventPublisher: ApplicationEventPublisher,
) {

  private val sync: com.app.figmaai.backend.common.synchronization.SyncTemplate<String> =
    com.app.figmaai.backend.common.synchronization.SyncTemplate()

  fun getUserByEmail(email: String): User = repository.findOneByEmail(email)

  fun updateSubscription(email: String, id: String, provider: SubscriptionProvider): User {
    val user = getUserByEmail(email)
    val subscription = when (provider) {
      SubscriptionProvider.paypal -> paypalValidator.status(id)
      SubscriptionProvider.lemon -> lemonValidator.status(id)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        Subscription(id, "active")
      }
    }
    require(subscription.status == "active")
    val type = SubscriptionType.values().find { it.lemonId == subscription.variant_id }

    if (user.subscriptionId != subscription.id) {
      user.subscriptionId = subscription.id
      user.subscriptionProvider = provider
      user.generations = type?.tokens?.toLong() ?: 800L
    }
    eventPublisher.publishEvent(UserSubscribedEvent())
    repository.save(user)
    return user
  }

  fun getSubscription(email: String): Subscription {
    val user = getUserByEmail(email)
    val id = user.subscriptionId
      ?: throw BadRequestException("User ${user.email} has no subscription")
    return when (user.subscriptionProvider) {
      SubscriptionProvider.paypal -> paypalValidator.status(id)
      SubscriptionProvider.lemon -> lemonValidator.status(id)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        Subscription(id, "active")
      }
    }
  }

  fun tryToValidateSubscription(user: User): User {
    sync.execute(user.id.toString()) {
      val subscription = getSubscription(user.email)
      user.isSubscribed = subscription.status == "active" && !user.subscriptionId.isNullOrEmpty()
      val now = ZonedDateTime.now()
      val shouldRenew = user.isSubscribed && (now.minusMonths(1).isAfter(user.lastSubscriptionData))
      if (shouldRenew) {
        user.generations = SubscriptionType.values().find { it.lemonId == subscription.variant_id }?.tokens?.toLong()
          ?: 800
      }

      user.nextEnergyRenew = now.plus(appProperties.subscriptionValidationRate)
      repository.save(user)
    }

    return user
  }

  fun getNextValidationTime() =
    repository.getNextSubscriptionValidationTime()

  fun getUsersByNextSubscriptionValidation(nextValidation: ZonedDateTime) =
    repository.findByNextSubscriptionValidationLessThanEqualOrderByNextEnergyRenewAsc(nextValidation)

  fun getSubscriptionLinks() =
    SubscriptionType.values().map { it.getLink() }
}
