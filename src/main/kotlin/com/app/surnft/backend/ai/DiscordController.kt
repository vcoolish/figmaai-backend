package com.app.surnft.backend.ai

import com.app.surnft.backend.constraint.Address
import com.app.surnft.backend.nft.mapper.NftMapper
import com.app.surnft.backend.nft.service.NftService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/ai")
class DiscordController(
  private val nftService: NftService,
) {

  @PostMapping("/message")
  fun onMessage(
    @RequestHeader keyword: String,
    @Valid @RequestBody body: AIOutput,
  ): Any {
    if (keyword != "poop") {
      return "Invalid keyword"
    }
    val nft = nftService.updateImage(body)

    return NftMapper.toExternalDto(nft)
  }

  @PostMapping("/imagine")
  fun imagine(
    @Address @RequestHeader keyword: String,
    @Valid @RequestBody body: com.app.surnft.backend.ai.AIInput,
  ): String {
    return ""
  }
}