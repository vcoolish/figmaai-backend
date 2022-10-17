package com.app.drivn.backend.user.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min

class UpdateUserDonationRequest(
  @Min(0)
  @Max(50)
  val donation: Short?
)

class WithdrawUserBalanceRequest(
  val type: String?,
  val amount: String?,
)

enum class BalanceType {
  coin,
  token
}