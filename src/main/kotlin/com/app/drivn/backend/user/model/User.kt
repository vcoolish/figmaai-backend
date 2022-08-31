package com.app.drivn.backend.user.model

import org.hibernate.Hibernate
import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
class User() {

  @Id
  lateinit var address: String
  var distance: Float = 0F
  var tokensToClaim: BigDecimal = BigDecimal.ZERO
  var maxEnergy: Float = 30F
  var energy: Float = this.maxEnergy

  constructor(address: String) : this() {
    this.address = address
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as User

    return address == other.address
  }

  override fun hashCode(): Int = address.hashCode()
}
