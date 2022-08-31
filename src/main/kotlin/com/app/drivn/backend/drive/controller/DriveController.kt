package com.app.drivn.backend.drive.controller

import com.app.drivn.backend.drive.dto.DriveInfoDto
import com.app.drivn.backend.drive.service.DriveService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DriveController(
  private val driveService: DriveService
) {

  @PutMapping("/address/{address}")
  fun drive(
    @PathVariable address: String,
    @RequestParam carId: Long,
    @RequestParam collectionId: Long,
    @RequestParam distance: Float,
    @RequestParam timestamp: Long,
    @RequestParam signature: String,
  ): DriveInfoDto {
    return driveService.drive(address, carId, collectionId, distance)
  }
}