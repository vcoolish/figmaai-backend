package com.app.figmaai.backend.config

import com.app.figmaai.backend.user.service.HttpServletRequestTokenHelper
import com.app.figmaai.backend.user.service.TokenProvider
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.Serializable
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomAuthenticationEntryPoint(
  private var errorServletResponseCreator: ErrorServletResponseCreator,
  private var tokenProvider: TokenProvider,
  private var tokenHelper: HttpServletRequestTokenHelper,
) : AuthenticationEntryPoint, Serializable {

  @Suppress("DEPRECATION")
  override fun commence(
    request: HttpServletRequest,
    response: HttpServletResponse,
    exception: AuthenticationException
  ) {
    val jwt = tokenHelper.resolveToken(request)
    val tokenRequest = runCatching { tokenProvider.getClaimsFromToken(jwt) }.exceptionOrNull()
    val isTokenExpired = tokenRequest is ExpiredJwtException
    when {
      tokenRequest == null -> {}
      isTokenExpired -> errorServletResponseCreator.build(
        servletResponse = response,
        error = Exception("Token expired"),
        status = HttpStatus.INSUFFICIENT_SPACE_ON_RESOURCE
      )
      else -> errorServletResponseCreator.build(
        servletResponse = response,
        error = Exception(exception.message ?: exception.localizedMessage),
        status = HttpStatus.UNAUTHORIZED
      )
    }
  }
}
