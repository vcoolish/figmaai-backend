package com.app.surnft.backend.config.probability

abstract class RangeProbabilityProperties<T : Comparable<T>> :
  ProbabilityProperties<ComparableRange<T>>() {

  abstract fun getNextValue(): T
}
