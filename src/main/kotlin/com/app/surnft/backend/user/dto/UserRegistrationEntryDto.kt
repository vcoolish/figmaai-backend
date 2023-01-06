package com.app.surnft.backend.user.dto

data class UserRegistrationEntryDto(
  val nonce: String,
  val message: String,
  val signature: String,
)