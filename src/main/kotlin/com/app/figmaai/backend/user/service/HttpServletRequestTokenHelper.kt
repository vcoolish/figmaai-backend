package com.app.figmaai.backend.user.service

import com.blueconic.browscap.BrowsCapField
import com.blueconic.browscap.UserAgentService
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class HttpServletRequestTokenHelper {
  companion object {
    private val IP_HEADER_CANDIDATES = listOf(
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR"
    )
    private const val USER_AGENT: String = "User-Agent"
    private const val UNKNOWN: String = "unknown"
    private const val REFRESH_TOKEN = "Refresh-Token"
    private val USER_AGENT_PARSER = UserAgentService().loadParser(
      listOf(
        BrowsCapField.BROWSER,
        BrowsCapField.BROWSER_TYPE,
        BrowsCapField.BROWSER_MAJOR_VERSION,
        BrowsCapField.BROWSER_BITS,
        BrowsCapField.DEVICE_TYPE,
        BrowsCapField.DEVICE_CODE_NAME,
        BrowsCapField.PLATFORM,
        BrowsCapField.PLATFORM_VERSION,
        BrowsCapField.PLATFORM_BITS
      )
    )
    //skip authorization
    private const val AUTHORIZATION_HEADER = "token"
    private const val AUTHORIZATION_PARAMETER = "access_token"
    private const val TOKEN_TYPE_STRING = "Bearer "
  }

  fun resolveToken(request: HttpServletRequest?): String? {
    val bearerToken = request?.getHeader(AUTHORIZATION_HEADER)
      ?: request?.getHeader(AUTHORIZATION_PARAMETER)
      ?: request?.getParameter(
        AUTHORIZATION_PARAMETER
      )
    if (!bearerToken.isNullOrBlank()) {
      val token = bearerToken.trim()
      if (token.startsWith(TOKEN_TYPE_STRING)) {
        return token.substring(TOKEN_TYPE_STRING.length)
      }
      return token
    }
    return null
  }

  fun getClientIpAddress(request: HttpServletRequest?): String {
    for (ipHeaderCandidate in IP_HEADER_CANDIDATES) {
      val ip: String? = request?.getHeader(ipHeaderCandidate)
      if (!ip.isNullOrBlank() && !UNKNOWN.equals(ip, true)) {
        return ip
      }
    }
    return request?.remoteAddr.orEmpty()
  }

  private fun getUserAgentInfo(request: HttpServletRequest?): String = request?.getHeader(USER_AGENT).orEmpty()

  fun generateHash(request: HttpServletRequest?): String =
    generateHash(getUserAgentInfo(request), getClientIpAddress(request))

  fun generateHash(userAgent: String, ipAddress: String): String =
    userAgent
      .let(USER_AGENT_PARSER::parse)
      .let {
        StringBuilder()
          .append(it.getValue(BrowsCapField.PLATFORM))
          .append(":")
          .append(it.getValue(BrowsCapField.PLATFORM_VERSION))
          .append(":")
          .append(it.getValue(BrowsCapField.PLATFORM_BITS))
          .append(":")
          .append(it.getValue(BrowsCapField.DEVICE_TYPE))
          .append(":")
          .append(it.getValue(BrowsCapField.DEVICE_CODE_NAME))
          .append(":")
          .append(it.getValue(BrowsCapField.BROWSER_TYPE))
          .append(":")
          .append(it.getValue(BrowsCapField.BROWSER))
          .append(":")
          .append(it.getValue(BrowsCapField.BROWSER_MAJOR_VERSION))
          .append(":")
          .append(it.getValue(BrowsCapField.BROWSER_BITS))
//          .append(":")
//          .append(ipAddress)
          .toString()
      }.let(DigestUtils::md5Hex)

  fun getRefreshToken(request: HttpServletRequest?): String = request?.getHeader(REFRESH_TOKEN).orEmpty()
}
