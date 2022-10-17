package com.app.drivn.backend.common.blockchain.entity

import com.app.drivn.backend.user.dto.BalanceType
import java.math.BigDecimal

data class TransactionCache(
  val address: String?,
  val to: Direction,
  val amount: BigDecimal,
  val type: BalanceType,
)

enum class Direction {
  deposit,
  withdraw,
}