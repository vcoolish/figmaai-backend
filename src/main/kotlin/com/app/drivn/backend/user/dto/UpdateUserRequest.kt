package com.app.drivn.backend.user.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min

class UpdateUserRequest(
  @Min(0)
  @Max(50)
  val donation: Int?
)
