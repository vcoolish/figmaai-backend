package com.app.surnft.backend.nft.mapper

import com.app.surnft.backend.nft.dto.NftBaseDto
import com.app.surnft.backend.nft.dto.NftExternalDto
import com.app.surnft.backend.nft.dto.NftInternalDto
import com.app.surnft.backend.nft.entity.ImageCollection
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.Nft
import com.app.surnft.backend.nft.model.Quality

object NftMapper {

  @Deprecated("It's too random. Use NftMapper.generateCar.")
  fun generateRandomCar(id: Long, collectionId: Long): ImageNft {
    val imageNft = ImageNft()
    imageNft.collectionId = collectionId

    imageNft.name = "Car #$id"
    imageNft.description = "NFT Car, drive it in surnft to earn"
    imageNft.externalUrl = "https://tofunft.com/nft/bsc/0xe73711e8331aD93ca115A2AE4D1AFAc74E15D644/$id"
    imageNft.creatorAddress = "0x691E1C66A852C5830d7Ba9a9e8C080F826D5ED01"

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

    imageNft.description = "AI powered NFT picture created from description prompt"
    imageNft.creatorAddress = "0x691E1C66A852C5830d7Ba9a9e8C080F826D5ED01"

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
      name = "AI NFT",
      family = "Public",
    )

    return dto
  }

  fun toInternalDto(imageNft: ImageNft): NftInternalDto {
    val dto = fillBaseDto(imageNft, NftInternalDto())

    dto.level = imageNft.level
    dto.quality = imageNft.quality
    dto.efficiency = imageNft.efficiency
    dto.luck = imageNft.luck
    dto.comfortability = imageNft.comfortability
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
        trait_type = "Luck",
        value = imageNft.luck.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "Durability",
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