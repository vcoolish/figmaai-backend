package com.app.drivn.backend.common.blockchain.repository

import com.app.drivn.backend.common.blockchain.model.BlockchainState
import org.springframework.data.jpa.repository.JpaRepository

interface BlockchainStateRepository : JpaRepository<BlockchainState, String>
