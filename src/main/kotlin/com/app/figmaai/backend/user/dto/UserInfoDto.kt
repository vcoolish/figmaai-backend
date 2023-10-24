package com.app.figmaai.backend.user.dto

import java.math.BigDecimal
import java.time.ZonedDateTime

open class UserInfoDto {
  lateinit var energy: BigDecimal
  lateinit var maxEnergy: BigDecimal
  var nextEnergyRenew: ZonedDateTime? = null
  var generations: Long = 0L
  var maxGenerations: Long = 0L
  var animations: Long = 0L
  var maxAnimations: Long = 0L
  var copyCredits: Long = 0L
  var uxCredits: Long = 0L
  var maxCredits: Long = 0L
  var email: String? = null
}
