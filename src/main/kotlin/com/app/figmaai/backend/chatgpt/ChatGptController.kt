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
    @RequestHeader token: String,
  ): ResponseEntity<List<String>> = ResponseEntity.ok(
    chatGptService.copyright(
      text = copyrightDto.text,
      mode = copyrightDto.mode,
      language = copyrightDto.language?.name,
      tone = copyrightDto.tone,
      token = token,
    ),
  )

  @PostMapping("/ux-builder")
  fun uxBuilder(
    @RequestBody @Valid uxDto: UxRequestDto,
    @RequestHeader token: String,
  ): ResponseEntity<List<String>> = ResponseEntity.ok(
    chatGptService.uxBuilder(
      text = uxDto.text,
      mode = uxDto.mode,
      token = token,
    ),
  )

  @GetMapping("/copyright/modes")
  fun getCopyrightModes(): List<ModeResponse> = CopyrightMode.values().map {
    ModeResponse(it.name, it.title)
  }

  @GetMapping("/copyright/languages")
  fun getCopyrightLanguages(): List<ModeResponse> = ChatGptLanguage.values().map {
    ModeResponse(it.name, it.code)
  }

  @GetMapping("/copyright/tones")
  fun getCopyrightTones(): List<ModeResponse> = ChatGptTone.values().map {
    ModeResponse(it.name, it.title)
  }

  @GetMapping("/ux-builder/modes")
  fun getUxModes(): List<ModeResponse> = UxMode.values().map {
    ModeResponse(it.name, it.title, it.inputs)
  }
}