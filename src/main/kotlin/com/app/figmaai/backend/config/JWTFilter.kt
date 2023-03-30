package com.app.figmaai.backend.config

import com.app.figmaai.backend.user.service.HttpServletRequestTokenHelper
import com.app.figmaai.backend.user.service.TokenProvider
import io.jsonwebtoken.JwtException
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class JWTFilter(
  private val tokenHelper: HttpServletRequestTokenHelper,
  private val tokenProvider: TokenProvider
) : Filter {

  override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
    val jwt = tokenHelper.resolveToken(servletRequest as HttpServletRequest)
    if (!jwt.isNullOrBlank()) {
      try {
        val claims = tokenProvider.getClaimsFromToken(jwt)
        if (tokenProvider.isClaimsContainsAuthKey(claims)) {
          val authentication = tokenProvider.getAuthentication(claims, jwt)
          SecurityContextHolder.getContext().authentication = authentication
        }
      } catch (ex: JwtException) {
        tokenProvider.logError(ex)
      }
    }
    filterChain.doFilter(servletRequest, servletResponse)
  }
}