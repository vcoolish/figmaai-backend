package com.app.surnft.backend.nft.repository.extra

import com.app.surnft.backend.common.util.SpecificationUtil.get
import com.app.surnft.backend.common.util.SpecificationUtil.join
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.user.model.User
import org.springframework.data.jpa.domain.Specification
import java.time.ZonedDateTime
import javax.persistence.criteria.Join

object ImageNftSpecification {

  fun userEqual(address: String?): Specification<ImageNft> = Specification { root, _, builder ->
    if (address == null) {
      return@Specification null
    }

    val userJoin: Join<ImageNft, User> = root.join(ImageNft::user)

    return@Specification builder.equal(userJoin.get(User::address), address.lowercase())
  }

  fun imageIsEmpty(): Specification<ImageNft> = Specification { root, _, builder ->
    return@Specification builder.equal(root.get(ImageNft::image), "")
  }

  fun hasMintedEntries(): Specification<ImageNft> = Specification { root, _, builder ->
    return@Specification builder.isTrue(root.get(ImageNft::isMinted))
  }

  fun createdAtGreaterOrEqual(dateTime: ZonedDateTime?): Specification<ImageNft> =
    Specification { root, _, builder ->
      if (dateTime == null) {
        return@Specification null
      }
      return@Specification builder.greaterThanOrEqualTo(root.get(ImageNft::createdAt), dateTime)
    }

  fun createdAtLessOrEqual(dateTime: ZonedDateTime?): Specification<ImageNft> =
    Specification { root, _, builder ->
      if (dateTime == null) {
        return@Specification null
      }
      return@Specification builder.lessThanOrEqualTo(root.get(ImageNft::createdAt), dateTime)
    }

}
