package com.app.surnft.backend.ai

import com.app.surnft.backend.blockchain.repository.BlockchainStateRepository
import com.app.surnft.backend.blockchain.service.PrivateKeyProvider
import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.user.service.UserEnergyService
import com.app.surnft.backend.user.service.UserService
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

