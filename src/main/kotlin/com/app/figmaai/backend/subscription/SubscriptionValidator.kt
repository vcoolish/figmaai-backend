package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.subscription.model.Subscription

interface SubscriptionValidator {
    fun validate(id: String)
    fun status(id: String): Subscription
}
