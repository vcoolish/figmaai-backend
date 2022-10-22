package com.app.drivn.backend.common.blockchain.model

import java.math.BigDecimal
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "transactions")
class TransactionCache {

  @Id
  @GeneratedValue
  lateinit var id: UUID

  @Column(nullable = false)
  lateinit var address: String

  @Column(nullable = false)
  lateinit var direction: String

  @Column(nullable = false)
  lateinit var amount: BigDecimal

  @Column(nullable = false)
  lateinit var txType: String
}
