package com.app.figmaai.backend.subscription.model

data class Subscription(
  val id: String,
  val status: String,
  val renews_at: String? = null,
  val ends_at: String? = null,
  val created_at: String? = null,
  val trial_ends_at: String? = null,
  val variant_id: String? = null,
)
data class LemonResponse(
  val data: LemonSubscription,
)

data class LemonListResponse(
  val data: List<LemonSubscription>,
)

data class LemonSubscription(
  val id: String,
  val attributes: SubscriptionAttributes,
)

data class SubscriptionAttributes(
  val user_email: String?,
  val status: String?,
  val status_formatted: String?,
  val pause: String?,
  val cancelled: Boolean?,
  val trial_ends_at: String?,
  val urls: Any?,
  val renews_at: String?,
  val ends_at: String?,
  val created_at: String?,
  val variant_id: Int?,
  val order_id: Int?,
)

data class PaypalSubscription(
  val status: String?,
  val status_update_time: String?,
  val id: String?,
  val plan_id: String?,
  val start_time: String?,
  val quantity: String?,
  val subscriber: Subscriber?,
  val create_time: String?,
  val update_time: String?,
  val plan_overridden: Boolean?,
  val billing_info: BillingInfo?,
) {

  val generations = when (plan_id) {
    "P-5C904719PS7528140MRGJZSQ" -> 300
    "P-2KK54741YU675415DMRGJZ7I" -> 500
    "P-33302185HJ953941AMRGJ2LA" -> 1000
    "P-0P805065T1698011YMRGJ2XI" -> 300
    "P-6L277222N7410725SMRGJ3AY" -> 500
    "P-8F65766989155762JMRGJ3MI" -> 1000
    else -> 300
  }

  data class BillingInfo(
    val cycle_executions: List<CycleExecution>?,
    val next_billing_time: String?,
  ) {

    data class CycleExecution(
      val tenure_type: String,
      val sequence: Int,
      val cycles_completed: Int,
      val cycles_remaining: Int,
      val current_pricing_scheme_version: Int,
      val total_cycles: Int,
    )
  }

  data class Subscriber(
    val email_address: String,
    val payer_id: String,
    val name: Name,
  ) {

    data class Name(
      val given_name: String,
      val surname: String,
    )
  }
}