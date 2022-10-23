package com.app.drivn.backend.nft.service

import com.app.drivn.backend.nft.repository.ImageRepository
import org.springframework.stereotype.Service

@Service
class ImageService(
  private val imageRepository: ImageRepository,
) {
}
