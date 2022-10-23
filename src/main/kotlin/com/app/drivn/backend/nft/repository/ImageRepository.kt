package com.app.drivn.backend.nft.repository

import com.app.drivn.backend.nft.model.Image
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository : JpaRepository<Image, Long> {

}
