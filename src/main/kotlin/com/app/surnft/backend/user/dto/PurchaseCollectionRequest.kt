package com.app.surnft.backend.user.dto

data class PurchaseCollectionRequest(
  val prompt: String,
  val provider: String,
  val option: Int,
  val name: String,
  val symbol: String,
)
