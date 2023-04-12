package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.model.UserCreateData
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
class UserRegistrationService(
  private val userService: UserService,
) {
  private val logger = logger()

  @Transactional
  fun registerUser(registerDto: UserCreateData): User {
    checkEmail(registerDto.email)

    val user = registerDto
      .let { userService.create(it) }
      .run { userService.saveNewUser(this) }

    return user
  }

  @Transactional
  fun registerUser(userDto: UserCreateData, createdBy: User? = null): User {
    checkEmail(userDto.email)

    val user = userDto
      .let { userService.create(it) }
      .apply {
        this.verified = true
        this.enabled = true
      }.run { userService.saveNewUser(this) }

    return user
  }

  fun checkEmail(email: String) {
    if (userService.isEmailExist(email)) {
      throw BadRequestException("Email $email already reserved.")
    }
  }
}