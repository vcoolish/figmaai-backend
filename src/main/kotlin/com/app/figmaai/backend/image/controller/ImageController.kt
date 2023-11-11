package com.app.figmaai.backend.image.controller

import com.amazonaws.HttpMethod
import com.app.figmaai.backend.ai.AiProvider
import com.app.figmaai.backend.ai.AiVersion
import com.app.figmaai.backend.constraint.Figma
import com.app.figmaai.backend.exception.InProgressException
import com.app.figmaai.backend.exception.InsufficientBalanceException
import com.app.figmaai.backend.image.dto.*
import com.app.figmaai.backend.image.mapper.ImageMapper
import com.app.figmaai.backend.image.model.UploadResult
import com.app.figmaai.backend.image.service.AwsS3Service
import com.app.figmaai.backend.image.service.ImageService
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/images")
class ImageController(
  private val imageService: ImageService,
  private val awsS3Service: AwsS3Service,
) {

  @GetMapping("")
  fun getAll(
    @ParameterObject
    @PageableDefault(
      size = 15,
      page = 0,
      sort = ["imageId"],
      direction = Sort.Direction.DESC
    ) pageable: Pageable,
    @ParameterObject request: GetAllNftRequest,
    @RequestHeader token: String,
  ): Page<ImageInternalDto> = imageService.getAll(pageable, request, token)
    .map { ImageMapper.toInternalDto(it) }

  @GetMapping("/{id}")
  fun getImageInternalInfo(@PathVariable id: Long): ImageInternalDto =
    ImageMapper.toInternalDto(imageService.get(id))

  @PatchMapping("/generate")
  fun purchase(
    @Figma @RequestHeader figma: String,
    @Valid @RequestBody request: PurchaseImageRequest,
  ): ResponseEntity<ImageInternalDto?> = try {
    val provider = com.app.figmaai.backend.ai.AiProvider.values().find {
      it.name.equals(request.provider, true)
    } ?: AiProvider.MIDJOURNEY
    imageService.create(
      id = figma,
      prompt = request.prompt.trim(),
      provider = provider,
      version = AiVersion.valueOf(request.version.uppercase()),
      height = request.height,
      width = request.width,
      strength = request.strengthPercent,
    ).let { ResponseEntity.ok(ImageMapper.toInternalDto(it)) }
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InProgressException) {
    ResponseEntity.status(406).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }

  @PatchMapping("/generate/animated")
  fun purchaseAnimated(
    @Figma @RequestHeader figma: String,
    @Valid @RequestBody request: PurchaseAnimatedImageRequest,
  ): ResponseEntity<ImageInternalDto?> = try {
    imageService.createAnimated(
      id = figma,
      prompt = request.prompt.trim(),
      height = request.height,
      width = request.width,
      strength = request.strengthPercent,
    ).let { ResponseEntity.ok(ImageMapper.toInternalDto(it)) }
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InProgressException) {
    ResponseEntity.status(406).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }

  @PatchMapping("/generate/video")
  fun purchaseVideo(
    @Figma @RequestHeader figma: String,
    @Valid @RequestBody request: PurchaseVideoRequest,
  ): ResponseEntity<List<ImageInternalDto>?> = try {
    imageService.createVideo(
      id = figma,
      prompt = request.prompt.trim(),
      orientation = request.orientation,
      size = request.size,
      locale = request.locale,
    ).let { ResponseEntity.ok(it.map { ImageMapper.toInternalDto(it) }) }
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InProgressException) {
    ResponseEntity.status(406).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }

  @DeleteMapping("/{id}")
  fun delete(
    @PathVariable id: Long,
    @Figma @RequestHeader figma: String,
  ): Boolean {
    return imageService.delete(figma, id)
  }

  @PostMapping("/upload/{filename}")
  fun upload(
    @Figma @RequestHeader figma: String,
    @PathVariable filename: String,
  ): UploadResult {
    val s3File = UUID.randomUUID().toString() + filename
    val url = awsS3Service.generatePreSignedUrl(
      filePath = s3File,
      bucketName = "surpics",
      httpMethod = HttpMethod.PUT,
    )
    return UploadResult(url = url, filename = s3File)
  }
}
