package com.app.figmaai.backend.image.dto

class PurchaseImageRequest(
  val prompt: String,
  val provider: String,
  val version: String = "V5",
  val height: Int = 512,
  val width: Int = 512,
  val strengthPercent: Int = 35,
)

class PurchaseAnimatedImageRequest(
  val prompt: String,
  val height: Int = 512,
  val width: Int = 512,
  val strengthPercent: Int = 35,
)