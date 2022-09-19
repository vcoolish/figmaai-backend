package com.app.drivn.backend.drive.service

import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.drive.mapper.DriveMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.service.NftService
import com.app.drivn.backend.user.service.EarnedTokenRecordService
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.MathContext
import java.util.concurrent.ThreadLocalRandom

@Service
class DriveService(
  private val nftService: NftService,
  private val userService: UserService,
  private val tokenRecordService: EarnedTokenRecordService,
  private val userEnergyService: UserEnergyService
) {

  companion object {

    val FIVE: BigDecimal = BigDecimal.valueOf(5)
  }

  val log: Logger = logger()

  fun drive(address: String, carId: Long, collectionId: Long, distance: BigDecimal): DriveInfoDto {
    val user = userService.get(address)
    if (user.energy <= BigDecimal.ZERO) {
      log.warn("User $address has no fuel!")
      return DriveMapper.toDto(user)
    }

    val earnedTokensForDay = tokenRecordService.getEarnedTokensForDay(address)
    val availableToEarn = user.tokensLimitPerDay - earnedTokensForDay
    if (availableToEarn <= BigDecimal.ZERO) {
      log.warn("User $address has reached his tokens limit per day!")
      return DriveMapper.toDto(user)
    }

    val car: CarNft = nftService.get(carId, collectionId)
    if (car.durability <= 0) {
      log.warn("The car $collectionId-$carId of user $address is broken!")
      return DriveMapper.toDto(user)
    }

    log.debug("User $address with car $collectionId-$carId drove through $distance")

    val realDistance: BigDecimal
    val realConsumedEnergy: BigDecimal
    val realReward: BigDecimal

    val consumedEnergy: BigDecimal = BigDecimal.ZERO.max(distance * FIVE).min(user.energy)

    val reward = consumedEnergy
      .divide(BigDecimal.TEN, MathContext.DECIMAL128)
      .multiply(car.body.earnEfficiency)
      .multiply(BigDecimal.valueOf(car.quality.efficiency.toDouble()))
      .multiply(BigDecimal.valueOf((car.efficiency / 200.0) + 1))

    if (reward <= availableToEarn) {
      realDistance = distance
      realConsumedEnergy = consumedEnergy
      realReward = if (ThreadLocalRandom.current().nextDouble(100.0) <= car.luck) {
        log.info("User $address with car $collectionId-$carId are lucky!")
        reward + BigDecimal("0.1")
      } else {
        reward
      }
    } else {
      // if user can't receive the whole reward
      // then we give only available and minus according to that value
      val available = availableToEarn.max(BigDecimal.ZERO)
      val diffPercent = ((reward - available) / reward)

      realDistance = distance * diffPercent
      realConsumedEnergy = consumedEnergy * diffPercent
      realReward = available
    }

    val finalConsumedEnergy = realConsumedEnergy
      .let { it + (it * car.body.fuelEfficiency) }
      .let { it - (it * BigDecimal.valueOf(car.economy / 200.0)) }

    userEnergyService.spendEnergy(user, finalConsumedEnergy)

    user.distance += realDistance
    car.odometer += realDistance.toFloat()

    val newDurability = BigDecimal.valueOf(car.durability.toDouble())
      .subtract(distance / FIVE)
      .max(BigDecimal.ZERO)

    car.durability = (
        (newDurability * car.body.durabilityCoefficient) * BigDecimal.valueOf(car.comfortability / 200.0)
        ).toFloat()

    user.tokensToClaim += realReward

    tokenRecordService.recordEarnedTokens(address, realReward)
    nftService.save(car)
    userService.save(user)
    return DriveMapper.toDto(user)
  }

}
