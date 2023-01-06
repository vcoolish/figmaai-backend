package com.app.surnft.backend.config.probability

import java.util.*
import java.util.concurrent.ThreadLocalRandom

abstract class ProbabilityProperties<T : Comparable<T>> {

  companion object {

    fun <T : Comparable<T>> getProbabilities(proportions: Map<T, Double>): SortedMap<ClosedRange<Double>, Set<T>> {
      val sum = proportions.values.sum()

      val result: SortedMap<ClosedRange<Double>, Set<T>> =
        sortedMapOf({ o1: ClosedRange<Double>, o2: ClosedRange<Double> ->
          o1.start.compareTo(o2.start)
        })

      val sortedProportions = proportions.toSortedMap(naturalOrder())
      for (entry in sortedProportions) {
        val start = if (result.isEmpty()) 0.0 else result.lastKey().endInclusive

        val probability: Double = entry.value / sum
        result.merge(start.rangeTo(start + probability), setOf(entry.key), Set<T>::plus)
      }

      return result
    }
  }

  abstract val proportions: Map<T, Double>

  protected open val probabilities: SortedMap<ClosedRange<Double>, Set<T>> by lazy {
    getProbabilities(proportions)
  }

  open fun getNextRandom(): T {
    val chance = ThreadLocalRandom.current().nextDouble()

    for (probability in probabilities) {
      if (probability.key.contains(chance)) {
        return probability.value.random()
      }
    }
    throw NoSuchElementException("Probability not found!")
  }
}
