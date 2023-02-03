package com.app.surnft.backend.nft.entity

import com.app.surnft.backend.nft.model.Quality

enum class ImageCollection(
  val collectionId: Long,
  val price: Double,
  val mintPrice: Double,
  val title: String,
  val quality: Quality,
  val minSpeed: Int,
  val maxSpeed: Int,
  val efficiency: Int,
  val luck: Int,
  val comfortability: Int,
  val maxDurability: Int,
) {
  SUR(
    collectionId = 0,
    price = 0.002,
    mintPrice = 0.02,
    title = "Sur",
    quality = Quality.COMMON,
    minSpeed = 20,
    maxSpeed = 120,
    efficiency = 30,
    luck = 10,
    comfortability = 20,
    maxDurability = 100,
  ),
}