package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.util.Utils.randomString
import org.springframework.stereotype.Component

@Component
class PasswordGeneratorImpl : PasswordGenerator {

  companion object {
    private val passwordRegexp = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$")
  }

  override fun generate(): String {
    val password = randomString(length = 15, lower = true, upper = true, digits = true)
    if (!passwordRegexp.matches(password)) {
      return generate()
    }
    return password
  }
}

interface PasswordGenerator {
  fun generate(): String
}