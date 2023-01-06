package com.app.surnft.backend.user.scheduler

import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.user.data.UserSpendEnergyEvent
import com.app.surnft.backend.user.service.UserEnergyService
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class UserEnergyRenewScheduler(
  private val userEnergyService: UserEnergyService,
  private val executor: TaskScheduler
) : Trigger, Runnable {

  private final val logger = logger()

  private var currentNextExecutionTime: Date? = null

  @EventListener(ApplicationStartedEvent::class)
  fun onApplicationStarted(event: ApplicationStartedEvent) {
    schedule()
  }

  @EventListener(UserSpendEnergyEvent::class)
  fun onSpendEnergy(userSpendEnergyEvent: UserSpendEnergyEvent) {
    schedule()
  }

  private fun schedule() {
    if (currentNextExecutionTime == null) {
      executor.schedule(this, this)
    }
  }

  override fun nextExecutionTime(triggerContext: TriggerContext): Date? =
    userEnergyService.getNextEnergyRenewTime()
      .map(Date::from)
      .orElse(null)
      .also { currentNextExecutionTime = it }

  override fun run() {
    logger.debug("Started user energy renew scheduler!")
    userEnergyService.getUsersByNextEnergyRenew(ZonedDateTime.now())
      .forEach { userEnergyService.tryToRenew(it) }
  }
}
