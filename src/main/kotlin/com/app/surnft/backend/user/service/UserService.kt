package com.app.surnft.backend.user.service

import com.app.surnft.backend.blockchain.service.validateMessageSign
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.user.dto.UpdateUserDonationRequest
import com.app.surnft.backend.user.dto.UserRegistrationEntryDto
import com.app.surnft.backend.user.model.User
import com.app.surnft.backend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*


@Service
class UserService(
  private val repository: UserRepository,
  private val appProperties: AppProperties,
) {

  fun getOrCreate(address: String): User = repository.findById(address.lowercase())
    .orElseGet {
      User(
        address.lowercase(),
        appProperties.defaultUserTokensLimitPerDay,
        appProperties.defaultUserEnergyLimit
      ).let(repository::save)
    }

  fun get(address: String): User = repository.findById(address.lowercase()).orElseThrow()

  fun getSignMessage(address: String): Optional<String> = address
    .lowercase()
    .let(repository::findSignMessageByAddress)

  fun save(user: User) {
    repository.save(user)
  }

  fun updateDonation(address: String, request: UpdateUserDonationRequest): User {
    val user = get(address)

    Optional.ofNullable(request.donation)
      .ifPresent { user.donation = it }

    return repository.save(user)
  }

  fun updateSignMessage(address: String, request: UserRegistrationEntryDto): User {
    if (!validateMessageSign(address, request.message, request.signature)) {
      throw IllegalStateException("Verification failed")
    }
    val user = getOrCreate(address)
    user.signMessage = request.message
    return repository.save(user)
  }

  fun addToBalance(address: String, amount: BigDecimal): User {
    val user = getOrCreate(address)
    user.balance = user.balance.add(amount)
    return repository.save(user)
  }

  fun subtractFromBalance(address: String, amount: BigDecimal): User {
    val user = get(address)
    if (user.balance >= amount) {
      user.balance = user.balance.subtract(amount)
      return repository.save(user)
    }
    return user
  }

  fun addToTokenBalance(address: String, amount: BigDecimal): User {
    val user = get(address)
    user.tokensToClaim = user.tokensToClaim.add(amount)
    return repository.save(user)
  }

  fun subtractFromTokenBalance(address: String, amount: BigDecimal): User {
    val user = get(address)
    if (user.tokensToClaim >= amount) {
      user.tokensToClaim = user.tokensToClaim.subtract(amount)
      return repository.save(user)
    }
    return user
  }
}
