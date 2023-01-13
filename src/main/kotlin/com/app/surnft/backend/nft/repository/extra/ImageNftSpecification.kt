package com.app.surnft.backend.nft.repository.extra

import com.app.surnft.backend.common.util.SpecificationUtil.get
import com.app.surnft.backend.common.util.SpecificationUtil.join
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.user.model.User
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.Join

object ImageNftSpecification {

  fun userEqual(address: String?): Specification<ImageNft> = Specification { root, _, builder ->
    if (address == null) {
      return@Specification null
    }

    val userJoin: Join<ImageNft, User> = root.join(ImageNft::user)

    return@Specification builder.equal(userJoin.get(User::address), address)
  }

  fun userEqualInProgress(address: String?): Specification<ImageNft> = Specification { root, _, builder ->
    if (address == null) {
      return@Specification null
    }

    val userJoin: Join<ImageNft, User> = root.join(ImageNft::user)

    return@Specification builder
      .equal(userJoin.get(User::address), address)
  }
}
