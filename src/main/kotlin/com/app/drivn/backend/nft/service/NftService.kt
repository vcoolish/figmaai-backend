package com.app.drivn.backend.nft.service

import com.app.drivn.backend.blockchain.service.BlockchainService
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.exception.BadRequestException
import com.app.drivn.backend.exception.NotFoundException
import com.app.drivn.backend.nft.data.CarRepairInfo
import com.app.drivn.backend.nft.dto.CarLevelUpCostResponse
import com.app.drivn.backend.nft.entity.CarCollection
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.Image
import com.app.drivn.backend.nft.model.NftId
import com.app.drivn.backend.nft.repository.CarNftRepository
import com.app.drivn.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.min

@Service
class NftService(
  private val carNftRepository: CarNftRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageService: ImageService,
  private val carCreationService: CarCreationService,
  private val blockchainService: BlockchainService,
) {

  fun getAll(pageable: Pageable): Page<CarNft> {
    return carNftRepository.findAll(pageable)
  }

  private fun getNextFreeImage(): Image {
    return imageService.findFreeImage()
      ?: throw NotFoundException("Free image for NFT not found")
  }

  fun create(address: String, collectionId: Long): CarNft {
    val id = System.currentTimeMillis() / 100
    try {
      carNftRepository.getReferenceById(NftId(id, collectionId))
      throw IllegalStateException("Car already exists")
    } catch (t: Throwable) {
      val user = userService.get(address)
      val carType = CarCollection.values().first { it.collectionId == collectionId }
      if (user.balance < carType.price.toBigDecimal()) {
        throw IllegalStateException("Insufficient balance")
      }
      val image = getNextFreeImage()
      blockchainService.mint(
        contractAddress = appProperties.collectionAddress,
        address = address,
        tokenId = BigInteger.valueOf(id),
      )
      val nft = carCreationService.create(user, id, collectionId)
        .apply { this.image = image }
      user.balance = user.balance - carType.price.toBigDecimal()
      userService.save(user)
      return carNftRepository.save(nft)
    }
  }

  fun get(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId)).orElseThrow()
  }

  fun save(carNft: CarNft) {
    carNftRepository.save(carNft)
  }

  fun getRepairCost(car: CarNft) =
    appProperties.durabilityRepairCost

  fun getRepairableCost(car: CarNft, newDurability: Float): CarRepairInfo {
    if (newDurability < car.durability) {
      throw BadRequestException("New durability can't be less than current durability!")
    }

    if (car.durability < car.maxDurability) {
      val repairableAmount = min(newDurability, car.maxDurability) - car.durability

      return CarRepairInfo(
        repairableAmount,
        BigDecimal.valueOf(repairableAmount * appProperties.durabilityRepairCost)
      )
    }
    return CarRepairInfo()
  }

  @Transactional
  fun repair(id: Long, collectionId: Long, address: String, newDurability: Float): CarNft {
    val car = get(id, collectionId)

    val (repairableAmount, repairableCost) = getRepairableCost(car, newDurability)
    if (repairableCost > BigDecimal.ZERO) {
      val user = userService.get(address)

      if (user.tokensToClaim < repairableCost) {
        throw BadRequestException("User has not enough tokens: $repairableCost.")
      }

      user.tokensToClaim -= repairableCost
      car.durability += repairableAmount

      userService.save(user)
      carNftRepository.save(car)
    }

    return car
  }

  fun getLevelUpCost(car: CarNft): CarLevelUpCostResponse {
    val newLevel: Short = car.level.inc()

    val requiredDistance: Int = appProperties.carLevelDistanceRequirement[newLevel]
      ?: throw BadRequestException("Maximum level reached.")

    return CarLevelUpCostResponse(
      appProperties.levelUpCarCost * newLevel.toInt().toBigDecimal(),
      newLevel,
      requiredDistance
    )
  }

  @Transactional
  fun levelUp(id: Long, collectionId: Long, initiatorAddress: String): CarNft {
    val car = get(id, collectionId)

    val (cost, newLevel, requiredDistance) = getLevelUpCost(car)

    if (car.odometer < requiredDistance) {
      throw BadRequestException("The car did not drive the required $requiredDistance kilometers.")
    }

    val user = userService.get(initiatorAddress)

    if (user.tokensToClaim < cost) {
      throw BadRequestException("Insufficient tokens amount ${user.tokensToClaim}!")
    }

    user.tokensToClaim -= cost
    car.level = newLevel

    userService.save(user)
    return carNftRepository.save(car)
  }
}
