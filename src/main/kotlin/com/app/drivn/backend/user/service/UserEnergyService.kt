package com.app.drivn.backend.user.service

import com.app.drivn.backend.common.synchronization.SyncTemplate
import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.data.UserSpendEnergyEvent
import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

@Service
class UserEnergyService(
  private val userRepository: UserRepository,
  private val appProperties: AppProperties,
  private val eventPublisher: ApplicationEventPublisher
) {

  private val log = logger()
  private val sync: SyncTemplate<String> = SyncTemplate()

  fun getNextEnergyRenewTime(): Optional<Instant> = userRepository.getNextRenewTime()

  fun getUsersByNextEnergyRenew(nextEnergyRenew: ZonedDateTime): Set<User> =
    userRepository.findByNextEnergyRenewLessThanEqualOrderByNextEnergyRenewAsc(nextEnergyRenew)

  fun tryToRenew(address: String): Optional<User> =
    userRepository.findById(address).map(this::tryToRenew)

  fun tryToRenew(user: User): User {
    log.debug("Try to renewed energy for ${user.address}")

    sync.execute(user.address) {
      val now = ZonedDateTime.now()

      val remainsToMax = user.maxEnergy - user.energy
      val nextEnergyRenew = user.nextEnergyRenew

      if (remainsToMax > BigDecimal.ZERO && (nextEnergyRenew == null || nextEnergyRenew <= now)) {
        user.energy += remainsToMax.min(user.maxEnergy * appProperties.energyRenewPercent)

        if (user.maxEnergy > user.energy) {
          user.nextEnergyRenew = now.plus(appProperties.energyRenewRate)
        } else {
          user.nextEnergyRenew = null
        }

        log.debug("Renewed energy for ${user.address}")
        userRepository.save(user)
      }
    }

    return user
  }

  fun spendEnergy(user: User, energy: BigDecimal) {
    sync.execute(user.address) {
      if (user.energy > BigDecimal.ZERO && energy > BigDecimal.ZERO) {
        user.energy = BigDecimal.ZERO.max(user.energy - energy)
      }

      if (user.nextEnergyRenew == null) {
        user.nextEnergyRenew = ZonedDateTime.now().plus(appProperties.energyRenewRate)
      }

      userRepository.save(user)
    }

    eventPublisher.publishEvent(UserSpendEnergyEvent())
  }
}
