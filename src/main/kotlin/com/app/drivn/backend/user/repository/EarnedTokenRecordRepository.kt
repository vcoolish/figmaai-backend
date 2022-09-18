package com.app.drivn.backend.user.repository

import com.app.drivn.backend.user.model.EarnedTokenRecord
import com.app.drivn.backend.user.model.EarnedTokenRecordId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.ZonedDateTime

interface EarnedTokenRecordRepository : JpaRepository<EarnedTokenRecord, EarnedTokenRecordId> {

  @Query("select coalesce(sum(e.tokenAmount), 0) from EarnedTokenRecord e where e.address = ?1 and e.createdAt >= ?2")
  fun sumAmountByAddressAndCreatedAtGreaterThanEqual(
    address: String,
    createdAt: ZonedDateTime
  ): BigDecimal

}
