package com.app.figmaai.backend.activemq.ext

import java.io.Serializable

data class UserRegistrationConfirmationDto(
    var userUuid: String? = null,
    var event: Status? = null
) : Serializable {
    enum class Status {
        USER_CREATED,
    }
}
