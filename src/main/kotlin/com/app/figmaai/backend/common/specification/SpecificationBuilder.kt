package com.app.figmaai.backend.common.specification

import org.springframework.data.jpa.domain.Specification

class SpecificationBuilder<T> {
  private val empty: () -> Specification<T>? = { Specification { _, _, cb -> cb.conjunction() } }
  private var spec: Specification<T>? = empty.invoke()
  private var fetchSpec: Specification<T>? = empty.invoke()

  /** Combines Specification with an operation */
  private fun combineSpecification(
    specs: Iterable<Specification<T>?>,
    operation: Specification<T>.(Specification<T>?) -> Specification<T>?
  ): Specification<T>? {
    return specs.filterNotNull()
      .fold(empty.invoke())
      { existing, new -> existing?.operation(new) }
  }

  fun and(vararg specs: Specification<T>?): SpecificationBuilder<T> =
    and(specs.asIterable())

  fun and(specs: Iterable<Specification<T>?>): SpecificationBuilder<T> =
    and(combineSpecification(specs, Specification<T>::and))

  fun and(spec: Specification<T>?): SpecificationBuilder<T> {
    this.spec = this.spec?.and(spec)
    return this
  }

  fun andFetch(vararg specs: Specification<T>?) =
    andFetch(specs.asIterable())

  fun andFetch(specs: Iterable<Specification<T>?>) =
    andFetch(combineSpecification(specs, Specification<T>::and))

  fun andFetch(spec: Specification<T>?): SpecificationBuilder<T> {
    this.spec = this.spec?.and(spec)
    this.fetchSpec = this.fetchSpec?.and(spec)
    return this
  }

  fun orFetch(spec: Specification<T>?): SpecificationBuilder<T> {
    this.spec = this.spec?.or(spec)
    this.fetchSpec = this.fetchSpec?.or(spec)
    return this
  }

  fun or(spec: Specification<T>?): SpecificationBuilder<T> {
    this.spec = this.spec?.or(spec)
    return this
  }

  fun build() = this.spec

  fun buildFetch() = this.fetchSpec
}
