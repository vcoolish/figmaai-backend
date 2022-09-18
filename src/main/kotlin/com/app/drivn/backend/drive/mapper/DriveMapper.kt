package com.app.drivn.backend.drive.mapper

import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.user.model.User

object DriveMapper {
  fun toDto(user: User) = DriveInfoDto(
    user.distance,
    user.energy,
    user.maxEnergy,
    user.tokensToClaim,
    user.tokensLimitPerDay,
    user.nextEnergyRenew
  )
}
