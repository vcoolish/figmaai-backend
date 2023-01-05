package com.app.drivn.backend.nft.dto

import com.app.drivn.backend.nft.model.Quality

class NftInternalDto : NftBaseDto() {

  var level: Short = 0
  lateinit var quality: Quality
  var minSpeed: Int = 0
  var maxSpeed: Int = 0
  var odometer: Float = 0F
  var efficiency: Short = 0
  var luck: Short = 0
  var comfortability: Short = 0
  var economy: Short = 0
  var durability: Float = 0F
  var maxDurability: Float = 100F
  var mint: Int = 0
  var prompt: String = ""
  var isMinted: Boolean = false
}
