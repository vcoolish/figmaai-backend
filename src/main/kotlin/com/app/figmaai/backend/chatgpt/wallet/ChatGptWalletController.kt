package com.app.figmaai.backend.chatgpt.wallet

import com.app.figmaai.backend.chatgpt.PlayResponse
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

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
  ): PlayResponse = chatGptService.play(
    audio = audio.bytes,
    mode = mode,
  )
}