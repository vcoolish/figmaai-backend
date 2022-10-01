package com.app.drivn.backend.user.dto

import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.properties.Delegates

open class UserInfoDto {

  lateinit var distance: BigDecimal
  lateinit var energy: BigDecimal
  lateinit var maxEnergy: BigDecimal
  lateinit var tokenClaimable: BigDecimal
  lateinit var tokensLimitPerDay: BigDecimal
  var nextEnergyRenew: ZonedDateTime? = null
  var donation by Delegates.notNull<Short>()
}
