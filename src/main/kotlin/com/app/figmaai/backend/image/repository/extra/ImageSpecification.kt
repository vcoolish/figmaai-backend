package com.app.figmaai.backend.image.repository.extra

import com.app.figmaai.backend.common.util.SpecificationUtil.get
import com.app.figmaai.backend.common.util.SpecificationUtil.join
import com.app.figmaai.backend.image.dto.SearchType
import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.domain.Specification
import java.time.ZonedDateTime
import javax.persistence.criteria.Join

object ImageSpecification {

  fun userEqual(userUuid: String?): Specification<ImageAI> = Specification { root, _, builder ->
    if (userUuid == null) {
      return@Specification null
    }

    val userJoin: Join<ImageAI, User> = root.join(ImageAI::user)

    return@Specification builder.equal(userJoin.get(User::userUuid), userUuid)
  }

  fun findByPrompt(prompt: String?): Specification<ImageAI> = Specification { root, _, builder ->
    if (prompt.isNullOrEmpty()) {
      return@Specification null
    }

    return@Specification builder.like(builder.lower(root.get("prompt")), "%${prompt.lowercase()}%")
  }

  fun findByType(
    searchType: SearchType
  ): Specification<ImageAI> = Specification { root, k, builder ->
    return@Specification when (searchType) {
      SearchType.all -> null
      SearchType.animated -> builder
        .notEqual(root.get<String?>("gif"), null)
      SearchType.static -> builder
        .equal(root.get<String?>("gif"), null)
    }
  }

  fun imageIsEmpty(): Specification<ImageAI> = Specification { root, _, builder ->
    return@Specification builder.equal(root.get(ImageAI::image), "")
  }

  fun createdAtGreaterOrEqual(dateTime: ZonedDateTime?): Specification<ImageAI> =
    Specification { root, _, builder ->
      if (dateTime == null) {
        return@Specification null
      }
      return@Specification builder.greaterThanOrEqualTo(root.get(ImageAI::createdAt), dateTime)
    }

}
