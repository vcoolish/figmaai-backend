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
import java.math.RoundingMode
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

@Service
class DriveService(
  private val nftService: NftService,
  private val userService: UserService,
  private val tokenRecordService: EarnedTokenRecordService,
  private val userEnergyService: UserEnergyService
) {

  val log: Logger = logger()

  fun drive(address: String, carId: Long, collectionId: Long, distance: Float): DriveInfoDto {
    val user = userService.get(address)
    if (user.energy <= 0) {
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

    val realDistance: Float
    val realConsumedEnergy: Float
    val realReward: BigDecimal

    val consumedEnergy: Float = max(distance * 5, 0F)

    val reward = BigDecimal.valueOf(consumedEnergy.toDouble())
      .divide(BigDecimal.TEN, MathContext.DECIMAL128)
      .multiply(BigDecimal.valueOf(car.body.earnEfficiency.toDouble()))
      .multiply(BigDecimal.valueOf(car.quality.efficiency.toDouble()))
      .multiply(BigDecimal.valueOf((car.efficiency.toDouble() / 200) + 1))

    if (reward <= availableToEarn) {
      realDistance = distance
      realConsumedEnergy = consumedEnergy
      realReward = if (ThreadLocalRandom.current().nextDouble(100.0) <= car.luck) {
        log.info("User $address with car $collectionId-$carId are lucky!")
        reward + BigDecimal.valueOf(0.1)
      } else {
        reward
      }
    } else {
      // if user can't receive the whole reward
      // then we give only available and minus according to that value
      val available = availableToEarn.max(BigDecimal.ZERO)
      val diffPercent = ((reward - available) / reward).toFloat()

      realDistance = distance * diffPercent
      realConsumedEnergy = consumedEnergy * diffPercent
      realReward = available
    }

    val finalConsumedEnergy = realConsumedEnergy
      .let { it + (it * car.body.fuelEfficiency) }
      .let { it - (it * (car.economy / 200)) }

    userEnergyService.spendEnergy(user, finalConsumedEnergy)

    user.distance += realDistance
    car.odometer += realDistance

    car.durability = (max(car.durability - (distance / 5), 0F) * car.body.durabilityCoefficient)
      .let { it * car.comfortability / 200 }

    // HALF_EVEN?
    user.tokensToClaim = (user.tokensToClaim + realReward).setScale(1, RoundingMode.HALF_EVEN)

    tokenRecordService.recordEarnedTokens(address, realReward)
    nftService.save(car)
    userService.save(user)
    return DriveMapper.toDto(user)
  }

}
