package com.app.figmaai.backend.chatgpt

data class EditResponse(
  val choices: List<Text>,
  val usage: Usage,
) {
  data class Text(
    val text: String?,
    val message: Message?,
  )

  data class Message(
    val content: String,
    val role: String,
  )

  data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
  )
}