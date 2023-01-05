package com.app.drivn.backend.nft.mapper

import com.app.drivn.backend.nft.dto.NftBaseDto
import com.app.drivn.backend.nft.dto.NftExternalDto
import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.entity.ImageCollection
import com.app.drivn.backend.nft.model.ImageNft
import com.app.drivn.backend.nft.model.Nft
import com.app.drivn.backend.nft.model.Quality

object NftMapper {

  fun generateRandomCar(id: Long, collectionId: Long): ImageNft {
    val imageNft = ImageNft()
    imageNft.collectionId = collectionId

    imageNft.name = "Car #$id"
    imageNft.description = "NFT Car, drive it in DRIVN to earn"
    imageNft.externalUrl = "https://tofunft.com/nft/bsc/0x34031C84Ee86e11D45974847C380091A84705921/$id"
    imageNft.creatorAddress = "0xF36f148D6FdEaCD6c765F8f59D4074109E311f0c"

    imageNft.level = 0
    imageNft.quality = Quality.values().random()
    imageNft.efficiency = (1..100).random().toShort()
    imageNft.luck = (1..100).random().toShort()
    imageNft.comfortability = (1..100).random().toShort()
    imageNft.maxDurability = (100..200).random().toFloat()
    imageNft.minSpeed = (1..49).random()
    imageNft.maxSpeed = (50..100).random()
    imageNft.mint = (0..3).random()

    return imageNft
  }

  fun generateCar(
    collectionId: Long,
  ): ImageNft {
    val carType = ImageCollection.values().first { it.collectionId == collectionId }
    val imageNft = ImageNft()
    imageNft.collectionId = collectionId

    imageNft.description = "NFT Car, use it in DRIVN to earn while driving"
    imageNft.creatorAddress = "0xe418eE8ec1Bca66FFa7E088e4656Cc628661043d"

    imageNft.level = 0
    imageNft.quality = carType.quality
    imageNft.efficiency = (1..carType.efficiency).random().toShort()
    imageNft.luck = (1..carType.luck).random().toShort()
    imageNft.comfortability = (1..carType.comfortability).random().toShort()
    imageNft.durability = carType.maxDurability.toFloat()
    imageNft.maxDurability = carType.maxDurability.toFloat()
    imageNft.minSpeed = carType.minSpeed
    imageNft.maxSpeed = carType.maxSpeed
    imageNft.mint = 0

    return imageNft
  }

  private fun <T : NftBaseDto> fillBaseDto(nft: Nft, dto: T): T {
    dto.name = nft.name
    dto.description = nft.description
    dto.image = nft.image
    dto.externalUrl = nft.externalUrl
    dto.collection = NftBaseDto.Collection(
      name = "Car",
      family = "DRIVN",
    )

    return dto
  }

  fun toInternalDto(imageNft: ImageNft): NftInternalDto {
    val dto = fillBaseDto(imageNft, NftInternalDto())

    dto.level = imageNft.level
    dto.quality = imageNft.quality
    dto.minSpeed = imageNft.minSpeed
    dto.maxSpeed = imageNft.maxSpeed
    dto.odometer = imageNft.odometer
    dto.efficiency = imageNft.efficiency
    dto.luck = imageNft.luck
    dto.comfortability = imageNft.comfortability
    dto.economy = imageNft.economy
    dto.durability = imageNft.durability
    dto.maxDurability = imageNft.maxDurability
    dto.mint = imageNft.mint
    dto.prompt = imageNft.prompt
    dto.isMinted = imageNft.isMinted

    return dto
  }

  fun toExternalDto(imageNft: ImageNft): NftExternalDto {
    val dto = fillBaseDto(imageNft, NftExternalDto())

    dto.attributes = listOf(
      NftExternalDto.Attribute(
        trait_type = "Seed",
        value = imageNft.prompt,
      ),
      NftExternalDto.Attribute(
        trait_type = "luck",
        value = imageNft.luck.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "durability",
        value = imageNft.durability.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "Quality",
        value = imageNft.quality.name,
      ),
      NftExternalDto.Attribute(
        trait_type = "Level",
        value = imageNft.level.toString(),
      ),
    )

    return dto
  }
}