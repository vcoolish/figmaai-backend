package com.app.drivn.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.concurrent.Executors

@EnableAsync
@EnableScheduling
@Configuration
class SchedulingConfig : SchedulingConfigurer {

  private val taskScheduler: ConcurrentTaskScheduler by lazy {
    ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor())
  }

  @Bean
  fun taskScheduler(): TaskScheduler {
    return taskScheduler
  }

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setTaskScheduler(taskScheduler)
//    taskRegistrar.addTriggerTask();
  }
}
