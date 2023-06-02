package com.app.figmaai.backend.config

import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.config.properties.WebSecurityProperties
import com.app.figmaai.backend.user.service.UserService
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class SigFilter(
  private val properties: AppProperties,
//  private val userService: UserService,
  webSecurityProperties: WebSecurityProperties,
) : OncePerRequestFilter() {

  companion object {

    private val EMPTY_AUTH_TOKEN = UsernamePasswordAuthenticationToken(
      /* principal = */ "", /* credentials = */ "", /* authorities = */ listOf(),
    )

    fun constructAntMatchers(webSecurityProperties: WebSecurityProperties): List<RequestMatcher> =
      webSecurityProperties.roleAccessRestrictionPaths?.flatMap { restrictionPath ->
        restrictionPath.methods.takeUnless(Array<HttpMethod>::isEmpty)?.flatMap { method ->
          restrictionPath.paths.map { path: String ->
            AntPathRequestMatcher(path, method.toString())
          }
        } ?: restrictionPath.paths.map { path: String -> AntPathRequestMatcher(path) }
      } ?: emptyList()
  }

  private val requestMatchers: List<RequestMatcher> = constructAntMatchers(webSecurityProperties)

  fun isNotSecured(request: HttpServletRequest): Boolean {
    for (matcher in requestMatchers) {
      if (matcher.matches(request)) {
        return false
      }
    }
    return true
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    if (isNotSecured(request)) {
      filterChain.doFilter(request, response)
      return
    }
//    val figma: String = request.getHeader("figma")!!

//    val user = userService.get(figma)
//    val cachedRequest = CopyingRequestWrapper(request)
    //TODO add url check
//    if (!user.subscriptionId.isNullOrEmpty()) {
      SecurityContextHolder.getContext().authentication = EMPTY_AUTH_TOKEN
//    }
    filterChain.doFilter(request, response)
  }
}
