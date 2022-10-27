package com.app.drivn.backend.nft.controller

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.nft.dto.CarLevelUpCostResponse
import com.app.drivn.backend.nft.dto.NftExternalDto
import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.service.NftService
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

@Validated
@RestController
class NftController(
  private val nftService: NftService,
  private val appProperties: AppProperties,
) {
  @GetMapping("/nft")
  fun getAll(
    @ParameterObject
    @PageableDefault(
      size = 15,
      page = 0,
      sort = ["id"]
    ) pageable: Pageable
  ): Page<NftInternalDto> = nftService.getAll(pageable)
    .map { NftMapper.toInternalDto(it, appProperties.arweaveUrl) }

  @GetMapping("/nft/{collectionId}/{id}")
  fun getNftExternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftExternalDto =
    NftMapper.toExternalDto(nftService.getOrCreate(id, collectionId), appProperties.arweaveUrl)

  @GetMapping("/nft/{collectionId}/{id}/internals")
  fun getNftInternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftInternalDto =
    NftMapper.toInternalDto(nftService.getOrCreate(id, collectionId), appProperties.arweaveUrl)

  @GetMapping("/nft/{collectionId}/{id}/repair")
  fun getRepairCost(
    @PathVariable collectionId: Long,
    @PathVariable id: Long
  ): Double =
    nftService.getRepairCost(nftService.get(id, collectionId))

  @PatchMapping("/nft/{collectionId}/{id}/repair")
  fun repairCar(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @NotBlank @RequestParam initiatorAddress: String,
    @Positive @RequestParam newDurability: Float
  ): NftInternalDto = NftMapper.toInternalDto(
    nftService.repair(id, collectionId, initiatorAddress, newDurability),
    appProperties.arweaveUrl
  )

  @GetMapping("/nft/{collectionId}/{id}/level-up")
  fun getLevelUpCarCost(
    @PathVariable collectionId: Long,
    @PathVariable id: Long
  ): CarLevelUpCostResponse =
    nftService.getLevelUpCost(nftService.get(id, collectionId))

  @PatchMapping("/nft/{collectionId}/{id}/level-up")
  fun levelUpCar(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @NotBlank @RequestParam initiatorAddress: String
  ): NftInternalDto = NftMapper.toInternalDto(
    nftService.levelUp(id, collectionId, initiatorAddress),
    appProperties.arweaveUrl
  )

  @PatchMapping("/nft/{collectionId}/purchase")
  fun purchaseCar(
    @PathVariable collectionId: String,
    @NotBlank @RequestHeader address: String
  ): NftInternalDto {
    return NftMapper.toInternalDto(nftService.create(address, collectionId.toLong()))
  }
}
