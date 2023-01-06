package com.app.surnft.backend.user.dto

import com.app.surnft.backend.blockchain.model.BalanceType

class WithdrawUserBalanceRequest(
  val type: BalanceType,
)
