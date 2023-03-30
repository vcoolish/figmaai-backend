package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.model.AuthenticationMethod
import com.app.figmaai.backend.user.model.User

interface VerificationProvider {
  fun verify(authMethod: AuthenticationMethod, user: User): String
  fun hasAnyAdminAuths(user: User): Boolean
  fun check(authMethod: AuthenticationMethod, user: User, code: String)
}