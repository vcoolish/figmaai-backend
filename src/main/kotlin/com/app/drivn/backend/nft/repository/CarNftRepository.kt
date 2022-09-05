package com.app.drivn.backend.nft.repository

import com.app.drivn.backend.nft.model.CarNft
import com.app.drivn.backend.nft.model.NftId
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.web.PageableDefault

interface CarNftRepository : JpaRepository<CarNft, NftId>