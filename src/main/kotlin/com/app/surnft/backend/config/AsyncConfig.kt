package com.app.surnft.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@EnableAsync
@Configuration
class AsyncConfig {
  @Bean
  fun threadPoolTaskExecutor(): Executor {
    return ConcurrentTaskExecutor(Executors.newCachedThreadPool())
  }
}