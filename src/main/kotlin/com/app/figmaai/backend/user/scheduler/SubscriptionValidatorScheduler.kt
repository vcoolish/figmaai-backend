package com.app.figmaai.backend.user.scheduler

import com.app.figmaai.backend.user.data.UserSubscribedEvent
import com.app.figmaai.backend.user.service.SubscriptionService
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class SubscriptionValidatorScheduler(
  private val subscriptionService: SubscriptionService,
  private val executor: TaskScheduler
) : Trigger, Runnable {

  private var currentNextExecutionTime: Date? = null

  @EventListener(ApplicationStartedEvent::class)
  fun onApplicationStarted(event: ApplicationStartedEvent) {
    schedule()
  }

  @EventListener(UserSubscribedEvent::class)
  fun onSpendEnergy(userSubscribedEvent: UserSubscribedEvent) {
    schedule()
  }

  private fun schedule() {
    if (currentNextExecutionTime == null) {
      executor.schedule(this, this)
    }
  }

  override fun nextExecutionTime(triggerContext: TriggerContext): Date? =
    subscriptionService.getNextValidationTime()
      .map(Date::from)
      .orElse(null)
      .also { currentNextExecutionTime = it }

  override fun run() {
    subscriptionService.getUsersByNextSubscriptionValidation(ZonedDateTime.now())
      .forEach { subscriptionService.tryToValidateSubscription(it) }
  }
}
