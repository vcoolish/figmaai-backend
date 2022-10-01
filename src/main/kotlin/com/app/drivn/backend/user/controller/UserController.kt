package com.app.drivn.backend.user.controller

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.UpdateUserRequest
import com.app.drivn.backend.user.dto.UserExtendedDto
import com.app.drivn.backend.user.dto.UserInfoDto
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
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController("/address")
class UserController(
  private val userService: UserService,
  private val userEnergyService: UserEnergyService
) {

  @GetMapping("/{address}")
  fun getUser(@PathVariable address: String): UserExtendedDto {
//    val nfts = bounceClient.getNfts(address)
    val user = userService.getOrCreate(address)
    return UserMapper.toDto(user, nftInfoDtos)
  }

  @PostMapping("/{address}/energy")
  fun renewMyEnergy(@PathVariable address: String): ResponseEntity<UserInfoDto> =
    userEnergyService.tryToRenew(address)
      .map(UserMapper::toDto)
      .let { ResponseEntity.of(it) }

  @PatchMapping("/{address}")
  fun updateUser(
    @PathVariable address: String,
    @Valid @RequestBody request: UpdateUserRequest
  ): UserInfoDto = UserMapper.toDto(userService.update(address, request))

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