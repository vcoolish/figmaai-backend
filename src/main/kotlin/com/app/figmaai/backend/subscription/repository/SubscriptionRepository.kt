package com.app.figmaai.backend.subscription.repository

import com.app.figmaai.backend.subscription.model.Subscription
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface SubscriptionRepository : JpaRepository<Subscription, String>, JpaSpecificationExecutor<Subscription> {

  fun findSubscriptionsByUser(user: User): List<Subscription>
}