package com.app.figmaai.backend.subscription.model

enum class SubscriptionType(val lemonId: String, val tokens: Int, val slug: String) {
//  Basic_Month("81235", 300, "d035b962-b455-44bc-804c-dcb0cc52aae8"),
  Basic_Month("81235", 300, "9eeb76f6-bc13-455b-92d3-9a6a220b22ce"),
  Standard_Month("81225", 500, "427e9c1b-d1a0-4de1-a75f-51606b4fefed"),
  Advanced_Month("81226", 800, "3bf34566-7c9c-4acc-b866-db94a063b047"),
  Basic_Year("85009", 300, "d1daf6d3-5c71-4c3c-a34f-c1528153b80d"),
  Standard_Year("85011", 500, "6a1c1c0c-ef1a-4f6d-bd1e-d582a7972c6e"),
  Advanced_Year("85012", 1000, "5bc7d5d2-feee-4f82-94ab-3ce7e7a4b201");

  fun getLink() =
    SubscriptionLink(
//      url = "https://aidsnpro.lemonsqueezy.com/checkout/buy/${slug}",
      url = "https://aidsnpro.lemonsqueezy.com/checkout/buy/${slug}",
      generations = tokens.toString(),
      name = name,
      variant = lemonId,
    )
}

data class SubscriptionLink(
  val url: String,
  val generations: String,
  val name: String,
  val variant: String,
)