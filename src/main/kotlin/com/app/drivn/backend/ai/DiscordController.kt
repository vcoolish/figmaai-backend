package com.app.drivn.backend.ai

import com.app.drivn.backend.blockchain.service.BlockchainService
import com.app.drivn.backend.constraint.Address
import com.app.drivn.backend.nft.service.NftService
import com.app.drivn.backend.user.dto.UserExtendedDto
import com.app.drivn.backend.user.dto.UserRegistrationEntryDto
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.EarnedTokenRecordService
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.codehaus.jettison.json.JSONObject
import org.springframework.boot.jackson.JsonObjectDeserializer
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
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
  ): String {
    if (keyword != "poop") {
      return "Invalid keyword"
    }
    nftService.updateImage(body)
    return ""
  }


  @PostMapping("/imagine")
  fun imagine(
    @Address @RequestHeader keyword: String,
    @Valid @RequestBody body: AIInput,
  ): String {
    return ""
  }
}