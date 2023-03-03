package com.app.surnft.backend.nft.repository

import com.app.surnft.backend.nft.model.Collection
import com.app.surnft.backend.nft.model.ImageNft
import com.app.surnft.backend.nft.model.NftId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CollectionRepository : JpaRepository<Collection, Long>
