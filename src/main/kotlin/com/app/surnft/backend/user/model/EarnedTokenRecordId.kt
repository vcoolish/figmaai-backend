package com.app.surnft.backend.user.model

import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*


class EarnedTokenRecordId() : Serializable {

  lateinit var address: String

  @CreationTimestamp
  lateinit var createdAt: ZonedDateTime

  constructor(
    address: String,
    createdAt: ZonedDateTime
  ) : this() {
    this.address = address
    this.createdAt = createdAt
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as EarnedTokenRecordId

    return address == other.address
        && createdAt == other.createdAt
  }

  override fun hashCode(): Int = Objects.hash(address, createdAt)
}
