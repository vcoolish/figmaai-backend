package com.app.surnft.backend.drive.controller

import com.app.surnft.backend.constraint.Address
import com.app.surnft.backend.drive.dto.DriveInfoDto
import com.app.surnft.backend.drive.service.DriveService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@Validated
@RestController
class DriveController(
  private val driveService: DriveService,
) {

  @PutMapping("/address")
  fun drive(
    @Address @RequestHeader address: String,
    @RequestParam carId: Long,
    @RequestParam collectionId: Long,
    @RequestParam distance: BigDecimal,
    @RequestParam timestamp: Long,
    @RequestParam signature: String,
  ): DriveInfoDto {
    return driveService.drive(
      address = address,
      carId = carId,
      collectionId = collectionId,
      distance = distance,
      timestamp = timestamp,
      sig = signature,
    )
  }
}