package com.app.drivn.backend.entity

import com.app.drivn.backend.nft.entity.Nft

data class AddressInfo(
    val distance: String,
    val tokenClaimable: String,
    val energy: String,
    val nfts: List<Nft>
)