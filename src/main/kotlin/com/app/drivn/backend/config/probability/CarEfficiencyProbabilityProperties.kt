package com.app.drivn.backend.config.probability

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.util.concurrent.ThreadLocalRandom
import javax.validation.constraints.NotEmpty

@Validated
@ConfigurationProperties("probability.car.efficiency")
class CarEfficiencyProbabilityProperties : RangeProbabilityProperties<Int>() {

  @NotEmpty
  override lateinit var proportions: Map<ComparableRange<Int>, Double>

  override fun getNextValue(): Int {
    val range = getNextRandom()
    return ThreadLocalRandom.current().nextInt(range.start, range.endInclusive)
  }
}
