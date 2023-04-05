package com.app.figmaai.backend.user.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

open class UserInfoDto {

  lateinit var energy: BigDecimal
  lateinit var maxEnergy: BigDecimal
  var nextEnergyRenew: ZonedDateTime? = null
}