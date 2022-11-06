package com.app.drivn.backend.drive.mapper

import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.user.model.User
import java.math.BigDecimal

object DriveMapper {
  fun toDto(user: User, tokensEarnedForDay: BigDecimal) = DriveInfoDto(
    distance = user.distance,
    energy = user.energy,
    maxEnergy = user.maxEnergy,
    balance = user.balance,
    tokenClaimable = user.tokensToClaim,
    tokensLimitPerDay = user.tokensLimitPerDay,
    tokensEarnedForDay = tokensEarnedForDay,
    donation = user.donation,
    nextEnergyRenew = user.nextEnergyRenew,
  )
}
