package com.app.figmaai.backend.ai

data class DalleResponse(
  val created: Long,
  val data: List<Property>,
) {
  data class Property(
    val url: String,
  )
}