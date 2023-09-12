package com.app.figmaai.backend.subscription.model

enum class SubscriptionType(val lemonId: String, val animations: Int, val generations: Int, val tokens: Int, val slug: String) {
  Basic_Month(
    "81235",
    50,
    500,
    150000,
    "d035b962-b455-44bc-804c-dcb0cc52aae8"
  ),
  Standard_Month(
    lemonId = "95530",
    animations = 45,
    generations = 500,
    tokens = 83000,
    slug = "bce7ceae-9d2c-4d47-8951-9c82ea11a55f"
  ),
  Team_Month(
    "95539",
    60,
    800,
    116000,
    "3f9b8d4c-f875-4b2e-8661-4ca5daf53f3f"
  ),
  Basic_Year(
    "85009",
    50,
    500,
    150000,
    "d1daf6d3-5c71-4c3c-a34f-c1528153b80d"
  ),
  Standard_Year(
    "95537",
    45,
    500,
    100000,
    "0a83e7e6-fee4-4e3f-b3ec-a1e50566627f"
  ),
  Team_Year(
    "95540",
    60,
    1000,
    150000,
    "1c4ae36d-b562-498a-8106-e3738dc01a7b"
  ),
  Basic_Day(
    "108212",
    30,
    100,
    10000,
    "4826ebaf-d1fc-4028-905d-6af78d08158a"
  );

  fun getLink() =
    SubscriptionLink(
      url = "https://aidsnpro.lemonsqueezy.com/checkout/buy/${slug}",
      generations = generations.toString(),
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