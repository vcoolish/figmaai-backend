package com.app.drivn.backend.user.service

import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
  private val repository: UserRepository
) {

  fun getOrCreate(address: String): User {
    return repository.findById(address)
      .orElseGet { repository.save(User(address)) }
  }

  fun get(address: String): User {
    return repository.findById(address).orElseThrow()
  }

  fun save(user: User) {
    repository.save(user)
  }

}
