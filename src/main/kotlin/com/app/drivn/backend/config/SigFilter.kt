package com.app.drivn.backend.config

import com.app.drivn.backend.blockchain.service.validateMessageSign
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
  private val userService: UserService,
) : OncePerRequestFilter() {

  companion object {

    private val EMPTY_AUTH_TOKEN = UsernamePasswordAuthenticationToken(
      /* principal = */ "", /* credentials = */ "", /* authorities = */ listOf(),
    )
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val cachedRequest = CopyingRequestWrapper(request)

    val user = try {
      userService.get(request.getHeader("address"))
    } catch (t: Throwable) {
      null
    }
    val sig: String? = Optional.ofNullable(request.getHeader("signature"))
      .orElseGet { cachedRequest.getParameter("signature") }
    //todo: consider validating params too
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

    val isValid = validateMessageSign(
      address = user?.address ?: "",
      message = user?.signMessage ?: "",
      signature = sig ?: "",
    )
    if (isValid) {
      SecurityContextHolder.getContext().authentication = EMPTY_AUTH_TOKEN
    }
    filterChain.doFilter(cachedRequest, response)
  }
}