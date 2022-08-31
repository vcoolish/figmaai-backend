package com.app.drivn.backend.nft.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DriveController {

  @PutMapping("/address/{address}")
  fun index(
    @PathVariable address: String,
    @RequestParam(required = true) carId: String,
    @RequestParam(required = true) collectionId: String,
    @RequestParam(required = true) distance: String,
    @RequestParam(required = true) timestamp: String,
    @RequestParam(required = true) signature: String,
  ): String {
    return ""
  }
}