package com.app.figmaai.backend.image.service

import com.app.figmaai.backend.ai.*
import com.app.figmaai.backend.common.util.SpecificationUtil.not
import com.app.figmaai.backend.common.util.bannedPhrases
import com.app.figmaai.backend.common.util.bannedWords
import com.app.figmaai.backend.common.util.logger
import com.app.figmaai.backend.config.properties.AppProperties
import com.app.figmaai.backend.exception.BadRequestException
import com.app.figmaai.backend.image.dto.GetAllNftRequest
import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.image.repository.ImageRepository
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.createdAtGreaterOrEqual
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.findByPrompt
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.imageIsEmpty
import com.app.figmaai.backend.image.repository.extra.ImageSpecification.userEqual
import com.app.figmaai.backend.user.model.User
import com.app.figmaai.backend.user.service.RefreshTokenService
import com.app.figmaai.backend.user.service.UserEnergyService
import com.app.figmaai.backend.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.*
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URL
import java.time.Clock
import java.time.ZonedDateTime
import java.util.*
import javax.imageio.ImageIO

@Service
class ImageService(
  private val imageRepository: ImageRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageCreationService: ImageCreationService,
  private val awsS3Service: AwsS3Service,
  private val userEnergyService: UserEnergyService,
  private val refreshTokenService: RefreshTokenService,
) {

  private val logger = logger()
  private val restTemplate = RestTemplate()

  fun getAll(pageable: Pageable, request: GetAllNftRequest, token: String): Page<ImageAI> {
    val spec: Specification<ImageAI> = if (request.figma.isNullOrEmpty()) {
      findByPrompt(request.query)
        .and(imageIsEmpty().not())
        .and(userEqual(refreshTokenService.getOne(token)?.user?.userUuid).not())
    } else if (request.query.isEmpty()) {
      userEqual(userService.get(request.figma).userUuid)
    } else {
      userEqual(userService.get(request.figma).userUuid).and(findByPrompt(request.query))
    }
    return imageRepository.findAll(spec, pageable)
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
      throw BadRequestException("You already have an image in progress")
    }
    if (user.subscriptionId.isNullOrEmpty() && user.energy < provider.energy.toBigDecimal()) {
      throw BadRequestException("Not enough energy. Start your subscription for unlimited generations")
    }

    if (!hasSubscription(id)) {
      throw BadRequestException("Subscription expired")
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
        AiProvider.STABILITY ->
          createStabilityImage(prompt, user, height, width, strength)
        else ->
          createDalleImage(prompt, user)
    }
    image.name = "Image #${image.imageId}"
    image.prompt = cleanPrompt
    imageRepository.save(image)
    if (user.subscriptionId.isNullOrEmpty()) {
      userEnergyService.spendEnergy(user, provider.energy.toBigDecimal())
    }

    userService.save(user)

    return image
  }

  private fun checkImageCount(user: User) {
    val count = imageRepository.findUserImagesByDate(user, ZonedDateTime.now(Clock.systemUTC()).minusMonths(1), ZonedDateTime.now(Clock.systemUTC()))
    if (count >= 300) {
      throw BadRequestException("You have reached the limit of 300 images per month")
    }
  }

  private fun hasSubscription(id: String): Boolean = true

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
    user: User,
    height: Int,
    width: Int,
    strength: Int
  ): ImageAI {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", appProperties.stableKey)
    headers.add("Accept", "application/json")
    val (body, path) = if (prompt.startsWith("https://")) {
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
      body.add("image_strength", strength / 100f)

      headers.add("Content-Type", "multipart/form-data")
      Pair(
        body,
        "image-to-image"
      )
    } else {
      headers.add("Content-Type", "application/json")
      Pair(
        StabilityRequest(listOf(StabilityPrompt(prompt.substringAfter(" "))), height, width),
        "text-to-image",
      )
    }
    val httpEntity: HttpEntity<*> = HttpEntity<Any>(body, headers)

    val response = restTemplate.exchange(
      "https://api.stability.ai/v1/generation/stable-diffusion-xl-beta-v2-2-2/$path",
      HttpMethod.POST,
      httpEntity,
      StabilityResponse::class.java
    )
    val createdPicBytes = response.body?.artifacts?.firstOrNull()?.base64
      ?: throw BadRequestException("Failed to create image")

    val filename = UUID.randomUUID().toString()
    val url = awsS3Service.generatePreSignedUrl(
      filePath = filename,
      bucketName = "surpics-ai",
      httpMethod = com.amazonaws.HttpMethod.PUT,
    )
    val entity = HttpEntity(
      Base64.getDecoder().decode(createdPicBytes),
      HttpHeaders().apply { contentType = MediaType.IMAGE_PNG }
    )

    restTemplate.exchange(
      URI.create(url),
      HttpMethod.PUT,
      entity,
      Any::class.java,
    )
    return imageCreationService.create(user)
      .let(imageRepository::saveAndFlush).apply {
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
      com.app.figmaai.backend.ai.DalleResponse::class.java
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
    val filename = UUID.randomUUID().toString()
    val url = awsS3Service.generatePreSignedUrl(
      filePath = filename,
      bucketName = "surpics-ai",
      httpMethod = com.amazonaws.HttpMethod.PUT,
    )
    val headers = HttpHeaders()
    headers.contentType = MediaType.IMAGE_PNG
    //image bytes
    val imageBytes = ByteArrayOutputStream()
    ImageIO.write(image, "png", imageBytes)
    val entity = HttpEntity(imageBytes.toByteArray(), headers)

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
