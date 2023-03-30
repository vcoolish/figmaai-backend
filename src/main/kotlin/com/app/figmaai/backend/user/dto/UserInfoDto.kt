package com.app.figmaai.backend.user.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

open class UserInfoDto {

  lateinit var energy: BigDecimal
  lateinit var maxEnergy: BigDecimal
  lateinit var balance: BigDecimal
  var nextEnergyRenew: ZonedDateTime? = null
}
