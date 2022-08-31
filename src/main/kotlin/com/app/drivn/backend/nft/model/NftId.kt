package com.app.drivn.backend.nft.model

import java.io.Serializable

data class NftId(
  val id: Long,
  val collectionId: Long,
) : Serializable
