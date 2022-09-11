package com.app.drivn.backend.nft.model

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*

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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Nft

    return id == other.id
        && collectionId == other.collectionId
  }

  override fun hashCode(): Int = Objects.hash(id, collectionId)
}
