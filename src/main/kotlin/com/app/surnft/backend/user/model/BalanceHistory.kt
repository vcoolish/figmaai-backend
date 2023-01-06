package com.app.surnft.backend.user.model

import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "balance_history")
@Entity
class BalanceHistory {

  @Id
  @GeneratedValue
  lateinit var id: UUID

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_address", nullable = false)
  lateinit var user: User

  @Column(nullable = false)
  lateinit var balance: BigDecimal

  @Column(nullable = false)
  lateinit var txId: String

  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime
}
