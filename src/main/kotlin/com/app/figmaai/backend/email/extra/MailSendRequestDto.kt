package com.app.figmaai.backend.email.extra

data class MailSendRequestDto(
  val api_key: String,
  val to: List<String>,
  val sender: String,
  val subject: String,
  val text_body: String,
  val html_body: String,
)