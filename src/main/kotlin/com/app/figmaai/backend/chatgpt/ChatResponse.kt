package com.app.figmaai.backend.chatgpt

data class EditResponse(
  val choices: List<Text>
) {
  data class Text(
    val text: String,
  )
}