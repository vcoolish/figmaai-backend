package com.app.drivn.backend.user.controller

import com.app.drivn.backend.common.blockchain.BlockchainService
import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.*
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.Pattern

@Validated
@RestController
@RequestMapping("/address")
class UserController(
  private val userService: UserService,
  private val userEnergyService: UserEnergyService,
  private val blockchainService: BlockchainService,
) {

  @PostMapping("/register")
  fun registerUser(
    @Pattern(regexp = "^0x[\\da-fA-F]{40}$") @RequestHeader address: String,
    @Valid @RequestBody request: UserRegistrationEntryDto
  ): UserExtendedDto {
    val user = userService.updateSignMessage(address, request)
    return UserMapper.toDto(user, nftInfoDtos)
  }

  @GetMapping("")
  fun getUser(
    @Pattern(regexp = "^0x[\\da-fA-F]{40}$") @RequestHeader address: String
  ): UserExtendedDto {
//    val nfts = bounceClient.getNfts(address)
    val user = userService.getOrCreate(address)
    return UserMapper.toDto(user, nftInfoDtos)
  }

  @PostMapping("/energy")
  fun renewMyEnergy(@RequestHeader address: String): ResponseEntity<UserInfoDto> =
    userEnergyService.tryToRenew(address)
      .map(UserMapper::toDto)
      .let { ResponseEntity.of(it) }

  @PatchMapping("")
  fun updateUser(
    @RequestHeader address: String,
    @Valid @RequestBody request: UpdateUserDonationRequest
  ): UserInfoDto = UserMapper.toDto(userService.updateDonation(address, request))

  @PostMapping("/withdraw")
  fun withdraw(
    @RequestHeader address: String,
    @Valid @RequestBody request: WithdrawUserBalanceRequest
  ): UserInfoDto {
    val user = when (request.type) {
      BalanceType.coin.name -> blockchainService.withdrawCoin(address)
      BalanceType.token.name -> blockchainService.withdrawToken(address)
      else -> error("Unknown type")
    }
    return UserMapper.toDto(user)
  }

  companion object {

    @JvmStatic
    private val nftInfoDtos: List<NftInfoDto> = listOf(
      NftInfoDto(
        id = "123",
        collectionId = "12345",
      ),
      NftInfoDto(
        id = "124",
        collectionId = "12345",
      ),
    )
  }
}