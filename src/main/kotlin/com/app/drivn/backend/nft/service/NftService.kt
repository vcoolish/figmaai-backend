package com.app.drivn.backend.nft.service

import com.app.drivn.backend.nft.dto.NftInternalDto
import com.app.drivn.backend.nft.mapper.NftMapper
import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.NftId
import com.app.drivn.backend.nft.repository.CarNftRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class NftService(
  private val carNftRepository: CarNftRepository
) {
  fun getAll(pageable: Pageable): Page<NftInternalDto> {
    return carNftRepository.findAll(pageable).map(NftMapper::toInternalDto)
  }

  fun getOrCreate(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId))
      .orElseGet { carNftRepository.save(NftMapper.generateRandomCar(id, collectionId)) }
  }

  fun get(id: Long, collectionId: Long): CarNft {
    return carNftRepository.findById(NftId(id, collectionId)).orElseThrow()
  }

  fun save(carNft: CarNft) {
    carNftRepository.save(carNft)
  }

  fun repair(id: Long, collectionId: Long, repairAmount: Float) {
    val nft = get(id, collectionId)

    TODO("Not yet implemented")
  }
}
