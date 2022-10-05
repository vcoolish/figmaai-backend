package com.app.drivn.backend.config

import com.app.drivn.backend.common.util.sha256
import com.app.drivn.backend.config.properties.AppProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StreamUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import java.util.stream.Collectors
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
    val cachedRequest = CopyingRequestWrapper(request)

    val sig: String? = Optional.ofNullable(cachedRequest.getHeader("signature"))
      .orElseGet { cachedRequest.getParameter("signature") }
    val message = buildString {
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

    if (sha256(message) == sig) {
      SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
        /* principal = */
        "", /* credentials = */
        "", /* authorities = */
        listOf(GrantedAuthority { properties.baseRole }),
      )
    }
    filterChain.doFilter(cachedRequest, response)
  }
}