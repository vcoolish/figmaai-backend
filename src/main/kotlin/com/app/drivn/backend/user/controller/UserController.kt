package com.app.drivn.backend.user.controller

import com.app.drivn.backend.blockchain.model.BalanceType
import com.app.drivn.backend.blockchain.service.BlockchainService
import com.app.drivn.backend.constraint.Address
import com.app.drivn.backend.user.dto.UpdateUserDonationRequest
import com.app.drivn.backend.user.dto.UserExtendedDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.dto.UserRegistrationEntryDto
import com.app.drivn.backend.user.dto.WithdrawUserBalanceRequest
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.EarnedTokenRecordService
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/address")
class UserController(
  private val userService: UserService,
  private val userEnergyService: UserEnergyService,
  private val blockchainService: BlockchainService,
  private val earnedTokenRecordService: EarnedTokenRecordService,
) {

  @PostMapping("/register")
  fun registerUser(
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: UserRegistrationEntryDto
  ): UserExtendedDto {
    val user = userService.updateSignMessage(address, request)
    return UserMapper.toExtendedDto(user, BigDecimal.ZERO)
  }

  @GetMapping
  fun getUser(
    @Address @RequestHeader address: String
  ): UserExtendedDto = UserMapper.toExtendedDto(
    userService.get(address),
    earnedTokenRecordService.getEarnedTokensForDay(address)
  )

  @PostMapping("/energy")
  fun renewMyEnergy(@Address @RequestHeader address: String): ResponseEntity<UserInfoDto> =
    userEnergyService.tryToRenew(address)
      .map(UserMapper::toDto)
      .let { ResponseEntity.of(it) }

  @PatchMapping
  fun updateUser(
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: UpdateUserDonationRequest
  ): UserInfoDto = UserMapper.toDto(userService.updateDonation(address, request))

  @PostMapping("/withdraw")
  fun withdraw(
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: WithdrawUserBalanceRequest
  ): UserInfoDto {
    val user = when (request.type) {
      BalanceType.COIN.name -> blockchainService.withdrawCoin(address)
      BalanceType.TOKEN.name -> blockchainService.withdrawToken(address)
      else -> error("Unknown type")
    }
    return UserMapper.toDto(user)
  }
}