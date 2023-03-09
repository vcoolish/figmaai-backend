package com.app.surnft.backend.nft.controller

import com.amazonaws.HttpMethod
import com.app.surnft.backend.ai.AiProvider
import com.app.surnft.backend.constraint.Address
import com.app.surnft.backend.nft.dto.*
import com.app.surnft.backend.nft.mapper.NftMapper
import com.app.surnft.backend.nft.model.UploadResult
import com.app.surnft.backend.nft.service.AwsS3Service
import com.app.surnft.backend.nft.service.NftService
import com.app.surnft.backend.user.dto.PurchaseCollectionRequest
import com.app.surnft.backend.user.dto.PurchaseImageRequest
import com.app.surnft.backend.user.dto.RepairCarRequest
import com.app.surnft.backend.user.dto.RetryCollectionRequest
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.api.annotations.ParameterObject
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.io.InputStream
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Positive

@Validated
@RestController
class NftController(
  private val nftService: NftService,
  private val awsS3Service: AwsS3Service,
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

  @GetMapping("/nft/{collectionId}/internals")
  fun getCollectionInfo(@PathVariable collectionId: Long): CollectionDto {
    val collection = nftService.getCollection(collectionId)
    val inProgressCount = nftService.collectionInProgressCount(collectionId)
    return CollectionDto(
      id = collectionId,
      address = collection.address,
      count = collection.count,
      name = collection.name,
      symbol = collection.symbol,
      inProgressCount = inProgressCount,
    )
  }

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
  ): NftInternalDto {
    val provider = AiProvider.values().find {
      it.name.equals(request.provider, true)
    } ?: AiProvider.MIDJOURNEY
    return NftMapper.toInternalDto(
      nftService.create(
        address = address,
        collectionId = collectionId,
        prompt = request.prompt.trim(),
        provider = provider,
      )
    )
  }

  @PatchMapping("/nft/{collectionId}/purchase/{id}")
  fun mint(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @Address @RequestHeader address: String,
  ): Boolean {
    val nft = nftService.mint(address, collectionId, id)
    return nft.isMinted
  }

  @GetMapping("/nft/freeMint")
  fun hasFreeMint(
    @Address @RequestHeader address: String,
  ): Boolean {
    return nftService.hasFreeMint(address)
  }

  @GetMapping("/nft/price/{collectionId}")
  fun getMintPrice(
    @PathVariable collectionId: Long,
  ): Double {
    return nftService.getMintPrice(collectionId)
  }

  @GetMapping("/nft/prices")
  fun getMintPrice(): List<@Positive BigDecimal> = nftService.getCollectionPrices()

  @DeleteMapping("/nft/{collectionId}/{id}")
  fun delete(
    @PathVariable collectionId: Long,
    @PathVariable id: Long,
    @Address @RequestHeader address: String,
  ): Boolean {
    return nftService.delete(address, collectionId, id)
  }

  @PostMapping("/upload/{filename}")
  fun upload(
    @Address @RequestHeader address: String,
    @PathVariable filename: String,
  ): UploadResult {
    val s3File = UUID.randomUUID().toString() + filename
    val url = awsS3Service.generatePreSignedUrl(
      filePath = s3File,
      bucketName = "surpics",
      httpMethod = HttpMethod.PUT,
    )
    return UploadResult(url = url, filename = s3File)
  }

  @Operation(summary = "Download asset")
  @GetMapping(
    "/download/{assetId}",
    produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
  )
  fun download(@PathVariable assetId: Long): ResponseEntity<InputStreamResource> {
    val stream: InputStream = InputStream.nullInputStream()
    return ResponseEntity.status(HttpStatus.OK)
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .header(
        HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
            URLEncoder.encode("empty_file", StandardCharsets.UTF_8)
      )
      .body(InputStreamResource(stream))
  }

  @PatchMapping("/nft/purchase")
  fun purchaseCollection(
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: PurchaseCollectionRequest,
  ): Boolean {
    val provider = AiProvider.values().find {
      it.name.equals(request.provider, true)
    } ?: AiProvider.MIDJOURNEY
    nftService.deployCollection(
      address = address,
      prompt = request.prompt.trim(),
      provider = provider,
      option = request.option,
      name = request.name,
      symbol = request.symbol,
      styles = request.styles,
    )
    return true
  }

  @PatchMapping("/nft/retry")
  fun retryCollection(
    @Address @RequestHeader address: String,
    @Valid @RequestBody request: RetryCollectionRequest,
  ): Boolean {
    nftService.createCollection(
      collectionId = request.collectionId,
      prompt = request.prompt,
      count = request.count,
      name = request.name,
      userAddress = request.address,
      styles = request.styles,
    )
    return true
  }

}
