package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.subscription.model.LemonResponse
import com.app.figmaai.backend.subscription.model.Subscription
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class LemonSubscriptionValidator(
  val appProperties: AppProperties,
): SubscriptionValidator {

  private val restTemplate = RestTemplate()

  override fun validate(id: String) {
    val status = status(id).status
    if (status != "active") {
      error("Subscription is not active")
    }
  }

  override fun status(id: String): Subscription {
    val headers = HttpHeaders()
    headers.add("Accept", "application/vnd.api+json")
    headers.add("Content-Type", "application/vnd.api+json")
    headers.add("Authorization", "Bearer ${appProperties.lemonKey}")

    val requestEntity = HttpEntity<MultiValueMap<String, String>>(headers)

    val attrs = restTemplate.exchange(
      "${appProperties.paypalUrl}/v1/subscriptions/$id",
      HttpMethod.GET,
      requestEntity,
      LemonResponse::class.java,
    ).body?.data?.attributes ?: throw Exception("Subscription not found")
    return Subscription(
      status = attrs.status,
      renews_at = attrs.renews_at,
      ends_at = attrs.ends_at,
      created_at = attrs.created_at,
      variant_id = attrs.variant_id,
      trial_ends_at = attrs.trial_ends_at,
    )
  }

}