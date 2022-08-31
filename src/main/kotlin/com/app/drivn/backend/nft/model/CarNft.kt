package com.app.drivn.backend.nft.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "car_nfts")
class CarNft : Nft() {

  var level: Int = 0
  @Enumerated(EnumType.STRING)
  lateinit var quality: Quality
  @Enumerated(EnumType.STRING)
  lateinit var body: CarBody

  var minSpeed: Int = 0
  var maxSpeed: Int = 0

  var odometer: Float = 0F

  var efficiency: Float = 0F
  var luck: Float = 0F
  var comfortability: Float = 0F
  var resilience: Float = 0F
  var durability: Float = 0F
  var maxDurability: Float = 100F
}
