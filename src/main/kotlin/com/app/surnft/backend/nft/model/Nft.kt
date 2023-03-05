package com.app.surnft.backend.nft.model

import com.app.surnft.backend.common.model.AbstractJpaPersistable
import org.hibernate.Hibernate
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import org.springframework.batch.core.Entity
import java.io.Serializable
import java.util.*
import javax.persistence.*

@IdClass(NftId::class)
@MappedSuperclass
open class Nft : AbstractJpaPersistable<NftId> {

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

  @Transient
  val new: Boolean

  protected constructor(new: Boolean) : super() {
    this.new = new
  }

  /**
   * Default constructor for JPA and id autogeneration.
   */
  constructor() : this(true)

  constructor(id: Long) : this(false) {
    this.id = id
  }

  override fun isNew(): Boolean = new || super.isNew()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Nft

    return id == other.id
        && collectionId == other.collectionId
  }

  override fun hashCode(): Int = Objects.hash(id, collectionId)
}

class SetableSequenceGenerator : IdentifierGenerator {

  /**
   * Custom id generation. If id is set on the
   * com.curecomp.common.hibernate.api.Entity instance then use the set one,
   * if id is 'null' or '0' then generate one.
   */
  override fun generate(session: SharedSessionContractImplementor, obj: Any): Serializable {
    return if ((obj as? Entity)?.id == null) {
      val query = String.format(
        "select %s from %s",
        session.getEntityPersister(obj.javaClass.name, obj)
          .identifierPropertyName,
        obj.javaClass.simpleName
      )

      val ids = session.createQuery(query, Long::class.java).stream()

      val max: Long = ids.max { o1, o2 ->
        o1.compareTo(o2)
      }.orElse(1000000L)

      return max + 1
    } else {
      obj.id
    }
  }
}