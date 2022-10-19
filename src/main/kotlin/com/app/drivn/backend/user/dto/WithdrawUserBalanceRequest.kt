package com.app.drivn.backend.user.dto

class WithdrawUserBalanceRequest(
  val type: String?,
  val amount: String?,
)

enum class BalanceType {
  coin,
  token
}