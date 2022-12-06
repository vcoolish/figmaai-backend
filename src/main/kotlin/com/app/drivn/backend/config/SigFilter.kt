package com.app.drivn.backend.config

import com.app.drivn.backend.blockchain.service.validateMessageSign
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.config.properties.WebSecurityProperties
import com.app.drivn.backend.user.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import java.util.stream.Collectors
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse


@Component
class SigFilter(
  private val properties: AppProperties,
  private val userService: UserService,
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

  // TODO: figure out AuthenticationFilter, AuthorizationFilter, FilterSecurityInterceptor
  //  and maybe WebExpressionVoter, DefaultFilterInvocationSecurityMetadataSource
  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val normRequest = NormilizedHeaderRequest(request)

    if (isNotSecured(normRequest)) {
      filterChain.doFilter(normRequest, response)
      return
    }

    val cachedRequest = CopyingRequestWrapper(normRequest)
    val address = cachedRequest.getHeader("address")

    val signMessage = userService.getSignMessage(address)

    val sig: String? = Optional.ofNullable(normRequest.getHeader("signature"))
      .orElseGet { cachedRequest.getParameter("signature") }
    //todo: consider validating params too
    // val message = buildSignedPayload(cachedRequest)

    val isValid = validateMessageSign(
      address = address ?: "",
      message = signMessage.orElse(""),
      signature = sig ?: "",
    )
    if (isValid) {
      SecurityContextHolder.getContext().authentication = EMPTY_AUTH_TOKEN
    }
    filterChain.doFilter(cachedRequest, response)
  }

  private fun buildSignedPayload(cachedRequest: CopyingRequestWrapper): String = buildString {
    append(properties.sigKey)
    cachedRequest.parameterMap.forEach { (key, value) ->
      if (key != "signature") {
        append(value.fold("") { acc, str -> acc + str })
      }
    }

    // request body
    val inputStreamBytes: ByteArray = StreamUtils.copyToByteArray(cachedRequest.inputStream)
    if (inputStreamBytes.isNotEmpty()) {
      val jsonRequest: MutableMap<String, String> = ObjectMapper().readValue(inputStreamBytes)
      append(jsonRequest.values.stream().collect(Collectors.joining()))
    }
  }
}

class NormilizedHeaderRequest(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {

  override fun getHeader(name: String): String {
    val header = super.getHeader(name) ?: super.getHeader(name.lowercase())
    return header ?: super.getParameter(name)
  }

  override fun getMethod(): String {
    return super.getMethod().uppercase()
  }

  override fun getHeaderNames(): Enumeration<String>? {
    val names: MutableList<String> = Collections.list(super.getHeaderNames())
    names.addAll(Collections.list(super.getParameterNames()))
    return Collections.enumeration(names)
  }

  override fun getHeaders(name: String?): Enumeration<String> {
    return super.getHeaders(name?.lowercase()) ?: super.getHeaders(name)
  }
}