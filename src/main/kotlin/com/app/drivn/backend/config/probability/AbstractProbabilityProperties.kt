package com.app.drivn.backend.config.probability

import java.util.*

abstract class AbstractProbabilityProperties<T : Comparable<T>> : ProbabilityProperties<T> {

  private val _probabilities: SortedMap<ClosedRange<Double>, Set<T>> by lazy {
    ProbabilityProperties.getProbabilities(proportions)
  }

  override fun getProbabilities(): SortedMap<ClosedRange<Double>, Set<T>> = _probabilities
}
