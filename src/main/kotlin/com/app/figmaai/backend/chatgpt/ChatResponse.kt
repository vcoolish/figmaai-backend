package com.app.figmaai.backend.chatgpt

data class EditResponse(
  val choises: List<Text>
) {
  data class Text(
    val text: String,
  )
}