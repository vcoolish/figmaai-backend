package com.app.drivn.backend.drive.mapper

import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.user.model.User
import java.math.BigDecimal

object DriveMapper {
  fun toDto(user: User, tokensEarnedForDay: BigDecimal) = DriveInfoDto(
    user.distance,
    user.energy,
    user.maxEnergy,
    user.balance,
    user.tokensToClaim,
    user.tokensLimitPerDay,
    tokensEarnedForDay,
    user.donation,
    user.nextEnergyRenew,
  )
}
