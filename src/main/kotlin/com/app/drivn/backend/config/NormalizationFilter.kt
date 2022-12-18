package com.app.drivn.backend.config

import org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

class NormalizationFilter : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    filterChain.doFilter(NormalizedHeaderRequest(request), response)
  }

  class NormalizedHeaderRequest(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {

    override fun getHeader(name: String): String? {
      val header: String? = super.getHeader(name) ?: super.getHeader(name.lowercase())
      val value = header ?: super.getParameter(name)
      return if (name.equals(ACCESS_CONTROL_REQUEST_METHOD, true)) {
        value.uppercase()
      } else {
        value
      }
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
}
