package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.subscription.model.LemonResponse
import com.app.figmaai.backend.subscription.model.LemonUrls
import com.app.figmaai.backend.subscription.model.SubscriptionDto
import com.app.figmaai.backend.subscription.model.SubscriptionLink
import com.app.figmaai.backend.user.dto.UserExtendedDto
import com.app.figmaai.backend.user.dto.UserSubscriptionDto
import com.app.figmaai.backend.user.mapper.UserMapper
import org.springframework.http.ResponseEntity
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
  ): ResponseEntity<UserExtendedDto?> = try {
    ResponseEntity.ok(
      UserMapper.toExtendedDto(subscriptionService.deleteSubscription(request))
    )
  } catch (ex: BadRequestException) {
    ResponseEntity.status(403).body(null)
  }

  @PatchMapping("/subscription/pause")
  fun pauseSubscription(
    request: HttpServletRequest
  ): ResponseEntity<UserExtendedDto?> = try {
    ResponseEntity.ok(
      UserMapper.toExtendedDto(subscriptionService.pauseSubscription(request))
    )
  } catch (ex: BadRequestException) {
    ResponseEntity.status(403).body(null)
  }

  @PostMapping("/subscription-hook")
  fun onSubscription(
    @RequestBody body: LemonResponse,
  ) {//pooper
    subscriptionService.updateSubscription(body)
  }

  @GetMapping("/subscription/{email}")
  fun getSubscription(
    @PathVariable email: String,
  ): SubscriptionDto = subscriptionService.getSubscription(email)

  @GetMapping("/subscription/{email}/manage")
  fun getSubscriptionUrls(
    @PathVariable email: String,
  ): LemonUrls? = subscriptionService.loadSubscription(email).urls

  @GetMapping("/subscription/links")
  fun getSubscriptionLinks(): List<SubscriptionLink> = subscriptionService.getSubscriptionLinks()
}