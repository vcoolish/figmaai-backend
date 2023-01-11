package com.app.surnft.backend.nft.service

import com.app.surnft.backend.blockchain.service.BlockchainService
import com.app.surnft.backend.common.util.bannedPhrases
import com.app.surnft.backend.common.util.bannedWords
import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.exception.BadRequestException
import com.app.surnft.backend.nft.data.CarRepairInfo
import com.app.surnft.backend.nft.dto.CarLevelUpCostResponse
import com.app.surnft.backend.nft.dto.GetAllNftRequest
import com.app.surnft.backend.nft.entity.ImageCollection
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import com.app.surnft.backend.nft.repository.ImageNftRepository
import com.app.surnft.backend.nft.repository.extra.ImageNftSpecification.userEqual
import com.app.surnft.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.min


@Service
class NftService(
  private val imageNftRepository: ImageNftRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageCreationService: ImageCreationService,
  private val blockchainService: BlockchainService,
) {

  private val logger = logger()
  private val restTemplate = RestTemplate()

  fun getAll(pageable: Pageable, request: GetAllNftRequest): Page<ImageNft> {
    val spec: Specification<ImageNft> = userEqual(request.address)
    return imageNftRepository.findAll(spec, pageable)
  }

  fun create(address: String, collectionId: Long, prompt: String): ImageNft {
    validatePrompt(prompt)

    val user = userService.getOrCreate(address)
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
//    if (user.balance < carType.price.toBigDecimal()) {
//      throw BadRequestException("Insufficient balance")
//    }
    logger().info("{${prompt}}")

    restTemplate.postForEntity(
      "https://surnft-ai.herokuapp.com/task",
      mapOf(
        "prompt" to prompt,
      ),
      String::class.java,
    )

    val nft = imageCreationService.create(user, collectionId)
      .let(imageNftRepository::saveAndFlush)

    val id: Long = nft.id!!

    nft.name = "${carType.title} #$id"
    nft.prompt = prompt
    nft.externalUrl = "https://tofunft.com/nft/bsc/0xe73711e8331aD93ca115A2AE4D1AFAc74E15D644/$id"

//    user.balance = user.balance - carType.price.toBigDecimal()
    userService.save(user)

    return imageNftRepository.save(nft)
  }

  private fun validatePrompt(prompt: String) {
    val keywords = prompt.lowercase().split(" ")
    if (prompt.isEmpty()) {
      throw BadRequestException("Empty prompt")
    }
    for (jerk in bannedWords) {
      if (keywords.contains(jerk)) {
        throw BadRequestException("Banned word: $jerk")
      }
    }
    for (jerk in bannedPhrases) {
      if (prompt.contains(jerk)) {
        throw BadRequestException("Banned phrase: $jerk")
      }
    }
  }

  fun mint(address: String, collectionId: Long, id: Long): ImageNft {
    val user = userService.get(address)
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
    if (user.balance < carType.mintPrice.toBigDecimal()) {
      throw BadRequestException("Insufficient balance")
    }

    val nft = imageNftRepository.findById(NftId(id, collectionId))
      .orElseThrow()

    try {
      blockchainService.mint(
        contractAddress = appProperties.collectionAddress,
        address = address,
        tokenId = BigInteger.valueOf(id),
      )
    } catch (e: Exception) {
      logger.error("Failed to mint $collectionId-$id")
      throw e
    }
    nft.isMinted = true

    user.balance = user.balance - carType.mintPrice.toBigDecimal()
    userService.save(user)

    return imageNftRepository.save(nft)
  }

  fun updateImage(output: com.app.surnft.backend.ai.AIOutput): ImageNft {
    logger().info("{${output.prompt}}")
    val nft = imageNftRepository.findNftByPrompt(output.prompt).get()
    nft.image = "https" + output.url.substringAfter("https").substring(0, 58)
    imageNftRepository.save(nft)
    return nft
  }

  fun upgradeCar(id: Long, collectionId: Long, input: com.app.surnft.backend.ai.AIInput) {
    val nft = imageNftRepository.findById(NftId(id, collectionId))
    nft.get().image
  }

  fun get(id: Long, collectionId: Long): ImageNft {
    return imageNftRepository.findById(NftId(id, collectionId)).orElseThrow()
  }

  fun save(imageNft: ImageNft) {
    imageNftRepository.save(imageNft)
  }

  fun getRepairCost(car: ImageNft) =
    appProperties.durabilityRepairCost

  fun getRepairableCost(car: ImageNft, newDurability: Float): CarRepairInfo {
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
  fun repair(id: Long, collectionId: Long, address: String, newDurability: Float): ImageNft {
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
      imageNftRepository.save(car)
    }

    return car
  }

  fun getLevelUpCost(car: ImageNft): CarLevelUpCostResponse {
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
  fun levelUp(id: Long, collectionId: Long, initiatorAddress: String): ImageNft {
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
    return imageNftRepository.save(car)
  }
}
