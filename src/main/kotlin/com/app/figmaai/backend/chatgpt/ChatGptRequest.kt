package com.app.figmaai.backend.chatgpt

data class ChatGptRequest(
  val model: String,
  val user: String,
  val messages: List<CharMessage>,
) {

  data class CharMessage(
    val role: String,
    val content: String,
  )
}

data class EditRequest(
  val model: String,
  val instruction: String,
  val input: String,
)

enum class ChatModel(val value: String) {
  CHAT_3_5("gpt-3.5-turbo"),
  DAVINCI("text-davinci-edit-001"),
}

enum class ChatRole {
  user,
  assistant,
  system,
}