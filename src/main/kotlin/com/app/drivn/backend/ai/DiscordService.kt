package com.app.drivn.backend.ai

import com.app.drivn.backend.blockchain.repository.BlockchainStateRepository
import com.app.drivn.backend.blockchain.service.PrivateKeyProvider
import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.service.UserEnergyService
import com.app.drivn.backend.user.service.UserService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.net.http.WebSocket
import javax.annotation.PreDestroy

@Service
class DiscordService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
  private val appProperties: AppProperties,
  private val blockchainStateRepository: BlockchainStateRepository,
  private val userEnergyService: UserEnergyService,
) : WebSocket.Listener {

  val logger = logger()


  @PreDestroy
  fun onApplicationStopped() {

  }

  @EventListener(ApplicationReadyEvent::class)
  fun onApplicationStarted() {
    
  }


}

