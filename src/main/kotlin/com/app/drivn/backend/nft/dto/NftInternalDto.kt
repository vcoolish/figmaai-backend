package com.app.drivn.backend.nft.dto

import com.app.drivn.backend.nft.model.CarBody
import com.app.drivn.backend.nft.model.Quality

class NftInternalDto : NftBaseDto() {

  var level: Int = 0
  lateinit var quality: Quality
  lateinit var body: CarBody
  var minSpeed: Int = 0
  var maxSpeed: Int = 0
  var odometer: Float = 0F
  var efficiency: Float = 0F
  var luck: Float = 0F
  var comfortability: Float = 0F
  var resilience: Float = 0F
  var durability: Float = 0F
  var maxDurability: Float = 100F
}
