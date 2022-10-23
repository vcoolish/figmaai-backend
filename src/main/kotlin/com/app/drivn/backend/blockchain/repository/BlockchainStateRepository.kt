package com.app.drivn.backend.blockchain.repository

import com.app.drivn.backend.blockchain.model.BlockchainState
import org.springframework.data.jpa.repository.JpaRepository

interface BlockchainStateRepository : JpaRepository<BlockchainState, String>
