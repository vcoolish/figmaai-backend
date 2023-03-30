package com.app.figmaai.backend.image.service

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
import com.app.figmaai.backend.user.service.UserService
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
import java.net.URL
import java.time.Clock
import java.time.ZonedDateTime
import javax.imageio.ImageIO

@Service
class ImageService(
  private val imageRepository: ImageRepository,
  private val userService: UserService,
  private val appProperties: AppProperties,
  private val imageCreationService: ImageCreationService,
) {

  private val logger = logger()
  private val restTemplate = RestTemplate()

  fun getAll(pageable: Pageable, request: GetAllNftRequest): Page<ImageAI> {
    val spec: Specification<ImageAI> = if (request.figma.isNullOrEmpty()) {
      findByPrompt(request.query)
    } else {
      userEqual(request.figma).and(findByPrompt(request.query))
    }
    return imageRepository.findAll(spec, pageable)
  }

  @Transactional
  fun create(
    id: String,
    prompt: String,
    provider: com.app.figmaai.backend.ai.AiProvider,
  ): List<ImageAI> {
    validatePrompt(prompt)

    val user = userService.get(id)
    val spec: Specification<ImageAI> = imageIsEmpty()
      .and(userEqual(id))
      .and(createdAtGreaterOrEqual(ZonedDateTime.now(Clock.systemUTC()).minusMinutes(5)))
    val inProgress = imageRepository.exists(spec)
    if (inProgress) {
      throw BadRequestException("You already have an image in progress")
    }

    if (hasSubscription(id)) {
      throw BadRequestException("Subscription expired")
    }
    logger.info("{${prompt}}")

    val cleanPrompt = if (prompt.startsWith("https://")) prompt.substringAfter(" ") else prompt
    val images = if (provider == com.app.figmaai.backend.ai.AiProvider.MIDJOURNEY) {
      requestMidjourneyImage(prompt, user)
    } else {
      createDalleImage(prompt, user)
    }.map { image ->
      image.name = "Image #${image.imageId}"
      image.prompt = cleanPrompt
      imageRepository.save(image)
    }

    userService.save(user)

    return images
  }

  private fun hasSubscription(id: String): Boolean = true

  private fun requestMidjourneyImage(prompt: String, user: User): List<ImageAI> {
    restTemplate.postForEntity(
      "https://surnft-ai-collection.herokuapp.com/task",
      mapOf(
        "prompt" to prompt,
      ),
      String::class.java,
    )
    return (0..3).map {
      imageCreationService.create(user)
        .let(imageRepository::saveAndFlush)
    }
  }

  private fun createDalleImage(prompt: String, user: User): List<ImageAI> = (0..3).map {
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
        com.app.figmaai.backend.ai.DalleRequest(prompt.substringAfter(" ")),
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
    val url = restTemplate.postForEntity(
      "https://surnft-ai.herokuapp.com/upload",
      mapOf(
        "url" to createdPicUrl,
      ),
      String::class.java,
    ).body?.substringAfter("https")?.substring(0, 58)
      ?: throw BadRequestException("Image not uploaded")
    imageCreationService.create(user)
      .let(imageRepository::saveAndFlush).apply {
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

    val imageUrl = "https" + output.url.substringAfter("https").substring(0, 58)
    try {
      Thread.sleep(30000)
      ImageIO.read(URL(imageUrl))

      image.image = imageUrl
      imageRepository.save(image)
    } catch (e: IOException) {
      restTemplate.postForEntity(
        "https://surnft-ai.herokuapp.com/task",
        mapOf(
          "prompt" to output.prompt,
        ),
        String::class.java,
      )
    }

    return image
  }

  fun get(id: Long): ImageAI {
    return imageRepository.findById(id).orElseThrow()
  }

  fun save(imageAI: ImageAI) {
    imageRepository.save(imageAI)
  }
}
