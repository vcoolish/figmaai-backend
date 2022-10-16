package com.app.drivn.backend.config.probability

abstract class RangeProbabilityProperties<T : Comparable<T>> :
  ProbabilityProperties<ComparableRange<T>>() {

  abstract fun getNextValue(): T
}
