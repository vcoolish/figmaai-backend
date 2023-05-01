package com.app.figmaai.backend.user.controller

import com.app.figmaai.backend.common.util.previewImages
import com.app.figmaai.backend.constraint.Figma
import com.app.figmaai.backend.subscription.model.PaypalSubscription
import com.app.figmaai.backend.user.dto.UserExtendedDto
import com.app.figmaai.backend.user.dto.UserRegistrationDto
import com.app.figmaai.backend.user.dto.UserSubscriptionDto
import com.app.figmaai.backend.user.mapper.UserMapper
import com.app.figmaai.backend.user.service.UserRegistrationService
import com.app.figmaai.backend.user.service.UserService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
class UserController(
  private val userService: UserService,
  private val userRegistrationService: UserRegistrationService,
) {

  @ApiResponses(
    ApiResponse(
      responseCode = "400",
      description = "REQUEST_PARAMS_INVALID"
    ),
  )
  @PostMapping("/register")
  fun registerUser(
    @RequestBody @Valid registrationDto: UserRegistrationDto,
  ): UserExtendedDto {
    val user = userRegistrationService.registerUser(registrationDto)
    return UserMapper.toExtendedDto(user)
  }

  @GetMapping("/user")
  fun getUser(
    @Figma @RequestHeader figma: String
  ): UserExtendedDto = UserMapper.toExtendedDto(
    userService.get(figma),
  )

  @PostMapping("/subscription")
  fun updateSubscription(
    @RequestBody @Valid subscriptionDto: UserSubscriptionDto,
  ): UserExtendedDto = UserMapper.toExtendedDto(
    userService.updateSubscription(subscriptionDto.email, subscriptionDto.subscriptionId, subscriptionDto.provider),
  )

  @GetMapping("/subscription/{email}")
  fun getSubscription(
    @PathVariable email: String,
  ): PaypalSubscription = userService.getSubscription(email)

  @GetMapping("/preview")
  fun getPreview(): List<String> = previewImages
}