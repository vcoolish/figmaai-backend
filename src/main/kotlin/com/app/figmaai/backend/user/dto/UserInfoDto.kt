package com.app.figmaai.backend.user.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

open class UserInfoDto {
  lateinit var energy: BigDecimal
  lateinit var maxEnergy: BigDecimal
  var nextEnergyRenew: ZonedDateTime? = null
  var generations: Long = 0L
  var maxGenerations: Long = 0L
  var credits: Long = 0L
  var maxCredits: Long = 0L
}
