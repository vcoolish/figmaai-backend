package com.app.surnft.backend.nft.dto

open class NftBaseDto {

  lateinit var name: String
  lateinit var description: String
  lateinit var image: String
  lateinit var externalUrl: String
  lateinit var collection: Collection

  data class Collection(
    val name: String,
    val family: String,
  )
}
