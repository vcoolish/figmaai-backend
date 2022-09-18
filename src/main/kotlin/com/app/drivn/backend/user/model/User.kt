package com.app.drivn.backend.user.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import java.math.BigDecimal
import javax.persistence.Column
import java.time.ZonedDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class User() : AbstractJpaPersistable<String>() {

  @Id
  lateinit var address: String
  override fun getId(): String = address

  var distance: Float = 0F

  @Column(nullable = false)
  var tokensLimitPerDay: BigDecimal = BigDecimal.valueOf(10)

  @Column(nullable = false)
  var tokensToClaim: BigDecimal = BigDecimal.ZERO

  var maxEnergy: Float = 30F
  var energy: Float = this.maxEnergy
  var nextEnergyRenew: ZonedDateTime? = null

  constructor(address: String, tokensLimitPerDay: BigDecimal, maxEnergy: Float) : this() {
    this.address = address
    this.tokensLimitPerDay = tokensLimitPerDay
    this.maxEnergy = maxEnergy
  }
}
