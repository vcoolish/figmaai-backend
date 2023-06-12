package com.app.figmaai.backend.chatgpt

data class EditResponse(
  val choices: List<Text>
) {
  data class Text(
    val text: String?,
    val message: Message?,
  )

  data class Message(
    val content: String,
    val role: String,
  )
}