package com.app.drivn.backend.nft.controller

import com.app.drivn.backend.nft.entity.Attribute
import com.app.drivn.backend.nft.entity.Collection
import com.app.drivn.backend.nft.entity.NftMetadata
import org.springframework.web.bind.annotation.*

@RestController
class NftController {
    @GetMapping("/nft/{collectionId}/{id}")
    fun index(@PathVariable collectionId: String, @PathVariable id: String): NftMetadata {
        return NftMetadata(
            name = "Car #$id",
            description = "NFT Car, drive it in DRIVN to earn",
            image = "https://arweave.net/Zh1XgakvwR6xPoXefYZbHhIJVmaLfIyjuJXca3kRpE4",
            external_url = "https://opensea.io/",
            attributes = listOf(
                Attribute(
                    trait_type = "efficiency",
                    value = (1..100).random().toString(),
                ),
                Attribute(
                    trait_type = "luck",
                    value = (1..100).random().toString(),
                ),
                Attribute(
                    trait_type = "comfort",
                    value = (1..100).random().toString(),
                ),
                Attribute(
                    trait_type = "durability",
                    value = (1..100).random().toString(),
                ),
                Attribute(
                    trait_type = "Socket 1",
                    value = "empty",
                ),
                Attribute(
                    trait_type = "Socket 2",
                    value = "empty",
                ),
                Attribute(
                    trait_type = "Socket 3",
                    value = "empty",
                ),
                Attribute(
                    trait_type = "Socket 4",
                    value = "empty",
                ),
                Attribute(
                    trait_type = "Optimal Speed",
                    value = "30.0-50.0 km/h",
                ),
                Attribute(
                    trait_type = "Car-minting Count",
                    value = "0/7",
                ),
                Attribute(
                    trait_type = "Car type",
                    value = "Urban",
                ),
                Attribute(
                    trait_type = "Car quality",
                    value = "Common",
                ),
                Attribute(
                    trait_type = "Level",
                    value = "0",
                ),
            ),
            collection = Collection(
                name = "Car",
                family = "DRIVN",
            ),
        )
    }
}

@RestController
class DriveController {
    @PutMapping("/address/{address}")
    fun index(
        @PathVariable address: String,
        @RequestParam(required = true) carId: String,
        @RequestParam(required = true) collectionId: String,
        @RequestParam(required = true) distance: String,
        @RequestParam(required = true) timestamp: String,
        @RequestParam(required = true) signature: String,
    ): String {
        return ""
    }
}