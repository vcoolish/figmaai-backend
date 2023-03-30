package com.app.figmaai.backend.user.model

import java.util.*

interface UpdateTokenData {
  val user: User?
    get() = null
  val token: String?
    get() = null
  val expirationDate: Date?
    get() = null
  val hash: String?
    get() = null
}
