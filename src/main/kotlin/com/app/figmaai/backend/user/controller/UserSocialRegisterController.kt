package com.app.figmaai.backend.user.controller

import com.app.figmaai.backend.exception.NotFoundException
import com.app.figmaai.backend.user.dto.*
import com.app.figmaai.backend.user.model.Providers
import com.app.figmaai.backend.user.model.SocialConnection
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.service.*
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.social.connect.Connection
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping("/api/1.0/users/register/social")
@Validated
@ApiResponses(
  value = [
    ApiResponse(responseCode = "200", description = "SUCCESS"),
    ApiResponse(responseCode = "500", description = "SERVER_ERROR")
  ]
)
class UserSocialRegisterController(
  private val socialConnectionService: SocialConnectionService,
  private val providerConnectionService: ProviderConnectionService,
  private val userService: UserService,
  private val authService: AuthService,
  private val registerService: UserRegistrationService,
  private val providerSocialCollector: ProviderSocialUserInfoCollector,
  private val socialConnectionMapper: SocialConnectionMapper,
  private val socialNetworkCredentialService: SocialNetworkCredentialService,
) {
  private val helper: ControllerHelper = ControllerHelper()

  @PostMapping
  fun socialSignIn(
    @RequestBody @Valid dto: SocialUserRegistrationDto,
    request: HttpServletRequest?
  ): ResponseEntity<UrlDto> {
    val connectionUrl = dto
      .let { providerConnectionService.createConnectionUrl(it.provider, it.redirectUrl) }
    return with(socialConnectionService) {
      providerConnectionService.getState(connectionUrl)
        .let { socialConnectionMapper.toSocialConnectionCreateData(dto, it) }
        .let(::create)
        .run(::save)
    }.let { UrlDto(connectionUrl) }
      .let { ResponseEntity.ok(it) }
  }

  @PostMapping("/success")
  fun socialSignInSuccess(
    @RequestBody @Valid dto: SocialUserRegistrationSuccessDto,
    request: HttpServletRequest?
  ): ResponseEntity<TokensDto> {
    val socialConnection = socialConnectionService.getByState(dto.state)
    val provider = Providers.fromValue(socialConnection.provider)
    val connection = providerConnectionService.getConnection(provider, dto.code, socialConnection.redirectUrl)
    val userProfile = connection.fetchUserProfile()
    val socialId = userProfile.id.orEmpty().trim()
    if (providerConnectionService.isExistUserConnection(connection)) {
      return socialId
        .also { helper.checkSocialSignUp(it, provider) }
        .also { socialConnectionService.delete(socialConnection) }
        .let { userService.getUserWithProductRoles(it, provider) }
        .let { helper.loginUser(it, request) }
        .let { ResponseEntity.ok(it) }
    }
    return with(helper) {
      userProfile.email.orEmpty().trim()
        .also { checkEmail(it.toLowerCase()) }
        .let { userService.findByEmail(it) }
        .takeIf { it != null }
        ?.run { signInSocial(provider, connection, socialConnection, this) }
        ?: signUpSocial(provider, connection, socialConnection, request)
    }
      .also { authService.wireFigma(it, dto.writeToken) }
      .let { helper.loginUser(it, request) }
      .let { ResponseEntity.ok(it) }
  }

  @PostMapping("/error")
  fun socialSignInError(
    @RequestBody @Valid dto: SocialUserRegistrationErrorDto,
    request: HttpServletRequest?
  ): ResponseEntity<Any> {
    return with(socialConnectionService) {
      getByState(dto.state)
        .run(::delete)
    }.let { ResponseEntity.noContent().build() }
  }

  @PutMapping("/credentials/social-network")
  fun updateSocialNetworkCredentials(
    @RequestBody @Valid data: UpdateSocialCredentialsDto,
  ): ResponseEntity<Any> {
    require(data.key == "poop")
    return socialNetworkCredentialService.processUpdateSocialNetworksData(data)
      .let { ResponseEntity.ok(mapOf("message" to "${data.network} credentials will be applied shortly.")) }
  }

  open inner class ControllerHelper {

    @Transactional
    open fun signInSocial(
      provider: Providers,
      connection: Connection<*>,
      socialConnection: SocialConnection,
      userProfile: User
    ): User {
      val updateInfo = providerSocialCollector.collectUpdateInfo(provider, connection)
      return userProfile
        .run { userService.marge(this, updateInfo) }
        .also { providerConnectionService.doPostSignUp(connection) }
        .also { socialConnectionService.delete(socialConnection) }
    }

    @Transactional
    open fun signUpSocial(
      provider: Providers,
      connection: Connection<*>,
      socialConnection: SocialConnection,
      request: HttpServletRequest?,
    ): User {
      val createInfo = providerSocialCollector
        .collectCreateInfo(provider, connection)
      return createInfo
        .run { registerService.registerUser(this) }
        .also { providerConnectionService.doPostSignUp(connection) }
        .also { socialConnectionService.delete(socialConnection) }
    }

    fun checkSocialSignUp(socialId: String, provider: Providers) {
      if (!userService.isSocialSignUp(socialId, provider))
        throw Exception(
          "Social sign up connection already exists."
        )
    }

    fun checkEmail(email: String) {
      if (email.isBlank())
        throw NotFoundException(
          message = "Email is required."
        )
    }

    fun loginUser(user: User, request: HttpServletRequest?): TokensDto =
      authService.loginUser(user, request)
  }
}