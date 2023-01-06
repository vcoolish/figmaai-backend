package com.app.surnft.backend.blockchain.repository

import com.app.surnft.backend.blockchain.model.BlockchainState
import org.springframework.data.jpa.repository.JpaRepository

interface BlockchainStateRepository : JpaRepository<BlockchainState, String>
