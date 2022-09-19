package com.app.drivn.backend.user.model

import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Table

@IdClass(EarnedTokenRecordId::class)
@Entity
@Table(name = "user_earned_token_records")
class EarnedTokenRecord() : Comparable<EarnedTokenRecord> {

  @Id
  lateinit var address: String

  @Id
  @CreatedDate
  lateinit var createdAt: ZonedDateTime

  @Column(nullable = false, precision = 30, scale = 18)
  lateinit var tokenAmount: BigDecimal

  constructor(address: String, tokenAmount: BigDecimal) : this() {
    this.address = address
    this.tokenAmount = tokenAmount
  }

  constructor(address: String, earnedAmount: BigDecimal, at: ZonedDateTime) : this(
    address,
    earnedAmount
  ) {
    this.createdAt = at
  }

  override fun compareTo(other: EarnedTokenRecord): Int {
    val addressCompare = address.compareTo(other.address)

    if (addressCompare != 0) {
      return addressCompare
    }

    return createdAt.compareTo(other.createdAt)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as EarnedTokenRecord

    return address == other.address
        && createdAt == other.createdAt
  }

  override fun hashCode(): Int = Objects.hash(address, createdAt)
}
