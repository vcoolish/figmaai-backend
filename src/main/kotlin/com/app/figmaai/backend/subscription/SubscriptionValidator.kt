package com.app.figmaai.backend.subscription

import com.app.figmaai.backend.subscription.model.PaypalSubscription

interface SubscriptionValidator {
    fun validate(id: String)
    fun details(id: String): PaypalSubscription
}
