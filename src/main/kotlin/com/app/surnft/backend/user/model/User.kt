package com.app.surnft.backend.user.model

import com.app.surnft.backend.common.model.AbstractJpaPersistable
import com.app.surnft.backend.nft.model.Collection
import com.app.surnft.backend.nft.model.ImageNft
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
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
  var nfts: List<ImageNft> = listOf()

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "user")
  var collections: List<Collection> = listOf()

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

  @CreatedDate
  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime

  constructor(address: String, tokensLimitPerDay: BigDecimal, maxEnergy: BigDecimal) : this() {
    this.address = address
    this.tokensLimitPerDay = tokensLimitPerDay
    this.maxEnergy = maxEnergy
  }
}
