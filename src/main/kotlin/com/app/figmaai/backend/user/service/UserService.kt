package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.specification.SpecificationBuilder
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.user.dto.UserRegistrationEntryDto
import com.app.figmaai.backend.user.dto.UserUpdateData
import com.app.figmaai.backend.user.model.*
import com.app.figmaai.backend.user.repository.UserRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@Service
class UserService(
  private val repository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val tokenProvider: TokenProvider,
  private val httpServletRequestTokenHelper: HttpServletRequestTokenHelper,
) {

  fun getByEmail(email: String): User = repository.findOneByEmail(email)

  fun getOneOrNull(spec: Specification<User>?): User? =
    repository.findOne(spec).orElse(null)

  fun get(figma: String): User = repository.findByFigma(figma).first()

  fun get(request: HttpServletRequest): User {
    val jwt = httpServletRequestTokenHelper.resolveToken(request)
    if (jwt.isNullOrEmpty()) {
      throw BadRequestException(message = "Access token not valid")
    }
    val claims = tokenProvider.getClaimsFromToken(jwt)
    val userUuid: String = claims.subject
    return getByUuid(userUuid)
  }

  fun getByUuid(userUuid: String): User = repository.findByUserUuid(userUuid)

  fun save(user: User) {
    repository.save(user)
  }

  fun updateToken(id: String, request: UserRegistrationEntryDto): User {
    val user = get(id)
    return repository.save(user)
  }

  fun matchUserPassword(password: String, user: User): Boolean =
    passwordEncoder.matches(password, user.password)

  fun create(userDto: UserCreateData): User =
    User().apply {
      userUuid = generateUUID()
      email = userDto.email.lowercase()
      password = passwordEncoder.encode(userDto.password)
      enabled = userDto.enabled ?: enabled
      verified = userDto.verified ?: verified
      googleId = userDto.googleId ?: googleId
      provider = if (googleId.isNullOrEmpty()) AuthProvider.local else AuthProvider.google
      method = AuthenticationMethod.EMAIL
      createdAt = ZonedDateTime.now()
    }

  fun generateUUID(): String = UUID.randomUUID().toString().takeUnless(this::exists) ?: generateUUID()

  fun exists(userUuid: String?): Boolean = userUuid?.let(repository::existsByUserUuid) ?: false

  fun saveNewUser(user: User): User =
    repository.save(user)
//    try {
//    repository.save(user)
//  } catch (e: ConstraintViolationException) {
//    user.userUuid = generateUUID()
//    saveNewUser(user)
//  }

  fun isEmailExist(email: String?): Boolean =
    repository.findByEmail(email?.lowercase()) != null

  fun findByEmail(email: String): User? =
    repository.findByEmail(email.lowercase())

  private val margeUserInfoHelper = mapOf(
    { user: User, data: UserUpdateData -> user.googleId.isNullOrBlank() && !data.googleId.isNullOrBlank() } to
        { user: User, data: UserUpdateData -> user.googleId = data.googleId },
  ).asSequence()

  fun marge(user: User, userDto: UserUpdateData): User {
    margeUserInfoHelper
      .filter { it.key(user, userDto) }
      .forEach { it.value(user, userDto) }
    repository.save(user)
    return user
  }

  fun isSocialSignUp(socialId: String, provider: Providers): Boolean =
    socialSignUpHelper.getValue(provider).invoke(socialId)

  private val socialSignUpHelper = EnumMap<Providers, (String) -> Boolean>(Providers::class.java)
    .apply {
      put(Providers.GOOGLE) { repository.findOneByGoogleId(it.trim()) != null }
    }

  fun getUserWithProductRolesOrNull(socialId: String, provider: Providers): User? =
    SpecificationBuilder<User>()
      .and(
        socialUserSpecificationHelper
          .getValue(provider)
          .invoke(socialId)
      )
      .and(UserSpecification.isEnabled())
      .and(UserSpecification.isVerified())
      .build()
      .run { repository.findOne(this).orElse(null) }

  fun getUserWithProductRoles(socialId: String, provider: Providers): User =
    getUserWithProductRolesOrNull(socialId, provider)
      ?: throw BadRequestException(
        message = "User is disabled or not valid."
      )

  private val socialUserSpecificationHelper =
    EnumMap<Providers, (String) -> Specification<User>>(Providers::class.java)
      .apply {
        put(Providers.GOOGLE) { it.trim().let(UserSpecification.equalGoogleId) }
      }

  fun clearSignUpsForSocial(provider: Providers) {
//    repository.setProviderIdToNull(provider)
  }
}
