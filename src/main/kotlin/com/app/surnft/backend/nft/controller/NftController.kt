package com.app.surnft.backend.nft.controller

import com.app.surnft.backend.constraint.Address
import com.app.surnft.backend.nft.dto.CarLevelUpCostResponse
import com.app.surnft.backend.nft.dto.GetAllNftRequest
import com.app.surnft.backend.nft.dto.NftExternalDto
import com.app.surnft.backend.nft.dto.NftInternalDto
import com.app.surnft.backend.nft.mapper.NftMapper
import com.app.surnft.backend.nft.service.NftService
import com.app.surnft.backend.user.dto.PurchaseImageRequest
import com.app.surnft.backend.user.dto.RepairCarRequest
import org.springdoc.api.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
class NftController(
  private val nftService: NftService,
) {

  @GetMapping("/nft")
  fun getAll(
    @ParameterObject
    @PageableDefault(
      size = 15,
      page = 0,
      sort = ["id"],
      direction = Sort.Direction.DESC
    ) pageable: Pageable,
    @ParameterObject request: GetAllNftRequest
  ): Page<NftInternalDto> = nftService.getAll(pageable, request)
    .map { NftMapper.toInternalDto(it) }

  @GetMapping("/nft/{collectionId}/{id}")
  fun getNftExternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftExternalDto =
    NftMapper.toExternalDto(nftService.get(id, collectionId))

  @GetMapping("/nft/{collectionId}/{id}/internals")
  fun getNftInternalInfo(@PathVariable collectionId: Long, @PathVariable id: Long): NftInternalDto =
    NftMapper.toInternalDto(nftService.get(id, collectionId))

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
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: RepairCarRequest,
  ): NftInternalDto = NftMapper.toInternalDto(
    nftService.repair(id, collectionId, address, request.newDurability)
  )

  @GetMapping("/nft/{collectionId}/{id}/level-up")
  fun getLevelUpCost(
    @PathVariable collectionId: Long,
    @PathVariable id: Long
  ): CarLevelUpCostResponse =
    nftService.getLevelUpCost(nftService.get(id, collectionId))

  @PatchMapping("/nft/{collectionId}/{id}/level-up")
  fun levelUp(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @Address @RequestHeader address: String,
  ): NftInternalDto = NftMapper.toInternalDto(
    nftService.levelUp(id, collectionId, address)
  )

  @PatchMapping("/nft/{collectionId}/purchase")
  fun purchase(
    @PathVariable collectionId: Long,
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: PurchaseImageRequest,
  ): NftInternalDto = NftMapper.toInternalDto(
    nftService.create(address, collectionId, request.prompt.trim())
  )

  @PatchMapping("/nft/{collectionId}/purchase/{id}")
  fun mint(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @Address @RequestHeader address: String,
  ): Boolean {
    val nft = nftService.mint(address, collectionId, id)
    return nft.isMinted
  }
}
