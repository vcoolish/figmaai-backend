package com.app.drivn.backend.nft.model

import java.io.Serializable

class NftId() : Serializable {
  var id: Long = 0
  var collectionId: Long = 0

  constructor(
    id: Long,
    collectionId: Long,
  ) : this() {
    this.id = id
    this.collectionId = collectionId
  }
}
