package com.app.surnft.backend.nft.dto

data class RetryCollectionRequest(
  val prompt: String,
  val provider: String,
  val count: Int,
  val name: String,
  val symbol: String,
  val contract: String,
  val address: String,
  val collectionId: Long,
  val styles: List<String>,
)
