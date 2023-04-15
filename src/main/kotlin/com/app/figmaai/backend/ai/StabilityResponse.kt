package com.app.figmaai.backend.ai

data class StabilityResponse(
  val artifacts: List<Artifact>,
) {
  data class Artifact(
    val base64: String,
  )
}