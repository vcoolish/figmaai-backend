package com.app.drivn.backend.nft.mapper

import com.app.drivn.backend.nft.dto.NftBaseDto
import com.app.drivn.backend.nft.dto.NftExternalDto
import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.entity.CarCollection
import com.app.drivn.backend.nft.model.CarBody
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.Nft
import com.app.drivn.backend.nft.model.Quality

object NftMapper {

  fun generateRandomCar(id: Long, collectionId: Long): CarNft {
    val carNft = CarNft()
    carNft.id = id
    carNft.collectionId = collectionId

    carNft.name = "Car #$id"
    carNft.description = "NFT Car, drive it in DRIVN to earn"
    carNft.externalUrl = "https://tofunft.com/nft/bsc/0x34031C84Ee86e11D45974847C380091A84705921/$id"
    carNft.creatorAddress = "0xF36f148D6FdEaCD6c765F8f59D4074109E311f0c"

    carNft.level = 0
    carNft.body = CarBody.values().random()
    carNft.quality = Quality.values().random()
    carNft.efficiency = (1..100).random().toShort()
    carNft.luck = (1..100).random().toShort()
    carNft.comfortability = (1..100).random().toShort()
    carNft.maxDurability = (100..200).random().toFloat()
    carNft.minSpeed = (1..49).random()
    carNft.maxSpeed = (50..100).random()
    carNft.mint = (0..3).random()

    return carNft
  }

  fun generateCar(
    id: Long,
    collectionId: Long,
  ): CarNft {
    val carType = CarCollection.values().first { it.collectionId == collectionId }
    val carNft = CarNft()
    carNft.id = id
    carNft.collectionId = collectionId

    carNft.name = "${carType.title} #$id"
    carNft.description = "NFT Car, use it in DRIVN to earn while driving"
    carNft.image = "https://arweave.net/Zh1XgakvwR6xPoXefYZbHhIJVmaLfIyjuJXca3kRpE4"
    carNft.externalUrl = "https://tofunft.com/nft/bsc/0x34031C84Ee86e11D45974847C380091A84705921/$id"
    carNft.creatorAddress = "0xa81A54123dcafb6C3056a8f513DE28ef699790D8"

    carNft.level = 0
    carNft.body = carType.body
    carNft.quality = carType.quality
    carNft.efficiency = (1..carType.efficiency).random().toShort()
    carNft.luck = (1..carType.luck).random().toShort()
    carNft.comfortability = (1..carType.comfortability).random().toShort()
    carNft.maxDurability = carType.maxDurability.toFloat()
    carNft.minSpeed = carType.minSpeed
    carNft.maxSpeed = carType.maxSpeed
    carNft.mint = 0

    return carNft
  }

  private fun <T : NftBaseDto> fillBaseDto(nft: Nft, dto: T, arweaveUrl: String): T {
    dto.name = nft.name
    dto.description = nft.description
    dto.image = arweaveUrl + nft.image.dataTxId
    dto.externalUrl = nft.externalUrl
    dto.collection = NftBaseDto.Collection(
      name = "Car",
      family = "DRIVN",
    )

    return dto
  }

  fun toInternalDto(carNft: CarNft, arweaveUrl: String): NftInternalDto {
    val dto = fillBaseDto(carNft, NftInternalDto(), arweaveUrl)

    dto.level = carNft.level
    dto.quality = carNft.quality
    dto.body = carNft.body
    dto.minSpeed = carNft.minSpeed
    dto.maxSpeed = carNft.maxSpeed
    dto.odometer = carNft.odometer
    dto.efficiency = carNft.efficiency
    dto.luck = carNft.luck
    dto.comfortability = carNft.comfortability
    dto.economy = carNft.economy
    dto.durability = carNft.durability
    dto.maxDurability = carNft.maxDurability
    dto.mint = carNft.mint

    return dto
  }

  fun toExternalDto(carNft: CarNft, arweaveUrl: String): NftExternalDto {
    val dto = fillBaseDto(carNft, NftExternalDto(), arweaveUrl)

    dto.attributes = listOf(
      NftExternalDto.Attribute(
        trait_type = "efficiency",
        value = carNft.efficiency.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "luck",
        value = carNft.luck.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "comfort",
        value = carNft.comfortability.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "durability",
        value = carNft.durability.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "Optimal Speed",
        value = "${carNft.minSpeed}-${carNft.maxSpeed} km/h",
      ),
      NftExternalDto.Attribute(
        trait_type = "Car-minting Count",
        value = carNft.mint.toString(),
      ),
      NftExternalDto.Attribute(
        trait_type = "Car type",
        value = carNft.body.name,
      ),
      NftExternalDto.Attribute(
        trait_type = "Car quality",
        value = carNft.quality.name,
      ),
      NftExternalDto.Attribute(
        trait_type = "Level",
        value = carNft.level.toString(),
      ),
    )

    return dto
  }
}