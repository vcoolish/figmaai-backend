package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class SecurityContextService(
  val userRepository: UserRepository,
) {
  private val log = logger()

  fun currentUser(): User {
    val userUUID = getSafeCurrentUserUUID()
    return userRepository.findUserWithAuthorities(userUUID)
      ?: throw Exception("User with UUID: $userUUID not found")
        .also { log.error(it.message) }
  }

  fun getCurrentUserUUID(): Optional<String> {
    val securityContext = SecurityContextHolder.getContext()
    return Optional.ofNullable(securityContext.authentication)
      .map { authentication ->
        when (val principal = authentication.principal) {
          is UserDetails -> principal.username
          is String -> principal
          else -> null
        }
      }
  }

  fun getSafeCurrentUserUUID(): String {
    return getCurrentUserUUID()
      .orElseThrow {
        Exception("User isn't authorized")
          .also { log.error(it.message) }
      }
  }
}