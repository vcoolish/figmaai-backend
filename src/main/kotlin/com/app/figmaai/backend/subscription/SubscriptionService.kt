package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.subscription.model.*
import com.app.figmaai.backend.subscription.repository.SubscriptionRepository
import com.app.figmaai.backend.user.data.UserSubscribedEvent
import com.app.figmaai.backend.user.dto.SubscriptionProvider
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.repository.UserRepository
import com.app.figmaai.backend.user.service.HttpServletRequestTokenHelper
import com.app.figmaai.backend.user.service.TokenProvider
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest

@Service
class SubscriptionService(
  private val appProperties: AppProperties,
  private val userRepository: UserRepository,
  private val lemonValidator: LemonSubscriptionValidator,
  private val paypalValidator: PaypalSubscriptionValidator,
  private val eventPublisher: ApplicationEventPublisher,
  private val tokenProvider: TokenProvider,
  private val httpServletRequestTokenHelper: HttpServletRequestTokenHelper,
  private val subscriptionRepository: SubscriptionRepository,
) {

  private val sync: com.app.figmaai.backend.common.synchronization.SyncTemplate<String> =
    com.app.figmaai.backend.common.synchronization.SyncTemplate()

  fun getUserByEmail(email: String): User = userRepository.findOneByEmail(email)

  fun updateSubscription(email: String, id: String, provider: SubscriptionProvider): User {
    logger().info(id)
    val user = getUserByEmail(email)
    val subscription = when (provider) {
      SubscriptionProvider.paypal -> paypalValidator.status(id, null)
      SubscriptionProvider.lemon -> lemonValidator.status("", id)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        SubscriptionDto(id, "active")
      }
    }
    return updateSubscription(user, subscription, provider)
  }

  fun updateSubscription(user: User, subscription: SubscriptionDto, provider: SubscriptionProvider): User {
    val cached = subscriptionRepository.findSubscriptionByUser(user)

    val type = SubscriptionType.values().find { it.lemonId == subscription.variant_id }

    if (cached?.subscriptionId != subscription.id || cached.status != subscription.status) {
      if (cached != null && cached.subscriptionId != subscription.id) {
        subscriptionRepository.delete(cached)
      }
      user.subscriptionId = subscription.id
      user.subscriptionProvider = provider
      if (subscription.status == "active" || subscription.status == "on_trial") {
        val maxGenerations = type?.generations?.toLong() ?: 800L
        val maxCredits = type?.tokens?.toLong() ?: 116000L
        user.isSubscribed = true
        user.generations = maxGenerations
        user.maxGenerations = maxGenerations
        user.credits = maxCredits
        user.uxCredits = maxCredits
        user.maxCredits = maxCredits
      } else {
        user.isSubscribed = false
        user.generations = 0L
        user.credits = 0L
        user.uxCredits = 0L
      }
    }
    val subscriptionEntity = (cached ?: Subscription()).apply {
      this.subscriptionId = subscription.id
      this.provider = provider
      this.createdAt = runCatching { ZonedDateTime.parse(subscription.created_at) }.getOrNull()
      this.endsAt = runCatching { ZonedDateTime.parse(subscription.ends_at) }.getOrNull()
      this.trialEndsAt = runCatching { ZonedDateTime.parse(subscription.trial_ends_at) }.getOrNull()
      this.renewsAt = runCatching { ZonedDateTime.parse(subscription.renews_at) }.getOrNull()
      this.status = subscription.status
      this.user = user
      this.subscriptionName = subscription.name
      this.tokens = subscription.tokens?.toLong() ?: 0L
      this.orderId = subscription.order_id
      this.variantId = subscription.variant_id
      this.updatePaymentMethodUrl = subscription.urls?.update_payment_method
    }

    subscriptionRepository.save(subscriptionEntity)
    eventPublisher.publishEvent(UserSubscribedEvent())
    userRepository.save(user)
    return user
  }

  fun updateSubscription(body: LemonResponse): User {
    logger().info(body.toString())
    val attrs = body.data.attributes
    val type = SubscriptionType.values().find { it.lemonId == attrs.variant_id.toString() }
    val subscription = SubscriptionDto(
      id = body.data.id,
      name = attrs.variant_name,
      generations = type?.generations ?: 800,
      tokens = type?.tokens ?: 116000,
      status = attrs.status!!,
      renews_at = attrs.renews_at,
      ends_at = attrs.ends_at,
      created_at = attrs.created_at,
      variant_id = attrs.variant_id.toString(),
      trial_ends_at = attrs.trial_ends_at,
      order_id = attrs.order_id.toString(),
      urls = attrs.urls,
    )
    val user = getUserByEmail(attrs.user_email ?: error("User email is null"))
    return updateSubscription(user, subscription, SubscriptionProvider.lemon)
  }

  fun deleteSubscription(request: HttpServletRequest): User {
    val jwt = httpServletRequestTokenHelper.resolveToken(request)
    if (jwt.isNullOrEmpty()) {
      throw BadRequestException(message = "Access token not valid")
    }
    val claims = tokenProvider.getClaimsFromToken(jwt)
    val userUuid: String = claims.subject
    val user = userRepository.findByUserUuid(userUuid)
    when (user.subscriptionProvider) {
      SubscriptionProvider.paypal -> paypalValidator.delete(user.subscriptionId ?: error("Subscription not found"))
      SubscriptionProvider.lemon -> lemonValidator.delete(user.subscriptionId ?: error("Subscription not found"))
      else -> {
        throw IllegalArgumentException("Not supported")
      }
    }
    return user
  }

  fun getSubscription(email: String): SubscriptionDto {
    val user = getUserByEmail(email)
    val id = user.subscriptionId
      ?: throw BadRequestException("User ${user.email} has no subscription")
    return when (user.subscriptionProvider) {
      SubscriptionProvider.paypal,
      SubscriptionProvider.lemon -> {
        val subscription = subscriptionRepository.findSubscriptionByUser(user)
        if (subscription != null) {
          SubscriptionDto(
            id = subscription.id,
            name = subscription.subscriptionName,
            generations = subscription.generations.toInt(),
            tokens = subscription.tokens.toInt(),
            status = subscription.status ?: "",
            renews_at = subscription.renewsAt.toString(),
            ends_at = subscription.endsAt.toString(),
            created_at = subscription.createdAt.toString(),
            variant_id = subscription.variantId,
            trial_ends_at = subscription.trialEndsAt.toString(),
            order_id = subscription.orderId.toString(),
            urls = subscription.updatePaymentMethodUrl?.let { LemonUrls(it) },
          )
        } else {
          loadSubscription(email).also {
            updateSubscription(user, it, user.subscriptionProvider!!)
          }
        }
      }
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        SubscriptionDto(id, "active")
      }
      else -> throw BadRequestException("User ${user.email} has no subscription")
    }
  }

  fun loadSubscription(email: String): SubscriptionDto {
    val user = getUserByEmail(email)
    val id = user.subscriptionId
      ?: throw BadRequestException("User ${user.email} has no subscription")
    return when (user.subscriptionProvider) {
      SubscriptionProvider.paypal -> paypalValidator.status(id, null)
      SubscriptionProvider.lemon -> lemonValidator.status(id, null)
      SubscriptionProvider.google,
      SubscriptionProvider.apple -> {
        SubscriptionDto(id, "active")
      }
      else -> throw BadRequestException("User ${user.email} has no subscription")
    }
  }

  fun tryToValidateSubscription(user: User): User {
    sync.execute(user.id.toString()) {
      val now = ZonedDateTime.now()
      val subscription = loadSubscription(user.email)
      val user = updateSubscription(user, subscription, user.subscriptionProvider!!)
      val isSubscribed = subscription.status == "active" || subscription.status == "on_trial"
      val shouldRenew = isSubscribed && (now.minusDays(1).isBefore(
        runCatching { ZonedDateTime.parse(subscription.renews_at) }.getOrNull() ?: now
      ))
      if (shouldRenew) {
        val subscriptionType = SubscriptionType.values().find { it.lemonId == subscription.variant_id }
        val maxGenerations = subscriptionType?.generations?.toLong() ?: 800
        val maxCredits = subscriptionType?.tokens?.toLong() ?: 116000L
        user.generations = maxGenerations
        user.maxGenerations = maxGenerations
        user.credits = maxCredits
        user.uxCredits = maxCredits
        user.maxCredits = maxCredits
      }
      user.nextSubscriptionValidation = now.plus(appProperties.subscriptionValidationRate)
      userRepository.save(user)
    }

    return user
  }

  fun getNextValidationTime() =
    userRepository.getNextSubscriptionValidationTime()

  fun getUsersByNextSubscriptionValidation(nextValidation: ZonedDateTime) =
    userRepository.findByNextSubscriptionValidationLessThanEqualOrderByNextEnergyRenewAsc(nextValidation)

  fun getSubscriptionLinks() =
    SubscriptionType.values().map { it.getLink() }
}
