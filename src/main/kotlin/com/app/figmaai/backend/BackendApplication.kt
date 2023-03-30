package com.app.figmaai.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class BackendApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<com.app.figmaai.backend.BackendApplication>(*args)
    }
  }
}
