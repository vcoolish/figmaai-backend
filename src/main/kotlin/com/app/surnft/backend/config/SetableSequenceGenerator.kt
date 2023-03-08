package com.app.surnft.backend.config

import com.app.surnft.backend.nft.model.Nft
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import java.io.Serializable

class SetableSequenceGenerator : SequenceStyleGenerator() {
  companion object {

    const val NAME = "com.app.surnft.backend.nft.model.SetableSequenceGenerator"
  }

  /**
   * Custom id generation. If id is set on the
   * com.curecomp.common.hibernate.api.Entity instance then use the set one,
   * if id is 'null' or '0' then generate one.
   */
  override fun generate(session: SharedSessionContractImplementor, obj: Any): Serializable {
    if (obj is Nft) {
      val id: Long? = obj.id
      if (id != null) {
        return id
      }
    }
    return super.generate(session, obj)
  }
}
