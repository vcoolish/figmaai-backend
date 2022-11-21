package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.probability.CarCollectionProbabilityProperties
import com.app.drivn.backend.exception.BadRequestException
import com.app.drivn.backend.nft.entity.CarCollection
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.user.model.User
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service

@Service
@EnableConfigurationProperties(
  CarCollectionProbabilityProperties::class,
)
class CarCreationService(
  private val carCollectionProbabilityProperties: CarCollectionProbabilityProperties,
) {

  fun create(user: User, collectionId: Long): CarNft {
    val car = NftMapper.generateCar(collectionId)

    val collection = CarCollection.PORSCHE // TODO: select? generate?
    val properties = carCollectionProbabilityProperties.car[collection]
      ?: throw BadRequestException("Unsupported CarCollection $collection")

    car.quality = properties.quality.getNextRandom()
    car.body = properties.body.getNextRandom()
    car.efficiency = properties.efficiency.getNextValue().toShort()
    car.user = user

    return car
  }

}
