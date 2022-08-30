package com.app.drivn.backend.user.controller

import com.app.drivn.backend.entity.AddressInfo
import com.app.drivn.backend.nft.entity.Nft
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping("/address/{address}")
    fun index(@PathVariable address: String): AddressInfo {
//        val nfts = bounceClient.getNfts(address)
        return AddressInfo(
            distance = "1200",
            tokenClaimable = "10.0",
            energy = "30.0",
            nfts = listOf(
                Nft(
                    id = "123",
                    collectionId = "12345",
                ),
                Nft(
                    id = "124",
                    collectionId = "12345",
                ),
            )
        )
    }
}