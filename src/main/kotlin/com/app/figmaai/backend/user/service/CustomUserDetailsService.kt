package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.CustomUserDetails
import com.app.figmaai.backend.user.repository.UserRepository
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
  private val userRepository: UserRepository
) : UserDetailsService {

  override fun loadUserByUsername(username: String): UserDetails =
    userRepository
      .findUserByLoginWithAllRolesAndBlacklist(username)
      ?.let(::CustomUserDetails)
      ?.apply {
        AccountStatusUserDetailsChecker().check(this)
      } ?: throw Exception("User not found.")
}
