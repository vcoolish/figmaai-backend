package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.subscription.model.LemonListResponse
import com.app.figmaai.backend.subscription.model.LemonResponse
import com.app.figmaai.backend.subscription.model.Subscription
import com.app.figmaai.backend.subscription.model.SubscriptionType
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
    val status = status(id, null).status
    if (status != "active") {
      error("Subscription is not active")
    }
  }

  override fun status(id: String, licenseId: String?): Subscription {
    val headers = HttpHeaders()
    headers.add("Accept", "application/vnd.api+json")
    headers.add("Content-Type", "application/vnd.api+json")
    headers.add("Authorization", "Bearer ${appProperties.lemonKey}")

    val requestEntity = HttpEntity<MultiValueMap<String, String>>(headers)

    logger().info(id)
    if (licenseId.isNullOrEmpty()) {
      val body = restTemplate.exchange(
        "${appProperties.lemonUrl}/v1/subscriptions/$id",
        HttpMethod.GET,
        requestEntity,
        LemonResponse::class.java,
      ).body
      val attrs = body?.data?.attributes ?: throw Exception("Subscription not found")
      logger().info(body.toString())
      val variant = attrs.variant_id.toString()
      val type = SubscriptionType.values().first { it.lemonId == variant }
      return Subscription(
        id = id,
        name = attrs.variant_name,
        generations = type.generations,
        tokens = type.tokens,
        status = attrs.status!!,
        renews_at = attrs.renews_at,
        ends_at = attrs.ends_at,
        created_at = attrs.created_at,
        variant_id = variant,
        trial_ends_at = attrs.trial_ends_at,
        order_id = attrs.order_id.toString(),
        urls = attrs.urls,
      )
    } else {
      val orderId = restTemplate.exchange(
        "${appProperties.lemonUrl}/v1/license-keys?key=$licenseId",
        HttpMethod.GET,
        requestEntity,
        LemonListResponse::class.java,
      ).body?.data?.firstOrNull()?.attributes?.order_id ?: throw Exception("License key not found")
      val response = restTemplate.exchange(
        "${appProperties.lemonUrl}/v1/subscriptions?order_id=$orderId",
        HttpMethod.GET,
        requestEntity,
        LemonListResponse::class.java,
      ).body?.data?.firstOrNull() ?: throw Exception("Subscription not found")
      val attrs = response.attributes
      val variant = attrs.variant_id.toString()
      val type = SubscriptionType.values().first { it.lemonId == variant }
      return Subscription(
        id = response.id,
        name = attrs.variant_name,
        generations = type.generations,
        tokens = type.tokens,
        status = attrs.status!!,
        renews_at = attrs.renews_at,
        ends_at = attrs.ends_at,
        created_at = attrs.created_at,
        variant_id = attrs.variant_id.toString(),
        trial_ends_at = attrs.trial_ends_at,
        urls = attrs.urls,
      )
    }
  }

}