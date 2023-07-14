package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.subscription.model.LemonResponse
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
      SubscriptionProvider.paypal -> paypalValidator.status(id, null)
      SubscriptionProvider.lemon -> lemonValidator.status("", id)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        Subscription(id, "active")
      }
    }
    require(subscription.status == "active")
    val type = SubscriptionType.values().find { it.lemonId == subscription.variant_id }

    if (user.subscriptionId != subscription.id) {
      val maxGenerations = type?.generations?.toLong() ?: 800L
      val maxCredits = type?.tokens?.toLong() ?: 116000L
      user.subscriptionId = subscription.id
      user.subscriptionProvider = provider
      user.generations = maxGenerations
      user.maxGenerations = maxGenerations
      user.credits = maxCredits
      user.maxCredits = maxCredits
    }
    eventPublisher.publishEvent(UserSubscribedEvent())
    repository.save(user)
    return user
  }

  fun updateSubscription(body: LemonResponse): User {
    val attrs = body.data.attributes
    val user = getUserByEmail(attrs.user_email ?: error("User email is null"))
    val type = SubscriptionType.values().find { it.lemonId == attrs.variant_id.toString() }
    val subscriptionId = body.data.id
    if (user.subscriptionId != subscriptionId) {
      val maxGenerations = type?.generations?.toLong() ?: 800L
      val maxCredits = type?.tokens?.toLong() ?: 116000L
      user.subscriptionId = subscriptionId
      user.subscriptionProvider = SubscriptionProvider.lemon
      user.generations = maxGenerations
      user.maxGenerations = maxGenerations
      user.credits = maxCredits
      user.maxCredits = maxCredits
    }
    user.isSubscribed = attrs.status == "active"
    eventPublisher.publishEvent(UserSubscribedEvent())
    repository.save(user)
    return user
  }

  fun getSubscription(email: String): Subscription {
    val user = getUserByEmail(email)
    val id = user.subscriptionId
      ?: throw BadRequestException("User ${user.email} has no subscription")
    return when (user.subscriptionProvider) {
      SubscriptionProvider.paypal -> paypalValidator.status(id, null)
      SubscriptionProvider.lemon -> lemonValidator.status(id, null)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        Subscription(id, "active")
      }
      else -> throw BadRequestException("User ${user.email} has no subscription")
    }
  }

  fun tryToValidateSubscription(user: User): User {
    sync.execute(user.id.toString()) {
      val subscription = getSubscription(user.email)
      user.isSubscribed = subscription.status == "active" && !user.subscriptionId.isNullOrEmpty()
      val now = ZonedDateTime.now()
      val shouldRenew = user.isSubscribed && (now.minusMonths(1).isAfter(user.lastSubscriptionData))
      if (shouldRenew) {
        val subscriptionType = SubscriptionType.values().find { it.lemonId == subscription.variant_id }
        val maxGenerations = subscriptionType?.generations?.toLong() ?: 800
        val maxCredits = subscriptionType?.tokens?.toLong() ?: 116000L
        user.generations = maxGenerations
        user.maxGenerations = maxGenerations
        user.credits = maxCredits
        user.maxCredits = maxCredits
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
