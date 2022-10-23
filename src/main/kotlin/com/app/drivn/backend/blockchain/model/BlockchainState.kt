package com.app.drivn.backend.blockchain.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import javax.persistence.*

@Table(name = "blockchain_state")
@Entity
class BlockchainState : AbstractJpaPersistable<String>() {

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "blockchainState")
  lateinit var transactions: List<TransactionCache>

  @Id
  lateinit var lastProcessedBlock: String
  override fun getId(): String = lastProcessedBlock
}
