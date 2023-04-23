package com.app.figmaai.backend.user.dto

class UserSubscriptionDto(
  val subscriptionId: String,
  val provider: SubscriptionProvider,
)

enum class SubscriptionProvider {
  apple,
  google,
  paypal,
}