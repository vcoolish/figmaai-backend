package com.app.drivn.backend.user.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import com.app.drivn.backend.nft.model.CarNft
import java.math.BigDecimal
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "users")
class User() : AbstractJpaPersistable<String>() {

  @Id
  lateinit var address: String
  override fun getId(): String = address

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "user")
  var nfts: List<CarNft> = listOf()

  @Column(nullable = false, precision = 12, scale = 2)
  var distance: BigDecimal = BigDecimal.ZERO

  @Column(nullable = false, precision = 30, scale = 8)
  var tokensLimitPerDay: BigDecimal = BigDecimal.TEN

  @Column(nullable = false, precision = 30, scale = 8)
  var tokensToClaim: BigDecimal = BigDecimal.ZERO

  @Column(nullable = false, precision = 12, scale = 2)
  var maxEnergy: BigDecimal = BigDecimal.valueOf(30)

  @Column(nullable = false, precision = 12, scale = 2)
  var energy: BigDecimal = this.maxEnergy
  var nextEnergyRenew: ZonedDateTime? = null

  @Column(nullable = false, precision = 30, scale = 18)
  var balance: BigDecimal = BigDecimal.ZERO

  @Column(nullable = false)
  var signMessage: String = ""

  /**
   * Donation percent. From 0 to 50.
   */
  @Column(nullable = false)
  var donation: Short = 5

  constructor(address: String, tokensLimitPerDay: BigDecimal, maxEnergy: BigDecimal) : this() {
    this.address = address
    this.tokensLimitPerDay = tokensLimitPerDay
    this.maxEnergy = maxEnergy
  }
}
