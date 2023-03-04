package com.app.surnft.backend.user.dto

data class PurchaseCollectionRequest(
  val prompt: String,
  val provider: String,
  val option: Int,
  val name: String,
  val symbol: String,
)

data class RetryCollectionRequest(
  val prompt: String,
  val provider: String,
  val count: Int,
  val name: String,
  val symbol: String,
  val contract: String,
  val address: String,
  val id: Long
)
