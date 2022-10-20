package com.app.drivn.backend.common.blockchain.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import javax.persistence.*

@Table(name = "blockchain_state")
@Entity
class BlockchainState : AbstractJpaPersistable<String>() {

  @OneToMany
  @JoinColumn(name = "transaction_id")
  lateinit var transactions: List<TransactionCache>

  @Id
  lateinit var lastProcessedBlock: String
  override fun getId(): String = lastProcessedBlock
}
