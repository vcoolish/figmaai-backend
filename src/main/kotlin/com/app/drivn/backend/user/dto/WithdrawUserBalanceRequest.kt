package com.app.drivn.backend.user.dto

import com.app.drivn.backend.blockchain.model.BalanceType

class WithdrawUserBalanceRequest(
  val type: BalanceType,
)
