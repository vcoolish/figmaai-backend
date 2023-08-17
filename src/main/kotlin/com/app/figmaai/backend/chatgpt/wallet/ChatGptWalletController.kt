package com.app.figmaai.backend.chatgpt.wallet

import com.app.figmaai.backend.exception.InsufficientBalanceException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.social.ExpiredAuthorizationException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
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
    @RequestBody @Valid uxDto: WalletRequestDto,
  ): ResponseEntity<List<String>?> = try {
    ResponseEntity.ok(
      chatGptService.play(
        audio = uxDto.audio.bytes,
        mode = uxDto.mode,
      ),
    )
  } catch (ex: ExpiredAuthorizationException) {
    ResponseEntity.status(405).body(null)
  } catch (ex: InsufficientBalanceException) {
    ResponseEntity.status(415).body(null)
  }
}