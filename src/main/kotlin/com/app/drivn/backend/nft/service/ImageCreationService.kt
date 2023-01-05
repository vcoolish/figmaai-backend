package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.probability.CarCollectionProbabilityProperties
import com.app.drivn.backend.exception.BadRequestException
import com.app.drivn.backend.nft.entity.ImageCollection
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.ImageNft
import com.app.drivn.backend.user.model.User
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
    val car = NftMapper.generateCar(collectionId)

    val collection = ImageCollection.SUR // TODO: select? generate?
    val properties = carCollectionProbabilityProperties.car[collection]
      ?: throw BadRequestException("Unsupported CarCollection $collection")

    car.quality = properties.quality.getNextRandom()
    car.efficiency = properties.efficiency.getNextValue().toShort()
    car.user = user

    return car
  }

}
