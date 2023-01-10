package com.app.surnft.backend.config.probability

import com.app.surnft.backend.nft.model.Quality
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
//@ConfigurationProperties("probability.car.quality")
class CarQualityProbabilityProperties : ProbabilityProperties<Quality>() {

  @NotEmpty
  override lateinit var proportions: Map<Quality, Double>

}