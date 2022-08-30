package com.app.drivn.backend.nft.entity

data class Nft(
    val id: String,
    val collectionId: String,
)

data class NftMetadata(
    val name: String,
    val description: String,
    val image: String,
    val external_url: String,
    val attributes: List<Attribute>,
    val collection: Collection,
)

data class Attribute(
    val trait_type: String,
    val value: String,
)

data class Collection(
    val name: String,
    val family: String,
)

//data class Properties(
//    val files: List<FileMeta>,
//    val category: String,
//    val creators: List<Creator>,
//)

//data class FileMeta(
//    val uri: String,
//    val type: String,
//)

//data class Creator(
//    val address: String,
//    val share: String,
//)