package com.app.figmaai.backend.user.dto

class UserSubscriptionDto(
  val subscriptionId: String,
  val email: String,
  val provider: SubscriptionProvider,
)

enum class SubscriptionProvider {
  apple,
  google,
  paypal,
  lemon,
}