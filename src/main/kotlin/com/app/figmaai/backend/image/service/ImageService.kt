package com.app.figmaai.backend.image.service

import com.app.figmaai.backend.ai.*
import com.app.figmaai.backend.common.util.SpecificationUtil.not
import com.app.figmaai.backend.common.util.bannedPhrases
import com.app.figmaai.backend.common.util.bannedWords
import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.exception.InProgressException
import com.app.figmaai.backend.exception.InsufficientBalanceException
import com.app.figmaai.backend.image.dto.GetAllNftRequest
import com.app.figmaai.backend.image.dto.SearchType
import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.image.repository.ImageRepository
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.createdAtGreaterOrEqual
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.findByPrompt
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.imageIsEmpty
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.userEqual
import com.app.figmaai.backend.image.utils.GifSequenceWriter
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.service.TokenProvider
import com.app.figmaai.backend.user.service.UserEnergyService
import com.app.figmaai.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.*
import org.springframework.scheduling.annotation.Async
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.net.URL
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

@Service
class ImageService(
  private val imageRepository: ImageRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageCreationService: ImageCreationService,
  private val awsS3Service: AwsS3Service,
  private val userEnergyService: UserEnergyService,
  private val tokenProvider: TokenProvider,
) {

  private val logger = logger()
  private val restTemplate = RestTemplate()

  fun getAll(pageable: Pageable, request: GetAllNftRequest, token: String): Page<ImageAI> {
    val spec: Specification<ImageAI> = if (request.figma.isNullOrEmpty()) {
      val user = tokenProvider.createParser().parseClaimsJws(token).body.subject
      findByPrompt(request.query)
        .and(imageIsEmpty().not())
        .and(userEqual(user).not())
    } else if (request.query.isEmpty()) {
      userEqual(userService.get(request.figma).userUuid)
    } else {
      userEqual(userService.get(request.figma).userUuid).and(findByPrompt(request.query))
    }

    val searchSpec = when (request.searchType) {
      SearchType.all -> spec.and { root, k, builder -> null }
      SearchType.animated -> spec.and { root, k, builder ->
        builder
          .notEqual(root.get<String?>("gif"), "")
      }
      SearchType.video -> spec.and { root, k, builder ->
        builder
          .notEqual(root.get<String?>("video"), "")
      }
      SearchType.static -> spec.and { root, k, builder ->
        builder
          .equal(root.get<String?>("video"), "")
      }.and { root, k, builder ->
        builder
          .equal(root.get<String?>("gif"), "")
      }
    }
    return imageRepository.findAll(searchSpec, pageable)
  }

  @Transactional
  fun create(
    id: String,
    prompt: String,
    provider: AiProvider,
    version: AiVersion,
    height: Int,
    width: Int,
    strength: Int,
  ): ImageAI {
    validatePrompt(prompt)
    val user = userService.get(id)
    checkImageCount(user)

    val spec: Specification<ImageAI> = imageIsEmpty()
      .and(userEqual(user.userUuid))
      .and(createdAtGreaterOrEqual(ZonedDateTime.now(Clock.systemUTC()).minusMinutes(5)))
    val inProgress = imageRepository.exists(spec)
    if (inProgress) {
      throw InProgressException("You already have an image in progress")
    }
    if (user.subscriptionId.isNullOrEmpty() && user.energy < provider.energy.toBigDecimal()) {
      throw InsufficientBalanceException("Not enough energy. Start your subscription for unlimited generations")
    }
    if (!user.subscriptionId.isNullOrEmpty() && user.generations <= 0) {
      throw InsufficientBalanceException("You ran out of generations. Start new subscription to get more.")
    }

    if (!user.isSubscribed) {
      throw ExpiredAuthorizationException("Subscription expired")
    }
    logger.info("{${prompt}}")

    val prompt = prompt
      .split(" ")
      .filter { it.isNotBlank() }
      .joinToString(" ")
      .trim()
      .let {
        if (provider == AiProvider.MIDJOURNEY && version == AiVersion.V5) {
          "$it --v 5"
        } else {
          it
        }
      }

    val cleanPrompt = if (prompt.startsWith("https://")) prompt.substringAfter(" ") else prompt
    val image = when (provider) {
        AiProvider.MIDJOURNEY ->
          requestMidjourneyImage(prompt, user)
        AiProvider.STABILITY -> {
          val bytes = createStabilityImage(prompt, height, width, strength).first()
          uploadBase64Pic(user, bytes)
        }
        else ->
          createDalleImage(prompt, user)
    }
    image.name = "Image #${image.imageId}"
    image.prompt = cleanPrompt

    imageRepository.save(image)
    if (user.subscriptionId.isNullOrEmpty()) {
      userEnergyService.spendEnergy(user, provider.energy.toBigDecimal())
    } else {
      user.generations -= 1
    }

    userService.save(user)

    return image
  }

  fun createAnimated(
    id: String,
    prompt: String,
    height: Int,
    width: Int,
    strength: Int,
  ): ImageAI {
    validatePrompt(prompt)
    val user = userService.get(id)
    checkImageCount(user)

    val energy = BigDecimal.valueOf(30)
    if (user.subscriptionId.isNullOrEmpty() && user.energy < energy) {
      throw InsufficientBalanceException("Not enough energy. Start your subscription for unlimited generations")
    }
    if (!user.subscriptionId.isNullOrEmpty() && user.animations <= 0) {
      throw InsufficientBalanceException("You ran out of generations. Start new subscription to get more.")
    }

    if (!user.isSubscribed) {
      throw ExpiredAuthorizationException("Subscription expired")
    }
    logger.info("{${prompt}}")

    val prompt = prompt
      .split(" ")
      .filter { it.isNotBlank() }
      .joinToString(" ")
      .trim()

    val initImages = buildList {
      val firstImage = createStabilityImage(prompt, height, width, strength)
      val firstEntity = uploadBase64Pic(user, firstImage.first(), true)
      add(firstEntity)

//      val secondImage = createStabilityImage("${firstEntity.image} $prompt", height, width, 40)
//      val secondEntity = uploadBase64Pic(user, secondImage.first(), false)
//      add(secondEntity)
    }
    generateGif(initImages, user, prompt, height, width)

    return initImages.first()
  }

  fun createVideo(
    id: String,
    prompt: String,
    orientation: String,
    size: String,
    locale: String,
  ): List<ImageAI> {
    validatePrompt(prompt)
    val user = userService.get(id)
    checkImageCount(user)

    val energy = BigDecimal.valueOf(30)
    if (user.subscriptionId.isNullOrEmpty() && user.energy < energy) {
      throw InsufficientBalanceException("Not enough energy. Start your subscription for unlimited generations")
    }
    if (!user.subscriptionId.isNullOrEmpty()) { //&& user.animations <= 0
      throw InsufficientBalanceException("You ran out of generations. Start new subscription to get more.")
    }

    if (!user.isSubscribed) {
      throw ExpiredAuthorizationException("Subscription expired")
    }
    logger.info("{${prompt}}")

    val images = requestVideo(
      prompt = prompt,
      size = size,
      locale = locale,
      orientation = orientation,
      user = user
    )
    images.forEach {
      it.name = "Video #${it.imageId}"
      it.prompt = prompt
      imageRepository.save(it)
    }

    if (user.subscriptionId.isNullOrEmpty()) {
      userEnergyService.spendEnergy(user, 30.toBigDecimal())
    }
//    else {
//      user.animations -= 1
//    }

    userService.save(user)

    return images
  }

  @Async
  fun generateGif(
    initEntities: List<ImageAI>,
    user: User,
    prompt: String,
    height: Int,
    width: Int,
  ) {
    val entity = initEntities.first()
    val cleanPrompt = if (prompt.startsWith("https://")) prompt.substringAfter(" ") else prompt

    logger.info("image ${entity.image}")
    val images = initEntities.flatMap {
      createStabilityImage("${it.image} $cleanPrompt", height, width, 65, 10)
    }
    logger.info("images ${images.size}")
    val file = File.createTempFile(UUID.randomUUID().toString(), ".gif")
    val output = FileImageOutputStream(file)
    logger.info("read file")
    val initImagesIO = initEntities.map { ImageIO.read(URL(it.image)) }

    val writer = GifSequenceWriter(output, initImagesIO.first().type, 200, true)

    initImagesIO.forEach { writer.writeToSequence(it) }
    images.shuffled().forEach {
      logger.info("write image")
      val nextImage: BufferedImage = ImageIO.read(Base64.getDecoder().decode(it).inputStream())
      writer.writeToSequence(nextImage)
    }
    writer.close()
    output.close()
    logger.info("upload gif image")
    val gif = uploadBytesToS3(file.readBytes(), MediaType.IMAGE_GIF)
    file.delete()

    entity.name = "Image #${entity.imageId}"
    entity.prompt = cleanPrompt
    entity.gif = gif
    logger.info(gif)

    imageRepository.save(entity)
    if (user.subscriptionId.isNullOrEmpty()) {
      userEnergyService.spendEnergy(user, BigDecimal.valueOf(30))
    } else {
      user.animations -= 1
    }

    userService.save(user)
  }

  private fun checkImageCount(user: User) {
    val count = imageRepository.findUserImagesByDate(user, ZonedDateTime.now(Clock.systemUTC()).minusMonths(1), ZonedDateTime.now(Clock.systemUTC()))
    if (count >= 300) {
      throw BadRequestException("You have reached the limit of 300 images per month")
    }
  }

  private fun requestMidjourneyImage(prompt: String, user: User): ImageAI {
    restTemplate.postForEntity(
      "https://surnft-ai-collection.herokuapp.com/task",
      mapOf(
        "prompt" to prompt,
      ),
      String::class.java,
    )
    return imageCreationService.create(user)
      .let(imageRepository::saveAndFlush)
  }

  private fun createStabilityImage(
    prompt: String,
    height: Int,
    width: Int,
    strength: Int,
    samples: Int = 1,
  ): List<String> {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", appProperties.stableKey)
    headers.add("Accept", "application/json")
    val response = if (prompt.startsWith("https://")) {
      val fileContent = URL(prompt.substringBefore(" ")).openStream().readAllBytes()

      val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
      val contentDisposition = ContentDisposition
        .builder("form-data")
        .name("init_image")
        .filename("image.png")
        .build()

      fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
      val fileEntity = HttpEntity(fileContent, fileMap)

      val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
      body.add("init_image", fileEntity)
      body.add("text_prompts[0][text]", prompt.substringAfter(" "))
      body.add("image_strength", strength.toDouble() / 100.0)
      body.add("samples", samples)

      headers.add("Content-Type", "multipart/form-data")
      val httpEntity: HttpEntity<*> = HttpEntity<Any>(
        body,
        headers,
      )

      restTemplate.exchange(
        "https://api.stability.ai/v1/generation/stable-diffusion-xl-beta-v2-2-2/image-to-image",
        HttpMethod.POST,
        httpEntity,
        StabilityResponse::class.java
      )
    } else {
      headers.add("Content-Type", "application/json")
      val httpEntity: HttpEntity<*> = HttpEntity<Any>(
        StabilityRequest(
          listOf(StabilityPrompt(prompt.substringAfter(" "))),
          height,
          width,
          samples
        ),
        headers,
      )

      restTemplate.exchange(
        "https://api.stability.ai/v1/generation/stable-diffusion-xl-beta-v2-2-2/text-to-image",
        HttpMethod.POST,
        httpEntity,
        StabilityResponse::class.java
      )
    }
    return response.body?.artifacts?.map { it.base64 }
      ?: throw BadRequestException("Failed to create image")
  }

  private fun uploadBase64Pic(user: User, bytes: String, save: Boolean = true): ImageAI {
    val filename = UUID.randomUUID().toString()
    val url = awsS3Service.generatePreSignedUrl(
      filePath = filename,
      bucketName = "surpics-ai",
      httpMethod = com.amazonaws.HttpMethod.PUT,
    )
    val entity = HttpEntity(
      Base64.getDecoder().decode(bytes),
      HttpHeaders().apply { contentType = MediaType.IMAGE_PNG }
    )

    restTemplate.exchange(
      URI.create(url),
      HttpMethod.PUT,
      entity,
      Any::class.java,
    )
    return imageCreationService.create(user)
      .let { if (save) imageRepository.saveAndFlush(it) else it }.apply {
        image = "https://surpics-ai.s3.amazonaws.com/$filename"
      }
  }

  private fun createDalleImage(prompt: String, user: User): ImageAI {
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
    val url = uploadImageToS3(createdPicUrl)
    return imageCreationService.create(user)
      .let(imageRepository::saveAndFlush).apply {
        response.body?.data?.firstOrNull()?.url?.let {
          image = url
        } ?: throw BadRequestException("Image generation failed")
      }
  }

  private fun requestVideo(
    prompt: String,
    size: String?,
    locale: String?,
    orientation: String?,
    user: User,
  ): List<ImageAI> {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", appProperties.pexelsKey)
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(null, headers)

    val request = buildString {
      append("https://api.pexels.com/videos/search?query=$prompt&per_page=4")
      size?.let { append("&size=$size") }
      locale?.let { append("&locale=$locale") }
      orientation?.let { append("&orientation=$orientation") }
    }

    val response = restTemplate.exchange(
      request,
      HttpMethod.GET,
      httpEntity,
      PexelsResponse::class.java
    )

    return response.body?.videos?.mapNotNull { video ->
      imageCreationService.create(user)
        .let(imageRepository::saveAndFlush).apply {
          this.image = video.image
          this.video = video.video_files.firstOrNull()?.link ?: return@mapNotNull null
        }
    }.orEmpty()
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

  fun delete(figma: String, id: Long): Boolean {
    val image = get(id)
    require(image.user.figma.equals(figma, true)) { "Not your nft" }
    imageRepository.deleteById(id)
    return true
  }

  @Async
  fun updateImage(output: com.app.figmaai.backend.ai.AIOutput): ImageAI {
    val cleanPrompt = if (output.prompt.startsWith("<https://")) {
      output.prompt.substringAfter(" ")
    } else {
      output.prompt
    }
    val image = imageRepository.findNftByPrompt(cleanPrompt).first()

    val url = uploadImageToS3(output.url)

    image.image = url
    imageRepository.save(image)

    return image
  }

  fun uploadImageToS3(picUrl: String): String {
    val image = ImageIO.read(URL(picUrl))
    //image bytes
    val imageBytes = ByteArrayOutputStream()
    ImageIO.write(image, "png", imageBytes)

    return uploadBytesToS3(imageBytes.toByteArray(), MediaType.IMAGE_PNG)
  }

  fun uploadBytesToS3(bytes: ByteArray, contentType: MediaType): String {
    val filename = UUID.randomUUID().toString()
    val url = awsS3Service.generatePreSignedUrl(
      filePath = filename,
      bucketName = "surpics-ai",
      httpMethod = com.amazonaws.HttpMethod.PUT,
    )
    val headers = HttpHeaders()
    headers.contentType = contentType
    val entity = HttpEntity(bytes, headers)

    restTemplate.exchange(
      URI.create(url),
      HttpMethod.PUT,
      entity,
      Any::class.java,
    )
    return "https://surpics-ai.s3.amazonaws.com/$filename"
  }

  data class AwsResponse(val filename: String)

  fun get(id: Long): ImageAI {
    return imageRepository.findById(id).orElseThrow()
  }

  fun save(imageAI: ImageAI) {
    imageRepository.save(imageAI)
  }
}
