package com.app.drivn.backend.nft.service

import com.app.drivn.backend.ai.AIInput
import com.app.drivn.backend.ai.AIOutput
import com.app.drivn.backend.blockchain.service.BlockchainService
import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.exception.BadRequestException
import com.app.drivn.backend.exception.NotFoundException
import com.app.drivn.backend.nft.data.CarRepairInfo
import com.app.drivn.backend.nft.dto.CarLevelUpCostResponse
import com.app.drivn.backend.nft.entity.ImageCollection
import com.app.drivn.backend.nft.model.ImageNft
import com.app.drivn.backend.nft.model.Image
import com.app.drivn.backend.nft.model.NftId
import com.app.drivn.backend.nft.repository.ImageNftRepository
import com.app.drivn.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
  private val imageService: ImageService,
  private val imageCreationService: ImageCreationService,
  private val blockchainService: BlockchainService,
) {

  private val logger = logger()

  fun getAll(pageable: Pageable): Page<ImageNft> {
    return imageNftRepository.findAll(pageable)
  }

  private fun getNextFreeImage(): Image {
    return imageService.findFreeImage()
      ?: throw NotFoundException("Free image for NFT not found")
  }

  fun create(address: String, collectionId: Long, prompt: String): ImageNft {
    val user = userService.getOrCreate(address)
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
//    if (user.balance < carType.price.toBigDecimal()) {
//      throw BadRequestException("Insufficient balance")
//    }
    RestTemplate().postForEntity(
      "https://surnft-ai.herokuapp.com/task",
      mapOf(
        "prompt" to prompt,
      ),
      String::class.java,
    )

    val nft = imageCreationService.create(user, collectionId)
      .apply { this.image = getNextFreeImage() }
      .let(imageNftRepository::saveAndFlush)

    val id: Long = nft.id!!

    nft.name = "${carType.title} #$id"
    nft.prompt = prompt.trim()
    nft.externalUrl = "https://tofunft.com/nft/bsc/0x34031C84Ee86e11D45974847C380091A84705921/$id"

//    user.balance = user.balance - carType.price.toBigDecimal()
    userService.save(user)

    return imageNftRepository.save(nft)
  }

  fun mint(address: String, collectionId: Long, id: Long): ImageNft {
    val user = userService.get(address)
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
    if (user.balance < carType.mintPrice.toBigDecimal()) {
      throw BadRequestException("Insufficient balance")
    }

    val nft = imageNftRepository.findById(NftId(collectionId, id))
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

  fun updateImage(output: AIOutput) {
    val keywords = output.filename.split("_")
    val lastIndex = keywords.indexOfLast { it == ".png" }
    val prompt = keywords.subList(2, lastIndex).joinToString(" ").trim()
    val nft = imageNftRepository.findImageByPrompt(prompt)
      ?: throw NotFoundException("NFT with prompt $prompt not found")
    nft.image.dataTxId = output.url.substring(output.url.lastIndexOf("/") + 1)
    imageNftRepository.save(nft)
  }

  fun upgradeCar(id: Long, collectionId: Long, input: AIInput) {
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
