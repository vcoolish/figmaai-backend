package com.app.drivn.backend.config.probability

import com.app.drivn.backend.nft.model.CarBody
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("probability.car.body")
class CarBodyProbabilityProperties : ProbabilityProperties<CarBody>() {

  @NotEmpty
  override lateinit var proportions: Map<CarBody, Double>

}
