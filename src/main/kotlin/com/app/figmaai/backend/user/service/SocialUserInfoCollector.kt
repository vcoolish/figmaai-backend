package com.app.figmaai.backend.user.service

import com.app.figmaai.backend.user.dto.UserUpdateData
import com.app.figmaai.backend.user.model.UserCreateData
import org.springframework.social.connect.Connection

interface SocialUserInfoCollector<out V, out U>
    where U : UserUpdateData, V : UserCreateData {
  fun collectCreateInfo(connection: Connection<*>, figma: String): V
  fun collectUpdateInfo(connection: Connection<*>): U
}
