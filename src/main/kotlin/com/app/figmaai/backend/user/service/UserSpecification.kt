package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.domain.Specification

object UserSpecification {
  val equalUuid: (String) -> Specification<User> = {
    Specification { root, _, cb ->
      cb.equal(root.get<String>("userUuid"), it.trim())
    }
  }

  val equalEmail: (String) -> Specification<User> = {
    Specification { root, _, cb ->
      cb.equal(root.get<String>("email"), it)
    }
  }

  val equalGoogleId: (String) -> Specification<User> = {
    Specification { root, _, cb ->
      cb.equal(root.get<String>("googleId"), it)
    }
  }

  val inUuids: (Collection<String>) -> Specification<User> = {
    Specification { root, _, _ ->
      root.get<String>("userUuid").`in`(it)
    }
  }

  val isEnabled: () -> Specification<User> = {
    Specification { root, _, cb ->
      cb.isTrue(root.get<Boolean>("enabled"))
    }
  }

  val isVerified: () -> Specification<User> = {
    Specification { root, _, cb ->
      cb.isTrue(root.get<Boolean>("verified"))
    }
  }
}