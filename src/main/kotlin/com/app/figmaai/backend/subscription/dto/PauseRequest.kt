package com.app.figmaai.backend.subscription.dto

data class PauseRequest(
  val data: Data,
) {
  data class Data(
    val type: String,
    val id: String,
    val attributes: Attributes,
  ) {
    data class Attributes(
      val pause: Pause?,
    ) {
      data class Pause(
        val mode: String,
        val resumes_at: String,
      )
    }
  }
}