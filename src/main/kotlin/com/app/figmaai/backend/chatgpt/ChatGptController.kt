package com.app.figmaai.backend.chatgpt

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
class ChatGptController(
  private val chatGptService: ChatGptService,
) {

  @PostMapping("/copyright")
  fun copyright(
    @RequestBody @Valid copyrightDto: ChatCopyrightRequestDto,
  ): ResponseEntity<String> = ResponseEntity.ok(
    chatGptService.copyright(
      text = copyrightDto.text,
      mode = copyrightDto.mode,
      language = copyrightDto.language?.name,
    ),
  )

  @PostMapping("/ux-builder")
  fun uxBuilder(
    @RequestBody @Valid uxDto: UxRequestDto,
    @RequestHeader token: String,
  ): ResponseEntity<String> = ResponseEntity.ok(
    chatGptService.uxBuilder(
      text = uxDto.text,
      mode = uxDto.mode,
      token = token,
    ),
  )

  @GetMapping("/copyright/modes")
  fun getCopyrightModes(): Array<CopyrightMode> = CopyrightMode.values()

  @GetMapping("/copyright/languages")
  fun getCopyrightLanguages(): Array<ChatGptLanguage> = ChatGptLanguage.values()

  @GetMapping("/ux-builder/modes")
  fun getUxModes(): Array<UxMode> = UxMode.values()
}