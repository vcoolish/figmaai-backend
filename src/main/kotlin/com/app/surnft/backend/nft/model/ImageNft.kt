package com.app.surnft.backend.nft.model

import com.app.surnft.backend.user.model.User
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
@Table(name = "image_nfts")
class ImageNft : Nft {

  @ManyToOne
  @JoinColumn(name = "user_address")
  lateinit var user: User
  var level: Short = 0

  @Enumerated(EnumType.STRING)
  lateinit var quality: Quality

  lateinit var prompt: String

  var minSpeed: Int = 0
  var maxSpeed: Int = 0

  var odometer: Float = 0F

  var efficiency: Short = 0
  var luck: Short = 0
  var comfortability: Short = 0
  var economy: Short = 0
  var maxDurability: Float = 100F
  var durability: Float = maxDurability

  /**
   * What is car's number in the tree of creation. I.e. how many parents do it has.
   */
  var mint: Int = 0

  @CreatedDate
  @CreationTimestamp
  @Column(nullable = false)
  lateinit var createdAt: ZonedDateTime

  @LastModifiedDate
  @UpdateTimestamp
  lateinit var updatedAt: ZonedDateTime

  constructor() : super()
  constructor(id: Long, createdAt: ZonedDateTime) : super(id) {
    this.createdAt = createdAt
  }
}
