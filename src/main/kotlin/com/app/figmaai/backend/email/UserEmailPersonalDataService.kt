package com.app.figmaai.backend.email

import com.app.figmaai.backend.email.extra.UserEmailPersonalData

interface UserEmailPersonalDataService {
    fun processUserEmailPersonalData(userUuid: String): UserEmailPersonalData
}