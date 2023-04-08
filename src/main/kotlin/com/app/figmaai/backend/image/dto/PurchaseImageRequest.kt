package com.app.figmaai.backend.image.dto

class PurchaseImageRequest(
  val prompt: String,
  val provider: String,
  val version: String = "V5",
)
