package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.subscription.model.LemonResponse
import com.app.figmaai.backend.subscription.model.Subscription
import com.app.figmaai.backend.subscription.model.SubscriptionLink
import com.app.figmaai.backend.user.dto.UserExtendedDto
import com.app.figmaai.backend.user.dto.UserSubscriptionDto
import com.app.figmaai.backend.user.mapper.UserMapper
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Validated
@RestController
class SubscriptionController(
  private val subscriptionService: SubscriptionService,
) {

  @PostMapping("/subscription")
  fun updateSubscription(
    @RequestBody @Valid subscriptionDto: UserSubscriptionDto,
  ): UserExtendedDto = UserMapper.toExtendedDto(
    subscriptionService.updateSubscription(subscriptionDto.email, subscriptionDto.subscriptionId, subscriptionDto.provider),
  )

  @DeleteMapping("/subscription")
  fun deleteSubscription(
    request: HttpServletRequest
  ): UserExtendedDto = UserMapper.toExtendedDto(
    subscriptionService.deleteSubscription(request)
  )

  @PostMapping("/subscription-hook")
  fun onSubscription(
    @RequestBody body: LemonResponse,
  ) {
    subscriptionService.updateSubscription(body)
  }

  @GetMapping("/subscription/{email}")
  fun getSubscription(
    @PathVariable email: String,
  ): Subscription = subscriptionService.getSubscription(email)

  @GetMapping("/subscription/links")
  fun getSubscriptionLinks(): List<SubscriptionLink> = subscriptionService.getSubscriptionLinks()
}