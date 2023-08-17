package com.app.figmaai.backend.chatgpt.wallet

import com.app.figmaai.backend.exception.InsufficientBalanceException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@Validated
@RestController
class ChatGptWalletController(
  private val chatGptService: ChatGptWalletService,
) {

  @PostMapping(
    "/play",
    MediaType.MULTIPART_FORM_DATA_VALUE
  )
  fun uxBuilder(
    @RequestParam mode: PlayMode,
    @RequestPart audio: MultipartFile,
  ): ResponseEntity<List<String>?> = try {
    ResponseEntity.ok(
      chatGptService.play(
        audio = audio.bytes,
        mode = mode,
      ),
    )
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }
}