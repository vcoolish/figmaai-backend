package com.app.drivn.backend.blockchain.entity

import com.app.drivn.backend.blockchain.model.Direction
import com.app.drivn.backend.blockchain.model.BalanceType
import java.math.BigDecimal

data class TransactionUnprocessed(
  val address: String,
  val direction: Direction,
  val amount: BigDecimal,
  val type: BalanceType,
)
