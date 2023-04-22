package com.app.figmaai.backend.config

import com.app.figmaai.backend.credentials.DbCredentialsService
import com.app.figmaai.backend.credentials.DbCredentialsServiceImpl
import com.app.figmaai.backend.credentials.EncoderDecoderService
import com.app.figmaai.backend.credentials.RSAEncoderDecoderService
import com.app.figmaai.backend.user.service.JwtKeyStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager

@Configuration
class PaymentSystemConfig {

    @Bean
    fun encoderDecoderService(keyStore: JwtKeyStore): EncoderDecoderService {
        return RSAEncoderDecoderService(keyStore)
    }

    @Bean
    fun dbCredentialsService(
        entityManager: EntityManager,
        encoderDecoderService: EncoderDecoderService,
    ): DbCredentialsService {
        return DbCredentialsServiceImpl(entityManager, "prod", encoderDecoderService)
    }
}
