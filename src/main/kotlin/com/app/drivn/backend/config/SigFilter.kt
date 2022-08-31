package com.app.drivn.backend.config

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.config.properties.sha256
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SigFilter(
  private val properties: AppProperties,
) : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val sig = request.getParameter("signature")
    val message = buildString {
      append(properties.sigKey)
      request.parameterMap.forEach { (key, value) ->
        if (key != "signature") {
          append(value.fold("") { acc, str -> acc + str })
        }
      }
    }
    if (sha256(message) == sig) {
      SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken.authenticated(
          /* principal = */
          "", /* credentials = */
          "", /* authorities = */
          listOf(GrantedAuthority { properties.baseRole }),
        )
    }
    filterChain.doFilter(request, response)
  }
}