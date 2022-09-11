package com.app.drivn.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class BackendApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<BackendApplication>(*args)
    }
  }
}
