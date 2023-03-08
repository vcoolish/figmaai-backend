package com.app.surnft.backend.nft.service

import com.app.surnft.backend.config.probability.CarCollectionProbabilityProperties
import com.app.surnft.backend.exception.BadRequestException
import com.app.surnft.backend.nft.entity.ImageCollection
import com.app.surnft.backend.nft.mapper.NftMapper
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.user.model.User
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service

@Service
@EnableConfigurationProperties(
  CarCollectionProbabilityProperties::class,
)
class ImageCreationService(
  private val carCollectionProbabilityProperties: CarCollectionProbabilityProperties,
) {

  fun create(user: User, collectionId: Long): ImageNft {
    val creator = if (collectionId == 0L) null else user.address
    val car = NftMapper.generateCar(collectionId, creator)

    val collection = ImageCollection.SUR // TODO: select? generate?
    val properties = carCollectionProbabilityProperties.car[collection]
      ?: throw BadRequestException("Unsupported CarCollection $collection")

    car.quality = properties.quality.getNextRandom()
    car.efficiency = properties.efficiency.getNextValue().toShort()
    car.user = user
    car.image = ""

    return car
  }

}
