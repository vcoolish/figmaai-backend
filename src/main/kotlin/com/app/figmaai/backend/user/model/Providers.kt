package com.app.figmaai.backend.user.model

enum class Providers(val id: String) {
  GOOGLE("google");

  companion object {
    fun fromValue(value: String) =
      values().first { it.id.lowercase() == value.trim().lowercase() }
  }
}