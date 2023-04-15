package com.app.figmaai.backend.ai

enum class AiProvider(val energy: Int) {
  DALLE(6),
  MIDJOURNEY(6),
  STABILITY(6)
}

enum class AiVersion {
  V4,
  V5
}