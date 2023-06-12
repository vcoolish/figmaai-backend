package com.app.figmaai.backend.chatgpt

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
class ChatGptController(
  private val chatGptService: ChatGptService,
) {

  @PostMapping("/copyright")
  fun updateSubscription(
    @RequestBody @Valid copyrightDto: ChatCopyrightRequestDto,
  ): ResponseEntity<String> = ResponseEntity.ok(
    chatGptService.copyright(
      text = copyrightDto.text,
      mode = copyrightDto.mode,
      language = copyrightDto.language,
    ),
  )

  @PostMapping("/ux-builder")
  fun updateSubscription(
    @RequestBody @Valid uxDto: UxRequestDto,
  ): ResponseEntity<String> = ResponseEntity.ok(
    chatGptService.uxBuilder(
      text = uxDto.text,
      mode = uxDto.mode,
    ),
  )

  @GetMapping("/copyright/modes")
  fun getCopyrightModes(): Array<CopyrightMode> = CopyrightMode.values()

  @GetMapping("/ux-builder/modes")
  fun getUxModes(): Array<UxMode> = UxMode.values()
}