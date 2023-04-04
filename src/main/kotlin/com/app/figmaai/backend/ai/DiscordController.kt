package com.app.figmaai.backend.ai

import com.app.figmaai.backend.constraint.Figma
import com.app.figmaai.backend.image.service.ImageService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/ai")
class DiscordController(
  private val imageService: ImageService,
) {

  @PostMapping("/message")
  fun onMessage(
    @RequestHeader keyword: String,
    @Valid @RequestBody body: AIOutput,
  ): Any {
    if (keyword != "poop") {
      return "Invalid keyword"
    }
    imageService.updateImage(body)

    return true
  }

  @PostMapping("/imagine")
  fun imagine(
    @Figma @RequestHeader keyword: String,
    @Valid @RequestBody body: AIInput,
  ): String {
    return ""
  }
}