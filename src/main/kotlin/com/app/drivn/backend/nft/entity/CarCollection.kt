package com.app.drivn.backend.nft.entity

import com.app.drivn.backend.nft.model.CarBody
import com.app.drivn.backend.nft.model.Quality

enum class CarCollection(
  val collectionId: Long,
  val price: Double,
  val title: String,
  val body: CarBody,
  val quality: Quality,
  val minSpeed: Int,
  val maxSpeed: Int,
  val efficiency: Int,
  val luck: Int,
  val comfortability: Int,
  val maxDurability: Int,
) {
  PORSCHE(
    collectionId = 0,
    price = 0.1,
    title = "Porsche",
    body = CarBody.BASIC,
    quality = Quality.COMMON,
    minSpeed = 20,
    maxSpeed = 120,
    efficiency = 30,
    luck = 10,
    comfortability = 20,
    maxDurability = 100,
  ),
}