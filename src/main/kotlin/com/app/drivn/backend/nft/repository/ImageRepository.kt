package com.app.drivn.backend.nft.repository

import com.app.drivn.backend.nft.model.Image
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ImageRepository : JpaRepository<Image, Long> {

  @Query(
    value = """
      SELECT i.*
      FROM images i TABLESAMPLE bernoulli(:percentage) REPEATABLE ( 200 )
         LEFT JOIN car_nfts car on i.id = car.image_id
      WHERE car.image_id is null
      LIMIT 1
    """,
    nativeQuery = true
  )
  fun findFreeImage(@Param("percentage") percentage: Short): Image?

}
