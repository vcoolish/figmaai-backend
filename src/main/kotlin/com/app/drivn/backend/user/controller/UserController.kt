package com.app.drivn.backend.user.controller

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService,
  private val userEnergyService: UserEnergyService
) {

  @GetMapping("/address/{address}")
  fun getUser(@PathVariable address: String): UserInfoDto {
//    val nfts = bounceClient.getNfts(address)
    val user = userService.getOrCreate(address)
    return UserMapper.toDto(user, nftInfoDtos)
  }

  @PostMapping("/address/{address}/energy")
  fun renewMyEnergy(@PathVariable address: String): ResponseEntity<UserInfoDto> =
    userEnergyService.tryToRenew(address)
      .map { user -> UserMapper.toDto(user, nftInfoDtos) }
      .let { dto -> ResponseEntity.of(dto) }

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