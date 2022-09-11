package com.app.drivn.backend.user.service

import com.app.drivn.backend.common.synchronization.SyncTemplate
import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min

@Service
class UserEnergyService(
  private val userRepository: UserRepository,
  private val appProperties: AppProperties
) {

  private val log = logger()
  private val sync: SyncTemplate<String> = SyncTemplate()

  fun tryToRenew(address: String): Optional<User> =
    userRepository.findById(address).map(this::tryToRenew)

  fun tryToRenew(user: User): User {
    sync.execute(user.address) {
      val now = ZonedDateTime.now()

      val remainsToMax = user.maxEnergy - user.energy
      val nextEnergyRenew = user.nextEnergyRenew

      if (remainsToMax > 0 && (nextEnergyRenew == null || nextEnergyRenew <= now)) {
        user.energy += min(user.maxEnergy * 0.25F, remainsToMax)

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

  fun spendEnergy(user: User, energy: Float) {
    if (user.energy > 0 && energy > 0) {
      user.energy -= energy
    }

    if (user.nextEnergyRenew == null) {
      user.nextEnergyRenew = ZonedDateTime.now().plus(appProperties.energyRenewRate)
    }

    userRepository.save(user)
  }
}
