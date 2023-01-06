package com.app.surnft.backend.nft.model

import com.app.surnft.backend.common.model.AbstractJpaPersistable
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
open class Nft : AbstractJpaPersistable<Long>() {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_nfts_id_sequence_gen")
  @SequenceGenerator(
    name = "image_nfts_id_sequence_gen",
    sequenceName = "image_nfts_id_sequence",
    initialValue = 1_000_000,
    allocationSize = 1,
  )
  @Column(name = "id", nullable = false)
  private var id: Long? = null

  override fun getId(): Long? = id

  @Id
  @Column(nullable = false)
  var collectionId: Long? = null

  lateinit var name: String

  @Column(length = 512)
  lateinit var description: String

//  @OneToOne
//  @JoinColumn(name = "image_id")
  lateinit var image: String

  @Column(length = 2048)
  lateinit var externalUrl: String
  lateinit var creatorAddress: String
  var isMinted: Boolean = false

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Nft

    return id == other.id
        && collectionId == other.collectionId
  }

  override fun hashCode(): Int = Objects.hash(id, collectionId)
}