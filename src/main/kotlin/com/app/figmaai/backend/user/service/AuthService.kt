package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.specification.SpecificationBuilder
import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.exception.NotFoundException
import com.app.figmaai.backend.user.dto.LoginData
import com.app.figmaai.backend.user.dto.OauthTokensDto
import com.app.figmaai.backend.user.dto.TokensDto
import com.app.figmaai.backend.user.model.*
import com.app.figmaai.backend.user.repository.OauthRepository
import org.apache.commons.codec.digest.DigestUtils
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
  private val customUserDetailsService: UserDetailsService,
  private val oauthRepository: OauthRepository,
) {
  private val logger = logger()

  @Transactional
  @Deprecated("It's too heavy", ReplaceWith("simpleAuthenticateUser"))
  fun authenticateUser(loginDto: LoginData): User {
    val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.login, loginDto.password)
    val authenticationManager = authenticationManagerBuilder.getObject()
    val authentication = authenticationManager.authenticate(authenticationToken)
    SecurityContextHolder.getContext().authentication = authentication
    val user = (authentication.principal as CustomUserDetails).user
    wireFigma(user, loginDto.writeToken)
    return (authentication.principal as CustomUserDetails).user
  }

  fun wireFigma(user: User, writeToken: String?) {
    if (writeToken.isNullOrEmpty()) {
      return
    }
    runCatching {
      val figma = oauthRepository.findByWriteToken(writeToken)?.figma ?: return
      val dbUser = userService.getByUuid(user.userUuid)
      dbUser.figma = figma
      userService.save(dbUser)
    }
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
  fun oauthLogin(readToken: String): User {
    val oauth = oauthRepository.findByReadToken(readToken)
      ?: throw BadRequestException(message = "Token not found")
    val figma = oauth.figma
      ?: throw BadRequestException(message = "Figma not found")
    oauthRepository.delete(oauth)
    return userService.get(figma)
  }

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
  fun generateOAuthTokens(figma: String): OauthTokensDto {
    val current = oauthRepository.findByFigma(figma)
    if (current != null) {
      oauthRepository.delete(current)
    }
    val write = DigestUtils.md5Hex(UUID.randomUUID().toString())
    val read = DigestUtils.md5Hex(UUID.randomUUID().toString())
    val oauth = OAuthToken().apply {
      this.figma = figma
      writeToken = write
      readToken = read
    }
    oauthRepository.save(oauth)
    return OauthTokensDto(readToken = read, writeToken = write)
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
