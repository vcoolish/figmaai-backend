package com.app.figmaai.backend.user.controller

import com.app.figmaai.backend.user.dto.*
import com.app.figmaai.backend.user.service.AuthService
import com.app.figmaai.backend.user.service.SecurityContextService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@ApiResponses(
  ApiResponse(description = "SUCCESS", responseCode = "200"),
  ApiResponse(description = "TOKEN_EXPIRED", responseCode = "419"),
  ApiResponse(description = "SERVER_ERROR", responseCode = "500"),
)
@Validated
@RestController
class AuthController(
  private val securityContextService: SecurityContextService,
  private val authService: AuthService,
) {

  @ApiResponses(
    ApiResponse(responseCode = "400", description = "INVALID_CREDENTIALS"),
  )
  @PostMapping("/login")
  fun login(@RequestBody @Valid loginDto: LoginDto, request: HttpServletRequest?): ResponseEntity<TokensDto> {
    val user = authService.simpleAuthenticateUser(loginDto)
    return authService.loginUser(user, request)
      .let { ResponseEntity.ok(it) }
  }

  @PostMapping("/authorize")
  fun authorize(
    @RequestBody @Valid authorizationDto: AuthorizationDto,
    @RequestHeader token: String,
    request: HttpServletRequest?,
  ): ResponseEntity<String> {
    return authService.authenticatePlugin(token, authorizationDto.writeToken)
      .let { ResponseEntity.ok("Authorized") }
  }

  @PostMapping("/oauth-token")
  fun oauthToken(
    @RequestHeader("figma") figma: String,
  ): ResponseEntity<OauthTokensDto> =
    authService.generateOAuthTokens(figma)
      .let { ResponseEntity.ok(it) }

  @PostMapping("/oauth")
  fun readTokenAuth(
    @RequestBody @Valid readDto: ReadTokenDto,
    request: HttpServletRequest?,
  ): ResponseEntity<TokensDto> {
    val user = authService.oauthLogin(readDto.readToken)
    return authService.loginUser(user, request)
      .let { ResponseEntity.ok(it) }
  }

  @ApiResponses(
    ApiResponse(responseCode = "400", description = "TOKEN_DATA_INVALID"),
    ApiResponse(responseCode = "404", description = "NOT_FOUND"),
  )
  @PostMapping("/refresh-token")
  fun refreshToken(
    request: HttpServletRequest
  ): ResponseEntity<TokensDto> =
    authService.updateRefreshTokens(request)
      .let { ResponseEntity.ok(it) }

  @ApiResponses(
    ApiResponse(responseCode = "401", description = "UNAUTHORIZED"),
    ApiResponse(responseCode = "404", description = "NOT_FOUND"),
  )
  @DeleteMapping("/log-out")
  fun deleteRefreshToken(request: HttpServletRequest?): ResponseEntity<String> =
    authService.logOut(securityContextService.currentUser(), request)
      .let { ResponseEntity.ok("Logged out.") }

  companion object {
    const val REFRESH_TOKEN_HEADER = "Refresh-Token"
  }
}
