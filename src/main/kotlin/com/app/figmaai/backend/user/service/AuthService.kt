package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.specification.SpecificationBuilder
import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.exception.NotFoundException
import com.app.figmaai.backend.user.dto.LoginData
import com.app.figmaai.backend.user.dto.TokensDto
import com.app.figmaai.backend.user.model.CreateTokenData
import com.app.figmaai.backend.user.model.CustomUserDetails
import com.app.figmaai.backend.user.model.UpdateTokenData
import com.app.figmaai.backend.user.model.User
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.servlet.http.HttpServletRequest

@Service
class AuthService(
  private val refreshTokenService: RefreshTokenService,
  private val authenticationManagerBuilder: AuthenticationManagerBuilder,
  private val tokenProvider: TokenProvider,
  private val httpServletRequestTokenHelper: HttpServletRequestTokenHelper,
  private val userService: UserService,
  private val customUserDetailsService: UserDetailsService
) {
  private val logger = logger()

  @Transactional
  @Deprecated("It's too heavy", ReplaceWith("simpleAuthenticateUser"))
  fun authenticateUser(loginDto: LoginData): User {
    val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.login, loginDto.password)
    val authenticationManager = authenticationManagerBuilder.getObject()
    val authentication = authenticationManager.authenticate(authenticationToken)
    SecurityContextHolder.getContext().authentication = authentication
    return (authentication.principal as CustomUserDetails).user
  }

  @Transactional
  fun simpleAuthenticateUser(loginDto: LoginData): User {
    val user = customUserDetailsService
      .loadUserByUsername(loginDto.login)
      .let { (it as CustomUserDetails).user }
    if (!userService.matchUserPassword(loginDto.password, user)) {
      throw BadCredentialsException("Bad credentials!")
    }
    return user
  }

  @Transactional
  fun loginUser(user: User, request: HttpServletRequest?): TokensDto =
    generateTokens(user, httpServletRequestTokenHelper.generateHash(request))

  @Transactional
  fun updateRefreshTokens(request: HttpServletRequest): TokensDto {
    val refreshJwt = httpServletRequestTokenHelper.getRefreshToken(request)
    if (refreshJwt.isBlank()) {
      throw BadRequestException(message = "Refresh token not valid")
    }
    val claims = tokenProvider.getClaimsFromToken(refreshJwt)
    val userUuid: String = claims.subject
    val query = SpecificationBuilder<User>()
      .and(userUuid.let(UserSpecification.equalUuid))
      .and(UserSpecification.isEnabled())
      .build()
    val user = userService.getOneOrNull(query)
      ?: throw BadRequestException(
        message = "Enabled user with such UUID $userUuid not found"
      )

    val hash = httpServletRequestTokenHelper.generateHash(request)
    val existsRefreshToken = refreshTokenService.getOne(refreshJwt)
      ?: throw NotFoundException(message = "Token not found")
    if (existsRefreshToken.hash != hash) {
      refreshTokenService.delete(existsRefreshToken)
      throw BadRequestException(
        message = "Token data invalid. New hash: ${hash}, exists hash: ${existsRefreshToken.hash}. Token $refreshJwt"
      )
    }
    return generateTokens(user, hash)
  }

  @Transactional
  fun logOut(user: User, request: HttpServletRequest?) {
    httpServletRequestTokenHelper.generateHash(request)
      .let { refreshTokenService.getOne(user, it) }
      ?.run { refreshTokenService.delete(this) }
      ?: throw NotFoundException(message = "Token not found")
  }

  private fun processRefreshToken(user: User, hash: String, refreshToken: String) {
    with(refreshTokenService) {
      when (existsByHash(user, hash)) {
        true -> getOne(user, hash)?.run {
          update(this, object : UpdateTokenData {
            override val token: String = refreshToken
            override val expirationDate: Date = tokenProvider.getClaimsFromToken(refreshToken).expiration
          })
        }
        else -> create(object : CreateTokenData {
          override val user: User = user
          override val token: String = refreshToken
          override val expirationDate: Date = tokenProvider.getClaimsFromToken(refreshToken).expiration
          override val hash: String = hash
        }).run { save(this) }
      }
    }
  }

  @Transactional
  fun generateTokens(user: User, hash: String): TokensDto {
    val accessToken = tokenProvider.createAccessToken(user)
    val refreshToken = tokenProvider.createRefreshToken(user.userUuid)
    return TokensDto(accessToken, refreshToken)
      .also { processRefreshToken(user, hash, refreshToken) }
  }
}
