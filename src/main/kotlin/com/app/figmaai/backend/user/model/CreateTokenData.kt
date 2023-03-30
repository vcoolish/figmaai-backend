package com.app.figmaai.backend.user.model

import java.util.*

interface CreateTokenData {
  val user: User
  val token: String
  val expirationDate: Date
  val hash: String
}