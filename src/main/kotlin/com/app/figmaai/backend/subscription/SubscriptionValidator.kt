package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.subscription.model.SubscriptionDto

interface SubscriptionValidator {
    fun validate(id: String)
    fun status(id: String, licenseId: String?): SubscriptionDto

    fun delete(id: String)
    fun pause(id: String, isUnpause: Boolean)
}
