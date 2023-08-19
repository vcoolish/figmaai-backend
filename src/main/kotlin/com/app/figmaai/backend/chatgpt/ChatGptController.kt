package com.app.figmaai.backend.chatgpt

import com.app.figmaai.backend.exception.InsufficientBalanceException
import org.springdoc.api.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.social.ExpiredAuthorizationException
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
  ): ResponseEntity<List<String>?> = try {
    ResponseEntity.ok(
      chatGptService.copyright(
        text = copyrightDto.text,
        mode = copyrightDto.mode,
        language = copyrightDto.language?.name,
        tone = copyrightDto.tone,
        token = token,
      ),
    )
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }

  @PostMapping("/ux-builder")
  fun uxBuilder(
    @RequestBody @Valid uxDto: UxRequestDto,
    @RequestHeader token: String,
  ): ResponseEntity<List<String>?> = try {
    ResponseEntity.ok(
      chatGptService.uxBuilder(
        text = uxDto.text,
        mode = uxDto.mode,
        token = token,
      ),
    )
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }

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

  @GetMapping("/ux-builder/example")
  fun getChatExample(
    @ParameterObject uxDto: UxExampleRequestDto,
  ): UxExampleResponseDto {
    val id = (uxDto.index % 3) - 1
    return when (uxDto.mode) {
      UxMode.ujm -> UxExampleResponseDto(ujmInputExamples[id], ujmExamples[id])
      UxMode.userpersona -> UxExampleResponseDto(personaInputExamples[id], personaExamples[id])
      UxMode.mindmap -> UxExampleResponseDto(mindMapInputExamples[id], mindMapExamples[id])
      UxMode.userflow -> TODO()
      UxMode.sitemap -> TODO()
    }
  }
}