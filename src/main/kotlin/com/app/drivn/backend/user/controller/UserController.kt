package com.app.drivn.backend.user.controller

import com.app.drivn.backend.nft.dto.NftInfoDto
import com.app.drivn.backend.user.dto.UserInfoDto
import com.app.drivn.backend.user.mapper.UserMapper
import com.app.drivn.backend.user.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService
) {

  @GetMapping("/address/{address}")
  fun index(@PathVariable address: String): UserInfoDto {
//        val nfts = bounceClient.getNfts(address)
    val user = userService.getOrCreate(address)
    return UserMapper.toDto(
      user, listOf(
        NftInfoDto(
          id = "123",
          collectionId = "12345",
        ),
        NftInfoDto(
          id = "124",
          collectionId = "12345",
        ),
      )
    )
  }
}