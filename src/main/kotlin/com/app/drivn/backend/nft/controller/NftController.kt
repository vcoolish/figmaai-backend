package com.app.drivn.backend.nft.controller

import com.app.drivn.backend.nft.dto.NftExternalDto
import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.service.NftService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class NftController(
  private val nftService: NftService
) {

  @GetMapping("/nft/{collectionId}/{id}")
  fun getNftExternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftExternalDto {
    return NftMapper.toExternalDto(nftService.getOrCreate(id, collectionId))
  }

  @GetMapping("/nft/{collectionId}/{id}/internals")
  fun getNftInternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftInternalDto {
    return NftMapper.toInternalDto(nftService.getOrCreate(id, collectionId))
  }
}
