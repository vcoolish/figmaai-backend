package com.app.surnft.backend.user.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min

class UpdateUserDonationRequest(
  @Min(0)
  @Max(50)
  val donation: Short?
)