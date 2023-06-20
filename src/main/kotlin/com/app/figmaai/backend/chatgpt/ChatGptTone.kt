package com.app.figmaai.backend.chatgpt

enum class ChatGptTone(val value: String, val title: String) {
  neutral("without changing tone of voice", "Retain tone of voice"),
  friendly("with friendly and personable conversational tone of voice", "Friendly and Personable"),
  professional("with professional and authoritative tone of voice", "Conversational"),
  persuasive("with persuasive and convincing tone of voice", "Professional and Authoritative"),
  inspirational("with inspirational and motivational tone of voice", "Persuasive and Convincing"),
  humorous("with humorous and witty tone of voice", "Inspirational and Motivational"),
  instructive("with instructive and educational tone of voice", "Humorous and Witty"),
  storytelling("with storytelling and narrative tone of voice", "Instructive and Educational"),
  nurturing("with nurturing and supportive tone of voice", "Storytelling and Narrative"),
  formal("with formal and polite tone of voice", "Nurturing and Supportive"),
  sarcastic("with sarcastic tone of voice", "Formal and Polite");
}
