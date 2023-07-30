package com.app.figmaai.backend.image.mapper

import com.app.figmaai.backend.image.dto.ImageBaseDto
import com.app.figmaai.backend.image.dto.ImageInternalDto
import com.app.figmaai.backend.image.model.Image
import com.app.figmaai.backend.image.model.ImageAI

object ImageMapper {

  @Deprecated("It's too random. Use NftMapper.generateCar.")
  fun generateRandomImage(id: Long): ImageAI {
    val imageAI = ImageAI()

    imageAI.name = "Image #$id"
    imageAI.description = "AI powered picture created from description prompt"

    return imageAI
  }

  fun generateCar(): ImageAI {
    val imageAI = ImageAI()

    imageAI.description = "AI powered picture created from description prompt"

    return imageAI
  }

  private fun <T : ImageBaseDto> fillBaseDto(image: Image, dto: T): T {
    dto.name = image.name ?: ""
    dto.description = image.description
    dto.image = image.image
    dto.gif = image.gif

    return dto
  }

  fun toInternalDto(imageAI: ImageAI): ImageInternalDto {
    val dto = fillBaseDto(imageAI, ImageInternalDto())

    dto.id = imageAI.getSafeId()
    dto.prompt = imageAI.prompt

    return dto
  }
}