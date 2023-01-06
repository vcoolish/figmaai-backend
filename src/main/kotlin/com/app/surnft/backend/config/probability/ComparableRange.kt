package com.app.surnft.backend.config.probability

class ComparableRange<T : Comparable<T>>(
  override val endInclusive: T,
  override val start: T
) : ClosedRange<T>, Comparable<ComparableRange<T>> {

  override fun compareTo(other: ComparableRange<T>): Int {
    val startCompare = start.compareTo(other.start)
    val endCompare = endInclusive.compareTo(other.endInclusive)

    return startCompare + endCompare
  }
}
