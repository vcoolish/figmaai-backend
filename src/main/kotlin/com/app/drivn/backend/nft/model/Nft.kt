package com.app.drivn.backend.nft.model

import org.hibernate.Hibernate
import java.util.*
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.MappedSuperclass
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator

@IdClass(NftId::class)
@MappedSuperclass
open class Nft {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "car_nfts_id_sequence")
  @SequenceGenerator(name = "car_nfts_id_sequence", sequenceName = "car_nfts_id_sequence")
  @Column(nullable = false)
  var id: Long? = null

  @Id
  @Column(nullable = false)
  var collectionId: Long? = null

  lateinit var name: String

  @Column(length = 512)
  lateinit var description: String

  @OneToOne
  @JoinColumn(name = "image_id")
  lateinit var image: Image

  @Column(length = 2048)
  lateinit var externalUrl: String
  lateinit var creatorAddress: String

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Nft

    return id == other.id
        && collectionId == other.collectionId
  }

  override fun hashCode(): Int = Objects.hash(id, collectionId)
}