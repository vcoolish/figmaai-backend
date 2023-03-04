package com.app.surnft.backend.nft.dto

data class CollectionDto(
  val id: Long,
  val address: String,
  val count: Int,
  val name: String,
  val symbol: String,
  val inProgressCount: Int,
)
