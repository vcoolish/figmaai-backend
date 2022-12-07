package com.app.drivn.backend.config

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
    filterChain.doFilter(NormilizedHeaderRequest(request), response)
  }

  class NormilizedHeaderRequest(request: HttpServletRequest?) : HttpServletRequestWrapper(request) {

    override fun getHeader(name: String): String {
      val header: String? = super.getHeader(name) ?: super.getHeader(name.lowercase())
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
}
