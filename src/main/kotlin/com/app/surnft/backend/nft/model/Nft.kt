package com.app.surnft.backend.nft.model

import com.app.surnft.backend.common.model.AbstractJpaPersistable
import org.hibernate.Hibernate
import java.util.*
import javax.persistence.*

@IdClass(NftId::class)
@MappedSuperclass
open class Nft : AbstractJpaPersistable<NftId>() {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_nfts_id_sequence_gen")
  @SequenceGenerator(
    name = "image_nfts_id_sequence_gen",
    sequenceName = "image_nfts_id_sequence",
    initialValue = 1_000_000,
    allocationSize = 1,
  )
  @Column(name = "id", nullable = false)
  var id: Long? = null

  override fun getId(): NftId? {
    val id = id
    val collectionId = collectionId
    return if (id == null || collectionId == null) null else NftId(id, collectionId)
  }

  @Id
  @Column(nullable = false)
  var collectionId: Long? = null

  var name: String? = null

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