package com.app.figmaai.backend.user.dto

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.validation.constraints.NotBlank

class SocialUserRegistrationSuccessDto(state: String, code: String) {
  companion object {
    private val UTF_8 = StandardCharsets.UTF_8.toString()
    private const val facebookEndSymbol = "#_=_"
  }

  @field: NotBlank
  val code: String = URLDecoder.decode(code.substringBefore(facebookEndSymbol), UTF_8)

  @field: NotBlank
  val state: String = URLDecoder.decode(state.substringBefore(facebookEndSymbol), UTF_8)

  val writeToken: String? = null
}