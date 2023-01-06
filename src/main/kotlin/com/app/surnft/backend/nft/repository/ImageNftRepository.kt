package com.app.surnft.backend.nft.repository

import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ImageNftRepository : JpaRepository<ImageNft, NftId> {

  @Query(
    value = """
      SELECT i FROM image_nfts i
      WHERE i.prompt = :prompt
      LIMIT 1
    """,
    nativeQuery = true,
  )
  fun findImageByPrompt(@Param("prompt") prompt: String): ImageNft?

}