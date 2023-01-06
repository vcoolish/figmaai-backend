package com.app.surnft.backend.blockchain.model

import java.math.BigDecimal
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "transactions")
class TransactionCache {

  @Id
  @GeneratedValue
  lateinit var id: UUID

  @ManyToOne
  @JoinColumn(name = "block_id")
  lateinit var blockchainState: BlockchainState

  @Column(nullable = false)
  lateinit var address: String

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  lateinit var direction: Direction

  @Column(nullable = false, precision = 30, scale = 18)
  lateinit var amount: BigDecimal

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  lateinit var txType: BalanceType
}
