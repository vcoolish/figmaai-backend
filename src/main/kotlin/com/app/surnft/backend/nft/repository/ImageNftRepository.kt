package com.app.surnft.backend.nft.repository

import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ImageNftRepository : JpaRepository<ImageNft, NftId>, JpaSpecificationExecutor<ImageNft> {

}
