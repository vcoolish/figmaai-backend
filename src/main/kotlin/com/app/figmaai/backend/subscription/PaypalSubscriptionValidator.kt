package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.subscription.model.PaypalAccess
import com.app.figmaai.backend.subscription.model.PaypalSubscription
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
//  private val accessToken = getAccessToken()

  override fun validate(id: String) {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
//    headers.add("X-PAYPAL-SECURITY-CONTEXT", "{\"consumer\":{\"accountNumber\":1181198218909172527,\"merchantId\":\"5KW8F2FXKX5HA\"},\"merchant\":{\"accountNumber\":1659371090107732880,\"merchantId\":\"2J6QB8YJQSJRJ\"},\"apiCaller\":{\"clientId\":\"AdtlNBDhgmQWi2xk6edqJVKklPFyDWxtyKuXuyVT-OgdnnKpAVsbKHgvqHHP\",\"appId\":\"APP-6DV794347V142302B\",\"payerId\":\"2J6QB8YJQSJRJ\",\"accountNumber\":\"1659371090107732880\"},\"scopes\":[\"https://api-m.paypal.com/v1/subscription/.*\",\"https://uri.paypal.com/services/subscription\",\"openid\"]}");
    headers.add("Accept", "application/json")
    headers.setBasicAuth(appProperties.paypalId, appProperties.paypalSecret)

    val requestEntity = HttpEntity<MultiValueMap<String, String>>(headers)

    val response = restTemplate.exchange(
      "${appProperties.paypalUrl}/v1/billing/subscriptions/$id",
      HttpMethod.GET,
      requestEntity,
      PaypalSubscription::class.java,
    )
    if (response.body?.status != "ACTIVE") {
      error("Subscription is not active")
    }
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
}