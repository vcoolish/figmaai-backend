package com.app.drivn.backend.user.service

import com.app.drivn.backend.user.model.EarnedTokenRecord
import com.app.drivn.backend.user.repository.EarnedTokenRecordRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.ZonedDateTime

@Service
class EarnedTokenRecordService(
  private val earnedTokenRecordRepository: EarnedTokenRecordRepository
) {

  fun getEarnedTokensForDay(address: String): BigDecimal {
    val dayAgo: ZonedDateTime = ZonedDateTime.now().minusDays(1)
    return earnedTokenRecordRepository.sumAmountByAddressAndCreatedAtGreaterThanEqual(address, dayAgo)
  }

  fun recordEarnedTokens(address: String, earnedAmount: BigDecimal) : EarnedTokenRecord =
    earnedTokenRecordRepository.save(EarnedTokenRecord(address, earnedAmount))
}
