package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.probability.CarBodyProbabilityProperties
import com.app.drivn.backend.config.probability.CarEfficiencyProbabilityProperties
import com.app.drivn.backend.config.probability.CarQualityProbabilityProperties
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.user.model.User
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service

@Service
@EnableConfigurationProperties(
  CarQualityProbabilityProperties::class,
  CarBodyProbabilityProperties::class,
  CarEfficiencyProbabilityProperties::class,
)
class CarCreationService(
  private val carQualityProbabilityProperties: CarQualityProbabilityProperties,
  private val carBodyProbabilityProperties: CarBodyProbabilityProperties,
  private val carEfficiencyProbabilityProperties: CarEfficiencyProbabilityProperties
) {

  fun create(user: User, collectionId: Long): CarNft {
    val car = NftMapper.generateCar(collectionId)

    car.quality = carQualityProbabilityProperties.getNextRandom()
    car.body = carBodyProbabilityProperties.getNextRandom()
    car.efficiency = carEfficiencyProbabilityProperties.getNextValue().toShort()
    car.user = user

    return car
  }

}
