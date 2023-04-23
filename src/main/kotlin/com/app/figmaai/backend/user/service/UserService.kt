package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.common.specification.SpecificationBuilder
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.subscription.PaypalSubscriptionValidator
import com.app.figmaai.backend.user.dto.SubscriptionProvider
import com.app.figmaai.backend.user.dto.UserRegistrationEntryDto
import com.app.figmaai.backend.user.dto.UserUpdateData
import com.app.figmaai.backend.user.model.*
import com.app.figmaai.backend.user.repository.UserRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import javax.validation.ConstraintViolationException

@Service
class UserService(
  private val repository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val paypalValidator: PaypalSubscriptionValidator,
) {

  fun getOneOrNull(spec: Specification<User>?): User? =
    repository.findOne(spec).orElse(null)

  fun get(figma: String): User = repository.findByFigma(figma).first()

  fun getByUuid(userUuid: String): User = repository.findByUserUuid(userUuid)

  fun updateSubscription(figma: String, id: String, provider: SubscriptionProvider): User {
    val user = get(figma)
    when (provider) {
      SubscriptionProvider.paypal -> paypalValidator.validate(id)
      else -> error("Unknown provider")
    }
    user.subscriptionId = id
    repository.save(user)
    return user
  }

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

  fun saveNewUser(user: User): User = try {
    repository.save(user)
  } catch (e: ConstraintViolationException) {
    user.userUuid = generateUUID()
    saveNewUser(user)
  }

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
