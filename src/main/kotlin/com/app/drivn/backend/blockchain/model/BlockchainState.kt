package com.app.drivn.backend.blockchain.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "blockchain_state")
@Entity
class BlockchainState : AbstractJpaPersistable<String>() {

  @OneToMany(
    fetch = FetchType.EAGER,
    mappedBy = "blockchainState",
    cascade = [CascadeType.ALL],
    orphanRemoval = true
  )
  var transactions: MutableList<TransactionCache> = mutableListOf()

  @Id
  lateinit var lastProcessedBlock: String
  override fun getId(): String = lastProcessedBlock
}
