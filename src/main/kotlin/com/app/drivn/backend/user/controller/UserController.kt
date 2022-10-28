package com.app.drivn.backend.user.controller

import com.app.drivn.backend.blockchain.model.BalanceType
import com.app.drivn.backend.blockchain.service.BlockchainService
import com.app.drivn.backend.constraint.Address
import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.*
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

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
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: UserRegistrationEntryDto
  ): UserExtendedDto {
    val user = userService.updateSignMessage(address, request)
    return UserMapper.toExtendedDto(user)
  }

  @GetMapping("")
  fun getUser(
    @Address @RequestHeader address: String
  ): UserExtendedDto {
    val user = userService.get(address)
    return UserMapper.toExtendedDto(user)
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
      BalanceType.COIN.name -> blockchainService.withdrawCoin(address)
      BalanceType.TOKEN.name -> blockchainService.withdrawToken(address)
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