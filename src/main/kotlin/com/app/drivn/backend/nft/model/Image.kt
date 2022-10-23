package com.app.drivn.backend.nft.model

import com.app.drivn.backend.common.model.AbstractJpaPersistable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "images")
class Image : AbstractJpaPersistable<Long>() {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private var id: Long? = null
  override fun getId(): Long? = id

  @Column(nullable = false)
  lateinit var dataTxId: String

}
