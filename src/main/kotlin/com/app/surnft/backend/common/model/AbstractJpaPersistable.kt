package com.app.surnft.backend.common.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import java.lang.IllegalStateException
import javax.persistence.MappedSuperclass
import javax.persistence.Transient

@MappedSuperclass
abstract class AbstractJpaPersistable<PK : Serializable> : Persistable<PK>, Serializable {

  companion object {
    private const val serialVersionUID = -5554308939380869754L
  }

  fun getSafeId(): PK = id ?: throw IllegalStateException("Id not set!")

  @Transient
  @JsonIgnore
  override fun isNew(): Boolean {
    return null == id
  }

  override fun equals(other: Any?): Boolean {
    other ?: return false

    if (this === other) return true

    if (javaClass != ProxyUtils.getUserClass(other)) return false

    other as AbstractJpaPersistable<*>

    return if (null == id) false else id == other.id
  }

  override fun hashCode(): Int {
    var hashCode = 17

    hashCode += if (null == id) 0 else id.hashCode() * 31

    return hashCode
  }

  override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"
}
