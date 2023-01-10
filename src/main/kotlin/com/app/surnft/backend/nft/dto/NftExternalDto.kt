package com.app.surnft.backend.nft.dto

class NftExternalDto : NftBaseDto() {
  lateinit var attributes: List<Attribute>

  data class Attribute(
    val trait_type: String,
    val value: String,
  )
}