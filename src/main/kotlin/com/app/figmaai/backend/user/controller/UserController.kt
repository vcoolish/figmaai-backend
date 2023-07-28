package com.app.figmaai.backend.user.controller

import com.app.figmaai.backend.common.util.PreviewImage
import com.app.figmaai.backend.common.util.previewImages
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.user.dto.UserExtendedDto
import com.app.figmaai.backend.user.dto.UserRegistrationDto
import com.app.figmaai.backend.user.mapper.UserMapper
import com.app.figmaai.backend.user.service.UserRegistrationService
import com.app.figmaai.backend.user.service.UserService
import io.jsonwebtoken.ExpiredJwtException
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
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
    request: HttpServletRequest
  ): ResponseEntity<UserExtendedDto?> = try {
    ResponseEntity.ok(
      UserMapper.toExtendedDto(userService.get(request))
    )
  } catch (ex: BadRequestException) {
    ResponseEntity.status(403).body(null)
  } catch (ex: ExpiredJwtException) {
    ResponseEntity.status(403).body(null)
  }

  @GetMapping("/preview")
  fun getPreview(): List<String> = previewImages.map { it.image }

  @GetMapping("/preview-plugin")
  fun getPreviewPlugin(): List<PreviewImage> = previewImages
}