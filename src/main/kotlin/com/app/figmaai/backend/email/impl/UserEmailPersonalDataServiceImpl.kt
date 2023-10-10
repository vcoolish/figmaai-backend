package com.app.figmaai.backend.email.impl

import com.app.figmaai.backend.email.UserEmailPersonalDataService
import com.app.figmaai.backend.email.extra.UserEmailPersonalData
import com.app.figmaai.backend.user.service.UserService
import org.springframework.stereotype.Service

@Service
class UserEmailPersonalDataServiceImpl(
  private val userService: UserService,
) : UserEmailPersonalDataService {

    override fun processUserEmailPersonalData(userUuid: String): UserEmailPersonalData {
        val user = userService.getByUuid(userUuid)
        return UserEmailPersonalData(
            userEmail = user.email,
        )
    }
}
