package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.subscription.model.PaypalAccess
import com.app.figmaai.backend.subscription.model.PaypalSubscription
import com.app.figmaai.backend.subscription.model.SubscriptionDto
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class PaypalSubscriptionValidator(
  val appProperties: AppProperties,
): SubscriptionValidator {

  private val restTemplate = RestTemplate()

  override fun validate(id: String) {
    val status = status(id, null).status
    if (status != "ACTIVE") {
      error("Subscription is not active")
    }
  }

  override fun status(id: String, licenseId: String?): SubscriptionDto {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
    headers.add("Accept", "application/json")
    headers.setBasicAuth(appProperties.paypalId, appProperties.paypalSecret)

    val requestEntity = HttpEntity<MultiValueMap<String, String>>(headers)

    val sub = restTemplate.exchange(
      "${appProperties.paypalUrl}/v1/billing/subscriptions/$id",
      HttpMethod.GET,
      requestEntity,
      PaypalSubscription::class.java,
    ).body ?: throw Exception("Subscription not found")
    return SubscriptionDto(
      id = id,
      status = sub.status!!,
      renews_at = sub.billing_info?.next_billing_time,
      created_at = sub.create_time,
      variant_id = sub.plan_id,
    )
  }

  private fun getAccessToken(): String {
    val headers = HttpHeaders()
    headers.add("Authorization", "Basic ${appProperties.paypalId}:${appProperties.paypalSecret}")
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

    val map: MultiValueMap<String, String> = LinkedMultiValueMap()
    map.add("grant_type", "client_credentials")
    val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
    val response = restTemplate.postForEntity(
      "${appProperties.paypalUrl}/v1/oauth2/token",
      request,
      PaypalAccess::class.java
    )
    return response.body?.access_token ?: ""
  }

  override fun delete(id: String) {
    TODO("Not yet implemented")
  }

  override fun pause(id: String) {
    TODO("Not yet implemented")
  }
}