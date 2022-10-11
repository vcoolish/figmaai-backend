package com.app.drivn.backend.user.model

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
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
  
 @ManyToOne
 @JoinColumn(name = "user_address")
 lateinit var user: User
 lateinit var balance: BigDecimal
 lateinit var txId: String
 lateinit var createdAt: ZonedDateTime
}
