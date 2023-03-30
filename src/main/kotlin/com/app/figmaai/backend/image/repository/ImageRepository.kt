package com.app.figmaai.backend.image.repository

import com.app.figmaai.backend.image.model.ImageAI
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface ImageRepository : JpaRepository<ImageAI, Long>, JpaSpecificationExecutor<ImageAI> {


  @Query("select n from ImageAI n where n.image = ?2 and n.prompt = ?1")
  fun findNftByPrompt(prompt: String, image: String = ""): List<ImageAI>

}
