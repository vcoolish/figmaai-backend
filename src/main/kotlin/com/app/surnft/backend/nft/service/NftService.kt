package com.app.surnft.backend.nft.service

import com.app.surnft.backend.ai.AiProvider
import com.app.surnft.backend.ai.DalleImageRequest
import com.app.surnft.backend.ai.DalleRequest
import com.app.surnft.backend.ai.DalleResponse
import com.app.surnft.backend.blockchain.service.BlockchainService
import com.app.surnft.backend.common.util.bannedPhrases
import com.app.surnft.backend.common.util.bannedWords
import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.exception.BadRequestException
import com.app.surnft.backend.exception.InsufficientBalanceException
import com.app.surnft.backend.nft.data.CarRepairInfo
import com.app.surnft.backend.nft.dto.CarLevelUpCostResponse
import com.app.surnft.backend.nft.dto.GetAllNftRequest
import com.app.surnft.backend.nft.entity.ImageCollection
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import com.app.surnft.backend.nft.repository.ImageNftRepository
import com.app.surnft.backend.nft.repository.extra.ImageNftSpecification.hasMintedEntries
import com.app.surnft.backend.nft.repository.extra.ImageNftSpecification.userEqual
import com.app.surnft.backend.user.model.User
import com.app.surnft.backend.user.service.UserEnergyService
import com.app.surnft.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
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
  private val userEnergyService: UserEnergyService,
) {

  private val logger = logger()
  private val restTemplate = RestTemplate()

  fun getAll(pageable: Pageable, request: GetAllNftRequest): Page<ImageNft> {
    val spec: Specification<ImageNft> = userEqual(request.address)
    return imageNftRepository.findAll(spec, pageable)
  }

  @Transactional
  fun create(
    address: String,
    collectionId: Long,
    prompt: String,
    provider: AiProvider,
  ): ImageNft {
    validatePrompt(prompt)

    val user = userService.getOrCreate(address)
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
//    val spec: Specification<ImageNft> = imageIsEmpty()
//      .and(userEqual(address))
//      .and(createdAtGreaterOrEqual(ZonedDateTime.now(Clock.systemUTC()).minusMinutes(2)))
//    val inProgress = imageNftRepository.exists(spec)
//    if (inProgress) {
//      throw BadRequestException("You already have an image in progress")
//    }

    if (user.energy < provider.energy.toBigDecimal() && user.balance < carType.price.toBigDecimal()) {
      throw InsufficientBalanceException("Energy too low. Try to mint, wait for energy to regenerate or top up at least 0.002 SUR BNB.")
    }
    logger.info("{${prompt}}")

    val cleanPrompt = if (prompt.startsWith("https://")) prompt.substringAfter(" ") else prompt
    val nft = if (provider == AiProvider.MIDJOURNEY) {
      requestMidjourneyImage(prompt, user, collectionId)
    } else {
      createDalleImage(prompt, user, collectionId)
    }

    val id: Long = nft.getSafeId().id!!

    nft.name = "${carType.title} #$id"
    nft.prompt = cleanPrompt
    nft.externalUrl = "https://tofunft.com/nft/bsc/0xe73711e8331aD93ca115A2AE4D1AFAc74E15D644/$id"

    if (user.energy < provider.energy.toBigDecimal()) {
      user.balance -= carType.price.toBigDecimal()
    } else {
      userEnergyService.spendEnergy(user, provider.energy.toBigDecimal())
    }
    userService.save(user)

    return imageNftRepository.save(nft)
  }

  private fun requestMidjourneyImage(prompt: String, user: User, collectionId: Long): ImageNft {
    restTemplate.postForEntity(
      "https://surnft-ai.herokuapp.com/task",
      mapOf(
        "prompt" to prompt,
      ),
      String::class.java,
    )
    return imageCreationService.create(user, collectionId)
      .let(imageNftRepository::saveAndFlush)
  }

  private fun createDalleImage(prompt: String, user: User, collectionId: Long): ImageNft {
    val (body, path) = if (prompt.startsWith("https://")) {
      Pair(
        DalleImageRequest(prompt.substringAfter(" "), prompt.substringBefore(" ")),
        "edits"
      )
    } else {
      Pair(
        DalleRequest(prompt.substringAfter(" ")),
        "generations",
      )
    }
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")
    headers.add("OpenAI-Organization", "org-PPCMBOiIcK9DBzlYoBqyNeFJ")
    headers.add("Content-Type", "application/json")
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)

    val response = restTemplate.exchange(
      "https://api.openai.com/v1/images/$path",
      HttpMethod.POST,
      httpEntity,
      DalleResponse::class.java
    )
    val createdPicUrl = response.body?.data?.firstOrNull()?.url
      ?: throw BadRequestException("Failed to create image")
    val url = restTemplate.postForEntity(
      "https://surnft-ai.herokuapp.com/upload",
      mapOf(
        "url" to createdPicUrl,
      ),
      String::class.java,
    ).body?.substringAfter("https")?.substring(0, 58)
      ?: throw BadRequestException("Image not uploaded")
    return imageCreationService.create(user, collectionId)
      .let(imageNftRepository::saveAndFlush).apply {
        response.body?.data?.firstOrNull()?.url?.let {
          image = "https$url"
        } ?: throw BadRequestException("Image generation failed")
      }
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
    val hasFreeMint = hasFreeMint(address)
    if (user.balance < carType.mintPrice.toBigDecimal() && !hasFreeMint) {
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
    user.energy = user.maxEnergy

    if (!hasFreeMint) {
      user.balance = user.balance - carType.mintPrice.toBigDecimal()
    }
    userService.save(user)

    val updatedUser = userService.get(address)
    if (updatedUser.nfts.size == 3) {
      userEnergyService.increaseMaxEnergy(updatedUser)
    }

    return imageNftRepository.save(nft)
  }

  fun hasFreeMint(address: String): Boolean {
    val spec: Specification<ImageNft> = hasMintedEntries()
      .and(userEqual(address))
    val hasMinted = imageNftRepository.exists(spec)
    if (hasMinted) {
      return false
    }
    val user = userService.get(address)
    return user.balance > BigDecimal.ZERO
  }

  fun delete(address: String, collectionId: Long, id: Long): Boolean {
    val nft = get(id, collectionId)
    require(nft.user.address.equals(address, true)) { "Not your nft" }
    if (nft.isMinted) {
      return false
    }
    imageNftRepository.deleteById(NftId(id, collectionId))
    return true
  }

  fun updateImage(output: com.app.surnft.backend.ai.AIOutput): ImageNft {
    logger().info("{${output.prompt}}")
    val cleanPrompt = if (output.prompt.startsWith("<https://")) {
      output.prompt.substringAfter(" ")
    } else {
      output.prompt
    }
    val nft = imageNftRepository.findNftByPrompt(cleanPrompt).get()
    logger().info("nftprompt{${nft.prompt}}")
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

  fun getMintPrice(collectionId: Long) =
    ImageCollection.values().first { it.collectionId == collectionId }.mintPrice
}
