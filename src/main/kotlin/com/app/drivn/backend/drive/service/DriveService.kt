package com.app.drivn.backend.drive.service

import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.drive.mapper.DriveMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.service.NftService
import com.app.drivn.backend.user.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.MathContext
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max

@Service
class DriveService(
  private val nftService: NftService,
  private val userService: UserService
) {
  val log: Logger = LoggerFactory.getLogger(DriveService::class.java)


  fun drive(address: String, carId: Long, collectionId: Long, distance: Float): DriveInfoDto {
    val user = userService.get(address)
    if (user.energy <= 0) {
      log.warn("User $address has no fuel!")
      return DriveMapper.toDto(user)
    }

    val car: CarNft = nftService.get(carId, collectionId)
    if (car.durability <= 0) {
      log.warn("The car $collectionId-$carId of user $address is broken!")
      return DriveMapper.toDto(user)
    }

    var consumedEnergy: Float = max(user.energy - (distance * 5), 0F) * car.body.fuelEfficiency
    consumedEnergy -= consumedEnergy * (car.economy / 200)

    user.distance += distance
    user.energy = consumedEnergy
    user.tokensToClaim = user.tokensToClaim.add(
      BigDecimal(consumedEnergy.toDouble())
        .divide(BigDecimal.TEN, MathContext.DECIMAL128)
        .multiply(BigDecimal(car.body.earnEfficiency.toDouble()))
        .multiply(BigDecimal(car.quality.efficiency.toDouble()))
        .multiply(BigDecimal((car.efficiency.toDouble() / 200) + 1))
    )

    if ((ThreadLocalRandom.current().nextDouble(101.0) <= car.luck)) {
      log.info("User $address with car $collectionId-$carId are lucky!")
      user.tokensToClaim = user.tokensToClaim.add(BigDecimal(0.1))
    }

    car.odometer += distance
    car.durability = (max(car.durability - (distance / 5), 0F) * car.body.durabilityCoefficient)
      .let { it * car.comfortability / 200 }

    nftService.save(car)
    userService.save(user)
    return DriveMapper.toDto(user)
  }

}
