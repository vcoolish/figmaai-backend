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

class PurchaseVideoRequest(
  val prompt: String,
  val orientation: String = "square", // landscape, portrait or square
  val locale: String = "en-US", // 'en-US' 'pt-BR' 'es-ES' 'ca-ES' 'de-DE' 'it-IT' 'fr-FR' 'sv-SE' 'id-ID' 'pl-PL' 'ja-JP' 'zh-TW' 'zh-CN' 'ko-KR' 'th-TH' 'nl-NL' 'hu-HU' 'vi-VN' 'cs-CZ' 'da-DK' 'fi-FI' 'uk-UA' 'el-GR' 'ro-RO' 'nb-NO' 'sk-SK' 'tr-TR' 'ru-RU'.
  val size: String = "medium", // large, medium, small
)