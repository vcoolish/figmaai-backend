package com.app.figmaai.backend.chatgpt

data class ModeResponse(val name: String, val title: String, val inputs: Map<String, String>? = null)