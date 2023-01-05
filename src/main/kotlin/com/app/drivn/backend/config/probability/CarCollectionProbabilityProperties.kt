package com.app.drivn.backend.config.probability

import com.app.drivn.backend.nft.entity.ImageCollection
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("probability")
class CarCollectionProbabilityProperties {

  lateinit var car: Map<ImageCollection, CarProbabilityProperties>

  @ConstructorBinding
  data class CarProbabilityProperties(
    val quality: CarQualityProbabilityProperties,
    val efficiency: CarEfficiencyProbabilityProperties,
  )
}
