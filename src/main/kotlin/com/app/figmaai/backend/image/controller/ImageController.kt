package com.app.figmaai.backend.image.controller

import com.amazonaws.HttpMethod
import com.app.figmaai.backend.ai.AiProvider
import com.app.figmaai.backend.ai.AiVersion
import com.app.figmaai.backend.constraint.Figma
import com.app.figmaai.backend.image.dto.GetAllNftRequest
import com.app.figmaai.backend.image.dto.ImageInternalDto
import com.app.figmaai.backend.image.dto.PurchaseImageRequest
import com.app.figmaai.backend.image.mapper.ImageMapper
import com.app.figmaai.backend.image.model.UploadResult
import com.app.figmaai.backend.image.service.AwsS3Service
import com.app.figmaai.backend.image.service.ImageService
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
    @ParameterObject request: GetAllNftRequest
  ): Page<ImageInternalDto> = imageService.getAll(pageable, request)
    .map { ImageMapper.toInternalDto(it) }

  @GetMapping("/{id}")
  fun getImageInternalInfo(@PathVariable id: Long): ImageInternalDto =
    ImageMapper.toInternalDto(imageService.get(id))

  @PatchMapping("/generate")
  fun purchase(
    @Figma @RequestHeader figma: String,
    @Valid @RequestBody request: PurchaseImageRequest,
  ): ImageInternalDto {
    val provider = com.app.figmaai.backend.ai.AiProvider.values().find {
      it.name.equals(request.provider, true)
    } ?: AiProvider.MIDJOURNEY
    return imageService.create(
      id = figma,
      prompt = request.prompt.trim(),
      provider = provider,
      version = AiVersion.valueOf(request.version.uppercase()),
    ).let { ImageMapper.toInternalDto(it) }
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
