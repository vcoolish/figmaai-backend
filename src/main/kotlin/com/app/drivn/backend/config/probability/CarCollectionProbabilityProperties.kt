package com.app.drivn.backend.config.probability

import com.app.drivn.backend.nft.entity.CarCollection
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("probability")
class CarCollectionProbabilityProperties {

  lateinit var car: Map<CarCollection, CarProbabilityProperties>

  @ConstructorBinding
  data class CarProbabilityProperties(
    val quality: CarQualityProbabilityProperties,
    val body: CarBodyProbabilityProperties,
    val efficiency: CarEfficiencyProbabilityProperties,
  )
}
