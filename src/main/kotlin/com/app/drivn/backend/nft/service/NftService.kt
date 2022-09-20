package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.exception.BadRequestException
import com.app.drivn.backend.nft.data.CarRepairInfo
import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.NftId
import com.app.drivn.backend.nft.repository.CarNftRepository
import com.app.drivn.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.math.min

@Service
class NftService(
  private val carNftRepository: CarNftRepository,
  private val userService: UserService,
  private val appProperties: AppProperties
) {

  fun getAll(pageable: Pageable): Page<NftInternalDto> {
    return carNftRepository.findAll(pageable).map(NftMapper::toInternalDto)
  }

  fun getOrCreate(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId))
      .orElseGet { carNftRepository.save(NftMapper.generateRandomCar(id, collectionId)) }
  }

  fun get(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId)).orElseThrow()
  }

  fun save(carNft: CarNft) {
    carNftRepository.save(carNft)
  }

  fun getRepairableCost(car: CarNft, repairAmount: Float): CarRepairInfo {
    if (car.durability <= car.maxDurability) {
      val durabilityDiff = car.maxDurability - car.durability

      val repairableAmount = min(repairAmount, durabilityDiff)

      return CarRepairInfo(
        repairableAmount,
        BigDecimal.valueOf(repairableAmount * appProperties.durabilityRepairCost)
      )
    }
    return CarRepairInfo()
  }

  fun repair(id: Long, collectionId: Long, address: String, repairAmount: Float): CarNft {
    val car = get(id, collectionId)

    val (repairableAmount, repairableCost) = getRepairableCost(car, repairAmount)
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
}
