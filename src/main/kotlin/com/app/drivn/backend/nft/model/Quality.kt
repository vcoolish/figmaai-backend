package com.app.drivn.backend.nft.model

/**
 * It's more like rarity
 */
enum class Quality(val efficiency: Float) {
  COMMON(1F),
  UNCOMMON(1.1F),
  RARE(1.2F),
  EPIC(1.3F),
  LEGENDARY(1.4F)
}
