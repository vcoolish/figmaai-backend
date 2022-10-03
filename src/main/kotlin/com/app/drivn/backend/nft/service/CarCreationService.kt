package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.properties.CarQualityProbabilityProperties
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import org.springframework.stereotype.Service

@Service
class CarCreationService(
  private val carQualityProbabilityProperties: CarQualityProbabilityProperties
) {

  fun create(id: Long, collectionId: Long): CarNft {
    val car = NftMapper.generateRandomCar(id, collectionId)

    car.quality = carQualityProbabilityProperties.getNextRandom()

    return car
  }

}
