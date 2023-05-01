package com.app.figmaai.backend.subscription.model

data class PaypalSubscription(
  val status: String,
  val status_update_time: String,
  val id: String,
  val plan_id: String,
  val start_time: String,
  val quantity: String,
  val subscriber: Subscriber,
  val create_time: String,
  val update_time: String,
  val plan_overridden: Boolean,
  val billing_info: BillingInfo,
) {

  data class BillingInfo(
    val cycle_executions: List<CycleExecution>,
    val next_billing_time: String,
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