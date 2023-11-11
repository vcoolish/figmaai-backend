package com.app.figmaai.backend.image.model

import com.app.figmaai.backend.common.model.AbstractJpaPersistable
import org.hibernate.Hibernate
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import java.io.Serializable
import java.util.*
import javax.persistence.*

@MappedSuperclass
open class Image : AbstractJpaPersistable<Long>() {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "images_id_sequence_gen")
  @GenericGenerator(
    name = "images_id_sequence_gen",
    strategy = SetableSequenceGenerator.NAME,
    parameters = [
      Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "images_id_sequence"),
      Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1000000"),
      Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1"),
    ]
  )
  @Column(name = "id", nullable = false)
  var imageId: Long? = null

  override fun getId(): Long? = imageId

  var name: String? = null

  @Column(length = 512)
  lateinit var description: String

  lateinit var image: String
  var gif: String? = ""
  var video: String? = ""

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Image

    return id == other.id
  }

  override fun hashCode(): Int = Objects.hash(id)
}

class SetableSequenceGenerator : SequenceStyleGenerator() {
  companion object {

    const val NAME = "com.app.figmaai.backend.image.model.SetableSequenceGenerator"
  }

  /**
   * Custom id generation. If id is set on the
   * com.curecomp.common.hibernate.api.Entity instance then use the set one,
   * if id is 'null' or '0' then generate one.
   */
  override fun generate(session: SharedSessionContractImplementor, obj: Any): Serializable {
    if (obj is Image) {
      val id: Long? = obj.id
      if (id != null) {
        return id
      }
    }
    return super.generate(session, obj)
  }
}
