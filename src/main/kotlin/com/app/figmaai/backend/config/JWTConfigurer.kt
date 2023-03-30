package com.app.figmaai.backend.config

import com.app.figmaai.backend.user.service.HttpServletRequestTokenHelper
import com.app.figmaai.backend.user.service.TokenProvider
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component

@Component
class JWTConfigurer(
  private val tokenProvider: TokenProvider,
  private val tokenHelper: HttpServletRequestTokenHelper

) : SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http.addFilterBefore(JWTFilter(tokenHelper, tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
  }
}