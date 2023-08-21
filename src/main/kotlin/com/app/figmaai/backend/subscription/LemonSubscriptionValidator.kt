package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.subscription.dto.PauseRequest
import com.app.figmaai.backend.subscription.model.LemonListResponse
import com.app.figmaai.backend.subscription.model.LemonResponse
import com.app.figmaai.backend.subscription.model.SubscriptionDto
import com.app.figmaai.backend.subscription.model.SubscriptionType
import liquibase.pro.packaged.d
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class LemonSubscriptionValidator(
  val appProperties: AppProperties,
): SubscriptionValidator {

  private val restTemplate = RestTemplate()

  override fun validate(id: String) {
    val status = status(id, null).status
    if (status != "active" && status != "on_trial") {
      error("Subscription is not active")
    }
  }

  override fun status(id: String, licenseId: String?): SubscriptionDto {
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
      return SubscriptionDto(
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
      return SubscriptionDto(
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

  override fun delete(id: String) {
    val headers = HttpHeaders()
    headers.add("Accept", "application/vnd.api+json")
    headers.add("Content-Type", "application/vnd.api+json")
    headers.add("Authorization", "Bearer ${appProperties.lemonKey}")

    val requestEntity = HttpEntity<MultiValueMap<String, String>>(headers)

    restTemplate.exchange(
      "${appProperties.lemonUrl}/v1/subscriptions/$id",
      HttpMethod.DELETE,
      requestEntity,
      Any::class.java,
    ).body ?: throw Exception("Couldn't delete")
  }

  override fun pause(id: String, isUnpause: Boolean) {
    val headers = HttpHeaders()
    headers.add("Accept", "application/vnd.api+json")
    headers.add("Content-Type", "application/vnd.api+json")
    headers.add("Authorization", "Bearer ${appProperties.lemonKey}")

    val formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX'Z'", Locale.ENGLISH)
    val date = LocalDateTime.now().plusDays(14L).format(formatter)
    val body = PauseRequest(
      data = PauseRequest.Data(
        type = "subscriptions",
        id = id,
        attributes = PauseRequest.Data.Attributes(
          pause = if (!isUnpause) {
            null
          } else {
            PauseRequest.Data.Attributes.Pause(
              mode = "free",
              resumes_at = date,
            )
          }
        )
      )
    )

    val requestEntity = HttpEntity<PauseRequest>(body, headers)

    val requestFactory = HttpComponentsClientHttpRequestFactory()
    requestFactory.setReadTimeout(600000)
    requestFactory.setConnectTimeout(600000)
    val restTemplate = RestTemplate(requestFactory)
    restTemplate.exchange(
      "${appProperties.lemonUrl}/v1/subscriptions/$id",
      HttpMethod.PATCH,
      requestEntity,
      Any::class.java,
    ).body ?: throw Exception("Couldn't delete")
  }
}