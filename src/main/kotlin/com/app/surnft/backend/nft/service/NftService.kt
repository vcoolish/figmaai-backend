package com.app.surnft.backend.nft.service

import com.app.surnft.backend.ai.AiProvider
import com.app.surnft.backend.ai.DalleRequest
import com.app.surnft.backend.ai.DalleResponse
import com.app.surnft.backend.blockchain.service.BlockchainService
import com.app.surnft.backend.common.util.bannedPhrases
import com.app.surnft.backend.common.util.bannedWords
import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.exception.BadRequestException
import com.app.surnft.backend.exception.InsufficientBalanceException
import com.app.surnft.backend.exception.NotFoundException
import com.app.surnft.backend.nft.data.CarRepairInfo
import com.app.surnft.backend.nft.dto.CarLevelUpCostResponse
import com.app.surnft.backend.nft.dto.GetAllNftRequest
import com.app.surnft.backend.nft.entity.ImageCollection
import com.app.surnft.backend.nft.model.Collection
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import com.app.surnft.backend.nft.repository.CollectionRepository
import com.app.surnft.backend.nft.repository.ImageNftRepository
import com.app.surnft.backend.nft.repository.extra.ImageNftSpecification.hasMintedEntries
import com.app.surnft.backend.nft.repository.extra.ImageNftSpecification.userEqual
import com.app.surnft.backend.user.model.User
import com.app.surnft.backend.user.service.UserEnergyService
import com.app.surnft.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.min


@Service
class NftService(
  private val imageNftRepository: ImageNftRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageCreationService: ImageCreationService,
  private val blockchainService: BlockchainService,
  private val userEnergyService: UserEnergyService,
  private val collectionRepository: CollectionRepository,
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

  @Async
  fun deployCollection(
    address: String,
    prompt: String,
    provider: AiProvider,
    option: Int,
    name: String,
    symbol: String,
    styles: String,
  ) {
    validatePrompt(prompt)

    val user = userService.getOrCreate(address)

    val price = getCollectionPrices()[option]
//    if (user.balance < price) {
//      throw InsufficientBalanceException("Insufficient balance")
//    }
//    val collection = Collection()
    val count = getCountByOption(option)
    val contract = "0xbd8b7202f715f1f29db726b24007998d5d42bf21" //fill mighty frogs
//      blockchainService.deployCollection(
//      name = name,
//      symbol = symbol,
//      uri = "https://api.surnft.com/nft/${collection.id}/",
//      owner = address,
//      count = count,
//    )
//    user.balance -= price
//    collectionRepository.saveAndFlush(
//      collection.apply {
//        this.address = contract
//        this.user = user
//        this.count = count
//        this.name = name
//        this.symbol = symbol
//      }
//    )
    userService.save(user)

    createCollection(
      collectionId = 1678158393L,
      prompt = prompt,
      count = count,
      name = name,
      userAddress = user.address,
      contract = contract,
      styles = styles,
    )
  }

  fun createCollection(
    collectionId: Long,
    prompt: String,
    count: Int,
    name: String,
    userAddress: String,
    contract: String,
    attempt: Int = 0,
    startIndex: Long = 0,
    styles: String,
  ) {
    if (!collectionRepository.existsById(collectionId)) {
      throw NotFoundException("Collection not found")
    }
    val user = userService.getOrCreate(userAddress)
    val styleList = styles.split(",")
    try {
      (startIndex until count).map { id ->
        // pick random style from styleList
        val style = styleList.random()
        val currentPrompt = "$prompt $style"
//        if (startIndex == 0L) {
          val nft = imageCreationService.create(user, collectionId, id)
          nft.name = "$name #$id"
          nft.prompt = currentPrompt
          nft.externalUrl = "https://tofunft.com/nft/bsc/$contract/$id"
          nft.isMinted = true
          imageNftRepository.saveAndFlush(nft)
//        }
        if ((id % 4) == 0L || id == 99L) {
          restTemplate.postForEntity(
            "https://surnft-ai-collection.herokuapp.com/task",
            mapOf(
              "prompt" to currentPrompt,
            ),
            String::class.java,
          )
          Thread.sleep(300000)
        }
      }
    } catch (t: Throwable) {
      if (attempt < 3) {
        logger.error("Failed to create collection, retrying", t)
        createCollection(
          collectionId = collectionId,
          prompt = prompt,
          count = count,
          name = name,
          userAddress = user.address,
          contract = contract,
          attempt = attempt + 1,
          startIndex = imageNftRepository.countImageInCollection(collectionId) - 1,
          styles = styles,
        )
      } else {
        logger.error("Failed to create collection, giving up", t)
      }
    }
  }

  private fun getCountByOption(option: Int): Int =
    when (option) {
      0 -> 100
      1 -> 500
      2 -> 1000
      else -> throw IllegalArgumentException("Unkown option: $option")
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
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", "Bearer ${appProperties.dalleKey}")
    headers.add("OpenAI-Organization", "org-PPCMBOiIcK9DBzlYoBqyNeFJ")
    val (body, path) = if (prompt.startsWith("https://")) {
      val fileContent = URL(prompt.substringBefore(" ")).openStream().readAllBytes()

      val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
      val contentDisposition = ContentDisposition
        .builder("form-data")
        .name("image")
        .filename("image.png")
        .build()

      fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
      val fileEntity = HttpEntity(fileContent, fileMap)

      val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
      body.add("image", fileEntity)
      body.add("prompt", prompt.substringAfter(" "))

      headers.add("Content-Type", "multipart/form-data")
      Pair(
        body,
        "edits"
      )
    } else {
      headers.add("Content-Type", "application/json")
      Pair(
        DalleRequest(prompt.substringAfter(" ")),
        "generations",
      )
    }
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
    if (!appProperties.freeMintEnabled) {
      return false
    }
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

  fun getCollectionPrices() =
    appProperties.collectionPrices

  @Async
  fun updateImage(output: com.app.surnft.backend.ai.AIOutput): ImageNft {
    val cleanPrompt = if (output.prompt.startsWith("<https://")) {
      output.prompt.substringAfter(" ")
    } else {
      output.prompt
    }
    val nft = imageNftRepository.findNftByPrompt(cleanPrompt).first()

    val imageUrl = "https" + output.url.substringAfter("https").substring(0, 58)
    try {
      Thread.sleep(30000)
      ImageIO.read(URL(imageUrl))

      nft.image = imageUrl
      imageNftRepository.save(nft)
    } catch (e: IOException) {
      restTemplate.postForEntity(
        "https://surnft-ai.herokuapp.com/task",
        mapOf(
          "prompt" to output.prompt,
        ),
        String::class.java,
      )
    }

    return nft
  }

  fun get(id: Long, collectionId: Long): ImageNft {
    return imageNftRepository.findById(NftId(id, collectionId)).orElseThrow()
  }

  fun getCollectionNfts(collectionId: Long): List<ImageNft> {
    return imageNftRepository.findNftByCollection(collectionId)
  }

  fun collectionInProgressCount(collectionId: Long): Long {
    return imageNftRepository.countImageInCollection(collectionId)
  }

  fun getCollection(collectionId: Long): Collection {
    return collectionRepository.findById(collectionId).orElseThrow()
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
