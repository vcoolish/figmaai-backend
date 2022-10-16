package com.app.drivn.backend.config.probability

import com.app.drivn.backend.nft.model.Quality
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("probability.car.quality")
class CarQualityProbabilityProperties : ProbabilityProperties<Quality>() {

  @NotEmpty
  override lateinit var proportions: Map<Quality, Double>

}
