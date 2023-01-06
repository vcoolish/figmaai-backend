package com.app.surnft.backend.user.service

import com.app.surnft.backend.user.model.EarnedTokenRecord
import com.app.surnft.backend.user.repository.EarnedTokenRecordRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.ZonedDateTime

@Service
class EarnedTokenRecordService(
  private val earnedTokenRecordRepository: EarnedTokenRecordRepository
) {

  fun getEarnedTokensForDay(address: String): BigDecimal {
    val dayAgo: ZonedDateTime = ZonedDateTime.now().minusDays(1)
    return earnedTokenRecordRepository.sumAmountByAddressAndCreatedAtGreaterThanEqual(address.lowercase(), dayAgo)
  }

  fun recordEarnedTokens(address: String, earnedAmount: BigDecimal) : EarnedTokenRecord =
    earnedTokenRecordRepository.save(EarnedTokenRecord(address.lowercase(), earnedAmount, ZonedDateTime.now()))
}
