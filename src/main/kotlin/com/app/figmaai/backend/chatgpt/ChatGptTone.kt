package com.app.figmaai.backend.chatgpt

enum class ChatGptTone(val value: String) {
  neutral("without changing tone of voice"),
  friendly("with friendly and personable conversational tone of voice"),
  professional("with professional and authoritative tone of voice"),
  persuasive("with persuasive and convincing tone of voice"),
  inspirational("with inspirational and motivational tone of voice"),
  humorous("with humorous and witty tone of voice"),
  instructive("with instructive and educational tone of voice"),
  storytelling("with storytelling and narrative tone of voice"),
  nurturing("with nurturing and supportive tone of voice"),
  formal("with formal and polite tone of voice"),
  sarcastic("with sarcastic tone of voice");
}