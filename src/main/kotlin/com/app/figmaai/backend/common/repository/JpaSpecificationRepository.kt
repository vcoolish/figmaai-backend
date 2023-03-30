package com.app.figmaai.backend.common.repository

import com.app.figmaai.backend.common.specification.SpecificationBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface JpaSpecificationRepository<T> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
//  fun findOne(spec: Specification<T>?, nodes: Map<String, Map<String, Any>>): T?
//  fun <U> findOne(spec: Specification<T>?, nodes: Map<String, Map<String, Any>>, mapper: (T) -> U): U?
//  fun findAll(spec: Specification<T>?, pageable: Pageable, nodes: Map<String, Map<String, Any>>): Page<T>
//  fun <U> findAll(
//    spec: Specification<T>?,
//    pageable: Pageable,
//    nodes: Map<String, Map<String, Any>>,
//    mapper: (T) -> U
//  ): Page<U>
//
//  fun findAll(spec: Specification<T>?, nodes: Map<String, Map<String, Any>>): List<T>
//  fun <U> findAll(spec: Specification<T>?, nodes: Map<String, Map<String, Any>>, mapper: (T) -> U): List<U>
//  fun findAll(spec: Specification<T>?, sort: Sort, nodes: Map<String, Map<String, Any>>): List<T>
//  fun <U> findAll(spec: Specification<T>?, sort: Sort, nodes: Map<String, Map<String, Any>>, mapper: (T) -> U): List<U>
//  fun findOne(id: Long, nodes: Map<String, Map<String, Any>>): T?
//  fun findAllSubgraphCollectionFetch(
//    spec: Specification<T>?,
//    pageable: Pageable,
//    nodes: Map<String, Map<String, Any>>,
//    specFetch: Specification<T>?
//  ): Page<T>
//
//  fun findAllSubgraphCollectionFetch(
//    builder: SpecificationBuilder<T>,
//    pageable: Pageable,
//    nodes: Map<String, Map<String, Any>>
//  ): Page<T>
}