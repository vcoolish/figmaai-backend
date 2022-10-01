package com.app.drivn.backend.user.service

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.dto.UpdateUserRequest
import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService(
  private val repository: UserRepository,
  private val appProperties: AppProperties
) {

  fun getOrCreate(address: String): User = repository.findById(address)
    .orElseGet {
      User(
        address,
        appProperties.defaultUserTokensLimitPerDay,
        appProperties.defaultUserEnergyLimit
      ).let(repository::save)
    }

  fun get(address: String): User = repository.findById(address).orElseThrow()

  fun save(user: User) {
    repository.save(user)
  }

  fun update(address: String, request: UpdateUserRequest): User {
    val user = get(address)

    Optional.ofNullable(request.donation)
      .ifPresent { user.donation = it }

    return repository.save(user)
  }

}
