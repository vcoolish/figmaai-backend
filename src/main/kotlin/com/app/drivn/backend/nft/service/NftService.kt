package com.app.drivn.backend.nft.service

import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.NftId
import com.app.drivn.backend.nft.repository.CarNftRepository
import org.springframework.stereotype.Service

@Service
class NftService(
  private val carNftRepository: CarNftRepository
) {

  fun getOrCreate(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId))
      .orElseGet { carNftRepository.save(NftMapper.generateRandomCar(id, collectionId)) }
  }
}
