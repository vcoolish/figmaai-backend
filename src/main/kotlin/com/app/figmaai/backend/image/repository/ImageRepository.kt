package com.app.figmaai.backend.image.repository

import com.app.figmaai.backend.image.model.ImageAI
import com.app.figmaai.backend.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface ImageRepository : JpaRepository<ImageAI, Long>, JpaSpecificationExecutor<ImageAI> {


  @Query("select n from ImageAI n where n.image = ?2 and n.prompt = ?1")
  fun findNftByPrompt(prompt: String, image: String = ""): List<ImageAI>

  //count images by user from date to date
  @Query("select COUNT(n) from ImageAI n where n.user = ?1 and n.createdAt >= ?2 and n.createdAt <= ?3")
  fun findUserImagesByDate(user: User, fromDate: ZonedDateTime, toDate: ZonedDateTime): Long


}
