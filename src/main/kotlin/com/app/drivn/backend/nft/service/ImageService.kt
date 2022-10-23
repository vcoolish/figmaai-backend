package com.app.drivn.backend.nft.service

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.nft.repository.ImageRepository
import org.springframework.stereotype.Service

@Service
class ImageService(
  private val imageRepository: ImageRepository,
  private val appProperties: AppProperties,
) {

  fun disposeNextImage(): String {
    val imageEntry = imageRepository.findAll().first()
    val id = imageEntry.dataTxId
    imageRepository.delete(imageEntry)
    return "${appProperties.arweaveUrl}$id"
  }
}
