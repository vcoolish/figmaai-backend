package com.app.surnft.backend.nft.dto

import com.app.surnft.backend.nft.model.Quality

class NftInternalDto : NftBaseDto() {

  var id: String = ""
  var collectionId: String = ""
  var level: Short = 0
  lateinit var quality: Quality
  var efficiency: Short = 0
  var luck: Short = 0
  var comfortability: Short = 0
  var durability: Float = 0F
  var maxDurability: Float = 100F
  var mint: Int = 0
  var prompt: String = ""
  var isMinted: Boolean = false
}
