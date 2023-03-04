package com.app.surnft.backend.nft.repository

import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ImageNftRepository : JpaRepository<ImageNft, NftId>, JpaSpecificationExecutor<ImageNft> {


  @Query("select n from ImageNft n where n.image = ?2 and n.prompt = ?1")
  fun findNftByPrompt(prompt: String, image: String = ""): Optional<ImageNft>

  @Query("select n from ImageNft n where n.collectionId = ?1")
  fun findNftByCollection(collection: Long): List<ImageNft>


  @Query("select n from ImageNft n where n.collectionId = ?1 and n.image = ''")
  fun findEmptyImageInCollection(collection: Long): List<ImageNft>
}
