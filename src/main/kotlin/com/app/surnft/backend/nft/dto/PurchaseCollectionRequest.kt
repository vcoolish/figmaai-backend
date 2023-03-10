package com.app.surnft.backend.nft.dto

data class PurchaseCollectionRequest(
  val prompt: String,
  val provider: String,
  val option: Int,
  val name: String,
  val symbol: String,
  val styles: List<String>,
)
